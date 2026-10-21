# F-Droid merge request text (copy into the merge request description)

Title: `New App: Emberbyte (io.github.khaledbahaaeldin.emberbyte)`

---

Adds **Emberbyte**, a free and open-source Android data-usage monitor.

- Source: https://github.com/Khaledbahaaeldin/Emberbyte (GPL-3.0-or-later, `LICENSE` in the repository)
- Website: https://emberbyte-site.vercel.app
- Release: tag `v1.0.1` (commit `e5d17dffd04dd9375379b76320673eccf59f4387`), versionName 1.0.1, versionCode 2
- I am the author and maintainer.

**What it does:** measures mobile and Wi-Fi usage per app from Android's own network statistics, lets the user define
data plans (billing cycles, SIM binding, add-ons, rollover), forecasts when the plan runs out, alerts at thresholds,
and shows home-screen widgets and a live notification. Everything stays on the device.

**Anti-features:** none. The app has **no `INTERNET` permission**, no accounts, no analytics, no ads and no network code
(there is no VPN service either). All dependencies come from Maven Central and Google Maven and are free software
(listed in `NOTICE` and `docs/release/dependency-licences.txt`). Roboto Flex (SIL OFL) and community theme palettes
(MIT, licence texts under `licenses/`) are bundled with their licences.

**Permissions, and why:**
- `PACKAGE_USAGE_STATS`: per-app network usage buckets (`NetworkStatsManager`); the user grants it in system settings.
- `QUERY_ALL_PACKAGES`: to show app names and icons for the usage list; without it Android 11+ hides most apps.
- `READ_PHONE_STATE`: on multi-SIM phones, to bind usage to the SIM of a plan.
- `POST_NOTIFICATIONS`, `POST_PROMOTED_NOTIFICATIONS`, `FOREGROUND_SERVICE(_SPECIAL_USE)`, `RECEIVE_BOOT_COMPLETED`,
  `WAKE_LOCK`: the live notification, alerts and the sampler that keeps measuring after a reboot.
  The full table is in the README.

**Build:** Gradle (wrapper, Gradle 9.6.0), JDK 21 (the build server's own; the code is compiled for Java 17 bytecode), `compileSdk` 37 (platform `android-37.0`) and build-tools 36.0.0 (installed with `sdkmanager`), `subdir: app`. Release builds are reproducible: CI builds the tag twice and compares the APKs
(`tools/compare-apks.py`), and builds on Linux like the F-Droid server. The `fdroid build` job in this merge request's
pipeline passes on F-Droid's own build server (all 9 jobs are green).

**Signing:** F-Droid signs the build itself. (I considered reproducible-build verification against my GitHub APK, but the F-Droid server uses Debian's JDK while my CI uses Temurin, so I left it out to keep the first listing simple.)

**Metadata:** fastlane metadata (title, short and full description, changelogs `1.txt` and `2.txt`, icon and seven screenshots)
is in the source repository under `fastlane/metadata/android/en-US/`.

**Updates:** `AutoUpdateMode: Version` with `UpdateCheckMode: Tags`; I tag every release as `vX.Y.Z`.
