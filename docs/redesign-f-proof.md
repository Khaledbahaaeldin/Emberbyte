# Redesign Additions Proof — Tasks F0 through F3

This document provides formal verification proof for Part 7 additions (Tasks F0 to F3) of the Emberbyte Expressive Redesign on branch `redesign`.

---

## 1. Summary of Completed Additions

### Task F0 — Widget Polish (`a18b060`)
- **Fixes Applied**:
  - Today widget header label clipping eliminated.
  - Plan widget progress ring displays inner percentage label with palette styling.
  - Bento widget tile subtitle ("3.80 GB left") bottom clipping resolved.
  - Bento widget at 280x180 dp (4x2) dynamically displays hero tile, plan ring, and dual-tone network bar, gracefully degrading to hero plus single tile on narrower configurations.
  - Dashboard 7-day mini-chart renders cleanly as rounded bars with visible height distinction.
  - All widget tile containers enforce rounded and mixed-corner radii matching expressive design specifications.
- Recaptured verified store screenshot `docs/store/screenshots/07_widgets_preview.png`.

### Task F1 — Refined Predictive Back Navigation (`8ef91b1`)
- Enabled `android:enableOnBackInvokedCallback="true"` on `<application>` in `AndroidManifest.xml`.
- Created `BackTransitions` (`popExit`, `popEnter`, `cornerRadiusFor`, `SHRINK = 0.90f`, `SLIDE_DP = 28`, `BEHIND_SCALE_START = 0.94f`, `CORNER_DP = 32`).
- Implemented `GuardedBack` wrapping `PredictiveBackHandler` for dirty forms. Clean forms in Plan Editor, Plan Flow, and Onboarding do not intercept back gestures, allowing smooth system pop transitions. Dirty forms intercept back to display the expressive `EmberbyteDialog` discard confirmation.
- Sub-screen pop transitions now use `BackTransitions` instead of horizontal tab slide transitions. Reduced motion mode degrades to a clean 90 ms cross-fade. RTL layouts mirror transform origin and slide direction.

### Task F2 — Live-Update Promotion on Android 16 (`6fef91a`)
- Implemented `PromotedGate` supporting Android 16 live notification promotion:
  - API 36.0: Sets `"android.requestPromotedOngoing"` boolean extra.
  - API 36.1+: Invokes `Notification.Builder.setRequestPromotedOngoing(true)`.
  - System permission check via `NotificationManager.canPostPromotedNotifications()`.
  - Package-specific settings intent targeting `android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS`.
- Implemented `ChipText`: compact status-bar chip text guaranteed $\le 7$ characters across all unit systems and magnitudes ($0\text{ B}$ to $999\text{ TB}$).
- Handled daily dismissal via unexported `PromotedDismissReceiver`: records `promoted_dismissed_on` upon notification swipe dismissal, suppressing re-promotion until the next calendar day.
- Added user-facing `LiveUpdatesRow` in Settings Notifications group with allowed / turned off subtitle, direct intent navigation, and Samsung-specific guidance for Now Bar.
- Authored user checklist `docs/redesign-f2-phone-checklist.md` for physical Galaxy S24+ (One UI 8.5) validation.

### Task F3 — Documentation & End-to-End Proof
- Updated `docs/accessibility-audit.md` with matrix rows and cited tests for Guarded Predictive Back and Live Updates settings row (139 verified cited tests).
- Updated `CHANGELOG.md` and `README.md` reflecting predictive back and live update additions.
- End-to-end device execution across 21 verified screenshots on reference emulator `Violet_API_36`.

---

## 2. Gate & Quality Verification

### Clean Gate Execution
```powershell
.\gradlew.bat testDebugUnitTest :core:engine:test lintDebug assembleDebug assembleRelease lintRelease verifyReleaseApk --no-daemon "-Dorg.gradle.jvmargs=-Xmx2g" --max-workers=2 --stacktrace
```
- **Build Status**: `BUILD SUCCESSFUL` (0 test failures, 1422 total unit tests).
- **Lint Debug**: 0 errors, 11 warnings (baseline maintained, 0 new warnings).
- **Lint Release**: 0 errors, 11 warnings (baseline maintained, 0 new warnings).
- **Release APK**: `app-release-unsigned.apk` verified (6,315,215 bytes).

### Computed Test Counts (`tools/test-diff.py`)
- Against S5 commit (`6989548`):
  - `python tools/test-diff.py 6989548 HEAD --allow docs/redesign-test-changes.md`
  - Result: `ADDED 39`, 0 removed, 0 shrunk.
- Against Redesign base (`467c41b`):
  - `python tools/test-diff.py 467c41b HEAD --allow docs/redesign-test-changes.md`
  - Result: `ADDED 375`, all removed tests mapped to successor suites in `docs/redesign-test-changes.md`.

### Palettes Validation
- `python tools/palettes/generate_palettes.py --check`
- Result: `up to date` (code 0).

### Accessibility Audit Test Verification
- `python tools/check-cited-tests.py docs/accessibility-audit.md`
- Result: `PASS: All 139 cited test(s) verified across 1 file(s).` (code 0).

---

## 3. Device Proof (`Violet_API_36`)

All 21 device screenshots are archived in `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/f3/` and verified with `python tools/device/check_pngs.py`:

| File | Scenario | Verified Visual Behavior |
|:---|:---|:---|
| `01_onboarding_page2.png` | Onboarding Pager | Page 2 ("STEP 2 OF 4") rendered with dot indicator |
| `02_onboarding_swiped_back_page1.png` | Onboarding Back | Back action pops cleanly to Page 1 |
| `03_appearance_screen.png` | Appearance Screen | 12 theme presets and mode selectors |
| `04_appearance_swipe_back.png` | Subscreen Predictive Back | Predictive back gesture initiates shrink (0.90) and 32dp corner rounding |
| `05_settings_restored.png` | Settings Restored | Settings screen smoothly restored |
| `06_licences_screen.png` | Licences Screen | Full-screen open source notice text |
| `07_licences_swiped_back.png` | Licences Back | Clean pop back to Settings |
| `08_app_detail_screen.png` | App Detail Screen | App usage metrics and 7-day breakdown |
| `09_app_detail_swiped_back.png` | App Detail Back | Clean pop back to Apps list |
| `10_plan_editor_clean.png` | Clean Plan Editor | Empty form with unmodified fields |
| `11_plan_editor_clean_left.png` | Clean Form Exit | Dispatches back without prompting dialog, returning to Plans immediately |
| `12_plan_editor_dirty_input.png` | Dirty Plan Editor | User has modified form fields |
| `13_plan_editor_dirty_discard_dialog.png` | Dirty Form Guard | Back gesture intercepted; Discard confirmation dialog displayed |
| `14_plan_editor_discarded_to_plans.png` | Form Discarded | Tapping Discard dismisses editor and returns to Plans screen |
| `15_apps_filters_sheet_open.png` | Filters Bottom Sheet | Filters sheet displayed over Apps screen |
| `16_apps_filters_sheet_dismissed.png` | Sheet Dismissal | Back action dismisses sheet with stock animation |
| `17_live_notification_ok.png` | Live Notification (Ok) | `ProgressStyle` notification with primary color and remaining balance |
| `18_settings_live_updates_font_2_0.png` | Font Scale 2.0 | Settings Notifications group with Live updates row reflowing cleanly |
| `19_settings_live_updates_rtl.png` | RTL (Arabic) | Mirrored layout with right-to-left alignment |
| `20_subscreen_swipe_back_rtl.png` | RTL Predictive Back | Predictive swipe-back gesture originating from right edge |
| `21_settings_animations_off.png` | Reduced Motion | Instant transitions with animator duration scale 0 |

---

## 4. Hardware Verification & Limitations

- **CANNOT-VERIFY**:
  - The proprietary Samsung status bar live-update chip and Now Bar on Galaxy S24+ (One UI 8.5) cannot be validated on the AOSP reference emulator.
  - Step-by-step instructions for the maintainer are documented in `docs/redesign-f2-phone-checklist.md`.
