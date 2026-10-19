# Milestone 9 Proof: Plans Redesign, Urgency Ranking & Mid-Cycle Tracking

- **Milestone:** M9 Plans Redesign Close (Tasks C1–C10) & Stop S3 Repairs
- **Branch:** `redesign`
- **Target Commits:** `0738fcf` (`fix(plans): stack allowance inputs full-width and hide navbar on sub-screens`) / `46448e8` (`fix(home): put see all on header row, ensure rounded bar stub and fix rtl nav pill`)
- **Device Tested:** Headless AVD `Violet-API-36` on `emulator-5554` (API 36)
- **Date:** 2026-10-19
- **Verdict:** PASS

---

## 1. Executive Summary

Milestone 9 overhauled the data plan architecture and user interface across engine, data, backup, and presentation layers, followed by Stop S3 verification and layout repairs:
1. **Engine Core (C1):** Added `FirstCycleMode` (`PhoneRecord`, `Entered(amountBytes, enteredAt, cycleStart)`, `StartNextCycle`) to the domain model, supporting plans created mid-cycle with prorated windows, reset to phone records across cycle boundaries, and an urgency comparator prioritizing over-cap, active, near-cap, and dormant plans.
2. **Database v4 (C2):** Migrated Room database schema from version 3 to version 4 with `Migration(3, 4)` adding `firstCycleMode`, `firstCycleBytes`, `firstCycleAt`, `firstCycleStart`, and `pinned` columns with safe phone defaults (`'phone'`) for existing plans.
3. **Backup & Export v2 (C3):** Extended `.emberbyte` encrypted backup format and JSON export to schema version 2 carrying first-cycle fields and pinning state with seamless backward compatibility for version 1 files (`contractVersion = 1`) and forward-tolerant defaults.
4. **Urgency & Silencing (C4):** Implemented plan silencing for dormant plans (start-next-cycle before first renewal) suppressing ongoing notifications, widget representation, and alerts, while supporting user pinning to lock the front plan regardless of urgency rank.
5. **Urgency Bento (C5):** Designed the Plans Bento layout organizing active, dormant, and archived plans into an urgency-ranked adaptive grid with front-plan spotlight and quick action overflow menus (`180.dp` bottom padding so the floating navbar never overlaps the last card).
6. **Plan Tiles & States (C6):** Built `PlanTile` and `PlanCard` components rendering active wavy rings (`WavyRing`), entered usage footnotes (`"Includes X GB you entered"`), dormant state visual indicators (`"Starts DD MMM"`, no progress ring), and over-cap alert styling (`"120%"`, `"!"` burst badge, `"Data used up"`).
7. **Guided Plan Flow (C7):** Implemented the 4/5-step guided plan creation wizard (`PlanFlowScreen`) with dynamic mid-cycle detection when the selected renewal day does not match the creation day, offering three clear choices (`"Use what my phone recorded"`, `"I've used some already"`, and `"Start next cycle"`), full-width stacked allowance/used-so-far text fields above `ConnectedButtonGroup`, full-row radio selection, and hidden bottom navbar on sub-screens.
8. **Plan Editor Sections (C8):** Restructured plan editing into grouped cards with live `"Preview"` card, `"What if I use more?"` simulator, and the `"This cycle"` editor allowing users to inspect or change their first-cycle tracking mode.
9. **Automated Testing Suite (C9):** Added 6 exhaustive unit and integration tests across `:core:data` and `:app` pinning mid-cycle behavior across cycle boundaries, over-cap alerts, and backup roundtrips (10/10 passes in isolation).
10. **Device Verification, Upgrade Proof & S3 Repairs (C10):** Executed clean full gate (`BUILD SUCCESSFUL`), APK verification (`verifyReleaseApk` proving release APK is free of debug extras), real M6 upgrade migration on `Violet-API-36`, clean-install mid-cycle proof across all 36 captures in `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/c10/`, and B2 Home/Navbar layout fixes (`HomeLayoutTest.see_all_is_on_the_header_row_with_top_apps_today`, `AppRowRedesignTest.row_with_tiny_fraction_renders_rounded_stub_with_minimum_width`, `CompactNavBarTest.rtl_selected_nav_pill_has_correct_width_and_stays_within_slot`).

---

## 2. Quality Gate & Python Verification Outputs

### Full Gradle Quality Gate
```bash
./gradlew testDebugUnitTest :core:engine:test lintDebug assembleDebug assembleRelease lintRelease verifyReleaseApk --no-daemon "-Dorg.gradle.jvmargs=-Xmx2g" --max-workers=2 --stacktrace
```
- **Per-Module Unit Tests:**
  - `:app`: **579** tests, 0 failures
  - `:core:data`: **349** tests, 0 failures
  - `:core:engine`: **145** tests, 0 failures
  - `:ui:design`: **214** tests, 0 failures
  - **Total:** **1,287** tests, 0 failures
- **Lint (Debug & Release):** 0 errors, 11 baseline warnings.
- **Release APK Verification:** `verifyReleaseApk` PASS (`app-release-unsigned.apk`, 6,155,406 bytes). Verified that debug intent extra `"debug_fake_now"` is absent from release DEX.

### Python Verification Tools
- **`python tools/device/check_pngs.py`**:
  ```text
  $ python tools/device/check_pngs.py .superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/c10
  Total checked: 36, Invalid: 0
  ALL PNGs VALID!

  $ python tools/device/check_pngs.py .superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots
  Total checked: 148, Invalid: 0
  ALL PNGs VALID!
  ```
- **`python tools/palettes/generate_palettes.py --check`**:
  ```text
  up to date
  ```
- **`python tools/test-diff.py 467c41b HEAD --allow docs/redesign-test-changes.md`**:
  ```text
  ADDED 241
  Exit code: 0
  ```

---

## 3. Device Proof on Violet-API-36 (All 36 Opened Captures + B2 Home Fix)

All captures are stored under `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/c10/` (and full `.png` paths are cited in `.superpowers/sdd/2026-10-17-emberbyte-redesign/task-C10-test-report.md`). Every image was opened and inspected directly:

### Part (a): In-Place Upgrade Proof from M6 Build (`467c41b`)
- **Capture `01` (`01-upgrade-plans-survived`):**
  - Built M6 (`467c41b`) debug APK, installed on clean `Violet-API-36`, created two plans (`Active Plan 10v` active, `Archived Plan 2v` archived), and upgraded in-place via `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
  - **Visual contents:** Shows `"Plans"` screen with `"Active Plan 10v"` as the front full-width card, wavy ring reading `"11%"`, `"1.23 GB of 10.7 GB"`, `"9.51 GB left · 13 days left"`, `"Won't run out this cycle"`, and amber `"Estimated"` badge (confirming `firstCycleMode = "phone"`). Below it sits `"Show archived (1)"` (holding `"Archived Plan 2v"`). The status bar (`03:23`) displays the active Emberbyte water-drop live notification icon.
  - **SQLite verification:** `PRAGMA user_version` = `4`; both plans migrated with `firstCycleMode = 'phone'` and `pinned = 0`; zero fatal exceptions in `logcat -d`.

### Part (b): Clean Install Mid-Cycle Proof (`--el debug_fake_now 1792065600000`, 15 Oct 2026)
- **Captures `02`–`07` (Clean Onboarding, Seeded Restore & Empty Plans):**
  - `02`: Shows `"Welcome to Emberbyte"` with `"Usage access allowed"`, `"Notifications allowed"`, `"3. Your data plan"`, `"Set up a plan"`, `"Skip for now"`, and `"Continue"` (no floating navbar on onboarding).
  - `03`: Shows `"Home"` screen after onboarding (`"Today"`, `"0"` with `"B"` cookie badge, `"Top apps today"` and `"See all"` aligned on the same header row).
  - `04`–`06`: Shows `"Settings"` screen (floating navbar hidden), `"Restore backup"` passphrase dialog, and Settings after seeded restore.
  - `07`: Shows `"Plans"` empty state (`"No plans yet"`, `"Add your cap and renewal day to see what is left and when it runs out."`, `"Set up your first plan"`).
- **Captures `08`–`10` (Guided Plan Wizard Steps 1–3):**
  - `08`: `"Step 1 of 4"`, `"What is the plan called?"`, `Plan name` `"MidCycle_Phone"`, `"Not tied to a SIM"` selected, `"Continue"`.
  - `09`: `"Step 2 of 4"`, `"How much data do you get?"`, full-width `Data allowance` field showing `"10"`, full-width `ConnectedButtonGroup` below it (`"MB"`, `"✓ GB"` selected, `"TB"`), quick chips (`"5 GB"`, `"10 GB"`, `"20 GB"`, `"50 GB"`).
  - `10`: `"Step 3 of 5"` (dynamically expanded from 4 to 5 steps because renewal day `11` precedes fake clock `15 Oct`), `"✓ Monthly"`, `Day of month (1-31)` set to `"11"`, `"Renews next on 11 Nov"`.
- **Captures `11`–`13` (Mid-Cycle Choice 1: `"Use what my phone recorded"`):**
  - `11`: `"Step 4 of 5"`, `"This cycle is already under way."`, `"It started on 11 Oct. How should Emberbyte count the first cycle?"`, Choice 1 `"Use what my phone recorded"` (`"Might miss use from before Emberbyte could measure."`) selected.
  - `12`: `"Step 5 of 5"`, `"Review your plan"`, `"Name: MidCycle_Phone"`, `"Cap: 10 GB"`, `"Cycle: Monthly on day 11"`, `"This cycle: Phone's record (Estimated)"`, `"Create plan"`.
  - `13`: `"Plans"` screen showing `"MidCycle_Phone"` with wavy ring `"2%"`, `"259 MB of 10.0 GB"`, `"9.74 GB left · 27 days left"`, `"Won't run out this cycle"`.
- **Captures `14`–`16` (Mid-Cycle Choice 2: `"Start next cycle"` Dormant):**
  - `14`: `"Step 4 of 5"` with `"Start next cycle"` (`"Starts 11 Nov. No alerts until then."`) selected.
  - `15`: `"Step 5 of 5"`, `"Name: NextCycle_Plan"`, `"Cap: 10 GB"`, `"Cycle: Monthly on day 11"`, `"This cycle: Starts next cycle on 11 Nov"`.
  - `16`: `"Plans"` screen with `"MidCycle_Phone"` (`2%`) as front card and `"NextCycle_Plan"` below as a compact dormant tile reading `"NextCycle_Plan"` and `"Starts 11 Nov"` with no progress ring.
- **Captures `17`–`19` (Mid-Cycle Choice 3: `"I've used some already"`, `3 GB`):**
  - `17`: `"Step 4 of 5"` with `"I've used some already"` selected, revealing the full-width `"Used so far"` text field and `"MB"` / `"✓ GB"` / `"TB"` selector (`"Continue"` disabled while blank).
  - `18`: `"Step 5 of 5"`, `"Name: Entered_Plan"`, `"Cap: 10 GB"`, `"Cycle: Monthly on day 11"`, `"This cycle: Entered: 3 GB"`.
  - `19`: `"Plans"` screen where `"Entered_Plan"` promotes to the front card by urgency (`"30%"` wavy ring, `"3.00 GB of 10.0 GB"`, `"7.00 GB left · 27 days left"`, `"Won't run out this cycle"`, footnote `"Includes 3.00 GB you entered"`), with `"MidCycle_Phone"` (`"2%"`, `"9.74 GB left"`) and `"NextCycle_Plan"` (`"Starts 11 Nov"`) in the 2-column Bento row below.
- **Captures `20`–`22` (Over-Cap Entry: `12 GB` on `10 GB` Cap):**
  - `20`: `"Step 4 of 5"` with `"12"` entered in full-width `"Used so far"` (`"✓ GB"`), showing the amber warning `"This is already over your cap"` while `"Continue"` stays enabled.
  - `21`: `"Step 5 of 5"`, `"Name: Overcap_Plan"`, `"Cap: 10 GB"`, `"Cycle: Monthly on day 11"`, `"This cycle: Entered: 12 GB"`.
  - `22`: `"Plans"` screen with `"Overcap_Plan"` promoted to the front card in coral/danger state: wavy ring `"120%"` with `"!"` burst badge, `"12.0 GB of 10.0 GB"`, `"No data left · 27 days left"`, `"Data used up"`, and `"Includes 12.0 GB you entered"`.
- **Captures `23`–`27` (Pin/Unpin & Archive/Unarchive):**
  - `23`: 3-dot overflow menu open on `"Overcap_Plan"` showing `"Pin"`, `"Edit"`, `"Archive"`.
  - `24`: `"Overcap_Plan"` pinned with lavender pushpin icon (`Pinned`) beside the title.
  - `25`: `"Overcap_Plan"` unpinned (pushpin icon removed).
  - `26`–`27`: `"Overcap_Plan"` archived into `"Show archived (1)"`, then expanded (`"Hide archived"`) showing the archived `"Overcap_Plan"` card (`"120%"`, `"12.0 GB of 10.0 GB"`, `"Includes 12.0 GB you entered"`, `"Archived"`).
- **Captures `28`–`30` (Plan Editor Sections & First-Cycle Mode Change):**
  - `28`–`29`: `"Edit plan"` screen showing live `"Preview"` card, `"What if I use more?"` simulator (`"Extra per day"`), `"Basics"`, `"SIM"`, `"Cycle"`, `"Rollover"`, `"Add-on packs"`, `"Free windows"`, `"This cycle"` (`"Use my phone's record"`, `"I've used some already"`, `"Start tracking from the next cycle"`), and `"Alerts"`.
  - `30`: After changing `"MidCycle_Phone"` to `"Start tracking from the next cycle"` and saving, `"MidCycle_Phone"` on the Plans screen renders as a dormant card showing `"MidCycle_Phone"` and `"Starts 11 Nov"`.
- **Captures `31`–`33` (Backup Creation, Roundtrip & `contractVersion = 1` Restore):**
  - `31`–`32`: `"Create backup"` dialog with passphrase & confirmation filled, and Settings showing `"Backup saved"`. Restoring the v2 backup round-tripped all 3 plans (`"Restored 3 plans, 0 budgets and 831 usage rows (120 skipped because the app is not installed)"`).
  - `33`: Settings screen after restoring `tools/seed/fixtures/small.emberbyte` (`contractVersion = 1`, passphrase `test-passphrase`), showing `"Restored 1 plan, 1 budget and 0 usage rows (1,008 skipped because the app is not installed)"`.
- **Captures `34`–`36` (Accessibility: `font_scale = 2.0`, Animations Off, RTL Arabic `ar-AE`):**
  - `34`: Plans screen at `font_scale = 2.0` with 2-column Bento row collapsed into full-width stacked cards and `"2%"` centered inside the ring without clipping.
  - `35`: Plans screen with all three animation scales set to `0`, settled immediately with no stuck transitions.
  - `36`: Plans screen in `ar-AE` RTL layout (`"Plans"` header on right, `"+ New plan"` on left, `WavyRing` on right of card, 3-dot menu on left, LTR numbers/units, and mirrored `CompactNavBar` with selected `"Plans"` pill filling its slot cleanly).
- **B2 Home Fix Capture (`b2-home-fixed`):**
  - Shows `"Home"` scrolled to `"Top apps today"` with `"See all"` on the same header row, `"Shell"` (`2.40 MB`), `"Google Play services"` (`2.15 MB`), and `"Google Play Store"` (`1.52 MB`) with rounded green bars and pill value bubbles, and `"History"` 7-day `UsageBarRow` with rounded minimum bar stubs (`Sat`, `Sun`, `Tue`, `Wed`, `Thu`) and tall `Mon` bar (`19 Oct · 13.7 MB`).

---

## 4. Cited Automated Tests

- `MidCycleRepositoryTest.plan_created_on_the_15th_with_entered_3gb_shows_3gb_plus_later_measured_bytes`
- `MidCycleRepositoryTest.plan_created_on_the_15th_starting_next_cycle_shows_nothing_until_the_1st_then_counts_from_zero`
- `MidCycleRepositoryTest.entered_plan_across_the_cycle_boundary_resets_to_phone_record`
- `MidCycleRepositoryTest.restore_of_a_backup_with_entered_and_later_plans_gives_identical_states`
- `MidCycleRepositoryTest.pinned_plan_survives_backup_roundtrip_and_leads_the_urgency_order`
- `MidCycleIntegrationTest.entered_over_cap_raises_one_alert_and_the_widget_data_says_over`
- `PlanFlowScreenTest.step2_allowance_field_and_step4_entered_field_have_usable_width_and_row_text_selects_choice`
- `setting_debug_fake_now_intent_extra_sets_process_clock`
- `missing_intent_extra_keeps_default_clock`
- `HomeLayoutTest.see_all_is_on_the_header_row_with_top_apps_today`
- `AppRowRedesignTest.row_with_tiny_fraction_renders_rounded_stub_with_minimum_width`
- `CompactNavBarTest.rtl_selected_nav_pill_has_correct_width_and_stays_within_slot`

---

## 5. Conclusion & Stop S3 Readiness

Milestone 9 and all Stop S3 review items are verified and complete:
- All 36 C10 screenshots and B2 screenshots in `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/` pass `python tools/device/check_pngs.py` (`Invalid: 0`).
- Every screenshot was opened and visually verified.
- All 1,287 unit tests pass across all 4 modules (`ADDED 241` over M6 baseline).
- Emulator shut down cleanly (`adb -s emulator-5554 emu kill`).
