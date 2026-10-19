# Milestone 10 Proof: Onboarding, Settings, Dialogs & System Surfaces

- **Milestone:** M10 System Surfaces & Polish Close (Tasks D1–D7) & Stop S4 Verification
- **Branch:** `redesign`
- **Target Commits:** `4067299` (`feat(states): use the shared empty and loading states everywhere`) .. `4a23acd` (`feat(onboarding): replace the single page by a four-page intro`)
- **Device Tested:** Headless AVD `Violet_API_36` on `emulator-5554` (API 36 / Baklava)
- **Date:** 2026-10-19
- **Verdict:** PASS

---

## 1. Executive Summary

Milestone 10 completed the Emberbyte redesign across onboarding, settings, dialogs, notification styling, open-source licences, and unified empty/loading states:
1. **Four-Page Onboarding (D1):** Replaced the single-page permission wall with an accessible 4-page `HorizontalPager` explaining local data privacy, usage breakdown, and quota caps before requesting usage stats, with Skip jumping to permission and 2.0x font scale scroll resilience.
2. **Rounded Grouped Settings (D2):** Restructured Settings into elevated rounded surface cards with shaped heading icons, single-node 56dp minimum switch rows, an Appearance hero preview card, a units `ConnectedButtonGroup`, a battery background advice card, and About section.
3. **Expressive Dialog System (D3):** Unified all confirmation, discard, budget, and passphrase dialogs under `EmberbyteDialog` featuring expressive asymmetric corners, cookie-shaped icon containers, accessible pane semantics, and IME-aware scroll layouts that preserve action buttons and active inputs at 2.0x font scale.
4. **Active Theme Live Notification Colours (D4):** Coloured ongoing notifications using the active theme palette (light or dark per system night mode) with status accents: Ok (primary lilac), Warning (amber), and Exceeded (coral danger), styling both `Notification.Builder` and Android 16 `ProgressStyle` segments.
5. **In-App Open-Source Licences (D5):** Built the open-source licences browser from `licences.json` with search filtering, categorised sections, merged semantic rows, and automated build verification.
6. **Universal Loading & Empty States (D6):** Replaced all bare spinners with unified `LoadingState` and `EmptyState` across plans, settings, and detail views.
7. **Comprehensive System Proof (D7):** Validated clean quality gates, 286 added tests over M6 baseline, and 26 verified screenshots on `Violet_API_36`.

---

## 2. Quality Gate & Python Verification Outputs

### Full Gradle Quality Gate
```bash
./gradlew testDebugUnitTest :core:engine:test lintDebug assembleDebug assembleRelease lintRelease verifyReleaseApk --no-daemon "-Dorg.gradle.jvmargs=-Xmx2g" --max-workers=2
```
- **Per-Module Unit Tests:**
  - `:app`: **625** tests, 0 failures
  - `:core:data`: **349** tests, 0 failures
  - `:core:engine`: **145** tests, 0 failures
  - `:ui:design`: **233** tests, 0 failures
  - **Total:** **1,352** tests, 0 failures
- **Lint (Debug & Release):** 0 errors, 11 baseline warnings.
- **Release APK Verification:** `verifyReleaseApk` PASS (`app-release-unsigned.apk`, 6,284,527 bytes). Verified that release DEX is free of debug extras.

### Python Verification Tools
- **`python tools/device/check_pngs.py`**:
  ```text
  $ python tools/device/check_pngs.py .superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/d7
  Total checked: 26, Invalid: 0
  ALL PNGs VALID!
  ```
- **`python tools/palettes/generate_palettes.py --check`**:
  ```text
  up to date
  ```
- **`python tools/test-diff.py 467c41b HEAD --allow docs/redesign-test-changes.md`**:
  ```text
  ADDED 286
  Exit code: 0
  ```

---

## 3. Device Proof on Violet_API_36 (All 26 Opened Captures)

All captures are stored under `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/d7/` (and full paths are cited in `.superpowers/sdd/2026-10-17-emberbyte-redesign/task-D7-test-report.md`). Every image was opened directly and inspected:

### Part (a): Four-Page Onboarding Flow & Skip Behaviour (Captures 01–06)
- **Capture `01` (`01-onboarding-page1`):** Onboarding Page 1 of 4. Title `"Your data stays on your phone."`. Subtitle details zero tracking and local processing. Bottom row shows progress dot 1 active, text button `"Skip"` on left, and filled pill button `"Next"` on right. Floating navbar is hidden.
- **Capture `02` (`02-onboarding-page2`):** Onboarding Page 2 of 4. Title `"See your data"`. Explains live and historical usage graphs. Progress dot 2 active; `"Skip"` and `"Next"` displayed.
- **Capture `03` (`03-onboarding-page3`):** Onboarding Page 3 of 4. Title `"Stay under your cap"`. Explains budget alarms and notifications. Progress dot 3 active; `"Skip"` and `"Next"` displayed.
- **Capture `04` (`04-onboarding-page4`):** Onboarding Page 4 of 4. Title `"Allow usage access"`. Explains requirement for Android Usage Access. Features centered filled pill button `"Allow access"`. Progress dot 4 active; `"Skip"` button is absent.
- **Capture `05` (`05-onboarding-granted`):** Home screen immediately upon granting usage stats permission. Renders hero card `"Today"` with cookie badge `"0 B"`, section `"Top apps today"`, and floating bottom navigation pill bar with `"Home"` selected.
- **Capture `06` (`06-onboarding-skip-result`):** Result of tapping `"Skip"` on page 1. Tapping `"Skip"` navigated directly to page 4 without skipping permission grant; granting access navigated immediately to the Home screen.

### Part (b): Settings Screen, Licences & Appearance (Captures 07–17)
- **Capture `07` (`07-settings-main-top`):** Top of Settings screen. Features `"Settings"` top bar with back navigation arrow, grouped card `"Notifications"` with switches for `"Live notification"` (on) and `"Show speed"` (off), grouped card `"Alerts"` with switch for `"Spike alerts"` (on), and hero card `"Appearance"` showing current theme preview (`"Dusk"`).
- **Capture `08` (`08-settings-main-bottom`):** Middle of Settings screen. Features grouped section `"Units"` containing `ConnectedButtonGroup` with `"Decimal (GB)"` (selected with checkmark) and `"Binary (GiB)"`, and grouped card `"Data and backup"` with 4 single-node action rows (`"Export usage (CSV)"`, `"Export everything (JSON)"`, `"Create backup"`, `"Restore backup"`).
- **Capture `09` (`09-settings-about`):** Bottom of Settings screen. Features mint-tinted `"Background"` card with battery optimization notice and action button, and `"About"` card displaying version row `"Emberbyte 1.0.0"` and navigation row `"Open-source licences"`.
- **Capture `10` (`10-licences-screen`):** Open-source licences browser (`LicencesScreen`). Shows top search field (`"Search licences"`), `"Emberbyte is free software"` banner card, and categorized list of dependencies with merged name and licence tag pills (`"Roboto Flex"`, `"Apache-2.0"`, `"Bouncy Castle"`, `"MIT"`).
- **Capture `11` (`11-licences-search-okio`):** Licences screen filtered with search query `"okio"`. The list immediately filters to show only the `"okio"` item with version `"3.9.1"` and licence pill `"Apache-2.0"`.
- **Capture `12` (`12-licences-search-empty`):** Licences screen filtered with query `"nonexistent_dependency_xyz"`. Renders the unified `EmptyState` component displaying search icon, `"No licences found"`, and descriptive subtitle.
- **Capture `13` (`13-appearance-screen`):** Appearance screen in System mode. Displays 3-option theme selector (`System`, `Light`, `Dark`), AMOLED black toggle, and 12 theme swatches (`Dusk`, `Ember`, `Lavender`, `Teal`, `Rose`, `Slate`, `Catppuccin`, `Nord`, `Dracula`, `Forest`, `Ocean`, `Monochrome`).
- **Capture `14` (`14-appearance-light-mode`):** Appearance screen in Light mode. Background surface shifts to light theme canvas and swatches preview light palette tokens.
- **Capture `15` (`15-appearance-dark-mode`):** Appearance screen in Dark mode. Background shifts to dark theme canvas with lilac accents.
- **Capture `16` (`16-appearance-amoled-mode`):** Appearance screen in Dark mode with `"AMOLED black"` toggled ON. Canvas changes to pure black `#000000` with subtle border outlines.
- **Capture `17` (`17-settings-amoled`):** Settings screen viewed in pure AMOLED mode. Background canvas renders pure `#000000` with rounded container cards elevated above it.

### Part (c): Expressive Dialogs at Font Scale 2.0 (Captures 18–21)
- **Capture `18` (`18-dialog-create-backup-font20`):** `"Create backup"` dialog opened at `font_scale = 2.0` with soft keyboard active. Displays cookie-shaped lock icon, heading `"Create backup"`, scrollable passphrase fields, and action buttons (`"Create"`, `"Cancel"`). Inputs and buttons remain fully reachable above the IME.
- **Capture `19` (`19-dialog-restore-backup-font20`):** `"Restore backup"` confirmation dialog at `font_scale = 2.0`. Displays cookie-shaped warning icon, heading `"Restore backup?"`, explanatory warning text, and action buttons (`"Restore"`, `"Cancel"`).
- **Capture `20` (`20-dialog-budget-font20`):** Budget editor dialog (`BudgetEditorDialog`) at `font_scale = 2.0` with numeric keyboard open. Displays cookie wallet icon, heading `"Set daily budget"`, allowance input field with unit chips, and action buttons (`"Save"`, `"Cancel"`).
- **Capture `21` (`21-dialog-discard-changes-font20`):** Discard confirmation dialog at `font_scale = 2.0`. Displays alert triangle icon, heading `"Discard changes?"`, descriptive text, and coral/danger pill button for `"Discard"`.

### Part (d): Live Notification Styling on API 36 (Captures 22–24)
- **Capture `22` (`22-notification-ok`):** Live notification in **Ok** state (`10.0 GB left`). Styled with theme primary lilac accent (`#ffd0bcff`), `ProgressStyle` segment at 50%, text `"10.0 GB left · ↓ 0 B/s"`, and status bar drop icon.
- **Capture `23` (`23-notification-warning`):** Live notification in **Warning** state (`1.50 GB left`). Styled with theme amber warn accent (`#ffffd98a`), `ProgressStyle` segment at 85%, text `"1.50 GB left · ↓ 0 B/s"`.
- **Capture `24` (`24-notification-exceeded`):** Live notification in **Exceeded** state (`0 B left`). Styled with theme coral danger accent (`#ffffb4ab`), `ProgressStyle` segment filled to 100%, text `"No data left · ↓ 0 B/s"`.
- **API 36 Status Chip Note:** `dumpsys notification --noredact` confirmed `android.template=android.app.Notification$ProgressStyle`, `styledByProgress=true`, `requestPromotedOngoing=true`, and extras `android.shortCriticalText` ("10 GB", "1.5 GB", "0 B"). In the AVD UI of Android 16 (`Violet_API_36`), the status bar icon displays without an expanded inline status bar text pill (a known emulator SystemUI characteristic), marked CANNOT-VERIFY in status bar while verified in notification shade and dumpsys.

### Part (e): Accessibility: RTL & Motion (Captures 25–26)
- **Capture `25` (`25-rtl-home`):** Home screen under Arabic locale (`ar-AE`) with system animations disabled (`0.0`). Screen layout mirrors completely: header alignment, card placements, and bottom navigation pill tabs mirror correctly while preserving LTR numeric units (`"0 B"`).
- **Capture `26` (`26-rtl-settings`):** Settings screen under `ar-AE` locale with animations off (`0.0`). Grouped card headers, icon placements, action chevron arrows, and switches mirror seamlessly to the left side with instant layout transitions.

---

## 4. Cited Automated Tests

- `OnboardingScreenTest.four_pages_with_page_n_of_4_descriptions`
- `OnboardingScreenTest.next_advances_and_the_last_page_shows_allow_access`
- `OnboardingScreenTest.skip_jumps_to_the_permission_page_not_out_of_onboarding`
- `OnboardingScreenTest.permission_granted_completes_onboarding_exactly_once`
- `OnboardingScreenTest.font_scale_2_no_clip`
- `OnboardingScreenTest.reduce_motion_changes_pages_without_animation`
- `OnboardingScreenTest.rtl_reverses_the_swipe_direction`
- `SettingsScreenTest.every_setting_row_is_one_node_with_label_and_state`
- `SettingsScreenTest.switch_rows_toggle_on_whole_row_click`
- `SettingsScreenTest.group_headings_are_headings_for_talkback`
- `SettingsScreenTest.appearance_row_shows_the_current_theme_name_and_navigates`
- `SettingsScreenTest.font_scale_2_no_clip`
- `SettingsScreenTest.rtl`
- `LicencesScreenTest.screen_shows_the_list_and_a_search_filters_it`
- `LicencesScreenTest.each_entry_is_one_node_with_name_and_licence`
- `LicencesScreenTest.empty_search_shows_the_empty_state`
- `LicencesScreenTest.font_scale_2_no_clip`
- `EmberbyteDialogTest.title_is_a_heading_and_the_dialog_is_a_pane`
- `EmberbyteDialogTest.destructive_confirm_has_danger_colours_and_an_icon`
- `EmberbyteDialogTest.content_scrolls_and_the_buttons_stay_visible_at_font_scale_2`
- `EmberbyteDialogTest.keyboard_open_keeps_the_focused_field_and_the_buttons_visible`
- `LiveNotificationTest.accent_follows_the_theme_state_colour_for_ok_warning_exceeded`
- `LiveNotificationTest.accent_uses_the_light_palette_in_day_mode_and_dark_in_night_mode`
- `StatesEverywhereTest.no_screen_uses_a_bare_circular_progress_indicator`

---

## 5. Conclusion & Stop S4 Readiness

Milestone 10 and all Stop S4 requirements are verified and complete:
- All 26 D7 screenshots in `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/d7/` pass `python tools/device/check_pngs.py` (`Invalid: 0`).
- Every screenshot was opened and visually verified.
- All 1,352 unit tests pass across all 4 modules (`ADDED 286` over M6 baseline).
- Palettes check is up to date (`python tools/palettes/generate_palettes.py --check`).
- Release APK verified (`verifyReleaseApk`).
- Emulator restored and shut down cleanly.
