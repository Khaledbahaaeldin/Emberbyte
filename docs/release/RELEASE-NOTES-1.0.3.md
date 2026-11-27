# Emberbyte 1.0.3

A new look and a calmer set-up. Home, Apps, History, Plans and Settings are redesigned, onboarding is three steps, planning is easier to try before you commit, and text is readable in every theme. Your data and settings carry over as they are.

## What changed
- **New look** for Home, Apps, App detail, History, Plans and Settings: lighter headlines, clearer cards, a collapsing title, and values on every chart bar.
- **Home** has a Live tile and a Today by network tile, a bigger hero with a live unit badge, and one card that sets up your plan.
- **Plans:** a five-step new-plan flow, a one-page plan editor with a live preview and a Save button that never scrolls away, a **what-if** (+1, +5, +10 GB), a pace insight, a renewal timeline, and tap a plan to bring it to the front.
- **Archive is gone.** Plans you archived are restored as normal plans the first time you open 1.0.3. Deleting a plan now really deletes it, with Undo.
- **Onboarding in three steps** (Welcome, Allow access, Your plan). Granting usage access no longer ends onboarding before the other permissions.
- **What's new sheet** after an update, read from a changelog inside the app, so it works offline. It is also in Settings.
- **Readable in every theme:** all text roles meet the contrast checks in all 12 themes, in light, dark and pure black (AMOLED).

## Privacy
Emberbyte still has **no `INTERNET` permission**, no account, no analytics and no tracking. Nothing leaves your phone. It is free software under the GPL-3.0-or-later.

## Install and verify
- Download `emberbyte-1.0.3.apk` and check it against `emberbyte-1.0.3.apk.sha256`.
- The APK is signed by the maintainer with the same key as 1.0.0, 1.0.1 and 1.0.2, so it installs over them as an update. The certificate SHA-256 fingerprint is:
  `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`
  Check it with `apksigner verify --print-certs emberbyte-1.0.3.apk`.
- Requires Android 10 or newer (built for Android 16).
- The build is reproducible: see `docs/release/RELEASE.md` to rebuild it from source.

## Known limits
- The Updates row cannot tell you that a newer version exists, because the app has no network access by design. Use an updater app (Obtainium, Orion Store, F-Droid) or the GitHub releases page.
- Some screen text is still English only; the translation clean-up comes in a later version.
- Other limits are unchanged from 1.0.0; see the [1.0.0 release notes](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/docs/release/RELEASE-NOTES-1.0.0.md).

Full list of changes: [CHANGELOG.md](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md).
