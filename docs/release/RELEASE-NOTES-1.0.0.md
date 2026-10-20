# Emberbyte 1.0.0

The first release of Emberbyte: a data-usage monitor for Android that keeps everything on your phone.

## Highlights
- **Today's number first.** Your mobile and Wi-Fi usage for today in large, readable type, with the amount left on your plan and a forecast of whether you will run out this cycle.
- **Twelve themes, light, dark and pure black.** Dusk (the default), Ember, Monochrome, and nine community palettes: Catppuccin, Gruvbox, Nord, Rosé Pine, Solarized, Dracula, Tokyo Night, Everforest and Kanagawa. Every theme is checked for readable contrast in all modes. Wallpaper colours are optional on Android 12 and newer.
- **Plans that fit real life.** Monthly or every-N-days cycles, SIM binding, add-on packs, free-data windows, rollover, and a guided way to start a plan mid-cycle: use the phone's record, enter what you have already used, or start tracking from the next cycle. Pin the plan you care about most.
- **Alerts you control.** Threshold alerts per plan, per-app budgets, and a spike detector for sudden mobile-data use.
- **Four home-screen widgets.** Today, Plan, Bento and Dashboard, in the same theme as the app.
- **Live notification.** Today's usage and your current speed in the notification shade, and on Android 16 a live-update chip where the phone maker supports it.
- **Predictive back.** A refined Android back gesture across the app.
- **Real app icons** in friendly shapes, an expanding-pill navigation bar with spring motion, and a calm, expressive design throughout.
- **Export and encrypted backup.** CSV and JSON export, and encrypted backups (AES-256-GCM with Argon2id) that restore on a new phone.
- **Accessible.** Large text, right-to-left layouts, TalkBack descriptions and reduced-motion support are part of the design, not an afterthought.

## Privacy
Emberbyte has **no `INTERNET` permission**, no account, no analytics and no tracking. Nothing leaves your phone. It is free software under the GPL-3.0-or-later.

## Install and verify
- Download `emberbyte-1.0.0.apk` and check it against `emberbyte-1.0.0.apk.sha256`.
- The APK is signed by the maintainer. The certificate SHA-256 fingerprint is:
  `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`
  Check it with `apksigner verify --print-certs emberbyte-1.0.0.apk`.
- Requires Android 10 or newer (built for Android 16). The first launch asks for Usage access so the app can show per-app figures.
- The build is reproducible: see `docs/release/RELEASE.md` to rebuild it from source.

## Known limits
- English only for now; more languages are planned.
- The status bar chip for live updates depends on how the phone maker implements Android 16 Live Updates. On a Galaxy S24+ with One UI 8.5 the chip did not appear in our test; the live notification still shows in the notification shade. Settings has a "Live updates" row that shows whether Android allows it.
- Usage figures come from Android's own network statistics, so they can differ slightly from your carrier's count. Use the "I've used some already" option when you start a plan to match your carrier.
- A backup cannot be restored without its passphrase, and there is no way to recover it.

## Credits
- **Khaled Bahaa Eldin** ([@Khaledbahaaeldin](https://github.com/Khaledbahaaeldin)): maintainer, design and product.
- **Omar Amin** ([@OmarAminAI](https://github.com/OmarAminAI)): implementation and testing (History and alert fixes, the project's domain glossary) and the Live Lens research that led to Emberbyte staying free of any VPN or network code.
- Theme palettes by their original authors under their licences, and Roboto Flex under the SIL Open Font License; see `NOTICE` and the Licences screen in the app.
- Parts of the implementation were built with Gemini (Antigravity), and specifications and audits were written with Claude (Anthropic).

Full list of changes: [CHANGELOG.md](https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md).
