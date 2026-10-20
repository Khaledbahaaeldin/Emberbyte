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

**Build:** Gradle (wrapper, Gradle 9.6.0), JDK 17, `compileSdk` 37, build-tools 36.1.0, run from the repository root with
`subdir: app`. Release builds are reproducible: CI builds the tag twice and compares the APKs
(`tools/compare-apks.py`), and builds on Linux like the F-Droid server. I have **not** run `fdroid build` locally
(no suitable machine), so I rely on this pipeline and will fix whatever it reports.

**Reproducible build with my signature (optional):** the recipe sets `Binaries` and `AllowedAPKSigningKeys` so that,
if the build matches, the APK signed with my release key is published. Signing certificate SHA-256:
`72ad25a3d8366a07339c7b239be0dfd70d427cfb7dfe498b92621e4c604595ef`. The signed APK is built and signed by the public
workflow `.github/workflows/release.yml`. If the comparison fails, I will remove those two lines so F-Droid signs
the build itself.

**Metadata:** fastlane metadata (title, short and full description, changelog `1.txt`, icon and seven screenshots)
is in the source repository under `fastlane/metadata/android/en-US/`.

**Updates:** `AutoUpdateMode: Version` with `UpdateCheckMode: Tags`; I tag every release as `vX.Y.Z`.
