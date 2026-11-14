# Emberbyte 1.0.2

Smoother motion, a fix for the back animation and a stuck tab tap, and a new Updates row. Your data and settings carry over as they are.

## What changed
- **Fixed:** switching tabs from the navigation bar no longer plays the back animation, and a tap on a tab that drifted slightly before lifting is no longer ignored.
- **One back and push motion** across the app, with predictive back that follows your finger, also between the steps of plan creation.
- **Motion across the app:** the unit picker morphs between shapes, lists and loading animate, icon buttons change shape while pressed, the theme swatch morphs, and the plans "+" button grows in and out. Reduced motion still snaps everything.
- **New Updates row** in Settings: it names the app that keeps Emberbyte up to date (Obtainium, Orion Store or F-Droid) and opens it, or opens the latest GitHub release for any other installer.
- **New local navigation log:** long-press the version in Settings to copy the last 300 navigation events. It lives in memory only and is never saved or sent anywhere.

## Privacy
Emberbyte still has **no `INTERNET` permission**, no account, no analytics and no tracking. Nothing leaves your phone. It is free software under the GPL-3.0-or-later.

## Install and verify
- Download `emberbyte-1.0.2.apk` and check it against `emberbyte-1.0.2.apk.sha256`.
- The APK is signed by the maintainer with the same key as 1.0.0 and 1.0.1, so it installs over them as an update. The certificate SHA-256 fingerprint is:
  `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`
  Check it with `apksigner verify --print-certs emberbyte-1.0.2.apk`.
- Requires Android 10 or newer (built for Android 16).
- The build is reproducible: see `docs/release/RELEASE.md` to rebuild it from source.

## Known limits
- The Updates row cannot tell you that a newer version exists, because the app has no network access by design. Use an updater app (Obtainium, Orion Store, F-Droid) or the GitHub releases page.
- Other limits are unchanged from 1.0.0; see the [1.0.0 release notes](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/docs/release/RELEASE-NOTES-1.0.0.md).

Full list of changes: [CHANGELOG.md](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md).
