# Emberbyte 1.0.3 Implementation Plan

> **For agentic workers (Antigravity, Claude Code and humans):** implement this plan task by task. Each task is self-contained: it names the findings it closes, the files, the signatures, the tests to write first, and the acceptance criteria. Read the **Global constraints**, **Agent operating rules** and your own task before touching code. Steps use the RED, GREEN, commit rhythm.

**Goal:** make every screen of Emberbyte readable, consistent, correctly animated and faithful to Material 3 Expressive, closing all 72 findings of the 1.0.2 UI audit, which include the tester's History report (C8 to C10).
**Architecture:** fix the contrast gate and colour roles first, add shared building blocks (screen scaffold, width-aware button group, animated visibility wrappers, press morph, typography and shape tokens), then migrate screens onto them.
**Tech stack:** Kotlin, Jetpack Compose, Material 3 Expressive `1.5.0-alpha29`, Navigation Compose 2.10.2, Robolectric Compose tests.
**Spec of record:** `docs/release/RELEASE-PLAN-1.0.3-report.md` (the findings, with reproduction steps and root causes; IDs A1 to H7 and C8 to C10).
**Scope rule (house culture):** minor updates are large. Everything here ships in 1.0.3 unless a task says "stretch"; nothing is deferred without a ledger entry.
**Release:** version 1.0.3, versionCode 4, on `release/1.0.3`. Follows the per-release checklist in `RELEASE-PLAN-2026-Q4.md`.

## Agent operating rules

1. **Branches.** Work on one branch per task, named `t<N>-<slug>` (for example `t3-connected-group`), created from the **latest `release/1.0.3`**. Open the pull request into `release/1.0.3`, never into `main`. Do not merge your own pull request: the maintainer audits against this spec first.
2. **Ownership.** Edit only the files your task lists (plus the tests next to them). If you need a change in a file another task owns, write it in the pull request description and stop; do not edit it. Shared files (`strings.xml`, `ContrastPairs.kt`, `EmberbyteTheme.kt`) are edited through the task that owns them, in the order given under "Order and branches".
3. **Tests first.** Write the named tests, run them and **watch them fail** for the stated reason before writing code. A test that passes before the change is a finding about the test; report it. Never weaken or delete an existing test to get green; if a test must change, explain why in the pull request.
4. **No scope shrinking, no silent deviation.** If the spec is wrong or impossible, make the smallest change that satisfies the finding's acceptance criterion and record it in the pull request description under "Rulings" (what, why, cost if wrong). Do not drop a work item.
5. **Commits.** Small focused commits, one per work item, message explains why. End each commit message with the co-author line the tooling gives you. Check `git config user.name` and `user.email` before every commit (personal identity per `CLAUDE.md`). Never `--no-verify`, never force-push, never amend a pushed commit. Confirm before pushing to a shared branch.
6. **Verification before you say done.** Run the task's tests, then the CI-equivalent command below, and paste the final lines of the output in the pull request. On this project's low-memory PC use `--max-workers=2 -Dorg.gradle.jvmargs="-Xmx1400m -XX:MaxMetaspaceSize=512m"`, and run `verifyReleaseApk` alone (it runs the whole app suite).
7. **Do not touch** release paperwork (`CHANGELOG.md`, `fastlane/**`, `RELEASE-NOTES-*`, `app/build.gradle.kts` version fields) except in T15. Put a one-line "what changed" in your pull request description; the maintainer assembles the changelog.
8. **Device evidence.** Where a task says "device check", attach before and after screenshots (and a 4x slow-motion frame sheet for motion work) to the pull request. The emulator helper and recording method are described in the report's "Reproduction prerequisites".
9. **Definition of done (per task).** Named tests green; whole suite green; lint clean; acceptance criteria met on the device or emulator; no new `tween(`, `spring(`, raw radius, raw `sp` or hard-coded colour outside the allowed files; the pull request description lists findings closed and rulings.

## Ownership (filled in at kickoff)

| Task | Owner | Status |
|---|---|---|
| T1 Contrast gate and roles | | |
| T2 Screen scaffold and insets | | |
| T3 Width-aware ConnectedButtonGroup | | |
| T4 Loading states | | |
| T5 Data correctness and History | | |
| T6 New-plan flow | | |
| T7 Plan editor, Plans, what-if | | |
| T8 Settings, dialogs, AMOLED | | |
| T9 Motion foundations | | |
| T10 Numbers, rings, charts | | |
| T11 Press, Plus exit, back cancel | | |
| T12 Typography and shape scale | | |
| T13 Navbar and adaptive | | |
| T14 Localisation groundwork and cleanup | | |
| T15 Release 1.0.3 | | |

---

## Global constraints

- Kotlin, Jetpack Compose, Material 3 Expressive `1.5.0-alpha29`, Navigation Compose 2.10.2, minSdk 29, compileSdk 37, JDK 21 toolchain, Java 17 bytecode. No new dependency unless a task names it.
- **No `INTERNET` permission.** `ReleaseApkVerificationTest` must still pass.
- All new animation goes through `rememberEmberbyteMotion()` (`spatial`, `effects`, `fastSpatial`, `fastEffects`): `snap()`, or `tween(90)` for effects, when `LocalReduceMotion` is true. No new `tween(`/`spring(` outside `BackTransitions.kt`, `PillSpring.kt`, `CompactNavBar.kt`.
- All text colours come from the palette and must meet WCAG AA (4.5:1 normal, 3:1 large) on the surface they sit on, in all 12 palettes x light/dark.
- Touch targets stay at least 48 dp. Layouts must hold at font scale 2.0, display density 320 to 560, RTL, and reduced motion.
- No hard-coded colours outside `GeneratedPalettes.kt` (`NoHardcodedColorsDesignTest`). No new raw `RoundedCornerShape(` or `NN.sp` literal outside the token files once T12 lands.
- Tests first (RED, watched failing), then code (GREEN). One commit per work item. Every task ends with the whole suite green.
- CI-equivalent command (low-memory PC: add `--max-workers=2 -Dorg.gradle.jvmargs="-Xmx1400m -XX:MaxMetaspaceSize=512m"` and run `verifyReleaseApk` alone):
  `./gradlew testDebugUnitTest :core:engine:test lintDebug assembleDebug assembleRelease lintRelease verifyReleaseApk`
- Palette source of truth is `tools/palettes/palettes.json`; regenerate with `python tools/palettes/generate_palettes.py` (CI runs it with `--check`). Never hand-edit `GeneratedPalettes.kt` alone.
- Commit conventions per `CLAUDE.md`: personal identity check before every commit, small focused commits that explain why, never `--no-verify`, never force-push, confirm before pushing.

## Review Focus (input classes the tests below must pin)

1. A palette the user picks later (all 12, light and dark, AMOLED) never produces unreadable text roles.
2. Font scale 2.0 and density 560: no label breaks mid-word, no clipped selected pill, no truncated primary content.
3. First frame after launch is never a wrong empty state ("Set up a plan" shown while a plan exists).
4. Right-to-left and reduced motion: every new animation mirrors correctly and snaps when reduced.
5. Plan-flow input: empty, "0", "-", "1.5.2", "401" days, huge values, paste: never silent, never a crash.

## File structure

| Area | Files (existing unless marked new) | Tasks |
|---|---|---|
| Palette and contrast | `tools/palettes/palettes.json`, `generate_palettes.py`, `theme/palette/{ContrastPairs,PaletteMapping,GeneratedPalettes}.kt`, `PaletteContrastTest.kt` | T1 |
| Screen scaffold and insets | new `common/ScreenScaffold.kt`, `common/ScreenHeader.kt`, Settings, Appearance, History, Plan editor, Home, Apps, Licences | T2 |
| Connected button group | `ui/design/.../controls/ConnectedButtonGroup.kt` and its test | T3 |
| Loading and first paint | `HomeViewModel/Screen`, `AppsViewModel/Screen`, `AppDetail.kt` | T4 |
| Data correctness | `AppsViewModel.kt`, `AppDetail.kt`, `AppDetailBudget.kt`, `History.kt`, `HistoryHeader.kt` | T5 |
| New-plan flow | `plans/flow/PlanFlowSteps.kt`, `PlanFlowScreen.kt`, `PlanFlowViewModel` | T6 |
| Plan editor and Plans | `PlanEditorScreen.kt`, `EditorFields.kt`, `PlansScreen.kt`, `PlansBento.kt`, `WhatIf.kt` | T7 |
| Settings and dialogs | `SettingsScreen.kt`, `SettingsGroups.kt`, `DataSection.kt`, `AppearanceScreen.kt`, `BudgetEditorDialog.kt`, `PassphraseDialogs.kt`, `UpdatesRow.kt` | T8 |
| Motion foundations | new `ui/design/.../motion/EmberbyteVisibility.kt`, `MotionSpecs.kt` | T9 |
| Numbers, rings, charts | `MorphingNumber.kt`, `WavyBar.kt`, `WavyRing.kt`, `UsageBarRow.kt`, `HomeSections.kt` | T10 |
| Press, Plus, back, theme | `BentoTile.kt`, `AppRow.kt`, `PlusButton.kt`, `GuardedBack.kt`, `EmberbyteTheme.kt` | T11 |
| Theme foundation | `EmberbyteTheme.kt`, new `theme/EmberbyteTypography.kt`, new `theme/EmberbyteShapeScale.kt` (the `AppShape` tokens already live in `shapes/EmberbyteShapes.kt`) | T12 |
| Navbar and adaptive | `CompactNavBar.kt`, `PillSpring.kt`, adaptive navigation | T13 |
| Localisation and cleanup | `strings.xml`, `LicensesScreen.kt` (delete) | T14 |
| Release | `app/build.gradle.kts`, `CHANGELOG.md`, `fastlane/.../changelogs/4.txt`, `RELEASE-NOTES-1.0.3.md` | T15 |

## Order and branches

T1 first (it makes the contrast gate honest; every other task relies on it). T2 and T3 are independent and unblock most visual work. T9 (motion foundations) before T4, T7, T10, T11. T12 before the radius and size migration inside T7, T8. T6 and T7 share vocabulary strings: T6 first. T15 last, then the cleanup commit that removes the spec documents (same procedure as 1.0.2). One branch per task off `release/1.0.3`; each branch merged only with green CI and the audit done.
Suggested split (decide at kickoff): visual and data (T1, T2, T4, T5, T6, T7, T8, T13, T14, T15) and design system and motion (T3, T9, T10, T11, T12). Files named in both lists need an owner agreed first.

---

## T1. Contrast: make the gate honest, then fix the roles

**Closes:** A1, A2, A3, A4. **Files:** `tools/palettes/palettes.json`, `generate_palettes.py`, `theme/palette/ContrastPairs.kt`, `theme/palette/PaletteMapping.kt`, `plan/PlanCard.kt`, `plans/PlansBento.kt`, `apps/AppDetail.kt`, `settings/SettingsScreen.kt`. **Tests:** `PaletteContrastTest.kt` (existing), new `TextRoleContrastTest.kt`.

**Interfaces.**
- Produces `internal fun ensureContrast(foreground: Color, background: Color, minRatio: Double = 4.5): Color` in `ContrastPairs.kt`. Contract: returns `foreground` unchanged when `contrastRatio(foreground, background) >= minRatio`; otherwise returns the same hue and saturation with lightness moved toward black (if `background` is light) or white (if dark) in 1% steps until the ratio is met; never returns a colour with alpha below 1 for opaque input.
- Produces on `EmberbytePalette` three derived text roles (computed, not stored, so `fromDynamic` keeps working): `val accentText: Color`, `val dangerText: Color`, `val linkText: Color`, each `ensureContrast(role, surface2, 4.5)`. A second overload `dangerTextOnBackground` uses `background`.
- `PaletteMapping.toColorScheme` maps `error` to `dangerText` on the surface it appears (use `ensureContrast(danger, surface2)` for `error`, keep `errorContainer` as a tonal container with `onErrorContainer` at 4.5:1).

**Steps (work items, each its own commit).**
1. **RED: widen the gate.** Register in `ContrastPairs`: `"accent as text on surface2"` (`accent`, `surface2`, 4.5), `"danger as text on surface2"` (`danger`, `surface2`, 4.5), `"danger as text on background"` (`danger`, `background`, 4.5), `"primary as link on surface2"` (`primary`, `surface2`, 4.5), `"muted on surface2"` (`muted`, `surface2`, 4.5). Run `PaletteContrastTest`: expect FAIL listing the palettes in findings A3 (Dusk light 1.02 and 1.25, Ember light, Gruvbox dark, Nord, Solarized, Everforest, Tokyo Night light, and others). Record the failing list in the ledger as the baseline.
2. **GREEN, palette data.** In `palettes.json` give Dusk light and Ember light `danger #BA1A1A`, `onDanger #FFFFFF` (M3 baseline; keep `#FFB4AB` only in dark entries). Give Monochrome light a real red (`#B3261E`) for danger. Regenerate. Re-run: remaining failures are the other palettes.
3. **GREEN, derived roles.** Implement `ensureContrast`; add `accentText`, `dangerText`, `linkText`. Unit tests `ensure_contrast_keeps_a_passing_colour_unchanged`, `ensure_contrast_keeps_hue_and_meets_the_ratio`, `ensure_contrast_is_monotonic_toward_black_on_light`. Change registered pairs to use the derived roles where the UI uses roles as text. The gate must be green for all 24 combinations.
4. **Call sites.** `PlanCard.kt:98`: `val textColor = if (isExceeded) palette.dangerText else palette.accentText`; same in `PlansBento.kt`. Dialog errors, "Remove", "Discard changes?" and "Delete plan?" titles, text-button "See all" and "Set a budget" use `dangerText`/`linkText`. App detail legend swatches keep the pale colour as the swatch only; the legend *text* uses `text`. `grep -rn "palette.accent," app/src/main ui/design/src/main` must show no remaining use as `Text(color = ...)`.
5. **Monochrome (A4).** State glyph next to the ring percentage (check, warning, error icon, 16 dp) so state is never colour-only; test `monochrome_exceeded_shows_error_glyph`.

**Tests (names).** `every_pair_passes_in_every_theme_and_mode` (extended), `text_roles_meet_aa_for_all_palettes`, `plan_card_status_text_is_at_least_4_5_to_1_in_light_themes`, `dialog_error_text_is_at_least_4_5_to_1`, `monochrome_exceeded_shows_error_glyph`.
**Acceptance.** Zero failing pairs in all 24 combinations; Dusk light plan status and error text at least 4.5:1 on a device screenshot.

---

## T2. One scroll-screen pattern: top app bar and status-bar scrim

**Closes:** B1, D6 (pinned header part), B6 bottom padding. **Files:** new `common/ScreenScaffold.kt`; `common/ScreenHeader.kt`; `settings/SettingsScreen.kt`, `AppearanceScreen.kt`, `LicencesScreen.kt`; `history/History.kt`; `plans/PlanEditorScreen.kt`; `home/HomeScreen.kt`; `apps/AppsScreen.kt`. **Tests:** new `ScreenScaffoldTest.kt`.

**Interfaces.**
- Produces `@Composable fun EmberbyteScreenScaffold(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}, bottomBar: @Composable () -> Unit = {}, floatingToolbarReserve: Dp = 120.dp, content: @Composable (PaddingValues) -> Unit)`. Contract: a flexible top app bar (collapses on scroll, title always visible when collapsed), `exitUntilCollapsed` scroll behaviour wired through `Modifier.nestedScroll`; a status-bar scrim (24 dp vertical gradient, `palette.background` to transparent) drawn above content; `content` receives padding that includes the app bar height and `floatingToolbarReserve` at the bottom.
- Test tag constants `SCREEN_SCAFFOLD_SCRIM`, `SCREEN_SCAFFOLD_TITLE`.

**Steps.**
1. RED: `scrim_is_drawn_over_scrolling_content`, `title_stays_visible_when_collapsed`, `content_never_starts_under_the_status_bar`, `bottom_reserve_is_120dp_by_default` (and the editor passes 120 dp, not 112).
2. GREEN: implement the scaffold; migrate in this order: Settings, Appearance, History, Plan editor (these overlap the clock today), then Home, Apps, Licences (these hard-clip today). Delete the per-screen `statusBarsPadding()` workarounds (`ScreenHeader.kt:32`, `HomeScreen.kt:91`, `AppsScreen.kt:137`, `LicencesScreen.kt:112`) and the three `contentPadding` variants.
3. Home and Apps keep their large "Today" and "Apps" display titles as the expanded state of the same bar.
4. Device check: top 300 px of every screen after scrolling, light and dark, font 2.0.

**Acceptance.** On all seven scrolling screens no text appears under the clock or battery, and every sub-screen keeps its title and back arrow pinned.

---

## T3. Width-aware `ConnectedButtonGroup`

**Closes:** B2, E2 (sheet height jump). **Files:** `ui/design/.../controls/ConnectedButtonGroup.kt`; call sites `FiltersSheet.kt:95`, `AppearanceScreen.kt:119`, `SettingsScreen.kt:347`, history header, plan editor. **Tests:** extend `ConnectedButtonGroupTest.kt`.

**Interfaces.** Public signature unchanged: `ConnectedButtonGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)`.
**Layout contract.** Use `SubcomposeLayout` (or `Layout` with intrinsics): (1) measure each label with `maxLines = 1`, `softWrap = false`; (2) if the sum of intrinsic widths plus `ConnectedSpaceBetween` gaps fits the available width, give each segment `weight = intrinsicWidth` (not equal); (3) else drop the leading check icon when `LocalDensity.current.fontScale > 1.3f` and re-measure; (4) if it still does not fit, lay the segments in two rows, connected shapes recomputed per row (leading/middle/trailing within the row), all segments in a row the same height (`IntrinsicSize.Min`); (5) never `maxLines = 2` and never break a word.

**Steps.** RED tests first, at `Density(fontScale = 1.0/1.3/2.0)` and widths 280, 360, 411 dp:
`labels_are_never_broken_mid_word_at_font_scale_2`, `selected_pill_is_never_narrower_than_its_label`, `segments_in_a_row_have_equal_height`, `four_segments_wrap_to_two_rows_at_font_scale_2`, `segments_are_weighted_by_label_width`, `rtl_order_and_shapes_mirror`, plus the existing 249 `ui:design` tests unchanged. Then GREEN. The morph tests added in 1.0.2 (`selecting_another_segment_animates_the_shape`, `reduced_motion_changes_shape_instantly`) must still pass; replace their reflection helper with a test-only `Modifier.testShape` tag if the reflection breaks on the Compose update.
**Acceptance.** Device: Filters Period group at font 1.3, 2.0 and density 560 shows complete words, equal heights, a pill selected shape.

---

## T4. Loading states: no false empty state on first paint

**Closes:** G1, G10, G11. **Files:** `home/HomeViewModel.kt`, `HomeUiState.kt`, `HomeScreen.kt`; `apps/AppsViewModel.kt`, `AppsUiState`, `AppsScreen.kt`; `apps/AppDetail.kt`; `appicons/AppIconProvider.kt`; `placeholder/StartupPlaceholder.kt`, `EmberbyteApp.kt:98`. **Tests:** `HomeLoadingTest`, `AppsLoadingTest`, `AppDetailLoadingTest`.

**Interfaces.** Each UI state gains `val isLoading: Boolean` (true until the first repository emission), or becomes a sealed type `Loading | Ready(...)`. Home, Apps and App detail render `LoadingState` (existing `ContainedLoadingIndicator`) while loading and fade to content with `EmberbyteCrossfade` (T9). App icons use a crossfade from the letter placeholder when the bitmap arrives (`AppIcon` with `Crossfade(motion.effects())`). The startup placeholder fades out over `effects()`.

**Steps.** RED: `home_shows_loading_not_the_set_up_a_plan_card_before_the_first_emission`, `apps_shows_loading_not_zero_bytes_before_the_first_emission`, `app_detail_shows_loading_not_the_package_name_before_the_first_emission`, `icon_crossfades_from_placeholder`, `reduced_motion_loading_swaps_without_fade`. Then GREEN. Never emit the empty state until the first value has been collected.
**Device check.** Force-stop, launch, record 6 s at animation scale 1: no frame shows "Set up a plan" when a plan exists.

---

## T5. Data correctness

**Closes:** C1, C2, C3, C4, C5, C6, C7, C8, C9, C10. **Files:** `apps/AppsViewModel.kt:105`, `apps/AppDetail.kt:466-500`, `AppDetailBudget.kt`, `history/History.kt`, `HistoryHeader.kt`, `home/HomeMapping.kt`. **Tests:** `AppsViewModelTest`, `AppDetailChartTest`, `HistoryRangeTest`.

**Work items.**
1. **C1.** `featuredRow = shown.maxByOrNull { it.mobileBytes + it.wifiBytes }` when `text.isEmpty()`, regardless of sort; remove that app from the list below only when the sort is by usage (avoids a duplicate row). Tests: `featured_app_is_the_largest_even_when_sorted_by_name`, `featured_app_is_not_repeated_when_sorted_by_usage`.
2. **C2.** Day row uses `Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)` with `Modifier.weight(1f)` per slot, every slot labelled; today's bar uses `primary`, others `surface2`/`primaryContainer`. Test: `seven_bars_fit_inside_the_card_and_each_has_a_label` (assert the left edge of the first bar is at least the card's inner left).
3. **C3.** Decide with the owner: count the whole period (recommended) or label it "Since you set this budget". Test pins the chosen text and a case where an app used 600 MB before the budget existed.
4. **C4.** Hero label "Today" and a footnote or tooltip when totals include system traffic; the Apps total says "apps only".
5. **C5/C6.** History Day view: range label ("4 to 17 Nov"), peek the next bar with a trailing fade, hero label "Last 14 days"; remove the duplicate Total tile number; selected bar tinted `primary`, others `onSurfaceVariant` at 38%; Mobile/Wi-Fi legend dots under the split bar; row height reserved so nothing jumps; Home weekly bars either route to History for that day or lose their click. Tests: `history_labels_the_visible_range`, `selected_bar_is_tinted_and_others_dim`, `split_bar_has_a_legend`, `selecting_a_bar_does_not_move_the_chart`.
6. **C7.** Reserve the trend row ("No previous month"), `IntrinsicSize.Max` for the two stat tiles, label "Avg / active month". Test: `month_switch_keeps_chart_top_stable`.

7. **C8 value labels.** `UsageBarRow` gains `showValues: Boolean` (default true) and each `BarUi` gains `valueText: String` (formatted with `ByteUnits`; one decimal below 10 units, none otherwise). Layout: value above each bar; when the measured widths overlap (Week, Month, or fontScale above 1.3) show values only for the selected bar, the highest bar and today, plus a "Show values" `FilterChip` that reveals all in a scrolling row. The value text uses `clearAndSetSemantics {}` (the row description already speaks it). Tests: `every_bar_shows_its_value_at_default_font`, `values_reduce_to_selected_highest_and_today_when_crowded`, `values_respect_decimal_and_binary_units`, `value_text_is_not_announced_twice`, `values_do_not_overlap_at_font_scale_2`.
8. **C9 selected state.** Selected bar `primary` capsule, others `primaryContainer` at 38%; selected label bold with a tonal pill; a marker dot under the selected label; today's bar gets a ring so today and selected differ; colour and height animate with `spatial()`/`effects()` (T10). Tests: `selected_bar_has_a_distinct_colour_in_every_palette` (contrast at least 3:1 against unselected bars, all 12 palettes, light, dark and AMOLED), `selected_label_is_bold`, `today_and_selected_are_distinguishable`, `selecting_a_bar_does_not_shift_the_layout`.
9. **C10 labels.** Replace the label construction in `historyBars` (`History.kt:103-112`) with one `HistoryLabels` formatter: Day = weekday initial over day number, with the month on the first bar of a new month ("1 Dec"); Week = Monday-to-Sunday range "3 to 9 Nov", collapsing to "3 Nov" when narrow; Month = short month, year only at a year boundary. Locale from the user's locale; words in `strings.xml`. Confirm the tester's intent first (C10 in the findings). Tests with fixed dates: `day_labels_show_the_month_when_it_changes` (28 Nov to 3 Dec), `day_labels_show_weekday_initial_and_number`, `week_labels_are_monday_to_sunday_ranges`, `month_labels_show_the_year_only_at_a_year_boundary`, `labels_use_the_locale` (ar and en), `labels_never_truncate_at_font_scale_2`.

**Acceptance.** Each test above green; device screenshots of Apps (name sort), App detail, and History (Day across a month boundary, Week, Month) showing values, a clear selected bar and consistent labels.

---

## T6. New-plan flow

**Closes:** D1, D2, D3, D4 and the vocabulary part of D5. **Files:** `plans/flow/PlanFlowSteps.kt`, `PlanFlowScreen.kt`, `PlanFlowViewModel`, `strings.xml`. **Tests:** `PlanFlowInputTest`, `PlanFlowScreenTest` (existing).

**Interfaces.**
- Numeric fields: `KeyboardOptions(keyboardType = KeyboardType.Decimal /* cap */ or Number /* day, days */, imeAction = ImeAction.Next)`, an input filter that rejects characters other than digits and one decimal separator (locale aware), `supportingText` with `isError`.
- Validation messages (strings, shared with the editor in T7): `plan_error_cap_positive` "Enter a number greater than zero", `plan_error_day_range` "Day must be 1 to 31", `plan_error_days_range` "Enter 1 to 365 days".
- Step total: `val totalSteps: Int` derived once from the choices made on earlier steps (never from the field being edited). Continue stays enabled; tapping it with invalid input shows the error and focuses the field.
- Continue = filled `Button`; disabled state clearly greyed; Back = `TextButton`. Review step = `BentoTile` summary reusing `PlanCard` preview with rows: name, cap, cycle, SIM, next renewal ("Renews 17 Dec"), start option.

**Steps.** RED: `numeric_fields_use_numeric_keyboards`, `invalid_cap_shows_an_error_and_keeps_continue_enabled`, `one_point_five_point_two_is_rejected`, `day_401_shows_a_range_error`, `step_total_does_not_change_while_typing`, `review_shows_sim_and_next_renewal`, `continue_is_a_filled_button_when_valid`. Then GREEN.
**Acceptance.** Fuzz table of inputs (empty, "0", "-", "1.5.2", "401", "99999999999", pasted text with spaces) never crashes and always shows a message; the step counter is constant.

---

## T7. Plan editor, Plans screen and what-if

**Closes:** D5, D6, D7, D8, D9, D10, D11, B4, B5, G14. **Files:** `plans/PlanEditorScreen.kt`, `EditorFields.kt`, `PlansScreen.kt`, `PlansBento.kt`, `WhatIf.kt`. **Depends on:** T2, T3, T9, T12.

**Work items.**
1. **Structure.** Group into `BentoTile` sections (Basics, Cap and cycle, Rollover, Add-ons, Alerts, What-if, Danger zone); every single choice a `ConnectedButtonGroup` (units, cycle kind, rollover; alerts as multi-select group); `OutlinedTextField` with a trailing unit for Cap; leading calendar and clock icons on the date and time rows; shared strings with T6. Test: `editor_has_no_radio_buttons_or_loose_chips` (semantic scan for `Role.RadioButton` outside the SIM list) and `labels_match_the_new_plan_flow`.
2. **Sticky Save.** Use `EmberbyteScreenScaffold(bottomBar = ...)` with Save and Discard; Save never scrolls away. Test: `save_is_visible_after_scrolling_to_the_bottom`.
3. **Alerts.** Helper text "Notify me when the plan reaches…"; default thresholds 80% and 100% checked for new plans; "none" means no alerts (stated). Test: `new_plan_defaults_to_80_and_100_alerts`.
4. **Free window.** App picker reusing the Apps list; each add-on and window in a card with a trailing delete; weekday chips in a `FlowRow` with an "All days" toggle. Test: `free_window_picks_an_app_without_typing_a_package`.
5. **Archive.** Kebab on archived cards with Unarchive and Delete; archived card at 60% alpha with an "Archived" chip and no forecast; editor's Danger zone shows "Unarchive plan" when archived; Delete as a tonal error button with an icon; Snackbar "Plan archived · Undo" (Undo restores within 5 s). Tests: `archived_plan_can_be_unarchived`, `archive_shows_a_snackbar_with_undo`, `undo_restores_the_plan`.
6. **Menu and tile.** Options menu offset below the kebab, with icons and a divider before Archive, error tint on destructive; kebab moved outside the ring on the small tile. Test: `menu_does_not_overlap_the_card_text`.
7. **Titles.** One page-title style (bold, per Apps/Today) for Plans, History, Settings, Edit plan; one "New plan" control (keep the navbar "+").
8. **What-if.** Own bento card with an inline result chip and a full date ("runs out Sat 22 Nov"); excluded from the dirty state. Tests: `what_if_does_not_mark_the_plan_dirty`, `what_if_result_shows_a_full_date`.
9. **Conditional fields (G14).** Cycle kind, rollover cap, add-on lists animate with `EmberbyteAnimatedVisibility` and the column uses `animateContentSize`. Test: `switching_cycle_kind_animates_height` (in-between frame count above 1 with the test clock) and `reduced_motion_snaps`.

**Acceptance.** Device walkthrough of create, edit, archive, unarchive, delete, what-if at font 2.0 and in RTL.

---

## T8. Settings, dialogs and AMOLED

**Closes:** F1 to F8, D12, B8 (onboarding buttons). **Files:** `settings/SettingsScreen.kt`, `SettingsGroups.kt`, `DataSection.kt`, `AppearanceScreen.kt`, `AppearanceViewModel.kt`, `PaletteMapping.kt:124`, `apps/BudgetEditorDialog.kt`, `settings/PassphraseDialogs.kt`, `settings/UpdatesRow.kt`, `onboarding/OnboardingPages.kt`. **Tests:** `SettingsRowsTest`, `AmoledTest`, `DialogFamilyTest`, `PassphraseDialogTest`.

**Work items.**
1. **Icons and tiles (F1).** Remove the `iconText = title.firstOrNull()` default; require an `ImageVector`. Icons: Appearance `Palette`, AMOLED `DarkMode`, Export `FileDownload`, JSON `DataObject`, Backup `Backup`, Restore `Restore`. One shape for leading tiles (Cookie); Burst and Sunny only for group headers; one tint rule (container primary at 12%, glyph primary). Test: `no_settings_row_uses_a_letter_placeholder`.
2. **AMOLED row (F2).** Icon, switch with visible off track (`uncheckedTrackColor = surfaceVariant`, `uncheckedBorderColor = outline`), identical title and subtitle on Settings and Appearance ("Pure black (AMOLED)", "Dark mode only"), card swatches that include the currently selected theme and follow the current mode.
3. **AMOLED gating (F3).** `AppearanceViewModel.toUiState` takes `isSystemDark` as an input flow (from `isSystemInDarkTheme()` collected via `snapshotFlow` or a `ConfigurationChanged` flow) so it recomputes when the system flips. Test: `amoled_switch_enables_when_system_turns_dark`.
4. **AMOLED structure (F4).** In AMOLED set `surface` to `#0B0B0B` (or add a 1 dp `outlineVariant` border on cards); keep `background #000000`. Test: `amoled_cards_are_distinguishable_from_the_background` (ratio at least 1.05:1 or a border).
5. **Row columns (F5).** One `SettingsRow` leading slot of 48 dp used everywhere.
6. **Dialogs (F6, F8, D12).** One `EmberbyteDialog` icon policy (every dialog has an icon), button order dismiss then confirm, one destructive style. Export: three `ConnectedButtonGroup` or radio rows plus an explicit "Export" button and a text "Cancel". Passphrase: `isError` and `supportingText` on both fields, show/hide icon, one-line warning "If you lose this passphrase the backup cannot be recovered", and the reason Create is disabled. Budget: labelled unit groups, numeric input filter (reject "-" and spaces), reserved error height, confirm or Snackbar Undo on Remove. Tests: `every_dialog_has_an_icon`, `passphrase_mismatch_outlines_both_fields`, `budget_limit_rejects_minus_and_spaces`, `remove_budget_can_be_undone`.
7. **Updates row (F7).** One line plus an external-link glyph; subtitle "Opens your browser".
8. **Onboarding (B8).** Rename page 1-3 secondary to "Skip intro", add a Back control, `Button` (not tonal) for "Set up a plan", centre the page-1 copy block. Test: `onboarding_page_three_cta_is_a_filled_button`.

**Acceptance.** Device screenshots of Settings, Appearance, every dialog, in light, dark and AMOLED.

---

## T9. Motion foundations

**Closes:** G4 (foundation), G6, G9, G17, G18, G19 (stretch). **Files:** new `ui/design/.../motion/EmberbyteVisibility.kt`, `MotionSpecs.kt`; `EmberbyteTheme.kt`; `LicencesScreen.kt:288,336`; `HomeScreen.kt:131-193`, `AppsScreen.kt:141-309`. **Tests:** `EmberbyteVisibilityTest`, `ThemeCrossfadeTest`.

**Interfaces.**
```kotlin
@Composable fun EmberbyteAnimatedVisibility(
    visible: Boolean, modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
)   // expandVertically(spatial) + fadeIn(effects) / shrinkVertically + fadeOut; EnterTransition.None / ExitTransition.None when LocalReduceMotion
@Composable fun <T> EmberbyteCrossfade(targetState: T, modifier: Modifier = Modifier, content: @Composable (T) -> Unit)   // effects(); snap when reduced
@Composable fun animatePaletteAsState(target: EmberbytePalette): EmberbytePalette   // animateColorAsState per token, effects(); snap when reduced
```
**Steps.** RED: `visibility_has_in_between_frames`, `visibility_snaps_when_reduced`, `crossfade_has_in_between_frames`, `palette_change_animates_every_token`, `licences_uses_reduced_motion`. GREEN: implement; `EmberbyteTheme` wraps the palette in `animatePaletteAsState`; replace Licences default specs; add stable `key`s and `Modifier.animateItem(...)` (as in `PlansScreen`) to the un-keyed Home and Apps `item {}` blocks. Wrap each G4 cut site listed in the findings with the new wrappers (a checklist in the ledger, one tick per file).
**Acceptance.** A repo test `no_conditional_block_uses_a_bare_if_for_a_listed_site` is not practical; instead the ledger checklist plus device recordings at 4x slow show in-between frames for each listed site.

---

## T10. Expressive numbers, rings and charts

**Closes:** G2, G3, G5, B7 (hero label). **Files:** `ui/design/.../number/MorphingNumber.kt`, `progress/WavyBar.kt`, `WavyRing.kt`, `UsageBarRow.kt`, `home/HomeSections.kt:149`, `history/History.kt:316+`, `OnboardingPages.kt:228`. **Depends on:** T9.

**Work items.**
1. Wire `MorphingNumber` into: Home hero, live Speed tile (value plus unit), History totals, Apps "in total". Test: `hero_number_morphs_between_values` (in-between frame shows a mixed digit state), `reduced_motion_hero_snaps`.
2. `WavyBar` and `WavyRing`: start from 0 with `Animatable(0f)` and animate to the target on first composition (spatial spring); stagger rings in the Plans bento by 40 ms per card. Tests: `wavy_bar_grows_from_zero_on_first_show`, `wavy_ring_grows_from_zero`, `rings_snap_when_reduced`.
3. `UsageBarRow`: `animateDpAsState` for height and `animateColorAsState` for selection; grow-in on first show. Test: `bars_animate_height_when_data_changes`.
**Acceptance.** Device: cold start shows the hero, bar and rings animating in; selecting a History bar animates colour and height.

---

## T11. Press feedback, Plus exit, back cancel

**Closes:** G7, G12, G13, G8, G15 (stretch), G16. **Files:** `tiles/BentoTile.kt`, `AppRow.kt`, `navbar/PlusButton.kt`, `CompactNavBar.kt` (trailing exit), `nav/GuardedBack.kt:44-47,78`, `plans/flow/PlanFlowScreen.kt:227`, `settings/AppearanceScreen.kt:219,256,314`, `SettingsGroups.kt:182-210`, `onboarding/OnboardingScreen.kt:196`.

**Interfaces.** `Modifier.pressMorph(shape: CornerBasedShape, pressedScale: Float = 0.97f, pressedCorner: Dp = 12.dp): Modifier` in `ui/design/.../motion/PressMorph.kt` using `rememberEmberbyteMotion().fastSpatial()`; snaps when reduced.
**Steps.** RED: `bento_tile_morphs_corners_when_pressed`, `plus_button_animates_out`, `guarded_back_cancel_springs_back_to_full_size` (progress passes through values between the shrink and 1 using the test clock), `plan_flow_progress_is_wavy_and_animated`, `selection_border_animates`. GREEN. Replace `LinearProgressIndicator` with `LinearWavyProgressIndicator` (value animated); keep the last trailing content so the Plus exit can play.
**Acceptance.** Recordings at 4x: pressed tile, Plus exit, cancelled back gesture show in-between frames; reduced motion snaps.

---

## T12. Theme foundation: typography and shape scale

**Closes:** H1, H2, H3, H6 (token part). **Files:** `theme/EmberbyteTheme.kt:86`, new `theme/EmberbyteTypography.kt`, new `theme/EmberbyteShapeScale.kt`, existing `shapes/EmberbyteShapes.kt` (`AppShape`); migration across 31 `RoundedCornerShape(` sites, 66 `.sp` literals, 23 `.copy(fontWeight` overrides. **Tests:** `ThemeTokensTest`, `NoRawRadiiTest`, `NoRawFontSizesTest`.

**Interfaces.**
- `val EmberbyteTypography: Typography` with the Emphasized variants (`displayLargeEmphasized`, `headlineMediumEmphasized`, `titleLargeEmphasized`, `labelLargeEmphasized` and so on) mapped to Roboto Flex expressive settings used today in `EmberbyteType`.
- `val EmberbyteShapeScale: Shapes` (in `theme/EmberbyteShapeScale.kt`, not to be confused with `AppShape` in `shapes/EmberbyteShapes.kt`) with `extraSmall = 8.dp, small = 12.dp, medium = 16.dp, large = 24.dp, extraLarge = 32.dp, largeIncreased = 28.dp, extraLargeIncreased = 36.dp` (adjust to the design agreed at kickoff, but the set is the only radii allowed).
- `MaterialExpressiveTheme(colorScheme, motionScheme, shapes = EmberbyteShapeScale, typography = EmberbyteTypography, content)`.
**Steps.** RED: `theme_passes_typography_and_shapes`, `emphasized_styles_are_defined`, `no_raw_rounded_corner_shape_outside_tokens` (source scan, allow-list starts with the 31 current sites and must shrink to the token files), `no_raw_sp_outside_tokens`. GREEN in stages: tokens, then migrate file by file with a ledger checklist (radii: 10→8/12, 14→12/16, 20→16/24, 22→24, 26→24/28, 30→28/32, 36→36).
**Acceptance.** Scan tests green with an empty allow-list; visual diff of Home, Apps, Plans, Settings shows no regression beyond the intended snap to the scale.

---

## T13. Navbar details and adaptive layout

**Closes:** B6, H7. **Files:** `navbar/CompactNavBar.kt`, `PillSpring.kt`, `NavBarLogic.kt`, `EmberbyteApp.kt`, `ContentWidth.kt`. **Tests:** `CompactNavBarReducedMotionTest`, `NavBarVisibilityTest`.

**Work items.**
1. **Reduced-motion offset (investigate first).** Reproduce by toggling animation scale to 0 *while the app is open* (adb) and in an `android.test` with `LocalReduceMotion = true` and a cold start. If confirmed, find the `snap()` placement path that is measured before layout and fix it; RED test `pill_is_centered_when_reduced_motion`. If it does not reproduce, log it as "not reproducible" in the ledger and keep the test as a guard.
2. Pill outline: 1 dp `outlineVariant` stroke or 3 dp elevation (`pill_has_a_visible_edge_on_surface2_cards`).
3. Reappear at list end: `NavBarVisibility` shows the pill when the list can no longer scroll forward (`pill_reappears_at_the_end_of_a_list`).
4. **Adaptive (stretch).** `WindowSizeClass`: at width 600 dp and up use `WideNavigationRail`; cap sheets at 560 dp. Test: `uses_a_rail_at_600dp`.

---

## T14. Localisation groundwork and cleanup

**Closes:** H4, H5, H3 (parts). **Files:** `strings.xml`, `AndroidManifest.xml` (`localeConfig`), `res/xml/locales_config.xml` (new), `settings/LicensesScreen.kt` (delete) and its import at `EmberbyteApp.kt:66`.
**Steps.** Delete the dead screen and import (`grep -rn LicensesScreen` returns nothing). Externalise the editor, new-plan flow, dialogs and Settings strings first (T6, T7 and T8 already add keys; finish the rest with a source scan test `no_new_literal_ui_strings` whose allow-list shrinks). Declare `localeConfig` with `en` only (Arabic is a future release; RTL layout already verified). Replace direct `MaterialTheme.colorScheme` reads in `PlanEditorScreen` (15), `UsageBarRow` (4), `BudgetEditorDialog` (4) with the palette.
**Acceptance.** Lint clean; the literal-string allow-list is smaller than at the start of the release.

---

## T15. Release 1.0.3

**Files:** `app/build.gradle.kts` (`versionName = "1.0.3"`, `versionCode = 4`), `CHANGELOG.md` (dated the release day; categories Fixed, Changed, Added), `fastlane/metadata/android/en-US/changelogs/4.txt` (under 500 characters), `docs/release/RELEASE-NOTES-1.0.3.md` (certificate fingerprint `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`, known limits, the "no INTERNET permission" sentence). Then the per-release checklist from `RELEASE-PLAN-2026-Q4.md`: tag on `main`, release workflow, draft, fingerprint check, publish, check website, Obtainium and Orion Store, install over 1.0.2. The last commit on the release branch removes this spec and the findings document (nothing after it), then squash-merge.
**Note for the maintainer:** `gh` on the release PC is signed in as a read-only account; use the GitHub web UI for the pull request and the release environment approval.

---

## Device acceptance checklist (run on a phone before release)

Same format as 1.0.2 (one poll per verdict, one reply per failure with the copied navigation log).

1. Light Dusk and Ember: plan status line and every dialog error clearly readable.
2. Scroll Settings, Plan editor, History, Home, Apps: nothing under the clock; titles pinned.
3. Font 2.0 and density 560: Filters Period, Appearance Mode, Units show whole words.
4. Cold start: no "Set up a plan" flash; Home hero, bar and rings animate in.
5. Apps sorted by Name: the featured card is the biggest app; App detail chart shows seven labelled bars.
6. New plan: numeric keyboards, clear messages for "0", "1.5.2", "401"; step count never changes; Review shows renewal.
7. Plan editor: sticky Save, grouped sections, archive, unarchive, undo; what-if does not mark it edited.
8. Settings: icons not letters, AMOLED row and switch consistent, dialogs share a style, Export has an Export button.
9. Press a card, row and the Plus button: shape morph; Plus exits smoothly; cancelled back gesture springs back.
10. Switch theme and dark mode: colours cross-fade; reduced motion snaps everything; RTL (Arabic) mirrors.
11. Plan states: create a 1 MB cap to see Warn and Exceeded on every palette (this was not tested in the audit).
12. Fresh install onboarding: Skip intro, Back, filled "Set up a plan".
13. History (Day, Week, Month): every bar shows its value, the selected bar is obvious, Day labels make a month change visible, Week and Month labels read as dates.

## Out of scope (record, do not build)

Container transforms between tile and detail, pull-to-refresh, an Expressive dropdown for every menu, a real tablet two-pane Plans screen (only the navigation rail is in T13), and full translations. Each is listed in the findings (G19, H7) for a later release.
