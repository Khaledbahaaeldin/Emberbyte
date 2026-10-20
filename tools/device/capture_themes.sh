#!/usr/bin/env bash
set -euo pipefail

# Find adb if not in PATH
if command -v adb >/dev/null 2>&1; then
    ADB="adb"
elif [ -n "${LOCALAPPDATA:-}" ] && [ -x "$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
    ADB="$ANDROID_HOME/platform-tools/adb"
elif [ -x "$HOME/AppData/Local/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="$HOME/AppData/Local/Android/Sdk/platform-tools/adb.exe"
else
    echo "ERROR: adb not found" >&2
    exit 1
fi

DEVICE="${ADB_DEVICE:-emulator-5554}"
OUT_DIR="${1:-.superpowers/sdd/2026-10-17-emberbyte-redesign/palette-shots}"
mkdir -p "$OUT_DIR"

THEMES=(
    dusk
    ember
    monochrome
    catppuccin
    gruvbox
    nord
    rosepine
    solarized
    dracula
    tokyonight
    everforest
    kanagawa
)

echo "Capturing 36 theme gallery screenshots to $OUT_DIR using $DEVICE..."

# Initial launch to ensure activity is displayed and splash is dismissed
"$ADB" -s "$DEVICE" shell am start -W -n io.github.khaledbahaaeldin.emberbyte/.ThemeGalleryActivity --es theme "dusk" --ez dark true --ez amoled false >/dev/null
sleep 3.5


for theme in "${THEMES[@]}"; do
    echo "Processing theme: $theme (dark)"
    "$ADB" -s "$DEVICE" shell am start -n io.github.khaledbahaaeldin.emberbyte/.ThemeGalleryActivity --es theme "$theme" --ez dark true --ez amoled false >/dev/null
    sleep 0.8
    "$ADB" -s "$DEVICE" exec-out screencap -p > "$OUT_DIR/${theme}_dark.png"

    echo "Processing theme: $theme (amoled)"
    "$ADB" -s "$DEVICE" shell am start -n io.github.khaledbahaaeldin.emberbyte/.ThemeGalleryActivity --es theme "$theme" --ez dark true --ez amoled true >/dev/null
    sleep 0.8
    "$ADB" -s "$DEVICE" exec-out screencap -p > "$OUT_DIR/${theme}_amoled.png"

    echo "Processing theme: $theme (light)"
    "$ADB" -s "$DEVICE" shell am start -n io.github.khaledbahaaeldin.emberbyte/.ThemeGalleryActivity --es theme "$theme" --ez dark false --ez amoled false >/dev/null
    sleep 0.8
    "$ADB" -s "$DEVICE" exec-out screencap -p > "$OUT_DIR/${theme}_light.png"
done

echo "Successfully captured 36 theme screenshots in $OUT_DIR."
