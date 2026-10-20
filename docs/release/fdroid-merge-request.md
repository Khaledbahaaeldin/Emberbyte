# F-Droid merge request text (copy into the merge request description)

Title: `New App: Emberbyte (io.github.khaledbahaaeldin.emberbyte)`

---

Adds **Emberbyte**, a free and open-source Android data-usage monitor.

- Source: https://github.com/Khaledbahaaeldin/Emberbyte (GPL-3.0-or-later, `LICENSE` in the repository)
- Website: https://emberbyte-site.vercel.app
- Release: tag `v1.0.0` (commit `7909bbc8ca4ab790d2592127f9306d4acfe0394d`), versionName 1.0.0, versionCode 1
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

**Build:** Gradle (wrapper, Gradle 9.6.0), JDK 17 (installed through `sudo` in the recipe, because the project uses a Java 17 toolchain), `compileSdk` 37 and build-tools 36.1.0 (installed with `sdkmanager`), `subdir: app`. Release builds are reproducible: CI builds the tag twice and compares the APKs
(`tools/compare-apks.py`), and builds on Linux like the F-Droid server. I have **not** run `fdroid build` locally
(no suitable machine), so I rely on this pipeline and will fix whatever it reports.

**Signing:** F-Droid signs the build itself. (I considered reproducible-build verification against my GitHub APK, but the F-Droid server uses Debian's JDK while my CI uses Temurin, so I left it out to keep the first listing simple.)

**Metadata:** fastlane metadata (title, short and full description, changelog `1.txt`, icon and seven screenshots)
is in the source repository under `fastlane/metadata/android/en-US/`.

**Updates:** `AutoUpdateMode: Version` with `UpdateCheckMode: Tags`; I tag every release as `vX.Y.Z`.
