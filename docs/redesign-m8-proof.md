# Milestone 8 Proof: Core Screens Redesign

- **Milestone:** M8 Core Screens Close (Tasks B1–B7)
- **Branch:** `redesign`
- **Target Commit:** HEAD of branch `redesign`
- **Device Tested:** Headless AVD `Violet_API_36` on `emulator-5554` (API 36)
- **Date:** 2026-10-18
- **Verdict:** PASS

---

## 1. Executive Summary

Milestone 8 redesigned the primary application user experience across all core screens:
1. **UsageRow & Tiles (B1):** Redesigned app usage rows with value bubbles, proportional progress bars, and real app icons in stable geometric shapes (`AppIcon`, `shapeFor`).
2. **Home Screen (B2):** Transformed Home into a hero-centric view leading with today's usage in `HeroNumber`, responsive wavy progress bar (`WavyBar`), live network throughput tile, forecast safety bento tile, and top apps summary.
3. **Apps Screen & Filters (B3):** Added the Featured App card highlighting the top data consumer, the bottom Filters sheet with selectable period, network, sort order, and system app visibility toggles, and removable active filter chips.
4. **App Detail Screen (B4):** Built a deep-dive app analytics screen with daily usage bar chart, Wi-Fi vs. Mobile breakdown, foreground vs. background usage bento tiles, and package information.
5. **History Screen (B5):** Redesigned the historical trends screen with connected period selector (`ConnectedButtonGroup`), period usage total hero, daily/weekly bar chart, and average usage statistics.
6. **Polish & State Sharing (B6):** Unified empty and loading states, animated tab switching, and completed UI polish across core surfaces.
7. **Milestone Proof (B7):** Verified full quality gate, clean baseline test diff (+150 tests added), zero lint errors, and 26 device verification screenshots covering Dusk dark, Catppuccin light, Gruvbox dark with pure-black AMOLED, font scale 2.0, RTL layout direction, and reduced motion.

---

## 2. Quality Gate Verification

### Command
```bash
./gradlew testDebugUnitTest :core:engine:test lintDebug assembleDebug assembleRelease lintRelease verifyReleaseApk --stacktrace --no-daemon "-Dorg.gradle.jvmargs=-Xmx2g" --max-workers=2
```

### Gate Output
- **Unit Tests:** All unit tests passing across all modules (`:app`, `:core:data`, `:core:engine`, `:ui:design`).
- **Lint (Debug & Release):** 0 errors, 11 baseline warnings (identical to baseline).
- **Release APK Verification:** `verifyReleaseApk` task PASS (`app-release-unsigned.apk` 6,057,102 bytes).
- **Build Result:** `BUILD SUCCESSFUL`.

### Python Verification Tools
- `tools/palettes/generate_palettes.py --check`:
  ```
  up to date
  ```
- `tools/check-cited-tests.py docs/accessibility-audit.md`:
  ```
  PASS: All 93 cited test(s) verified across 1 file(s).
  ```
- `tools/test-diff.py 467c41b HEAD --allow docs/redesign-test-changes.md`:
  ```
  ADDED 150
  Exit code: 0
  ```

---

## 3. Device Proof on Violet_API_36

### Test Environment & Seed Data
- Device: `emulator-5554` (`Violet_API_36`, Android API 36).
- Seed Backup: Generated via `python tools/seed/make_backup.py --days 7 --scenario basic --pseudo-uid --end-time 2026-10-18T23:59:59Z --out tools/seed/fixtures/m8_seeded.emberbyte`.
- Restored cleanly in app: 1 plan (20 GB cap), 1 budget, and 1,008 usage rows restored.

### Screenshot Matrix (26 Screenshots)
All screenshots captured in binary mode from `adb -s emulator-5554 exec-out screencap -p` and verified with header `89504e47` (`\x89PNG`).

1. `screenshots/b7/01_dusk_dark_home.png`: Home screen in Dusk dark; 1.48 MB hero number with cookie badge, wavy progress bar, Forecast safe tile, live throughput tile, network breakdown, and top apps list.
2. `screenshots/b7/02_dusk_dark_apps.png`: Apps screen in Dusk dark; Featured card with Google Play services in soft burst shape, app list with real icons.
3. `screenshots/b7/03_dusk_dark_apps_scrolled_uid.png`: Apps screen with search for `10001` and `7 days` & `System apps` chips; `UID 10001` rendered with letter fallback "U" inside cookie shape and 327 MB bar.
4. `screenshots/b7/04_dusk_dark_apps_filter_sheet.png`: Filters modal bottom sheet showing Period (Today/7 days/Month), Network (All/Mobile/Wi-Fi), Sort order, and System apps switch.
5. `screenshots/b7/05_dusk_dark_apps_filter_applied.png`: Apps screen with active removable `Wi-Fi ✕` chip.
6. `screenshots/b7/06_dusk_dark_app_detail.png`: App Detail screen for Google Play services in Dusk dark; BentoTiles, 7-day daily bars.
7. `screenshots/b7/07_dusk_dark_app_detail_uid.png`: App Detail screen for pseudo-UID `UID 10001` with letter fallback "U" in cookie shape, BentoTiles, daily bars.
8. `screenshots/b7/08_dusk_dark_history.png`: History screen in Dusk dark; Day/Week/Month ConnectedButtonGroup, 2.80 GB hero number, 7-day bar chart, BentoTiles.
9. `screenshots/b7/09_appearance_catppuccin_light.png`: Appearance screen in Catppuccin theme with Light mode selected.
10. `screenshots/b7/10_catppuccin_light_home.png`: Home screen in Catppuccin Light (`#EFF1F5` background, Mauve hero number, Green forecast).
11. `screenshots/b7/11_catppuccin_light_apps.png`: Apps screen in Catppuccin Light; Featured card with Chrome in sunny shape, app list with Chrome, Messages, Phone, Settings, Camera, and UID 10001.
12. `screenshots/b7/12_catppuccin_light_app_detail.png`: Chrome App Detail in Catppuccin Light with real Chrome logo, Mauve hero number, and daily bars.
13. `screenshots/b7/13_catppuccin_light_history.png`: History screen in Catppuccin Light with Mauve tiles and daily bars.
14. `screenshots/b7/14_appearance_gruvbox_amoled.png`: Appearance screen in Gruvbox Dark mode with Pure black AMOLED enabled.
15. `screenshots/b7/15_gruvbox_amoled_home.png`: Home screen in Gruvbox Dark AMOLED; pure black `#000000` background, Gruvbox gold hero number, green forecast tile.
16. `screenshots/b7/16_gruvbox_amoled_apps.png`: Apps screen in Gruvbox Dark AMOLED; pure black background with gold chips and featured card.
17. `screenshots/b7/17_gruvbox_amoled_app_detail.png`: Chrome App Detail in Gruvbox Dark AMOLED with gold daily usage bars.
18. `screenshots/b7/18_gruvbox_amoled_history.png`: History screen in Gruvbox Dark AMOLED with gold hero number, bars, and bento tiles.
19. `screenshots/b7/19_font_scale_2_home.png`: Home screen at font scale 2.0; hero number, forecast card, network bar, and navbar rendered without clipping.
20. `screenshots/b7/20_font_scale_2_apps.png`: Apps screen at font scale 2.0; wrapped search label, filters button, chips, and featured card scaling legibly.
21. `screenshots/b7/21_font_scale_2_app_detail.png`: App Detail at font scale 2.0; Chrome logo, hero number, daily usage bar chart, and bento tiles.
22. `screenshots/b7/22_font_scale_2_history.png`: History screen at font scale 2.0; connected button group, hero number, 7-day bar chart, and bento tiles.
23. `screenshots/b7/23_rtl_home.png`: Home screen in Arabic RTL layout; Settings gear on left, Today on right, wavy bar progress filling from right to left, navbar ordered Plans (left), Apps (center), Home (right).
24. `screenshots/b7/24_rtl_apps.png`: Apps screen in Arabic RTL layout; title and search on right, filters on left, featured card with icon on right and values on left, app list icons on right, navbar mirrored.
25. `screenshots/b7/25_rtl_history.png`: History screen in Arabic RTL layout; back arrow pointing right on right, ConnectedButtonGroup Day on right and Month on left, daily bars flowing right to left from Monday to Sunday, bento tiles mirrored, navbar mirrored.
26. `screenshots/b7/26_animations_off_home.png`: Home screen rendered with system animations disabled (`0.0` scale), confirming instant crisp snap transitions.

### Key Unit & Regression Tests Cited
- `CompactNavBarTest.tabs_are_ordered_right_to_left_in_rtl`: verifies RTL tab mirroring in navigation bar.
- `HeroNumberTest.fits_at_font_scale_2_in_a_220_dp_container`: verifies hero number typography auto-shrinking at font scale 2.0.
- `AppIconTest.shows_the_first_letter_when_there_is_no_icon`: verifies letter fallback inside cookie shape when no icon exists.
- `AppIconProviderTest.returns_null_for_uid_pseudo_packages`: verifies null icon for `uid:` pseudo-packages.
- `ConnectedButtonGroupTest.selected_segment_has_selected_state_and_a_check_icon`: verifies period selector tabs and check icons.
- `WavyRingTest.state_colours_follow_the_palette`: verifies budget ring progress styling and palette integration.
- `AppRowRedesignTest.row_is_one_node_with_click_action_and_56dp_minimum_height`: verifies usage row value bubble and label layout.
- `ThemeSweepTest.home_screen_renders_in_all_twelve_themes_dark_and_light`: verifies core screens compose across 12 palettes in light/dark.
- `SeededBackupFixtureTest.restores_seeded_backup_fixture_with_expected_counts`: verifies seeded backup payload roundtrip.

### Crash Log Verification
- `adb logcat -d` inspected for `io.github.khaledbahaaeldin.emberbyte`.
- Fatal exceptions: exactly **0** crashes.
- Device settings restored: font scale 1.0, locale en-US, RTL direction 0, animation scales 1.0.
