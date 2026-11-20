# Emberbyte 1.0.3: brief for Omar

> **For agentic workers (Antigravity, Claude Code and humans):** this brief contains only your tasks, the contracts you consume from other people's tasks, and the shared rules. The full plan is `docs/release/RELEASE-PLAN-1.0.3-spec.md`; the findings, with reproduction steps and root causes, are in `docs/release/RELEASE-PLAN-1.0.3-report.md`. If this brief and the full spec disagree, the full spec wins; tell Khaled.

**Your branches (in order):** t5-data-history, t11-press-back, then (after S1) t4-loading, t10-numbers-charts

**How you start:** Start now with T5 and T11 (no dependency). After Khaled announces S1, merge `origin/release/1.0.3` into your branch, then do T4 and T10 and adopt the scaffold (T2) in Home, Apps and History, and the animation wrappers (T9) at the cut sites in your files.

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
| Khaled | T21 Apps page redesign (search and Filters pair, period buttons, summary card) | `t21-apps-page` | after T20, and Omar's T4, T5 and T11 merge |
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
- **Apps page (T21) is Khaled's.** It reuses T20's row and share-fill work and edits `AppsViewModel.kt` and `AppsScreen.kt`, which Omar's T4 and T5 also edit, so Khaled starts it only after T20 and those Omar pull requests have merged.
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

## T11. Press feedback and back cancel

**Closes:** G7, G12. (G13 Plus exit moved to T13; G8 moved to T6; G15 and G16 moved to T8.) **Owner:** Omar. **Files:** `tiles/BentoTile.kt`, `tiles/AppRow.kt`, `navbar/PlusButton.kt` (press only), `nav/GuardedBack.kt:44-47,78`, new `ui/design/.../motion/PressMorph.kt`. **Tests next to each.** No dependency: starts immediately (uses the existing `rememberEmberbyteMotion()`).

**Interfaces.** `Modifier.pressMorph(shape: CornerBasedShape, pressedScale: Float = 0.97f, pressedCorner: Dp = 12.dp): Modifier` in `PressMorph.kt` using `rememberEmberbyteMotion().fastSpatial()`; snaps when reduced.
**Steps.** RED: `bento_tile_morphs_corners_when_pressed`, `app_row_morphs_corners_when_pressed`, `plus_button_morphs_when_pressed`, `guarded_back_cancel_springs_back_to_full_size` (progress passes through values between the shrink and 1 using the test clock), `reduced_motion_press_snaps`. GREEN: implement; in `GuardedBack.kt` replace the snap on cancel and dialog dismiss with an `Animatable` progress using `BackTransitions.cancelSpring`.
**Acceptance.** Recordings at 4x: pressed tile and row, cancelled back gesture show in-between frames; reduced motion snaps.

---

## T4. Loading states: no false empty state on first paint

**Closes:** G1, G10, G11. **Files:** `home/HomeViewModel.kt`, `HomeUiState.kt`, `HomeScreen.kt`; `apps/AppsViewModel.kt`, `AppsUiState`, `AppsScreen.kt`; `apps/AppDetail.kt`; `appicons/AppIconProvider.kt`; `placeholder/StartupPlaceholder.kt`, `EmberbyteApp.kt:98`. **Tests:** `HomeLoadingTest`, `AppsLoadingTest`, `AppDetailLoadingTest`.

**Interfaces.** Each UI state gains `val isLoading: Boolean` (true until the first repository emission), or becomes a sealed type `Loading | Ready(...)`. Home, Apps and App detail render `LoadingState` (existing `ContainedLoadingIndicator`) while loading and fade to content with `EmberbyteCrossfade` (T9). App icons use a crossfade from the letter placeholder when the bitmap arrives (`AppIcon` with `Crossfade(motion.effects())`). The startup placeholder fades out over `effects()`.

**Steps.** RED: `home_shows_loading_not_the_set_up_a_plan_card_before_the_first_emission`, `apps_shows_loading_not_zero_bytes_before_the_first_emission`, `app_detail_shows_loading_not_the_package_name_before_the_first_emission`, `icon_crossfades_from_placeholder`, `reduced_motion_loading_swaps_without_fade`. Then GREEN. Never emit the empty state until the first value has been collected.
**Device check.** Force-stop, launch, record 6 s at animation scale 1: no frame shows "Set up a plan" when a plan exists.

---

## T10. Expressive numbers, rings and charts

**Closes:** G2, G3, G5, B7 (hero label). **Files:** `ui/design/.../number/MorphingNumber.kt`, `progress/WavyBar.kt`, `WavyRing.kt`, `UsageBarRow.kt`, `home/HomeSections.kt:149`, `history/History.kt:316+`, `OnboardingPages.kt:228`. **Depends on:** T9.

**Work items.**
1. Wire `MorphingNumber` into: Home hero, live Speed tile (value plus unit), History totals, Apps "in total". Test: `hero_number_morphs_between_values` (in-between frame shows a mixed digit state), `reduced_motion_hero_snaps`.
2. `WavyBar` and `WavyRing`: start from 0 with `Animatable(0f)` and animate to the target on first composition (spatial spring); stagger rings in the Plans bento by 40 ms per card. Tests: `wavy_bar_grows_from_zero_on_first_show`, `wavy_ring_grows_from_zero`, `rings_snap_when_reduced`.
3. `UsageBarRow`: `animateDpAsState` for height and `animateColorAsState` for selection; grow-in on first show. Test: `bars_animate_height_when_data_changes`.
**Acceptance.** Device: cold start shows the hero, bar and rings animating in; selecting a History bar animates colour and height.

---

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
19. Apps: header with the app count, search and Filters at the same height (badge when filters are active), Today / 7 days / 30 days / Cycle buttons that change the numbers at once, full-label removable chips, a summary card (total, Wi-Fi and Mobile split, "apps only"), a Most used card and share-fill rows that glide on re-rank; Filters has Network, Sort and System apps only; searching for nothing shows one message and no zero total; with animations off everything is static.
