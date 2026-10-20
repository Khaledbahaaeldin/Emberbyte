#!/usr/bin/env bash
# Signs the unsigned release APK with the maintainer's release key and verifies the result.
#
#   tools/release/sign.sh <keystore.jks> [unsigned.apk] [output-dir]
#
# - The keystore must live OUTSIDE the repository (the script refuses otherwise).
# - Passwords are never taken from arguments or files: they come from the environment
#   (EMBERBYTE_KS_PASS, EMBERBYTE_KEY_PASS) or, when unset, from a silent prompt, and are passed to
#   apksigner through environment variables so they do not appear in the process list.
# - The alias defaults to "emberbyte" (EMBERBYTE_KEY_ALIAS overrides it).
# - Output: <output-dir>/emberbyte-<versionName>.apk plus a .sha256 file, and the certificate
#   fingerprint printed for the F-Droid recipe. Default output-dir: dist/ (git-ignored).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
KEYSTORE="${1:-}"
UNSIGNED="${2:-$ROOT/app/build/outputs/apk/release/app-release-unsigned.apk}"
OUT_DIR="${3:-$ROOT/dist}"
ALIAS="${EMBERBYTE_KEY_ALIAS:-emberbyte}"

die() { echo "error: $*" >&2; exit 1; }

[ -n "$KEYSTORE" ] || die "usage: tools/release/sign.sh <keystore.jks> [unsigned.apk] [output-dir]"
[ -f "$KEYSTORE" ] || die "keystore not found: $KEYSTORE"
[ -f "$UNSIGNED" ] || die "unsigned APK not found: $UNSIGNED (run ./gradlew :app:assembleRelease first)"

abs() { (cd "$(dirname "$1")" && printf '%s/%s' "$(pwd)" "$(basename "$1")"); }
KS_ABS="$(abs "$KEYSTORE")"
case "$KS_ABS" in
  "$ROOT"/*) die "the keystore is inside the repository ($ROOT). Move it outside so it can never be committed." ;;
esac

# Locate build-tools (apksigner, zipalign): ANDROID_HOME, ANDROID_SDK_ROOT or local.properties sdk.dir.
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ] && [ -f "$ROOT/local.properties" ]; then
  SDK="$(sed -n 's/^sdk.dir=//p' "$ROOT/local.properties" | head -1 | sed 's#\\\\#/#g; s#\\:#:#g')"
fi
[ -n "$SDK" ] && [ -d "$SDK/build-tools" ] || die "Android SDK not found. Set ANDROID_HOME."
BT="$SDK/build-tools/$(ls "$SDK/build-tools" | sort -V | tail -1)"
APKSIGNER="$BT/apksigner"; [ -f "$APKSIGNER" ] || APKSIGNER="$BT/apksigner.bat"
ZIPALIGN="$BT/zipalign"; [ -f "$ZIPALIGN" ] || ZIPALIGN="$BT/zipalign.exe"
[ -f "$APKSIGNER" ] && [ -f "$ZIPALIGN" ] || die "apksigner or zipalign missing in $BT"

# Passwords: environment first, else a silent prompt. Never echoed, never written to disk.
if [ -z "${EMBERBYTE_KS_PASS:-}" ]; then
  read -r -s -p "Keystore password: " EMBERBYTE_KS_PASS; echo
fi
if [ -z "${EMBERBYTE_KEY_PASS:-}" ]; then
  read -r -s -p "Key password (Enter = same as keystore): " EMBERBYTE_KEY_PASS; echo
  [ -n "$EMBERBYTE_KEY_PASS" ] || EMBERBYTE_KEY_PASS="$EMBERBYTE_KS_PASS"
fi
export EMBERBYTE_KS_PASS EMBERBYTE_KEY_PASS

VERSION="$(sed -n 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"\(.*\)"/\1/p' "$ROOT/app/build.gradle.kts" | head -1)"
[ -n "$VERSION" ] || VERSION="unknown"
mkdir -p "$OUT_DIR"
ALIGNED="$(mktemp -d)/aligned.apk"
SIGNED="$OUT_DIR/emberbyte-$VERSION.apk"

"$ZIPALIGN" -f -p 4 "$UNSIGNED" "$ALIGNED"
"$APKSIGNER" sign --ks "$KS_ABS" --ks-key-alias "$ALIAS" \
  --ks-pass env:EMBERBYTE_KS_PASS --key-pass env:EMBERBYTE_KEY_PASS \
  --out "$SIGNED" "$ALIGNED"
rm -f "$ALIGNED" "$SIGNED.idsig"

"$APKSIGNER" verify --verbose --print-certs "$SIGNED" | grep -E "^(Verifies|Verified using)|certificate (DN|SHA-256 digest)" || true
"$APKSIGNER" verify "$SIGNED" >/dev/null || die "signature verification FAILED"

if command -v sha256sum >/dev/null 2>&1; then
  (cd "$OUT_DIR" && sha256sum "emberbyte-$VERSION.apk" > "emberbyte-$VERSION.apk.sha256" && cat "emberbyte-$VERSION.apk.sha256")
fi
echo "Signed APK: $SIGNED"
echo "Put the 'certificate SHA-256 digest' above into the F-Droid recipe (AllowedAPKSigningKeys) if you publish reproducible builds."
