# Emberbyte 1.0.1

A build-only release. The app itself is unchanged from 1.0.0: no new features, no changed behaviour, and your data and settings carry over as they are.

## What changed
- The project now builds with JDK 21 (the compiled code still targets Java 17). F-Droid's build server has no JDK 17, so this lets F-Droid build Emberbyte from source.

## Privacy
Emberbyte has **no `INTERNET` permission**, no account, no analytics and no tracking. Nothing leaves your phone. It is free software under the GPL-3.0-or-later.

## Install and verify
- Download `emberbyte-1.0.1.apk` and check it against `emberbyte-1.0.1.apk.sha256`.
- The APK is signed by the maintainer with the same key as 1.0.0, so it installs over 1.0.0 as an update. The certificate SHA-256 fingerprint is:
  `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`
  Check it with `apksigner verify --print-certs emberbyte-1.0.1.apk`.
- Requires Android 10 or newer (built for Android 16).
- The build is reproducible: see `docs/release/RELEASE.md` to rebuild it from source.

## Known limits
Unchanged from 1.0.0; see the [1.0.0 release notes](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/docs/release/RELEASE-NOTES-1.0.0.md).

Full list of changes: [CHANGELOG.md](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md).
