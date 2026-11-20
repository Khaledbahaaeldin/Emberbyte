# Emberbyte 1.0.3: brief for Joe (Youssef Elbanna)

> **For agentic workers (Antigravity, Claude Code and humans):** this brief contains only your tasks, the contracts you consume from other people's tasks, and the shared rules. The full plan is `docs/release/RELEASE-PLAN-1.0.3-spec.md`; the findings, with reproduction steps and root causes, are in `docs/release/RELEASE-PLAN-1.0.3-report.md`. If this brief and the full spec disagree, the full spec wins; tell Khaled.

**Your branches (in order):** t6-plan-flow, t8-settings-dialogs, then (after S1) t7-plans-editor, t16-whats-new

**How you start:** Start now with T6 and the independent parts of T8 (icons, AMOLED logic, dialog consistency, passphrase and budget dialogs). After Khaled announces S1, merge `origin/release/1.0.3` into your branch, adopt the scaffold (T2) in Settings, Appearance and the Plan editor, then do T7 and finally T16 (its data layer and tests can start early; the UI waits for T8 to merge).

---

## Team split, sync points and re-homed items

**People.** Khaled (maintainer: foundations, navigation, release, and the only person who merges into `release/1.0.3`), Omar, Joe (Youssef Elbanna).
**Principle.** Khaled owns every task that others depend on, so nobody waits for another developer: Omar and Joe start immediately on work that has no dependency, and pick up the dependent parts after Khaled announces a sync point. Each person owns a disjoint set of files.

### Branches (one per task, created from the latest `release/1.0.3`, pull request into `release/1.0.3`)

| Person | Order of work | Branch | Starts when |
|---|---|---|---|
| Khaled | T9 wrappers | `t9-motion-foundations` | now |
| Khaled | T3 group | `t3-connected-group` | now |
| Khaled | T1 contrast (design module) | `t1-contrast` | now |
| Khaled | T2 scaffold (new files and Licences) | `t2-screen-scaffold` | now |
| Khaled | T12a tokens (typography, shapes, theme wiring) | `t12a-theme-tokens` | now |
| Khaled | T13 navbar (incl. Plus exit) | `t13-navbar` | after S1 |
| Khaled | T12b migration sweep | `t12b-token-migration` | after S2 (everyone merged) |
| Khaled | T14 localisation and cleanup | `t14-localisation` | after T12b |
| Khaled | T15 release | `t15-release` | last |
| Omar | T5 data and History | `t5-data-history` | now |
| Omar | T11 press morph and back cancel | `t11-press-back` | now |
| Omar | T4 loading states | `t4-loading` | after S1 |
| Omar | T10 numbers, rings, charts | `t10-numbers-charts` | after S1 |
| Khaled | T17 Home hero (44 dp mark, weight bloom, live GB badge) | `t17-home-hero` | after Omar's T4, T10, T11 merge |
| Khaled | T18 Home bento tiles (Live and Network, upload switch) | `t18-home-bento` | after T17 merges |
| Khaled | T19 Home plan setup card (merged call to action) | `t19-plan-setup-card` | after T18 merges |
| Khaled | T20 Home usage card (Top apps with History inside) | `t20-usage-card` | after T19, and Omar's T5 and T11 merge |
| Joe | T6 new-plan flow (incl. wavy progress) | `t6-plan-flow` | now |
| Joe | T8 Settings, dialogs, AMOLED (incl. onboarding and Appearance motion) | `t8-settings-dialogs` | now (T1, T2 adoption after S1) |
| Joe | T7 plan editor, Plans, what-if | `t7-plans-editor` | after S1 (non-dependent items can start now) |
| Joe | T16 What's new sheet | `t16-whats-new` | data layer now, UI after T8 merges and S1 |

### Sync points (Khaled announces each one in Discord when merged)

- **S1 (first, small pull requests in this order):** `T9` (new `EmberbyteVisibility.kt` file, `animatePaletteAsState`, no screen wiring), `T3` (`ConnectedButtonGroup`, public signature unchanged), `T1` (design-module palette, `ContrastPairs`, derived roles, `PlanCard`), `T2` (new `ScreenScaffold.kt` and its test, plus Licences only), `T12a` (tokens and theme wiring, no migration). After S1, Omar and Joe run `git merge origin/release/1.0.3` into their branches and use the new building blocks.
- **S2:** Omar's and Joe's pull requests are audited and merged by Khaled (merge order: Omar `t5`, `t11`, `t4`, `t10`; Joe `t6`, `t8`, `t7`, `t16`; they touch disjoint files, so any order works, but `t4` before `t16` because both touch `EmberbyteApp.kt`).
- **S3:** after S2, Khaled does T12b (token migration sweep across all files), T14, T13 leftovers and T15, then the cleanup commit.

### Re-homed items (so each person's files never overlap with another's)

- **T2 adoption.** Khaled builds `EmberbyteScreenScaffold` and migrates Licences. Each owner migrates their own screens after S1: Omar (Home, Apps, History), Joe (Settings, Appearance, Plan editor). Do not migrate a screen you do not own.
- **T9 wiring.** Khaled delivers the wrappers. Each owner applies them at the G4 cut sites in their own files (Omar: Home, Apps, App detail; Joe: Plan editor, Plans, plan flow, Data section, dialogs). Khaled fixes Licences reduced motion.
- **T1 call sites.** Khaled changes the design module and `PlanCard.kt`. Call sites in app-module files are adopted by their owner: Joe (`PlansBento.kt`, `BudgetEditorDialog.kt`, `PassphraseDialogs.kt`, Settings), Omar (`AppDetail.kt` legend colours).
- **Plus button exit (G13)** moves from T11 to T13 (Khaled, because `CompactNavBar.kt` is his file). Omar's T11 keeps `pressMorph` (including `PlusButton.kt` press) and `GuardedBack.kt`.
- **Plan-flow progress (G8)** moves from T11 to T6 (Joe, because `PlanFlowScreen.kt` is his).
- **Appearance selection and onboarding motion (G16, G15)** move from T11 to T8 (Joe).
- **Home redesign (T17 to T20) is Khaled's.** The hero, bento tiles, plan setup card and usage card (Top apps with History inside) are the maintainer's own tasks. They edit `HomeSections.kt`, `HomeScreen.kt`, `HomeViewModel.kt` and the History screen, which Omar's T4, T5, T10 and T11 also edit, so Khaled starts them **only after those four Omar pull requests have merged**. Omar does not touch T17 to T20.
- **T12 split.** T12a (tokens, merged first) and T12b (migration sweep, last, by Khaled) so the 31 radius sites and 66 size literals are migrated once, after everyone else has merged.

### Conflict rules

- Edit only the files your tasks list. A needed change in someone else's file goes into your pull request description; Khaled applies it or asks the owner.
- `EmberbyteApp.kt` is touched by Omar (T4 startup placeholder fade) and Joe (T16 What's new wiring): Omar merges first, Joe rebases.
- `strings.xml`: add your keys under your own comment block at the end of the file, with your prefix (Omar `home_`, `apps_`, `history_`; Joe `plan_`, `settings_`, `dialog_`, `whatsnew_`; Khaled `common_`, `nav_`). Never reorder or reformat existing entries.
- `ContrastPairs.kt`, `EmberbyteTheme.kt`, `ConnectedButtonGroup.kt`, `MotionSpecs.kt`: Khaled only.
- After every sync point announcement, merge `origin/release/1.0.3` into your task branch before you continue, and re-run your tests.

---

## Contracts you consume from Khaled's tasks (code against these; they land at sync point S1)

### T1. Contrast: make the gate honest, then fix the roles

**Interfaces.**
- Produces `internal fun ensureContrast(foreground: Color, background: Color, minRatio: Double = 4.5): Color` in `ContrastPairs.kt`. Contract: returns `foreground` unchanged when `contrastRatio(foreground, background) >= minRatio`; otherwise returns the same hue and saturation with lightness moved toward black (if `background` is light) or white (if dark) in 1% steps until the ratio is met; never returns a colour with alpha below 1 for opaque input.
- Produces on `EmberbytePalette` three derived text roles (computed, not stored, so `fromDynamic` keeps working): `val accentText: Color`, `val dangerText: Color`, `val linkText: Color`, each `ensureContrast(role, surface2, 4.5)`. A second overload `dangerTextOnBackground` uses `background`.
- `PaletteMapping.toColorScheme` maps `error` to `dangerText` on the surface it appears (use `ensureContrast(danger, surface2)` for `error`, keep `errorContainer` as a tonal container with `onErrorContainer` at 4.5:1).

### T2. One scroll-screen pattern: top app bar and status-bar scrim

**Interfaces.**
- Produces `@Composable fun EmberbyteScreenScaffold(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}, bottomBar: @Composable () -> Unit = {}, floatingToolbarReserve: Dp = 120.dp, content: @Composable (PaddingValues) -> Unit)`. Contract: a flexible top app bar (collapses on scroll, title always visible when collapsed), `exitUntilCollapsed` scroll behaviour wired through `Modifier.nestedScroll`; a status-bar scrim (24 dp vertical gradient, `palette.background` to transparent) drawn above content; `content` receives padding that includes the app bar height and `floatingToolbarReserve` at the bottom.
- Test tag constants `SCREEN_SCAFFOLD_SCRIM`, `SCREEN_SCAFFOLD_TITLE`.

### T3. Width-aware `ConnectedButtonGroup`

**Interfaces.** Public signature unchanged: `ConnectedButtonGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)`.
**Layout contract.** Use `SubcomposeLayout` (or `Layout` with intrinsics): (1) measure each label with `maxLines = 1`, `softWrap = false`; (2) if the sum of intrinsic widths plus `ConnectedSpaceBetween` gaps fits the available width, give each segment `weight = intrinsicWidth` (not equal); (3) else drop the leading check icon when `LocalDensity.current.fontScale > 1.3f` and re-measure; (4) if it still does not fit, lay the segments in two rows, connected shapes recomputed per row (leading/middle/trailing within the row), all segments in a row the same height (`IntrinsicSize.Min`); (5) never `maxLines = 2` and never break a word.

### T9. Motion foundations

**Interfaces.**
```kotlin
@Composable fun EmberbyteAnimatedVisibility(
    visible: Boolean, modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
)   // expandVertically(spatial) + fadeIn(effects) / shrinkVertically + fadeOut; EnterTransition.None / ExitTransition.None when LocalReduceMotion
@Composable fun <T> EmberbyteCrossfade(targetState: T, modifier: Modifier = Modifier, content: @Composable (T) -> Unit)   // effects(); snap when reduced
@Composable fun animatePaletteAsState(target: EmberbytePalette): EmberbytePalette   // animateColorAsState per token, effects(); snap when reduced
```

### T12. Theme foundation: typography and shape scale

**Interfaces.**
- `val EmberbyteTypography: Typography` with the Emphasized variants (`displayLargeEmphasized`, `headlineMediumEmphasized`, `titleLargeEmphasized`, `labelLargeEmphasized` and so on) mapped to Roboto Flex expressive settings used today in `EmberbyteType`.
- `val EmberbyteShapeScale: Shapes` (in `theme/EmberbyteShapeScale.kt`, not to be confused with `AppShape` in `shapes/EmberbyteShapes.kt`) with `extraSmall = 8.dp, small = 12.dp, medium = 16.dp, large = 24.dp, extraLarge = 32.dp, largeIncreased = 28.dp, extraLargeIncreased = 36.dp` (adjust to the design agreed at kickoff, but the set is the only radii allowed).
- `MaterialExpressiveTheme(colorScheme, motionScheme, shapes = EmberbyteShapeScale, typography = EmberbyteTypography, content)`.

---

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

---

## Review Focus (input classes the tests below must pin)

1. A palette the user picks later (all 12, light and dark, AMOLED) never produces unreadable text roles.
2. Font scale 2.0 and density 560: no label breaks mid-word, no clipped selected pill, no truncated primary content.
3. First frame after launch is never a wrong empty state ("Set up a plan" shown while a plan exists).
4. Right-to-left and reduced motion: every new animation mirrors correctly and snaps when reduced.
5. Plan-flow input: empty, "0", "-", "1.5.2", "401" days, huge values, paste: never silent, never a crash.

---

# Your tasks

## T6. New-plan flow

**Closes:** D1, D2, D3, D4 and the vocabulary part of D5. **Files:** `plans/flow/PlanFlowSteps.kt`, `PlanFlowScreen.kt`, `PlanFlowViewModel`, `strings.xml`. **Tests:** `PlanFlowInputTest`, `PlanFlowScreenTest` (existing).

**Interfaces.**
- Numeric fields: `KeyboardOptions(keyboardType = KeyboardType.Decimal /* cap */ or Number /* day, days */, imeAction = ImeAction.Next)`, an input filter that rejects characters other than digits and one decimal separator (locale aware), `supportingText` with `isError`.
- Validation messages (strings, shared with the editor in T7): `plan_error_cap_positive` "Enter a number greater than zero", `plan_error_day_range` "Day must be 1 to 31", `plan_error_days_range` "Enter 1 to 365 days".
- Step total: `val totalSteps: Int` derived once from the choices made on earlier steps (never from the field being edited). Continue stays enabled; tapping it with invalid input shows the error and focuses the field.
- Continue = filled `Button`; disabled state clearly greyed; Back = `TextButton`. Review step = `BentoTile` summary reusing `PlanCard` preview with rows: name, cap, cycle, SIM, next renewal ("Renews 17 Dec"), start option.

**Steps.** RED: `numeric_fields_use_numeric_keyboards`, `invalid_cap_shows_an_error_and_keeps_continue_enabled`, `one_point_five_point_two_is_rejected`, `day_401_shows_a_range_error`, `step_total_does_not_change_while_typing`, `review_shows_sim_and_next_renewal`, `continue_is_a_filled_button_when_valid`. Then GREEN.
**Plan-flow progress (G8, moved here from T11).** Replace the stock `LinearProgressIndicator` at `PlanFlowScreen.kt:227` with `LinearWavyProgressIndicator` whose value animates with `rememberEmberbyteMotion().spatial()`; the step label and the Continue to Create swap use `EmberbyteCrossfade` once S1 has landed. Tests: `plan_flow_progress_is_wavy_and_animated`, `step_label_crossfades`.

**Acceptance.** Fuzz table of inputs (empty, "0", "-", "1.5.2", "401", "99999999999", pasted text with spaces) never crashes and always shows a message; the step counter is constant.

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

9. **Appearance and onboarding motion (G16, G15, moved here from T11).** Selection border and check badge (`AppearanceScreen.kt:219,256`) and enabled/disabled alpha (`SettingsGroups.kt:182-210`, `AppearanceScreen.kt:314`) animate with `animateFloatAsState`/`animateColorAsState` on `rememberEmberbyteMotion()`; onboarding dot colour (`OnboardingScreen.kt:196`) animates and the bottom bar swap uses `EmberbyteCrossfade` (stretch). Tests: `selection_border_animates`, `disabled_row_alpha_animates`, `onboarding_dot_color_animates`, each with a reduced-motion twin that snaps.

**Acceptance.** Device screenshots of Settings, Appearance, every dialog, in light, dark and AMOLED.

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

## T16. What's new sheet (offline)

**Closes:** feature request from the maintainer (decision: keep the app free of the `INTERNET` permission; tell users what changed from the changelog bundled in the APK). **Files:** new `app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/whatsnew/{WhatsNewRepository,WhatsNewViewModel,WhatsNewSheet}.kt`; new asset `app/src/main/assets/whats_new.json`; `EmberbyteApp.kt`; `settings/SettingsScreen.kt` (one row, after T8 lands); `settings/UpdatesRow.kt` (one link, after T8); `core/data/.../AppPreferences.kt` and `DataStoreSettingsRepository.kt` (one key); `res/values/strings.xml`; `ReleaseApkVerificationTest.kt` (two checks). **Depends on:** T8, T9, T12 (for the finished UI); T15 writes the release entry.
**Hard rule:** this task must not add any network code, any `INTERNET` permission, or any URL fetch. `UpdatesPermissionTest` and `ReleaseApkVerificationTest` must stay green; the sheet's "See all changes" link only opens the browser through the system (`UriHandler`), exactly like the Updates row does today.

**Behaviour.**
- On the first launch after an update the app shows a "What's new in X.Y.Z" bottom sheet **once**.
- A **fresh install** never shows it: if onboarding is not completed, record the current `versionCode` silently. After onboarding completes, nothing is shown.
- **Upgrade from 1.0.2** (no stored value, onboarding already completed): show it.
- Dismissing by the button, swipe, back or scrim all count as seen: the stored `lastSeenWhatsNewVersionCode` is written on dismiss (and on a skipped fresh install), never before the sheet is visible.
- It is never shown over onboarding, a permission request, a dialog, or a back-gesture in progress; if another surface is up, wait until it closes. Rotation and process death do not show it twice.
- Entry points that always work: Settings > About > "What's new" (opens the sheet with the current version expanded and older versions below), and a "See what's new" link on the Updates row.

**Interfaces.**
- Asset `whats_new.json`: `{ "entries": [ { "versionCode": 4, "versionName": "1.0.3", "date": "2026-11-24", "highlights": [ { "kind": "added" | "changed" | "fixed", "text": "..." } ] }, ... ] }`, newest first, English, one entry per release from 1.0.2 onward (copy the 1.0.2 entry from `CHANGELOG.md` in user words, short). Each `text` is at most 120 characters and written for users, not developers.
- `internal interface WhatsNewRepository { val entries: List<WhatsNewEntry>; suspend fun lastSeenVersionCode(): Int?; suspend fun markSeen(versionCode: Int) }` with `data class WhatsNewEntry(val versionCode: Int, val versionName: String, val date: LocalDate, val highlights: List<Highlight>)` and `data class Highlight(val kind: Kind, val text: String)`, `enum class Kind { Added, Changed, Fixed }`. Parsing uses `kotlinx.serialization` if already a dependency, otherwise `org.json` from the platform (no new dependency).
- `fun shouldShowWhatsNew(currentVersionCode: Int, lastSeen: Int?, onboardingCompleted: Boolean, hasEntryForCurrent: Boolean): Boolean` (pure, unit-tested): `onboardingCompleted && hasEntryForCurrent && (lastSeen == null || lastSeen < currentVersionCode)`.
- `AppPreferences` gains `val lastSeenWhatsNewVersionCode: Int?` (a DataStore key; **not** a Room schema change, so no migration test is needed).
- `@Composable fun WhatsNewSheet(entry: WhatsNewEntry, olderEntries: List<WhatsNewEntry>, onDismiss: () -> Unit, onOpenChangelog: () -> Unit)`.

**Design (Material 3 Expressive, premium).**
- `ModalBottomSheet` with the Expressive top shape from T12, skip partial expansion, drag handle, max width 560 dp.
- Header: a Cookie-shaped tile with the app mark, title "What's new in 1.0.3", a tonal date chip ("24 Nov 2026"), one-line subtitle "No account, no internet, still just your data."
- Body: one tonal card per kind, in the order Added, Changed, Fixed. Each card has a leading icon in a shaped tile (`NewReleases`/`AutoAwesome` for Added, `Tune` for Changed, `Build` for Fixed), a heading and bullet rows. Colours come from the palette and must meet 4.5:1 (T1 roles).
- Motion: the cards enter with a staggered fade and slide (40 ms stagger, `spatial()` spring); the sheet content scrolls; everything snaps under reduced motion (`rememberEmberbyteMotion()` only).
- Footer: a filled "Got it" button and a text button "See all changes" (opens `https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md` in the browser). Below, an "Earlier versions" section with collapsible entries (expand with `EmberbyteAnimatedVisibility`).
- Works at font 2.0 (scrolls, no clipped text), density 320 to 560, RTL (leading icons mirror, bullets stay readable), dark, light and AMOLED, and landscape.
- Accessibility: the sheet title is a heading; each card is announced as "Added, 3 items"; bullet rows are single nodes; "Got it" and "See all changes" are 48 dp targets; focus moves to the title when the sheet opens.

**Steps (work items, each its own commit).**
1. **RED: logic.** Unit tests for `shouldShowWhatsNew`: `fresh_install_does_not_show`, `upgrade_from_102_shows`, `already_seen_does_not_show`, `missing_entry_for_current_version_does_not_show`, `downgrade_or_same_version_does_not_show`. GREEN: implement.
2. **RED: persistence.** `last_seen_is_written_only_on_dismiss`, `fresh_install_records_current_version_silently`, `process_death_does_not_show_twice` (repository fake plus ViewModel). GREEN: DataStore key and ViewModel.
3. **RED: asset.** Add to `ReleaseApkVerificationTest` (or a new `WhatsNewAssetTest`): `whats_new_newest_entry_matches_version_name_and_code_in_build_gradle`, `whats_new_newest_entry_matches_the_newest_changelog_version`, `whats_new_entries_are_newest_first_and_non_empty`, `every_highlight_is_at_most_120_characters`. These fail until T15 writes the 1.0.3 entry, which makes the release checklist unforgettable. GREEN: create `whats_new.json` with the 1.0.2 entry and (in T15) the 1.0.3 entry.
4. **RED: UI.** `sheet_shows_title_date_and_three_sections`, `got_it_dismisses_and_marks_seen`, `swipe_dismiss_marks_seen`, `see_all_changes_opens_the_changelog_url`, `earlier_versions_expand_and_collapse`, `sheet_scrolls_at_font_scale_2`, `reduced_motion_shows_content_without_stagger`, `rtl_renders` (Robolectric Compose tests). GREEN: implement the sheet.
5. **Wiring.** `EmberbyteApp.kt` shows the sheet after onboarding when `shouldShowWhatsNew` is true and no dialog or permission flow is active; Settings > About gets a "What's new" row (icon `NewReleases`, subtitle "Changes in 1.0.3") and the Updates row a "See what's new" link. Tests: `settings_about_row_opens_the_sheet`, `updates_row_link_opens_the_sheet`, `sheet_is_not_shown_over_onboarding`.
6. **Guards.** Run `UpdatesPermissionTest` and `ReleaseApkVerificationTest` (no `INTERNET`), and add `whats_new_adds_no_network_permission_or_client` (source scan: no `HttpURLConnection`, `OkHttp`, `URL(` fetch, `java.net.Socket` under `whatsnew/`).

**Review Focus for this task.**
1. Upgrade from 1.0.2 shows once; fresh install and onboarding never show.
2. Swipe, back and scrim dismissals persist as "seen".
3. A missing or malformed `whats_new.json` entry must not crash: the sheet is simply skipped and the Settings row shows "No changes recorded" (test `malformed_asset_does_not_crash`).
4. Font scale 2.0, RTL and landscape do not clip.
5. Airplane mode and a device with no browser: "See all changes" fails quietly (wrap `openUri` in `runCatching`).

**Acceptance.** On a device updated from the 1.0.2 build the sheet appears once with the 1.0.3 changes, never again after dismissal, reopens from Settings > About, and the APK still has no `INTERNET` permission.

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
14. Update from 1.0.2 to the 1.0.3 build: the "What's new in 1.0.3" sheet appears once on the first launch, never again after dismissing it (also not after swiping it away); a fresh install and onboarding never show it; Settings > About > What's new reopens it; Updates row has a "See what's new" link; works in airplane mode.
15. Home: a cold start blooms the big number digit by digit with an overshoot, the Emberbyte mark is 44 dp and still, the GB badge turns and speeds up while data flows, a changing value pulses only the changed digit, and with animations off all of it is static. App detail, History and Onboarding hero numbers are unchanged.
16. Home: the Live and Today by network tiles sit side by side in Space Grotesk with interlocking corners; pressing the Live tile rolls between download and upload (arrow flip, trace morph, tint change) and back; the network number counts up once and the mobile segment is visible; with animations off everything is static and the switch is instant.
17. Home with no plan: one setup card under the hero (ghost ring drawing itself, "Set up your data plan", one big "Set up plan" button) replaces the old link and card; the button opens the new-plan flow; creating a plan removes the card; with a plan nothing of it remains; with animations off everything is static.
18. Home: one Top apps card with a week strip (tap a day: the apps re-rank with a glide), a featured first app with share-fill rows, and three connected buttons; Filters (Network, System apps) narrows strip, summary and ranking together with a badge and removable chips; History grows out of its button, shows the chosen day and returns it; the old Top apps and History cards are gone; with animations off everything is static.
