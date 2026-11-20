# Emberbyte 1.0.3: brief for Khaled

> **For agentic workers (Antigravity, Claude Code and humans):** this brief contains only your tasks, the contracts you consume from other people's tasks, and the shared rules. The full plan is `docs/release/RELEASE-PLAN-1.0.3-spec.md`; the findings, with reproduction steps and root causes, are in `docs/release/RELEASE-PLAN-1.0.3-report.md`. If this brief and the full spec disagree, the full spec wins; tell Khaled.

**Your branches (in order):** t9-motion-foundations, t3-connected-group, t1-contrast, t2-screen-scaffold, t12a-theme-tokens, then t13-navbar, the Home redesign (t17-home-hero, t18-home-bento, t19-plan-setup-card, t20-usage-card, t21-apps-page, after Omar's Home and Apps PRs merge), t12b-token-migration, t14-localisation, t15-release

**How you start:** You are the foundation owner. Deliver S1 first, in this order: T9, T3, T1, T2, T12a. Announce each in Discord. You also audit and merge everyone's pull requests into `release/1.0.3`.

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
**Steps.** RED: `visibility_has_in_between_frames`, `visibility_snaps_when_reduced`, `crossfade_has_in_between_frames`, `palette_change_animates_every_token`, `licences_uses_reduced_motion`. GREEN: implement; `EmberbyteTheme` wraps the palette in `animatePaletteAsState`; replace Licences default specs; add stable `key`s and `Modifier.animateItem(...)` (as in `PlansScreen`) to the un-keyed Home and Apps `item {}` blocks. Khaled delivers the wrappers and the Licences fix only. Each owner wraps the G4 cut sites in their own files after S1 (Omar: Home, Apps, App detail; Joe: Plan editor, Plans, plan flow, Data section, dialogs), with a checklist in their pull request, one tick per file.
**Acceptance.** A repo test `no_conditional_block_uses_a_bare_if_for_a_listed_site` is not practical; instead the ledger checklist plus device recordings at 4x slow show in-between frames for each listed site.

---

## T3. Width-aware `ConnectedButtonGroup`

**Closes:** B2, E2 (sheet height jump). **Files:** `ui/design/.../controls/ConnectedButtonGroup.kt`; call sites `FiltersSheet.kt:95`, `AppearanceScreen.kt:119`, `SettingsScreen.kt:347`, history header, plan editor. **Tests:** extend `ConnectedButtonGroupTest.kt`.

**Interfaces.** Public signature unchanged: `ConnectedButtonGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)`.
**Layout contract.** Use `SubcomposeLayout` (or `Layout` with intrinsics): (1) measure each label with `maxLines = 1`, `softWrap = false`; (2) if the sum of intrinsic widths plus `ConnectedSpaceBetween` gaps fits the available width, give each segment `weight = intrinsicWidth` (not equal); (3) else drop the leading check icon when `LocalDensity.current.fontScale > 1.3f` and re-measure; (4) if it still does not fit, lay the segments in two rows, connected shapes recomputed per row (leading/middle/trailing within the row), all segments in a row the same height (`IntrinsicSize.Min`); (5) never `maxLines = 2` and never break a word.

**Steps.** RED tests first, at `Density(fontScale = 1.0/1.3/2.0)` and widths 280, 360, 411 dp:
`labels_are_never_broken_mid_word_at_font_scale_2`, `selected_pill_is_never_narrower_than_its_label`, `segments_in_a_row_have_equal_height`, `four_segments_wrap_to_two_rows_at_font_scale_2`, `segments_are_weighted_by_label_width`, `rtl_order_and_shapes_mirror`, plus the existing 249 `ui:design` tests unchanged. Then GREEN. The morph tests added in 1.0.2 (`selecting_another_segment_animates_the_shape`, `reduced_motion_changes_shape_instantly`) must still pass; replace their reflection helper with a test-only `Modifier.testShape` tag if the reflection breaks on the Compose update.
**Acceptance.** Device: Filters Period group at font 1.3, 2.0 and density 560 shows complete words, equal heights, a pill selected shape.

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
4. **Call sites.** Khaled changes the design module and `PlanCard.kt`; call sites in app-module files are adopted by their owner after S1 (Joe: `PlansBento.kt`, dialogs, Settings; Omar: `AppDetail.kt` legend). `PlanCard.kt:98`: `val textColor = if (isExceeded) palette.dangerText else palette.accentText`; same in `PlansBento.kt`. Dialog errors, "Remove", "Discard changes?" and "Delete plan?" titles, text-button "See all" and "Set a budget" use `dangerText`/`linkText`. App detail legend swatches keep the pale colour as the swatch only; the legend *text* uses `text`. `grep -rn "palette.accent," app/src/main ui/design/src/main` must show no remaining use as `Text(color = ...)`.
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
2. GREEN: implement the scaffold and migrate **Licences only** (Khaled). Every other screen is migrated by its owner after S1: Joe (Settings, Appearance, Plan editor), Omar (History, Home, Apps). Settings, Appearance, History and the Plan editor overlap the clock today; Home, Apps and Licences hard-clip today. Delete the per-screen `statusBarsPadding()` workarounds (`ScreenHeader.kt:32`, `HomeScreen.kt:91`, `AppsScreen.kt:137`, `LicencesScreen.kt:112`) and the three `contentPadding` variants.
3. Home and Apps keep their large "Today" and "Apps" display titles as the expanded state of the same bar.
4. Device check: top 300 px of every screen after scrolling, light and dark, font 2.0.

**Acceptance.** On all seven scrolling screens no text appears under the clock or battery, and every sub-screen keeps its title and back arrow pinned.

---

## T12. Theme foundation: typography and shape scale

**Closes:** H1, H2, H3, H6 (token part). **Files:** `theme/EmberbyteTheme.kt:86`, new `theme/EmberbyteTypography.kt`, new `theme/EmberbyteShapeScale.kt`, existing `shapes/EmberbyteShapes.kt` (`AppShape`); migration across 31 `RoundedCornerShape(` sites, 66 `.sp` literals, 23 `.copy(fontWeight` overrides. **Tests:** `ThemeTokensTest`, `NoRawRadiiTest`, `NoRawFontSizesTest`.

**Interfaces.**
- `val EmberbyteTypography: Typography` with the Emphasized variants (`displayLargeEmphasized`, `headlineMediumEmphasized`, `titleLargeEmphasized`, `labelLargeEmphasized` and so on) mapped to Roboto Flex expressive settings used today in `EmberbyteType`.
- `val EmberbyteShapeScale: Shapes` (in `theme/EmberbyteShapeScale.kt`, not to be confused with `AppShape` in `shapes/EmberbyteShapes.kt`) with `extraSmall = 8.dp, small = 12.dp, medium = 16.dp, large = 24.dp, extraLarge = 32.dp, largeIncreased = 28.dp, extraLargeIncreased = 36.dp` (adjust to the design agreed at kickoff, but the set is the only radii allowed).
- `MaterialExpressiveTheme(colorScheme, motionScheme, shapes = EmberbyteShapeScale, typography = EmberbyteTypography, content)`.
**Steps.** RED: `theme_passes_typography_and_shapes`, `emphasized_styles_are_defined`, `no_raw_rounded_corner_shape_outside_tokens` (source scan, allow-list starts with the 31 current sites and must shrink to the token files), `no_raw_sp_outside_tokens`. GREEN in stages: tokens, then migrate file by file with a ledger checklist (radii: 10→8/12, 14→12/16, 20→16/24, 22→24, 26→24/28, 30→28/32, 36→36).
**Split.** T12a (tokens, the two scan tests with the current allow-list, theme wiring) merges first at S1. T12b (the migration of the 31 radius sites, 66 size literals and 23 weight overrides) is done last by Khaled, after S2, so it never conflicts with Omar's or Joe's files.
**Acceptance.** Scan tests green with an empty allow-list; visual diff of Home, Apps, Plans, Settings shows no regression beyond the intended snap to the scale.

---

## T13. Navbar details and adaptive layout

**Closes:** B6, H7. **Files:** `navbar/CompactNavBar.kt`, `PillSpring.kt`, `NavBarLogic.kt`, `EmberbyteApp.kt`, `ContentWidth.kt`. **Tests:** `CompactNavBarReducedMotionTest`, `NavBarVisibilityTest`.

**Work items.**
1. **Reduced-motion offset (investigate first).** Reproduce by toggling animation scale to 0 *while the app is open* (adb) and in an `android.test` with `LocalReduceMotion = true` and a cold start. If confirmed, find the `snap()` placement path that is measured before layout and fix it; RED test `pill_is_centered_when_reduced_motion`. If it does not reproduce, log it as "not reproducible" in the ledger and keep the test as a guard.
2. Pill outline: 1 dp `outlineVariant` stroke or 3 dp elevation (`pill_has_a_visible_edge_on_surface2_cards`).
3. Reappear at list end: `NavBarVisibility` shows the pill when the list can no longer scroll forward (`pill_reappears_at_the_end_of_a_list`).
4. **Plus button exit (G13, moved here from T11).** Mirror the entrance on exit and keep the last trailing content while it plays (`CompactNavBar.kt` `AnimatedVisibility` exit with `scaleOut + fadeOut + shrinkHorizontally` on the motion specs). Tests: `plus_button_animates_out` (in-between frames with the test clock), `plus_exit_snaps_when_reduced`.
5. **Adaptive (stretch).** `WindowSizeClass`: at width 600 dp and up use `WideNavigationRail`; cap sheets at 560 dp. Test: `uses_a_rail_at_600dp`.

---

## T14. Localisation groundwork and cleanup

**Closes:** H4, H5, H3 (parts). **Files:** `strings.xml`, `AndroidManifest.xml` (`localeConfig`), `res/xml/locales_config.xml` (new), `settings/LicensesScreen.kt` (delete) and its import at `EmberbyteApp.kt:66`.
**Steps.** Delete the dead screen and import (`grep -rn LicensesScreen` returns nothing). Externalise the editor, new-plan flow, dialogs and Settings strings first (T6, T7 and T8 already add keys; finish the rest with a source scan test `no_new_literal_ui_strings` whose allow-list shrinks). Declare `localeConfig` with `en` only (Arabic is a future release; RTL layout already verified). Replace direct `MaterialTheme.colorScheme` reads in `PlanEditorScreen` (15), `UsageBarRow` (4), `BudgetEditorDialog` (4) with the palette.
**Acceptance.** Lint clean; the literal-string allow-list is smaller than at the start of the release.

---

## T17. Home hero: bigger mark, weight-bloom entrance, live GB badge

**Closes:** maintainer request of 2026-11-20 (not an audit finding). **Owner:** Khaled. **Branch:** `t17-home-hero`, started **after T10 merges** (Omar's T10 wires `MorphingNumber` into the Home hero and edits `HomeSections.kt`; T17 builds on that file and the number components, so it starts only after Omar's T4, T10 and T11 have merged into `release/1.0.3`). **Files:** `app/.../home/HomeSections.kt` (`BrandMark(size = 32.dp)` at the Home header, and the `HeroNumber(` call in the hero section), `ui/design/.../number/HeroNumber.kt`, `ui/design/.../tiles/UnitBadge.kt`, `ui/design/.../theme/EmberbyteType.kt` (axis factory), `app/.../home/HomeUiState.kt` / `HomeMapping.kt` (live rate to the badge). **Tests next to each.**
**Mockup (the source of the numbers below):** https://claude.ai/artifact/EWJvWQLRst5pWkNVnJFDWM . The maintainer chose these options on 2026-11-20 and nothing else from the mockup ships.

**Decisions (locked).**
1. **Mark: bigger and still.** The Emberbyte `BrandMark` beside the day (the Home header) goes from **32 dp to 44 dp**, with **no motion**. The day title keeps its style and the row stays vertically centred; the settings button does not move.
2. **Hero entrance: "weight bloom, digit by digit".**
   - Each digit of the value starts at Roboto Flex `wght` **100** and settles at today's hero weight **800** (`wdth` 36 and `opsz` 144 stay constant, exactly as `EmberbyteType.hero` has them now).
   - Digits start **70 ms apart**, left to right, the first one **150 ms** after the screen's first composition.
   - Each digit scales from **0.93** to **1.0** in step with its weight.
   - Spring: weight uses `spring(dampingRatio = 0.35f, stiffness = 750f)`. The maintainer set the mockup's bounce slider to its maximum (1.00), which is this damping ratio; the overshoot reaches the font's **1000** cap and is clamped there before settling at 800. `wght` is always clamped to 100..1000.
   - The entrance plays **once per app start** (the first time Home is shown), not on every tab switch, rotation or value update. *Open question for the maintainer: replay on every Home visit instead?* Until answered, implement once per app start behind a single constant `HERO_ENTRANCE_REPLAYS = false`.
3. **Live pulse on change.** When the displayed value changes, only the digits whose character changed get a kick of **+1500 wght/s** (a short surge to about 850 that springs back to 800); the others stay still and the entrance never re-runs. This is the mockup's "Simulate live usage" behaviour, kept.
4. **GB badge: spins with live speed.** The unit badge (the cookie shape) rotates continuously. Speed in degrees per second = `8 + 46 x speedMBps`, with `speedMBps` clamped to 0..5 (idle about 45 s per turn; at 5 MB/s about 238 degrees per second). The "GB" label is counter-rotated so it always reads upright. The speed comes from the Live tile's existing throughput (the same value that shows "10 B/s"); when it is unknown the badge turns at the idle speed.
5. **Reduced motion.** The hero shows the final weight immediately, no pulse; the badge does not rotate. Nothing else changes.
6. **Other screens unchanged.** `HeroNumber` is also used by App detail, History and Onboarding. The entrance and the spinning badge are opt-in parameters that only Home passes.

**Interfaces.**
- `enum class HeroEntrance { None, WeightBloom }` and new `HeroNumber` parameters `entrance: HeroEntrance = HeroEntrance.None`, `badgeSpinDegPerSec: Float = 0f`. Default behaviour is identical to today.
- Pure helpers, unit-tested without Compose: `internal fun badgeSpinDegPerSec(speedMBps: Float?): Float` (the formula above, `null` gives 8, clamps to 0..5); `internal fun changedDigitIndices(old: String, new: String): List<Int>` (indices whose character differs; a length change returns all indices); `internal data class HeroBloomSpec(val startWght: Float = 100f, val endWght: Float = 800f, val staggerMs: Int = 70, val delayMs: Int = 150, val dampingRatio: Float = 0.35f, val stiffness: Float = 750f)` with `fun startAtMs(index: Int): Int`.
- `EmberbyteType.heroAxes(size: TextUnit, wght: Float): TextStyle` builds the hero style with a given weight on the same Roboto Flex font and the fixed `wdth` 36 / `opsz` 144 axes.
- `UnitBadge(unit, size, spinDegPerSec: Float = 0f, ...)`. Rotation uses `graphicsLayer { rotationZ = ... }` driven by an `Animatable` or `withInfiniteAnimationFrameNanos` loop, so no text recomposes per frame. Text remains upright through a counter-rotation.

**Steps (each its own commit, test first, watch it fail).**
1. **Mark.** RED `home_brand_mark_is_44dp` (assert the mark node's size) and `other_screens_keep_their_mark_size`. Change `BrandMark(size = 32.dp)` to 44.dp in the Home header only. Check the header at font scale 2.0 and RTL for clipping.
2. **Pure logic.** RED `badge_spin_speed_follows_the_formula` (null, 0, 1.2, 5 and 9 give 8, 8, 63.2, 238, 238), `changed_digit_indices_marks_only_changed_digits` ("3.71" to "3.72" gives [3]; "3.79" to "3.80" gives [2, 3]; "9.99" to "10.00" gives all), `bloom_spec_staggers_digits_70ms_after_150ms`. GREEN the three helpers.
3. **Hero entrance.** RED (use the test clock, `mainClock.autoAdvance = false`): `hero_starts_thin_and_ends_at_final_weight` (expose the current weight per digit through a test-only semantics key or a `Modifier.testTag`-readable state), `hero_digits_start_in_order`, `hero_weight_overshoots_then_settles_at_800`, `hero_weight_never_exceeds_1000`, `entrance_snaps_when_reduced`, `entrance_does_not_replay_on_value_change`, `entrance_plays_once_per_app_start`, `other_screens_hero_has_no_entrance`. GREEN: per-digit `Animatable` weights, `EmberbyteType.heroAxes`. Keep the single accessibility node (`clearAndSetSemantics`) and the auto-shrink size logic of `HeroNumber`; the digits must not change the measured width in a way that clips at font scale 2.0 (measure with the final weight, then animate inside that box).
4. **Live pulse.** RED `changed_digit_pulses_not_the_others`, `pulse_is_skipped_when_reduced`. GREEN.
5. **Badge.** RED `badge_rotates_over_time`, `badge_label_stays_upright`, `badge_spins_faster_with_higher_speed`, `badge_idles_when_speed_is_unknown`, `badge_is_still_when_reduced`, `badge_does_not_recompose_text_each_frame` (count recompositions of the label). GREEN. Wire `HomeUiState` so the hero receives the live download rate; Home passes `badgeSpinDegPerSec(rate)` and `HeroEntrance.WeightBloom`.
6. **Performance gate (device).** Rebuilding font variation settings per frame allocates a typeface each time. On a mid-range phone record the entrance with `adb shell dumpsys gfxinfo` (or the profiler) and report the janky-frame percentage in the PR. If more than 5 percent of frames are janky, implement the fallback and say so in "Rulings": a cross-fade between three pre-built hero styles (wght 100, 400, 800) with the same stagger.

**Review Focus for this task.**
1. Font scale 2.0, display size 560 and RTL: the number never clips during the entrance (a heavier weight is wider).
2. A value that changes length ("9.99" to "10.00", "999 MB" to "1.00 GB") must not replay the entrance or crash.
3. Reduced motion toggled while the entrance is running snaps to the final weight.
4. App detail, History and Onboarding look exactly as before.
5. Battery and CPU: the badge loop must stop when Home is not visible (lifecycle-aware).

**Acceptance.** On a device, a cold start shows the Home number blooming digit by digit with an overshoot and a bigger still mark; the GB badge turns slowly and visibly faster while data is flowing; a ticking value pulses only the changed digit; with animations off everything is static; the other three screens that show a hero number are unchanged.

---

## T18. Home bento tiles: Live and Today by network

**Closes:** maintainer request of 2026-11-20 (not an audit finding). **Owner:** Khaled. **Branch:** `t18-home-bento`, started **after T17 merges** (both edit `HomeSections.kt`; T11 provides `pressMorph`, T12a the shape scale, T1 the contrast helpers). **Files:** `app/.../home/HomeSections.kt` (`HomeBentoRow`, `HomeNetworkTile`, the Live `SpeedTile` call sites), `app/.../home/HomeViewModel.kt`, `HomeUiState.kt`, `HomeMapping.kt`, `ui/design/.../tiles/Tiles.kt` (existing `SpeedTile`), new `ui/design/.../tiles/LiveBentoTile.kt` and `NetworkBentoTile.kt`, new `ui/design/.../theme/palette/TileColours.kt`, `ui/design/.../theme/EmberbyteType.kt` (grotesque family), new font `ui/design/src/main/res/font/space_grotesk.ttf`, `app/src/main/assets/licences.json` and `NOTICE` (licence entry for the font). **Tests next to each.**
**Mockup (the source of every number below):** https://claude.ai/artifact/JzNkx4P5r5P5nLmZi8HaeH . The maintainer chose these options on 2026-11-20: bento pair, colour-blocked, interlocking corners, Space Grotesk, "Roll and morph", session-only direction. Nothing else from the mockup ships.

**Decisions (locked).**
1. **Layout.** The Live tile and the Today by network tile sit **side by side**, equal width, 10 dp apart, directly under the hero and "Set up a plan" line. Min height 212 dp. Corners interlock: Live uses 28 dp on three corners and 8 dp on the bottom end; Network uses 8 dp on the top start and 28 dp on the rest (`mixedCorners`, which mirrors in RTL). *With a plan:* the existing `ForecastTile` moves to its own full-width row above the pair. *Open question for the maintainer:* confirm this plan-state layout; the mockup shows only the no-plan state.
2. **Colour-blocked.** Live is tinted from the theme's primary, Network from its green accent, and Upload (see 8) from its warm warn colour. New file `TileColours.kt`: `internal data class TileColours(val container: Color, val onContainer: Color)` and `fun EmberbytePalette.liveTile(): TileColours`, `networkTile()`, `uploadTile()`. Container = `lerp(surface2, primary, 0.22f)`, `lerp(surface2, accent, 0.5f)` and `lerp(surface2, warn, 0.30f)`; `onContainer` = `ensureContrast(text, container, 7.0)`. A test checks 7:1 for the primary label and 4.5:1 for secondary text in all 12 palettes, light, dark and AMOLED.
3. **Typeface.** Space Grotesk (variable, `wght` 400 to 700), bundled as `res/font/space_grotesk.ttf` (SIL Open Font License; add the licence text to `licences.json` and `NOTICE`, because `NoticeCoversDependenciesTest` and the Licences screen cover bundled fonts). Used **only in these two tiles** in this task: `EmberbyteType.grotesk(size: TextUnit, weight: Int): TextStyle`. Rolling it out app-wide is a separate decision.
4. **No icon** beside the tile labels. Live label: "Live · Wi-Fi" (or the current network name as today) with a second small line "Download" or "Upload". Network label: "Today by network".
5. **Live tile content (top to bottom):** the label block; the arrow disc (24 dp, a filled circle with a 13 dp arrow cut out) beside the **speed number** (grotesque, 44 sp, weight 600, letter spacing -0.03 em, tabular figures) and its unit (15 sp); the **trace** (see 10); a chip showing the **other direction** ("↑ 70 KB/s" while showing download, "↓ 1.2 MB/s" while showing upload) with a "⇅" mark.
6. **Network tile content:** the label; one **big number centred in the free space**: the **larger** of the two networks' bytes (Wi-Fi in the mockup), grotesque 60 sp, weight 600, letter spacing -0.045 em, with a 14 sp line under it ("GB on Wi-Fi" or "MB on mobile"); the **wavy bar** (existing `WavyBar`/`NetworkBar` colours: primary for mobile, accent green for Wi-Fi) with the mobile segment given a **minimum visible width of 14 dp whenever mobile bytes are above zero** (9 MB of 3.7 GB is under 1 percent); and a centred "Mobile 9.35 MB" line (the smaller network). The big number auto-shrinks with the same binary-search logic as `HeroNumber` (minimum 36 sp) so "12.45 GB" or "103 GB" never clips, including at font scale 2.0.
7. **Entrance.** Tiles spring in one after the other: 140 ms then +110 ms, scale 0.86 to 1.0, translate 18 dp to 0, fade, on the `spatial()` spring. The big network number **counts up** from 0 to its value over 950 ms starting 380 ms in: the value follows `1 - (1 - p)^3` (it never exceeds the real total), while the scale follows an ease-out-back from 0.9 to 1.0. Plays once per app start together with the T17 hero entrance (reuse its flag).
8. **Switching download and upload (Roll and morph).** Pressing the Live tile toggles direction. State is `rememberSaveable { mutableStateOf(Direction.Download) }`: **session only**, default Download, never written to settings. The transition (all on `rememberEmberbyteMotion()` specs, snapping when reduced):
   - the old number slides up by 105 percent of its height and fades out over 380 ms while the new number rolls in from 105 percent below, `spring(dampingRatio = 0.6f, stiffness = 380f)`, about 520 ms (use `MorphingNumber`);
   - the arrow disc rotates 0 to 180 degrees (and back) with `spring(dampingRatio = 0.5f, stiffness = 330f)`, overshooting to about 208 degrees before settling;
   - the trace morphs from the download series to the upload series point by point with `spring(dampingRatio = 0.62f, stiffness = 240f)` (it may overshoot by up to 12 percent);
   - the container animates from the Live tint to the Upload tint over 500 ms;
   - the second label line rolls in from 60 percent below and the chip from 70 percent below.
   Reduced motion: everything switches at once.
   Semantics: `Role.Button`, `stateDescription` "Showing download speed" or "Showing upload speed", `onClickLabel` "Show upload speed" or "Show download speed". Keyboard and switch access work through the normal click.
9. **Press and ambient motion.** Both tiles use `pressMorph` (T11): scale 0.96 and corners to 16 dp on a spatial spring. Ambient: the trace scrolls smoothly, the end point pulses (radius 3.6 dp plus or minus 1.1 dp at about 0.8 Hz), the wavy bar's phase advances at 2.2 rad/s, and a faint corner shape (13 percent opacity, 96 dp, offset -26 dp from the bottom end corner) turns slowly: 9 degrees per second on Live, minus 7 on Network. Reduced motion: all static. The loops must stop when Home is not visible (lifecycle-aware).
10. **Trace and data.** The Live tile needs a short history. `HomeViewModel` keeps an **in-memory ring buffer of the last 60 samples, one per second, for the receive and transmit rates separately** (reuse the existing `live` rate flow that already feeds `liveRxBps` and `liveTxBps`; no new service, nothing persisted, cleared on process death). `HomeUiState` gains `liveRxHistory: List<Long>` and `liveTxHistory: List<Long>` (oldest first). Each series is drawn scaled to its own maximum with a floor so an idle connection shows a flat low line, not noise: `max(observedMax, 256_000L)` bytes per second for download and `max(observedMax, 32_000L)` for upload. The line is a smooth quadratic curve with a gradient fill beneath it (28 percent to 0 percent of the label colour).

**Interfaces.**
- `@Composable fun LiveBentoTile(rxBps: Long, txBps: Long, rxHistory: List<Long>, txHistory: List<Long>, networkLabel: String, units: ByteUnits, direction: LiveDirection, onToggle: () -> Unit, modifier: Modifier = Modifier)` and `enum class LiveDirection { Download, Upload }`.
- `@Composable fun NetworkBentoTile(mobileBytes: Long, wifiBytes: Long, units: ByteUnits, playEntrance: Boolean, modifier: Modifier = Modifier)`.
- Pure helpers (unit tests, no Compose): `internal fun bigNetwork(mobile: Long, wifi: Long): BigNetwork` (the larger one is big; a tie picks Wi-Fi; both zero shows Wi-Fi 0), `internal fun mobileSegmentWidth(mobile: Long, wifi: Long, barWidthDp: Float): Float` (proportional, but at least 14 dp when mobile is above zero, and 0 when it is zero), `internal fun traceScale(series: List<Long>, floorBps: Long): Long`, `internal fun appendSample(history: List<Long>, sample: Long, capacity: Int = 60): List<Long>`, `internal fun countUpValue(progress: Float, target: Double): Double` (ease-out cubic, never above target).

**Steps (each its own commit, test first, watch it fail).**
1. **Pure logic.** RED: `big_network_is_the_larger_one`, `big_network_ties_pick_wifi`, `mobile_segment_has_a_minimum_visible_width`, `mobile_segment_is_zero_when_there_is_no_mobile_data`, `trace_scale_has_a_floor_for_an_idle_connection`, `append_sample_keeps_the_last_60_oldest_first`, `count_up_never_exceeds_the_target_and_ends_on_it`. GREEN.
2. **Colours.** RED `tile_containers_meet_contrast_in_every_palette_and_mode` (7:1 label, 4.5:1 secondary text), `upload_tint_differs_from_live_tint`. GREEN `TileColours.kt`.
3. **Font.** Add `space_grotesk.ttf`, the `licences.json` entry and the `NOTICE` line. RED `notice_covers_the_bundled_grotesque` (extend `NoticeCoversDependenciesTest`), `grotesk_style_uses_the_bundled_family`. GREEN `EmberbyteType.grotesk`.
4. **Data.** RED (`HomeViewModelTest`) `live_history_keeps_receive_and_transmit_separately`, `live_history_is_capped_at_60`, `history_is_not_persisted`. GREEN.
5. **Tiles.** RED (Compose, Robolectric): `live_and_network_tiles_sit_side_by_side_with_equal_width`, `corners_interlock_and_mirror_in_rtl`, `tiles_have_no_icon_beside_their_label`, `network_big_number_is_centred_and_never_clips_at_font_scale_2`, `network_tile_shows_the_larger_network_as_the_big_number`, `live_tile_shows_the_other_direction_in_the_chip`. GREEN.
6. **Switch.** RED: `pressing_the_live_tile_switches_to_upload_and_back`, `direction_survives_recreation_but_is_not_saved_to_settings`, `semantics_announce_the_direction_and_the_action`, `roll_transition_has_in_between_frames` (test clock: the old and the new number are both present mid-transition), `arrow_overshoots_then_settles_at_180`, `trace_morphs_between_the_two_series`, `container_tint_animates`, `switch_is_instant_when_reduced`. GREEN with the spring specs in decision 8.
7. **Entrance and ambient.** RED `tiles_enter_in_order`, `network_number_counts_up_and_never_exceeds_the_value`, `entrance_plays_once_per_app_start`, `ambient_motion_is_static_when_reduced`, `ambient_loops_stop_when_home_is_not_visible`. GREEN.
8. **Home wiring.** Replace `HomeNetworkTile` and the Live `SpeedTile` use in `HomeBentoRow`/`HomeScreen` with the new pair; keep the plan-state `ForecastTile` row above. Existing Home tests must still pass; update them where they assert the old layout and say why in the pull request.
9. **Performance gate (device).** Record the Home screen on a mid-range phone while the trace scrolls and report the janky-frame percentage; if more than 5 percent are janky, draw the trace and the wave with `drawWithCache` and lower the redraw rate to 30 Hz, and record it under "Rulings".

**Review Focus for this task.**
1. Font scale 2.0, display size 560 and RTL: nothing clips, corners mirror, the big number shrinks instead of overflowing.
2. First launch with no data (all zeros), mobile-only data, and Wi-Fi-only data render sensibly.
3. A speed that crosses 1 MB/s flips its unit ("999 KB/s" to "1.0 MB/s") without the number jumping or the roll animation replaying.
4. Pressing during a running transition does not stack or glitch.
5. Contrast of every label on both tints in all palettes; the Upload tint must not look like a warning state.
6. APK size: the font adds about 100 to 150 KB; note the exact figure in the pull request.

**Acceptance.** On a device the Home screen shows the two tiles side by side with the new typeface; pressing the Live tile rolls from download to upload with the arrow flip, the trace morph and the tint change, and back; the network tile counts its big number up once and shows a visible mobile segment; with animations off everything is static and the switch is instant; a plan state keeps the forecast on its own row.

---

## T19. Home plan call to action: one merged setup card

**Closes:** maintainer request of 2026-11-20 (not an audit finding). **Owner:** Khaled. **Branch:** `t19-plan-setup-card`, started **after T18 merges** (it uses T18's bundled grotesque font, the tile colours in `TileColours.kt`, and edits the same Home files). **Files:** `app/.../home/HomeSections.kt` (the "Set up a plan" text at line 179, shown under the hero when there is no plan), `app/.../home/HomeScreen.kt` (the "Data plan" card at lines 196 to 208), new `ui/design/.../plan/PlanSetupCard.kt`, `ui/design/.../progress/WavyRing.kt` (a ghost-ring mode). **Tests next to each.**
**Mockup (the source of every number below):** https://claude.ai/artifact/4cy3dwk2i4Jz4baTmXd3ZJ . The maintainer chose "No plan, proposed" with the **merged card with a button** on 2026-11-20. The other two variants in the mockup (inline pill, example preview) do not ship.

**Decisions (locked).**
1. **One card replaces two things.** While there is **no plan**, the floating "Set up a plan" text under the hero and the large "Data plan" card further down are both removed and replaced by one `PlanSetupCard`, shown directly under the hero. Once a plan exists, nothing changes in the hero line ("X left of Y" with its wavy bar) and the card is not shown.
2. **Card.** `mixedCorners(28.dp, 8.dp)` with the small corner at the bottom end (RTL mirrors), container and text colours from `palette.liveTile()` (T18 `TileColours.kt`: the primary-tinted container, label at 7:1). Padding 16 dp.
   - **Ghost ring** (76 dp, at the start): a wavy ring, track at 20 percent opacity and an arc filling **70 percent** of the circle in `palette.primary`, stroke 7 percent of the size (about 5.3 dp), wave amplitude 2.6 percent of the radius, 14 waves. It is decorative (`clearAndSetSemantics`). Reuse `WavyRing` with a ghost mode; do not draw a second ring implementation.
   - **Text** (to the right of the ring), in the T18 grotesque: title **"Set up your data plan"** (21 sp, bold, letter spacing -0.01 em, line height 1.1) and body **"See what is left and when it runs out."** (14 sp, medium, 85 percent opacity, line height 1.35).
   - **Button** (full width under both): a filled `Button`, `primary` on `onPrimary`, pill shape, min height **52 dp**, label **"Set up plan"** (17 sp, bold) and a trailing arrow icon (18 dp). The action is the same one the old "Set up your data plan" button has today (open the new-plan flow); do not add a new route.
3. **Motion** (all on `rememberEmberbyteMotion()` specs, static when reduced).
   - **Entrance:** the card fades and rises (translate 14 dp to 0, scale 0.97 to 1.0, spatial spring) 80 ms after the hero entrance, before the bento tiles (T18 starts 100 ms later).
   - **Ghost ring:** on first show the arc draws from 0 to 70 percent over 1.3 s; the wave phase then advances at 2 rad/s. Reduced motion: the arc is simply at 70 percent, flat, with no wave.
   - **Press:** the card and the button both use `pressMorph` (T11): scale 0.97 and 0.96, corners to 18 dp, on a spatial spring.
   - The wave loop stops when Home is not visible (lifecycle-aware).
4. **Accessibility.** The title is a heading; the card itself is not clickable, the button is the only action (target at least 48 dp, here 52). Focus order: title, body, button. Content description of the button: "Set up your data plan".
5. **Plan state (shared open question with T18).** With a plan, the Forecast tile sits on its own full-width row above the Live and Network pair. This task does not change that layout; T18 owns it. The mockup shows it under "With a plan".

**Interfaces.**
- `@Composable fun PlanSetupCard(onSetUp: () -> Unit, modifier: Modifier = Modifier, playEntrance: Boolean = true)`.
- `WavyRing` gains an optional `ghost: GhostRing? = null` parameter with `data class GhostRing(val fraction: Float = 0.7f, val drawDurationMs: Int = 1300)`; with `ghost == null` it behaves exactly as today. Pure helper `internal fun ghostArcFraction(progress: Float, target: Float): Float` (clamped linear draw, 0 to `target`).

**Steps (each its own commit, test first, watch it fail).**
1. **Pure logic.** RED `ghost_arc_grows_from_zero_to_the_target_and_stops`, `ghost_arc_is_clamped_outside_zero_to_one`. GREEN.
2. **Card.** RED (Compose, Robolectric): `card_shows_the_title_the_body_and_the_button`, `button_is_at_least_52dp_and_calls_onSetUp`, `title_is_a_heading_and_the_ring_is_not_announced`, `corners_have_one_small_corner_and_mirror_in_rtl`, `card_text_meets_contrast_in_every_palette`, `card_does_not_clip_at_font_scale_2` (the text wraps, the button stays visible). GREEN `PlanSetupCard`.
3. **Motion.** RED `ghost_ring_draws_over_time_and_ends_at_70_percent`, `ghost_ring_is_static_when_reduced`, `entrance_has_in_between_frames`, `entrance_snaps_when_reduced`, `press_morphs_the_card_and_the_button`, `wave_loop_stops_when_home_is_not_visible`. GREEN.
4. **Home wiring.** RED (`HomeScreenTest`): `no_plan_shows_the_setup_card_and_neither_the_old_link_nor_the_old_card`, `with_a_plan_the_setup_card_is_not_shown`, `setup_card_button_opens_the_new_plan_flow`, `setup_card_sits_under_the_hero_above_the_tiles`. GREEN: remove the "Set up a plan" text (`HomeSections.kt:179`) and the "Data plan" card (`HomeScreen.kt:196-208`) for the no-plan state and insert `PlanSetupCard`. Update existing Home tests that assert the old strings and say why in the pull request.
5. **Widgets and onboarding are not touched.** The widgets and onboarding still say "Set up a plan"; leave them unchanged and add `widgets_and_onboarding_keep_their_setup_copy` as a guard.

**Review Focus for this task.**
1. Font scale 2.0 and display size 560: the ring stays 76 dp while the text wraps, and the button never leaves the screen.
2. RTL: the ring moves to the right, the arrow icon mirrors, the corner flips.
3. Creating a plan from the card makes the card disappear without a flash of the old link or card.
4. A user who dismissed onboarding without permission still sees a sensible card (it does not depend on usage access).
5. The card must not look like an error or a warning: it uses the calm primary tint.

**Acceptance.** On a device with no plan, Home shows one card under the hero with the ghost ring drawing itself, the two lines of copy and one big button, and nothing else about plans; pressing the button opens the new-plan flow; creating a plan removes the card; with animations off everything is static; widgets and onboarding are unchanged.

---

## T20. Home usage card: Top apps with History inside

**Closes:** maintainer request of 2026-11-20 (not an audit finding). **Owner:** Khaled. **Branch:** `t20-usage-card`, started **after T19 merges**, and after Omar's T5 (History values, selected-day mark and labels) and T11 (`pressMorph`) have merged. **Files:** `app/.../home/HomeSections.kt` and `HomeScreen.kt` (the "Top apps today" card and the separate "History" card), `app/.../home/HomeViewModel.kt`, `HomeUiState.kt`, `HomeMapping.kt`, new `ui/design/.../usage/UsageCard.kt`, `UsageWeekStrip.kt`, `AppUsageRow.kt`, `UsageFiltersSheet.kt`, `app/.../nav/` (History transition and the day argument). **Tests next to each.**
**Mockup (the source of every number below):** https://claude.ai/artifact/Q4pn1FJFdnMTcFu5q25JBE . The maintainer chose "one card, History inside" with **connected buttons (All apps, Filters, History)** on 2026-11-20. The split-button variant does not ship.

**Decisions (locked).**
1. **One card replaces two.** `UsageCard` replaces the "Top apps today" card and the separate "History" card on Home. Card container: the theme surface colour, `mixedCorners(30.dp, 10.dp)` with the small corner at the bottom start, padding 16 dp (14 dp sides). Type is the T18 grotesque.
2. **Header and summary.** Title **"Top apps"** (22 sp, bold, letter spacing -0.01 em) with a pill on the right showing the selected day ("Today" or the weekday; 13 sp bold, primary-tinted container). Under it a summary line, **"3.71 GB on Friday, so far"** (the amount in bold; ", so far" only for today; the unit switches to MB below 1 GB).
3. **Week strip = day picker.** Seven buttons for the **last seven days ending today** (rolling), each with: a value above the bar (11 sp; the selected one 12 sp bold), a bar (max width 34 dp, height 74 dp, corners 13 dp) and a weekday initial in a 26 dp disc below. Unselected bars are the primary at 28 percent; the selected bar is solid primary and its disc is filled; today's disc has a 2 dp ring. Bar height is `total / (maxTotal x 1.12)` with a floor of 6 percent. Value label: one decimal in GB at 0.095 GB and above, otherwise whole MB with an "M" suffix. Selecting a day updates the pill, the summary and the ranking; the bars animate their height on a spatial spring (about 700 ms) and the colours over 300 ms. Entrance: bars grow from 6 percent starting 420 ms after the card, 60 ms apart. Each day button announces "Friday, 3.71 gigabytes" with a pressed state.
4. **App rows.** The **top four apps** for the selected day (after filters), ranked by usage.
   - **Featured first row:** min height 88 dp, corners 26 dp, 60 dp icon in its `AppShape`, name 21 sp bold, amount 30 sp bold (letter spacing -0.02 em) with the unit, a sub-line "Wi-Fi 1.59 GB · Mobile 0 B", and its share in percent under the amount.
   - **Other rows:** min height 64 dp, corners 22 dp, 44 dp icon, name 17 sp bold, amount 21 sp bold, sub-line "NN% of today" (or of the weekday).
   - **Share fill:** a rounded rectangle behind each row from the start edge, width `max(14%, usage / firstRowUsage)`, in the T18 primary-tinted container; the row base is the text colour at 7 percent. The fill animates its width on the spatial spring (about 800 ms).
   - **Press and expand:** rows use `pressMorph` (corners to 16 dp, scale 0.97). A tap expands the row inline (spring height, about 450 ms) to show "Wi-Fi X", "Mobile Y" and "App details", which opens the existing app detail screen. Only one row is open at a time; the row announces its expanded state.
   - **Re-rank motion:** rows keyed by package name use `animateItem` (placement spring about 560 ms); amounts roll with `MorphingNumber`; a row entering scales from 0.9 and fades in over 420 ms; one leaving fades out. Real app icons are used (the mockup's letters are stand-ins).
5. **Connected buttons.** Three segments in one row, 3 dp apart, min height 54 dp, outer corners 27 dp and inner corners 9 dp: **"All apps"** (tonal, text only; the old "See all" action, opens Apps), **"Filters"** (tonal, filter icon, label and a count badge) and **"History"** (filled primary, clock icon; opens History). Widths 1 : 1.1 : 1.15. With any filter active the Filters segment turns primary-tinted and shows the count badge; its label becomes "Filters, N active" for accessibility.
6. **Filters.** The Filters button opens a bottom sheet inside Home (draft, then Apply or Reset, like the Apps `FiltersSheet`): **Network** as a connected group (All, Mobile, Wi-Fi) and **"Show system apps"** as a switch with a check in its thumb, then Reset and Apply buttons and the note "Period, sort and more are in Apps". The filters affect the strip totals and bars, the summary, and the ranking together. Active filters also show as **removable chips under the summary line**, labelled in full ("Network: Mobile", "System apps"), at least 32 dp tall, tonal primary; one tap removes that filter. State lives in `HomeViewModel`, session only, **independent of the Apps screen's filters** (*decided by the maintainer on 2026-11-20: independent*). There is no period filter in the card: the strip is the period control.
7. **History.** The History button opens the History screen with a **container transform** that grows out of the button (clip-reveal from the button's bounds to the full screen, about 620 ms on a spring, the chart bars growing in after 260 ms, 50 ms apart); going back reverses it in about 380 ms. The **selected day is passed in** and, if changed there, **returned**, so the card keeps the day last chosen. Implement with `SharedTransitionLayout`/`sharedBounds` if Navigation Compose 2.10.2 supports it for this route; otherwise use the app's standard push transition and say so under "Rulings". Reduced motion: instant.
8. **Data.** `HomeUiState` gains `usageDays: List<DayUsage>` (`data class DayUsage(val date: LocalDate, val totalBytes: Long, val mobileBytes: Long, val wifiBytes: Long)`, oldest first, seven entries) and `HomeViewModel` exposes per-app rows for the selected day and filters through `AppUsageRow(packageName, label, mobileBytes, wifiBytes, isSystem)`. Both come from the repository queries the History and Apps screens already use; no new permission and no network.
9. **Reduced motion.** Everything static: bars at their height, ranking changes instantly, no glide, the History open is a plain switch.

**Interfaces.**
- `@Composable fun UsageCard(state: UsageCardState, onSelectDay: (Int) -> Unit, onOpenApp: (String) -> Unit, onAllApps: () -> Unit, onOpenFilters: () -> Unit, onRemoveFilter: (UsageFilter) -> Unit, onOpenHistory: (selectedDayIndex: Int) -> Unit, modifier: Modifier = Modifier)`.
- `data class UsageCardState(val days: List<DayUsage>, val selectedIndex: Int, val rows: List<AppUsageRowUi>, val filters: UsageFilters, val units: ByteUnits)`; `data class UsageFilters(val network: NetworkFilter = NetworkFilter.All, val showSystemApps: Boolean = false)`, `enum class NetworkFilter { All, Mobile, WiFi }`, `enum class UsageFilter { Network, SystemApps }`.
- Pure helpers (unit tests, no Compose): `internal fun stripBarFractions(totals: List<Long>): List<Float>` (the formula above), `internal fun stripLabel(bytes: Long, units: ByteUnits): String`, `internal fun rankApps(rows: List<AppUsageRow>, filters: UsageFilters, limit: Int = 4): List<RankedApp>` (filtered amount, descending, top four), `internal fun dayTotal(day: DayUsage, rows: List<AppUsageRow>, filters: UsageFilters): Long`, `internal fun shareFillFraction(usage: Long, first: Long): Float` (min 0.14), `internal fun summaryText(day: LocalDate, today: LocalDate, total: Long, units: ByteUnits): String`.

**Steps (each its own commit, test first, watch it fail).**
1. **Pure logic.** RED `strip_bar_fractions_scale_to_the_max_with_a_floor`, `strip_label_uses_gb_above_95_mb_and_whole_mb_below`, `rank_apps_orders_by_the_filtered_amount`, `mobile_filter_ranks_by_mobile_bytes`, `system_apps_are_included_only_when_the_filter_is_on`, `day_total_follows_the_filters`, `share_fill_has_a_14_percent_minimum`, `summary_says_so_far_only_for_today`. GREEN.
2. **Data.** RED (`HomeViewModelTest`) `usage_days_has_seven_days_ending_today`, `per_app_rows_follow_the_selected_day`, `filters_are_session_only_and_independent_of_apps`. GREEN.
3. **Strip and rows (Compose).** RED `strip_shows_seven_days_with_values_and_a_marked_selected_day`, `selecting_a_day_updates_the_summary_and_the_ranking`, `first_row_is_featured`, `rows_expand_one_at_a_time_and_announce_the_state`, `row_share_fill_follows_usage`, `rows_glide_to_new_positions_when_the_ranking_changes` (test clock), `reduced_motion_ranking_changes_instantly`, `day_buttons_announce_the_day_and_value`. GREEN.
4. **Buttons and filters.** RED `connected_buttons_show_all_apps_filters_history_in_order`, `filters_segment_shows_a_badge_and_tint_when_active`, `filters_sheet_applies_and_resets_a_draft`, `active_filters_show_as_removable_full_labelled_chips`, `removing_a_chip_updates_the_card`, `network_filter_changes_totals_bars_and_ranking_together`. GREEN.
5. **History hand-off.** RED `history_receives_the_selected_day`, `history_returns_the_changed_day_to_the_card`, `history_open_is_instant_when_reduced`. GREEN (container transform or the fallback in decision 7).
6. **Home wiring.** Replace the two old cards with `UsageCard`; update the Home tests that asserted the old strings and say why in the pull request. Existing History screen tests must still pass.
7. **Performance gate (device).** The card re-ranks and animates on every day change; record it on a mid-range phone and report the janky-frame percentage. If above 5 percent, drop the share-fill spring to a tween and shorten the glide, and note it under "Rulings".

**Review Focus for this task.**
1. Font scale 2.0, display size 560 and RTL: names ellipsize, the connected buttons keep their labels or drop to icons plus badge, the strip does not overflow.
2. A day with no usage, a filter that matches nothing (empty state: "No apps match these filters" with a Reset action), and fewer than four apps.
3. A first day of data (history shorter than seven days): missing days show as empty bars, not crashes.
4. Switching day during a running animation does not stack or glitch; the History round trip keeps the day.
5. The permission-missing state still shows the existing prompt instead of an empty card.

**Acceptance.** On a device the Home screen has one Top apps card with a week strip, a featured first app, share-fill rows and three connected buttons; tapping a day re-ranks the apps with a glide; Filters narrows everything with a badge and removable chips; History grows out of its button, shows the chosen day and returns it; the old Top apps and History cards are gone; with animations off everything is static.

---

## T21. Apps page redesign

**Closes:** E1, E2, E3 (audit) and the maintainer request of 2026-11-20. **Owner:** Khaled. **Branch:** `t21-apps-page`, started **after T20 merges** (it reuses T20's app row, share fill and connected-button work) and after Omar's T4 (loading states), T5 (the "apps only" total, C4) and T11 (`pressMorph`) have merged. **Files:** `app/.../apps/AppsScreen.kt`, `AppsViewModel.kt`, `AppsUiState`, `FeaturedAppCard.kt`, `FiltersSheet.kt`, new `apps/AppsSummaryCard.kt`, `apps/PeriodButtons.kt`, `apps/AppsFilterChips.kt`. **Tests next to each.**
**Mockup (the source of every number below):** https://claude.ai/artifact/CiTJo6SKnjjq5aotKLTu6t . Approved by the maintainer on 2026-11-20.

**Decisions (locked).**
1. **Header.** "Apps" at 34 sp bold in the T18 grotesque, letter spacing -0.02 em, and a pill on the right with the app count ("47 apps", "1 app"; container `palette.primaryContainer`-style tint as T20's day pill).
2. **Search and Filters are one connected pair**, 6 dp apart, both **56 dp** tall: the search field `mixedCorners` 28 dp outer start, 10 dp inner (RTL mirrors), tonal container (text colour at 9 percent), search icon, "Search apps", a 44 dp clear button when text is present; on focus a 2.5 dp primary inner outline and all corners 28 dp. The Filters button is 64 x 56 dp, primary fill, tune icon, corners 10 dp inner and 28 dp outer, and shows a **count badge** (18 dp, number of non-default filters) at its top end. Replaces the 64 dp outlined field plus 48 dp tonal button (E3).
3. **Period buttons on the page.** Four connected buttons directly under the search row: **Today, 7 days, 30 days, Cycle** (min height 48 dp, 3 dp apart, outer corners 24 dp, inner 9 dp). The selected one is primary-filled and 1.25 times wider; the change animates colour over 300 ms and the corners on a spatial spring. They set `AppsRange` immediately. **The period is removed from the Filters sheet** and from the chips.
4. **Filters sheet** keeps its draft, Apply and Reset model (E2): **Network** as a connected group (All, Mobile, Wi-Fi), **Sort by** as a connected group (Most, Least, Name), a "Show system apps" row with a switch (type setting identical to the group labels, check in the thumb), then Reset (a tonal, 52 dp button, clearly visible) and Apply (primary, 1.6 times wider). Use the Expressive sheet top shape (`shapes.extraLarge` top corners), a grab handle and tighter spacing (no 130 px gaps). Apply closes the sheet; Reset only resets the draft. Filters stay **independent of Home's** (T20): *decided by the maintainer*.
5. **Active filter chips (E1).** 32 dp tonal removable chips under the period buttons, labelled in full: "Network: Mobile", "Sort: Least data", "System apps". One tap removes that filter (`AppsEvent.RemoveFilter`). A trailing "Reset" text chip appears when two or more are active. The row wraps instead of clipping or scrolling off the end. Each chip announces "Remove Network: Mobile filter".
6. **Summary card** (replaces the plain "X in total" line and the "No app data" text above the list): `mixedCorners(28dp, 10dp)` with the small corner at the bottom start, container `palette.surface2`. Label "Apps used today (apps only)", then the total at **48 sp bold** with the unit at 20 sp (`tabular-nums`, `MorphingNumber` from T10), then a **Wi-Fi / Mobile split bar** (16 dp tall, 3 dp gap, 14 dp minimum segment, primary for Wi-Fi and green for Mobile, `flex` change on a spatial spring), then "Wi-Fi X" and "Mobile Y" on one line. The label names the period ("today", "in 7 days", "in 30 days", "this cycle"). The "apps only" wording closes C4 together with Omar's T5 (Home's hero total includes system traffic). The card is hidden in the empty state.
7. **Most used card** (replaces `FeaturedAppCard`'s layout): `mixedCorners` with the small corner at the top end, container the primary-tinted container from `TileColours.kt`, **64 dp icon** (`AppShape`), caption "MOST USED" (11 sp, bold, letter spaced), app name 22 sp bold, amount 30 sp bold on the right and "N% of total" under it. Tapping opens the app. It is the top row of the *unfiltered by search* ranking for the current period and network (a search that excludes it hides the card).
8. **List.** Rows reuse T20's `AppUsageRow` look (min height 68 dp, corners 22 dp, 44 dp icon, name 17 sp bold, amount 20 sp bold, share fill behind the row at `max(14%, usage / firstRowUsage)` in the tinted container, `pressMorph` and `animateItem` re-rank with `motion.spatial()`). The sub-line is "Wi-Fi X · Mobile Y" for the All network and "N% of total" when one network is selected. A header row above the list shows "All apps" and the current sort ("Most data", "Least data", "Name"). **List style: A, share fill only (decided by the maintainer on 2026-11-20).** No rank numbers and no section headers.
9. **Empty states.** Search with no match shows **one** message, "No apps match "chr"", with a Clear search button, and hides the summary card, the most used card and the list header (E3); no "0 B in total". No data yet keeps the existing explanation. Filters that match nothing show "Nothing to show for these filters" with a Reset filters button.
10. **Loading and permission.** Uses Omar's T4 loading state (no false empty state) and the existing usage-access prompt, which sits under the period buttons.
11. **Reduced motion.** Static: no entrance, no glide on re-rank, the split bar and fills at their final size, period buttons switch instantly.
12. **Entrance.** Header, search row, period buttons, summary card, most used card and list header fade and rise in a stagger of 70 ms starting 80 ms after the screen is shown (translate 16 dp, scale 0.97, spatial spring); rows use `animateItem`. Replays every time the tab is opened is *not* wanted: once per app start.

**List style (decided).** The maintainer chose **A**, share fill only, on 2026-11-20. The mockup's B (rank numbers) and C (Top 3 and the rest) do not ship.

**Interfaces.**
- `AppsEvent` gains `SetRange(range: AppsRange)`; `ApplyFilters` loses its `range` parameter; `AppFilterKind.RANGE` is removed (the chips no longer include the period). `AppsUiState` gains `val activeFilterCount: Int`, `val wifiBytes: Long`, `val mobileBytes: Long` (the totals for the card), and `val period: AppsRange` (already `range`).
- Pure helpers (unit tests): `internal fun activeFilterCount(network: AppsNetwork, sort: AppSort, showSystemApps: Boolean): Int`, `internal fun periodLabel(range: AppsRange): String` ("today", "in 7 days", "in 30 days", "this cycle"), `internal fun splitWeights(wifi: Long, mobile: Long): Pair<Float, Float>` (each at least 14 dp worth when non-zero), `internal fun mostUsed(rows: List<AppRowUi>): AppRowUi?`, `internal fun shareFill(usage: Long, first: Long): Float` (shared with T20).
- `@Composable fun AppsSummaryCard(total: Long, wifi: Long, mobile: Long, periodLabel: String, units: ByteUnits, modifier: Modifier = Modifier)`; `@Composable fun PeriodButtons(selected: AppsRange, onSelect: (AppsRange) -> Unit, modifier: Modifier = Modifier)`; `@Composable fun AppsFilterChips(filters: List<ActiveFilter>, onRemove: (AppFilterKind) -> Unit, onReset: () -> Unit, modifier: Modifier = Modifier)`.

**Steps (each its own commit, test first, watch it fail).**
1. **Pure logic.** RED `active_filter_count_ignores_defaults`, `period_label_names_each_range`, `split_weights_keep_a_minimum_for_a_non_zero_side`, `most_used_is_the_top_row_by_the_selected_network`, `share_fill_has_a_14_percent_minimum`. GREEN.
2. **ViewModel.** RED (`AppsViewModelTest`) `set_range_changes_the_rows_and_totals_immediately`, `apply_filters_does_not_change_the_range`, `remove_filter_resets_only_that_filter`, `totals_split_into_wifi_and_mobile`, `filters_are_independent_of_homes`. GREEN.
3. **Header, search and period buttons.** RED `search_and_filters_are_both_56dp`, `filters_button_shows_a_count_badge_when_active`, `period_buttons_set_the_range_and_mark_the_selected_one`, `period_buttons_announce_their_state`, `clear_button_appears_with_text`. GREEN.
4. **Chips and sheet.** RED `chips_are_labelled_in_full_and_removable`, `reset_chip_appears_with_two_or_more_filters`, `chips_wrap_instead_of_clipping`, `sheet_has_network_sort_and_system_apps_but_no_period`, `sheet_apply_closes_and_reset_only_resets_the_draft`, `reset_button_meets_contrast`. GREEN.
5. **Summary and most used cards.** RED `summary_shows_the_total_split_and_apps_only_label`, `summary_label_names_the_period`, `split_bar_has_a_minimum_segment`, `most_used_card_shows_icon_name_amount_and_share`, `most_used_card_opens_the_app`, `cards_are_hidden_in_the_empty_state`. GREEN.
6. **List and empty states.** RED `rows_show_split_sub_line_for_all_networks_and_percent_for_one`, `rows_glide_when_the_period_changes` (test clock), `search_with_no_match_shows_one_message_and_a_clear_button`, `no_zero_total_in_the_empty_search_state`, `reduced_motion_applies_changes_instantly`. GREEN.
7. **Wiring.** Replace the old header, search row, chips, `FeaturedAppCard` layout and total line; update existing Apps tests that asserted the old strings (say why in the pull request). The Apps detail screen is untouched.
8. **Performance gate (device).** Changing the period re-ranks about 100 rows with a glide; record it on a mid-range phone and report the janky-frame percentage; if above 5 percent, limit `animateItem` to the first 20 rows.

**Review Focus for this task.**
1. Font scale 2.0 and display size 560 and RTL: the search and Filters pair stays on one line, the period buttons keep their labels or shrink to fit, the badge does not clip, the chips wrap.
2. A user with no usage access, a first install with no data, and a period with no usage (7 days with only today's data): no crash, no misleading "0 B" card.
3. A search that matches only a system app while system apps are hidden, and a network filter that matches nothing.
4. Changing the period while the list is mid-glide, and returning to the tab, keep the selected period and filters.
5. The total on the card uses the same units setting as the rows.

**Acceptance.** On a device the Apps page shows one header with the app count, a matched search and Filters pair (badge when filters are active), period buttons that change the numbers at once, full-label removable chips, a summary card with the Wi-Fi and Mobile split and the words "apps only", a Most used card, and share-fill rows that glide when the ranking changes; Filters has Network, Sort and System apps only; a search with no match shows one message; with animations off everything is static.

---

## T15. Release 1.0.3

**Files:** `app/build.gradle.kts` (`versionName = "1.0.3"`, `versionCode = 4`), `CHANGELOG.md` (dated the release day; categories Fixed, Changed, Added), `fastlane/metadata/android/en-US/changelogs/4.txt` (under 500 characters), `app/src/main/assets/whats_new.json` (the 1.0.3 entry, user wording, newest first; `whats_new_newest_entry_matches_version_name_and_code_in_build_gradle` fails until it exists), `docs/release/RELEASE-NOTES-1.0.3.md` (certificate fingerprint `72:AD:25:A3:D8:36:6A:07:33:9C:7B:23:9B:E0:DF:D7:0D:42:7C:FB:7D:FE:49:8B:92:62:1E:4C:60:45:95:EF`, known limits, the "no INTERNET permission" sentence). Then the per-release checklist from `RELEASE-PLAN-2026-Q4.md`: tag on `main`, release workflow, draft, fingerprint check, publish, check website, Obtainium and Orion Store, install over 1.0.2. The last commit on the release branch removes this spec and the findings document (nothing after it), then squash-merge.
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
14. Update from 1.0.2 to the 1.0.3 build: the "What's new in 1.0.3" sheet appears once on the first launch, never again after dismissing it (also not after swiping it away); a fresh install and onboarding never show it; Settings > About > What's new reopens it; Updates row has a "See what's new" link; works in airplane mode.
15. Home: a cold start blooms the big number digit by digit with an overshoot, the Emberbyte mark is 44 dp and still, the GB badge turns and speeds up while data flows, a changing value pulses only the changed digit, and with animations off all of it is static. App detail, History and Onboarding hero numbers are unchanged.
16. Home: the Live and Today by network tiles sit side by side in Space Grotesk with interlocking corners; pressing the Live tile rolls between download and upload (arrow flip, trace morph, tint change) and back; the network number counts up once and the mobile segment is visible; with animations off everything is static and the switch is instant.
17. Home with no plan: one setup card under the hero (ghost ring drawing itself, "Set up your data plan", one big "Set up plan" button) replaces the old link and card; the button opens the new-plan flow; creating a plan removes the card; with a plan nothing of it remains; with animations off everything is static.
18. Home: one Top apps card with a week strip (tap a day: the apps re-rank with a glide), a featured first app with share-fill rows, and three connected buttons; Filters (Network, System apps) narrows strip, summary and ranking together with a badge and removable chips; History grows out of its button, shows the chosen day and returns it; the old Top apps and History cards are gone; with animations off everything is static.
19. Apps: header with the app count, search and Filters at the same height (badge when filters are active), Today / 7 days / 30 days / Cycle buttons that change the numbers at once, full-label removable chips, a summary card (total, Wi-Fi and Mobile split, "apps only"), a Most used card and share-fill rows that glide on re-rank; Filters has Network, Sort and System apps only; searching for nothing shows one message and no zero total; with animations off everything is static.
