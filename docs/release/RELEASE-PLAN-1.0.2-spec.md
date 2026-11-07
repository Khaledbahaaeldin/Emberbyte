# Emberbyte 1.0.2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship 1.0.2 as a full minor-sized update: permission-free Updates row, tab-transition fix, one coherent back and motion system, the library-based unit picker, and a navigation diagnostic that catches the stuck tap.

**Architecture:** Pure decision logic (tab routes and direction, tap landing, installer classification, back-progress math) goes in small testable functions; Compose code only wires them. The expressive library components replace hand-built ones behind unchanged public signatures. Every new animation reads the Material motion scheme and respects `LocalReduceMotion`.

**Tech Stack:** Kotlin 2.4.20, Compose BOM 2026.09.00, Material 3 `1.5.0-alpha29` (expressive), Navigation Compose 2.10.2, activity-compose 1.13.0, Robolectric 4.17 (sdk 35), JDK 21 target.

**Spec:** `docs/release/RELEASE-PLAN-1.0.2-report.md` (evidence and reasoning). This file and the report are committed on branch `release/1.0.2` only so the maintainer and Omar share one spec; Task T10 removes them in the final commit. They must never reach `main`.

## Global Constraints

- Release 1.0.2: `versionName = "1.0.2"`, `versionCode = 3`, Tuesday 10 Nov 2026. Rule: real changes only; if the audit is not clean, the date moves, quality does not.
- **No INTERNET permission.** `ReleaseApkVerificationTest` must keep asserting its absence. No new permission of any kind. `QUERY_ALL_PACKAGES` is already declared and covers the installer lookup.
- minSdk 29, targetSdk 36, compileSdk 37. API-30-only calls need an API-29 fallback.
- No hard-coded colours (`NoHardcodedColorsTest`): use `LocalEmberbytePalette`.
- No unconditional or bare `BackHandler` (`NoBareBackHandlerTest`). A `BackHandler` or `PredictiveBackHandler` is allowed only with `enabled` bound to a condition.
- Reduced motion (`LocalReduceMotion`, animator scale 0) means: no spatial animation, instant state changes, at most a 90 ms fade (`BackTransitions.REDUCED_MOTION_DURATION_MS`).
- Accessibility must not regress: 48 dp targets, `Role.Tab` on tabs, `Role.RadioButton` plus `selected` on picker segments, font scale 2 fit, RTL mirroring.
- Public signatures of `ConnectedButtonGroup`, `CompactNavBar`, `PredictiveBackSubScreen`, `GuardedBack`, `LoadingState` do not change in a breaking way (new parameters get defaults); the 8 existing `SettingsContent(` call sites in tests keep compiling.
- English strings inline like the surrounding code. Plain, calm wording; no claims the app cannot know.
- Internal documents: this plan and the report live on `release/1.0.2` only and are deleted by the last commit (T10). Diagnostics logs and the Q4 calendar are never committed.
- Commits: small, focused, message explains why, end with `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Identity `Khaledbahaaeldin` / `khaled.bahaaeldin@aiu.edu.eg`. Never push without the maintainer's confirmation.
- Gradle caches test results: a green run of about 2 minutes means the tests did not run. Real runs take 6 to 10 minutes.

## Review Focus

Input classes the spec implies that no single task's happy path exercises:

1. Tab to tab through every pair (Home, Apps, Plans, both directions) with the `popUpTo(Home)` options: never plays a pop-style or scale/slide-out transition, and the arriving tab is the tapped one. (T2)
2. A tap on a non-selected tab with 8 to 20 dp of finger drift: selects that tab. A real drag across cells still drags. (T3)
3. Installer is unknown, `null`, a package the app does not recognise, or the call throws: the Updates row falls back to the GitHub link and never crashes. (T6)
4. Reduced motion and RTL with each new animation: no motion, mirrored direction, nothing clipped. (T4, T5, T7, T8)
5. Back gesture cancelled halfway, committed with a fast flick, and started again during the settle: the screen returns to full size, no stuck scale, corners, shadow or dialog. (T5)
6. Font scale 2 on the picker (3 segments in 280 dp) and on the Updates row text. (T7, T6)

## File Structure

| File | Responsibility | Task |
|---|---|---|
| `app/build.gradle.kts`, `ReleaseApkVerificationTest.kt` | Version bump; test reads the version from the build, not literals | T0, T9 |
| `app/.../nav/NavTransitions.kt` | Tab route helpers and tab transitions (pure + specs) | T2 |
| `app/.../nav/BackTransitions.kt` | One back-motion definition: progress math, specs, predictive transitions | T4 |
| `app/.../nav/GuardedBack.kt` | Uses the same back-motion math | T4 |
| `app/.../EmberbyteApp.kt` | NavHost wiring, navbar visibility, diagnostics hooks | T2, T4, T1 |
| `app/.../nav/NavDiagnostics.kt` (new) | In-memory ring buffer of navigation events | T1 |
| `ui/design/.../navbar/NavBarLogic.kt` | `resolveTapLanding` pure function | T3 |
| `ui/design/.../navbar/CompactNavBar.kt` | Uses it; diagnostics callback; animated trailing slot | T3, T1, T5 |
| `ui/design/.../controls/ConnectedButtonGroup.kt` | Re-implemented on `ToggleButton` | T7 |
| `app/.../plans/flow/PlanFlowScreen.kt` | Predictive back between steps; directional step transition | T5 |
| `ui/design/.../states/LoadingState.kt` | `LoadingIndicator` | T8 |
| `app/.../apps`, `plans`, `history` screens | `animateItem`, icon-button shapes | T8 |
| `app/.../settings/UpdateSource.kt` (new) | Installer classification and reader | T6 |
| `app/.../settings/SettingsScreen.kt`, `SettingsGroups.kt` | Updates row, version long-press | T6, T1 |
| `CHANGELOG.md`, `fastlane/.../changelogs/3.txt`, `docs/release/RELEASE-NOTES-1.0.2.md` | Release paperwork | T9 |

## Order and branches

T0 first (T10 is always last). T1, T2, T3, T6, T7 are independent after T0 and can run in parallel branches. T4 depends on T2 (same file region). T5 depends on T4. T8 depends on T7 for style consistency only. T9 last. One branch per task, merged to `main` only with green CI and the audit done. The audit (Claude) checks every task against its own tests, this plan's Global Constraints, and the device checklist at the end.

---

### Task T0: Make local tests runnable

**Files:** `gradle.properties`, `app/build.gradle.kts` (test task), possibly the local JDK setup.

**Interfaces:** Produces a working `./gradlew :app:testDebugUnitTest --tests "*nav.NavTransitionsTest*"` on the maintainer's PC.

- [ ] Step 1: Reproduce. Run `./gradlew :app:testDebugUnitTest --tests "*nav.NavTransitionsTest*" --offline`. Currently ends with `Execution failed for task ':app:testDebugUnitTest' > Java heap space` in about 10 s, with a 3 GB and a 5 GB daemon and with `--no-build-cache`.
- [ ] Step 2: Diagnose with `--stacktrace`. Compare local JDK (17) with CI (21, `ci.yml`). Check whether a stale `app/build/test-results` or a very large test output is serialised. Do not change production code.
- [ ] Step 3: Fix the cause (JDK 21 toolchain, `maxHeapSize` on the Test task, or the culprit found). Record the cause in `docs/release/real-device-checklist.md` under a "Local build" note only if it affects other developers.
- [ ] Step 4: Run the full `:app:testDebugUnitTest` once; expect a real run of several minutes and green.

### Task T1: Navigation diagnostics (catch the stuck tap)

**Files:** Create `app/src/main/kotlin/.../nav/NavDiagnostics.kt`; modify `EmberbyteApp.kt`, `ui/design/.../navbar/CompactNavBar.kt`, `settings/SettingsScreen.kt`. Test: `app/src/test/.../nav/NavDiagnosticsTest.kt`.

**Interfaces:**
- Produces `object NavDiagnostics { fun record(kind: String, detail: String = ""); fun snapshot(): List<String>; fun clear(); const val CAPACITY = 300 }`. Thread-safe, in memory only, never written to disk, never sent anywhere.
- Produces `CompactNavBar(..., onDiagnostic: (String) -> Unit = {})` (new last parameter with default).
- Consumed by T2 (`record("TRANSITION", ...)`).

Event kinds (one line each, with a monotonic `HH:mm:ss.SSS` prefix added by `snapshot()`): `TAB_DOWN x= cell= selected=`, `TAB_DRAG_START dx=`, `TAB_UP x= cell= dragging= landing=`, `TAB_SELECT id= from=`, `NAV_REQUEST route=`, `NAV_RESULT route= stack=[home,plans]`, `TRANSITION kind=tab|push|pop from= to=`, `BACK_START`, `BACK_COMMIT`, `BACK_CANCEL`.

- [ ] Step 1: Tests in `NavDiagnosticsTest`: `keeps_only_the_newest_300_events` (record 350, snapshot size 300, first line is event 51); `snapshot_lines_are_time_stamped_and_ordered`; `clear_empties_the_buffer`; `concurrent_records_do_not_lose_the_capacity_bound` (4 threads x 200).
- [ ] Step 2: Implement `NavDiagnostics` (ArrayDeque guarded by a lock; time from `SystemClock.elapsedRealtime()` formatted relative to process start, injectable for tests via an internal `clock: () -> Long`).
- [ ] Step 3: Hook points. In `EmberbyteApp.kt`: log `NAV_REQUEST` in the tab `onSelect`; register `navController.addOnDestinationChangedListener` (remove it in `onDispose`) that logs `NAV_RESULT` with `navController.currentBackStack.value.mapNotNull { it.destination.route }`. In `CompactNavBar`: call `onDiagnostic` from the pointer handler at down, drag start, and up (cell index from `itemIndexAt`), and from the cell `onClick`. Also `android.util.Log.d("EmberNav", line)` for `adb logcat`.
- [ ] Step 4: Access. In `SettingsScreen`, a long press on the "Emberbyte <version>" title copies `NavDiagnostics.snapshot().joinToString("\n")` to the clipboard (header line: version name and code, Android version, device model) and shows the existing message mechanism: "Copied navigation log (N events)". No new screen, no permission. The log contains route names, coordinates and times only, never usage data.
- [ ] Step 5: Test `SettingsContent` with a version long press: clipboard text starts with the header and contains the recorded events (Robolectric `ClipboardManager`).
- [ ] Step 6: Commit `feat(diagnostics): keep a local navigation log the maintainer can copy`.

### Task T2: Tab transitions never behave like back

**Files:** Modify `app/.../nav/NavTransitions.kt`, `EmberbyteApp.kt`. Tests: extend `NavTransitionsTest.kt`; create `app/src/test/.../nav/TabNavigationProbeTest.kt`.

**Interfaces:**
- Produces `internal fun isTabRoute(route: String?): Boolean` (true only for the exact routes `home`, `apps`, `plans`).
- Produces `internal fun tabDirection(from: String?, to: String?): Int` (+1 when the target tab is later in `Destination.entries` order, -1 when earlier, 0 if either is not a tab or equal; flips sign in RTL at the call site).
- Produces `internal fun isTabSwitch(from: String?, to: String?): Boolean = isTabRoute(from) && isTabRoute(to) && from != to`.
- Changes `tabEnterTransition(motion, reduceMotion, direction: Int = 0)` and `tabExitTransition(...)`: with `direction != 0`, slide horizontally by 10 percent of the width in the direction of travel using `motion.defaultSpatialSpec<IntOffset>()` plus a fade from `motion.defaultEffectsSpec()` / `motion.fastEffectsSpec()`; reduced motion returns `None`.

- [ ] Step 1: Unit tests: `isTabRoute_is_true_only_for_the_three_tab_routes` (including `"plans/new"`, `"apps/{packageName}"`, `"settings"`, `null` are false); `tabDirection_follows_navbar_order` (home to apps = 1, plans to apps = -1, apps to home = -1, home to home = 0, home to settings = 0); `reduce_motion_returns_none_for_directional_transitions`.
- [ ] Step 2: Probe test `TabNavigationProbeTest` (Robolectric, `createComposeRule`, sdk 35): a `NavHost` with the production tab `NavOptions` (`popUpTo("home") { saveState = true }`, `launchSingleTop`, `restoreState`) and dummy composables `home`, `apps`, `plans`, `plans/new`, `apps/{packageName}`. Record which of the four transition lambdas ran. Assert for all six ordered tab pairs: after the transition the current route is the target, and the lambda chosen through the production selection function is the tab one. This test proves the `isPop` finding (Apps to Home currently runs the pop lambdas); it must fail before Step 3 and pass after.
- [ ] Step 3: In `EmberbyteApp.kt` wrap all four NavHost lambdas: `val tab = isTabSwitch(initialState.destination.route, targetState.destination.route)`; if `tab`, return `tabEnter/ExitTransition(motion, reduceMotion, tabDirection(...) * layoutDirectionSign)` for enter, exit, popEnter and popExit alike; otherwise keep the existing push and pop behaviour (T4 replaces it). Record `TRANSITION kind=tab|push|pop from= to=` through `NavDiagnostics` (T1).
- [ ] Step 4: Add `random_tab_sequence_always_lands_on_the_tapped_tab`: 6 seeds x 80 steps mixing tab taps, a plain `navigate("apps")` (as Home does), `plans/new` then back, `apps/<pkg>` then back, with the test clock advanced by 0 to 450 ms between steps (`mainClock.autoAdvance = false`). Assert the route after each tab tap. If it fails, keep the failing sequence in the test as a named regression and report it: that is the stuck-tap reproduction.
- [ ] Step 5: Commit `fix(nav): switching tabs no longer plays the back animation`.

### Task T3: A tap with finger drift still selects the tab

**Files:** `ui/design/.../navbar/NavBarLogic.kt`, `CompactNavBar.kt`. Tests: `NavBarLogicTest.kt` (new or extended), `CompactNavBarTest.kt`.

**Interfaces:**
- Produces `internal fun resolveTapLanding(startX: Float, endX: Float, totalDragPx: Float, flickPxPerSec: Float, selectedIndex: Int, starts: List<Float>, ends: List<Float>, tapSlopPx: Float, flickThresholdPx: Float): Int?` returning the index to select for a gesture that began as a touch, or `null` when the gesture is a real drag and the existing landing logic applies. Rule: if `abs(totalDragPx) <= tapSlopPx` and `abs(flickPxPerSec) < flickThresholdPx`, return `itemIndexAt(endX, starts, ends)`. `tapSlopPx` = 20 dp in pixels (constant `NavBarSpring.TAP_SLOP_DP = 20`).

- [ ] Step 1: Tests: `drift_of_12dp_ending_on_apps_while_plans_is_selected_returns_apps`; `drift_over_the_slop_returns_null`; `a_fast_flick_returns_null_even_when_short`; `drift_ending_on_the_selected_tab_returns_that_tab` (caller treats it as no change).
- [ ] Step 2: In the pointer handler, when `dragging` was set only because of drift (distance within `TAP_SLOP_DP`), call `resolveTapLanding`; a non-null result is routed through `launchTargetTransition` and `currentOnSelect` exactly like a tap (haptic once). A real drag keeps the current behaviour. Emit `TAB_UP landing=` through `onDiagnostic`.
- [ ] Step 3: Compose test `slightly_slidey_tap_on_another_tab_selects_it`: down on Apps, move 10 dp, up; assert `onSelect("apps")` called once. Existing `dragging_from_item_0_to_item_2_selects_item_2` and `haptic_fires_once_per_landing_not_per_frame` still pass.
- [ ] Step 4: Commit `fix(navbar): a tap with a little finger movement selects the tab it ends on`.

### Task T4: One back-motion definition (push, pop, predictive, forms)

**Files:** `nav/BackTransitions.kt`, `nav/GuardedBack.kt`, `nav/NavTransitions.kt`, `EmberbyteApp.kt`. Tests: `BackTransitionsTest.kt`, `GuardedBackTest.kt`, `NavTransitionsTest.kt`.

**Interfaces:**
- `BackTransitions` keeps `SHRINK`, `CORNER_DP`, `SCRIM_START_ALPHA`, `REDUCED_MOTION_DURATION_MS`, `cornerRadiusFor`, `transformOriginFor`, `slideSignFor`; adds:
  - `const val SLIDE_FRACTION = 0.08f` (of the screen width) replacing `SLIDE_DP * 3` (fixes the pixels-for-dp bug; remove `SLIDE_DP`).
  - `fun shrinkFor(progress: Float): Float = 1f - (1f - SHRINK) * progress.coerceIn(0f, 1f)`.
  - `fun shadowDpFor(progress: Float): Dp` (8 dp at 25 percent progress and above, linear below).
  - `fun predictivePopExit(direction): AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> ExitTransition` and `predictivePopEnter(...)`: `scaleOut(SHRINK, origin by edge)` + `slideOutHorizontally { (it * SLIDE_FRACTION * sign).roundToInt() }` + `fadeOut` starting at 85 percent; all with `tween(durationMillis = 300, easing = LinearEasing)` so the animation follows the finger one to one.
  - `fun pushEnter(motion, direction)` / `pushExit(...)` and `popEnter(motion, direction)` / `popExit(...)` for committed (non-gesture) navigation: the same family, using `motion.defaultSpatialSpec()` and `motion.defaultEffectsSpec()`, entering with a scale from 0.94 and a fade, leaving with the reverse. Push and pop must be mirror images.
- `PredictiveBackSubScreen` takes its corner radius and shadow from the same `transition` progress through `animateFloat` with `tween(300, LinearEasing)` and `cornerRadiusFor` / `shadowDpFor`, so the gesture drives them; the opaque `palette.background` stays.
- `GuardedBack` uses `shrinkFor`, `cornerRadiusFor` and `shadowDpFor` instead of its own formulas.
- Consumes T2's `isTabSwitch` (tab pairs keep the tab transition) and T1's `NavDiagnostics`.

- [ ] Step 1: Tests: `slide_is_a_fraction_of_the_width_not_a_fixed_pixel_count` (call the slide lambda with widths 1080 and 2160, expect 86 and 173); `shrinkFor_is_linear_between_one_and_090`; `shadowDpFor_reaches_8dp_at_a_quarter_of_the_gesture`; `predictive_transitions_use_a_linear_easing` (inspect the spec via the transition's `toString` is not allowed: instead expose `internal val PREDICTIVE_EASING = LinearEasing` and assert the constant is used); `push_and_pop_use_the_same_scale_constants`; `reduced_motion_pop_and_push_are_the_90ms_fade`; extend `GuardedBackTest` with `guarded_back_uses_the_shared_shrink_math`.
- [ ] Step 2: Implement the functions above; delete `SLIDE_DP`; keep the object public API otherwise.
- [ ] Step 3: Wire `NavHost(... predictivePopEnterTransition = ..., predictivePopExitTransition = ...)` (the 2.10.2 overload with the `Function2` parameters); non-tab `enterTransition`/`exitTransition` use `pushEnter/pushExit`; non-tab `popEnter/popExit` use the committed pop family. Tab pairs go through T2's wrapper first.
- [ ] Step 4: Navbar: keep `CompactNavBar` always composed in `AppScaffold` and pass `visible = visibility.visible && isTopLevel`, so its own `AnimatedVisibility` animates in and out; reduced motion keeps the instant branch. Update `CompactNavBarTest.hidden_when_not_visible` if needed and add `navbar_slides_out_when_a_sub_screen_opens` (visible false removes the node after the animation).
- [ ] Step 5: Record `BACK_START`, `BACK_COMMIT`, `BACK_CANCEL` (T1) from `GuardedBack` and the plan-flow handler (T5).
- [ ] Step 6: Commit `feat(nav): one back and push motion family, driven by the gesture`.

### Task T5: Plan flow and forms follow the same back motion

**Files:** `plans/flow/PlanFlowScreen.kt`, `plans/PlanEditorScreen.kt` (only if it has its own animation), tests `PlanFlowScreenTest.kt`, `NoBareBackHandlerTest.kt`.

**Interfaces:** Consumes `BackTransitions.shrinkFor`, `PlanFlowEvent.PreviousStep`. Produces `internal fun stepDirection(from: Int, to: Int, rtl: Boolean): Int` (+1 forward, -1 backward, mirrored in RTL).

- [ ] Step 1: Tests: `stepDirection_is_forward_for_increasing_steps_and_mirrors_in_rtl`; `back_on_step_2_returns_to_step_1_and_keeps_the_entered_data`; `a_cancelled_back_gesture_leaves_the_step_unchanged` (use the existing PlanFlow test harness; drive `PredictiveBackHandler` via `OnBackPressedDispatcher.dispatchOnBackStarted/Progressed/Cancelled`); `no_bare_back_handler` still passes.
- [ ] Step 2: Replace `BackHandler(enabled = state.step > 1)` with `PredictiveBackHandler(enabled = state.step > 1)`: while the gesture runs, the step content scales with `shrinkFor(progress)` and rounds its corners with `cornerRadiusFor(progress)` (same origin as `GuardedBack`); completion calls `onEvent(PreviousStep)`; cancellation resets to scale 1 with `BackTransitions.cancelSpring`.
- [ ] Step 3: Replace `fadeIn() togetherWith fadeOut()` in the step `AnimatedContent` with a directional slide (`slideInHorizontally` and `slideOutHorizontally`, one third of the width, `motion.defaultSpatialSpec()`) plus `motion.defaultEffectsSpec()` fades, direction from `stepDirection(initialState, targetState, rtl)`, and `SizeTransform(clip = false)`. Reduced motion keeps the existing `when` branch.
- [ ] Step 4: Commit `feat(plans): step back follows the finger and steps slide in their direction`.

### Task T6: Updates row (no network)

**Files:** Create `app/.../settings/UpdateSource.kt`; modify `SettingsScreen.kt`, `SettingsGroups.kt`. Tests: `UpdateSourceTest.kt` (new), `SettingsContentTest.kt`/`SettingsScreenTest.kt`.

**Interfaces:**
- Produces `enum class InstallerKind { Obtainium, OrionStore, FDroid, Other, Unknown }`.
- Produces `internal fun classifyInstaller(packageName: String?): InstallerKind`: `null` or empty gives `Unknown`; `dev.imranr.obtainium` and `dev.imranr.obtainium.fdroid` give `Obtainium`; `com.orion.store` gives `OrionStore` (read from Orion's `build.gradle.kts`, `applicationId`); `org.fdroid.fdroid`, `org.fdroid.basic` give `FDroid`; any other non-empty package gives `Other`.
- Produces `class InstallSourceReader(private val context: Context) { fun kind(): InstallerKind }` using `getInstallSourceInfo(packageName).installingPackageName` on API 30+ and `getInstallerPackageName(packageName)` below; any exception returns `Unknown`.
- Produces `data class UpdatesUiState(val kind: InstallerKind, val versionName: String)` and `@Composable fun UpdatesRow(state: UpdatesUiState, onOpenUpdater: () -> Unit, onOpenReleases: () -> Unit, modifier: Modifier = Modifier)` built on `SettingsRow` (icon vector `Icons.Rounded.SystemUpdate`, `AppShape.Squircle`, chevron).
- `SettingsContent(... updatesState: UpdatesUiState = UpdatesUiState(InstallerKind.Unknown, versionName), onOpenUpdater: () -> Unit = {}, onOpenReleases: () -> Unit = {})` (defaults keep the 8 test call sites compiling).
- Constant `RELEASES_URL = "https://github.com/Khaledbahaaeldin/Emberbyte/releases/latest"` and the Obtainium deep link already in the README.

Row text:

| Kind | Title | Subtitle | Tap |
|---|---|---|---|
| Obtainium | Updates | Obtainium keeps Emberbyte up to date | Open Obtainium (launch intent), else open `RELEASES_URL` |
| OrionStore | Updates | Orion Store keeps Emberbyte up to date | Open Orion (launch intent), else `RELEASES_URL` |
| FDroid | Updates | F-Droid keeps Emberbyte up to date | Open F-Droid (launch intent), else `RELEASES_URL` |
| Other, Unknown | Updates | You have <versionName>. Tap to see the latest release on GitHub. Emberbyte never goes online by itself, so it cannot tell you when one appears. | Open `RELEASES_URL` in the browser |

- [ ] Step 1: `UpdateSourceTest`: `classifies_the_known_updaters`; `null_and_empty_are_unknown`; `an_unrecognised_package_is_other`; `reader_falls_back_when_the_lookup_throws` (a fake `PackageManager` through Robolectric shadow); `reader_uses_the_api_29_call_below_30` (`@Config(sdk = [29])` and `[35]`).
- [ ] Step 2: UI tests: `unknown_installer_shows_the_github_wording_and_opens_releases`; `obtainium_installer_shows_its_wording`; `row_is_at_least_48dp_and_has_a_click_label`; `row_fits_at_font_scale_2`; `row_mirrors_in_rtl`.
- [ ] Step 3: Place the row at the top of the About group content (above the version line pills), wire `InstallSourceReader` in `SettingsScreen` with `remember(context)`, launch intents via `packageManager.getLaunchIntentForPackage(...)` (visibility is covered by `QUERY_ALL_PACKAGES`), URL opening through the existing `LocalUriHandler` call with `runCatching`.
- [ ] Step 4: Add a release gate check: `ReleaseApkVerificationTest` keeps `Release APK must NOT declare INTERNET permission`; add `updates_row_needs_no_extra_permission` in a manifest test that lists the allowed permissions (the current 9) and fails on any addition.
- [ ] Step 5: Commit `feat(settings): add an Updates row that points to the right updater without going online`.

### Task T7: Unit picker on the library's ToggleButton

**Files:** `ui/design/.../controls/ConnectedButtonGroup.kt`. Tests: `ConnectedButtonGroupTest.kt`, `SettingsScreenTest.kt` (existing).

**Interfaces:** Same public signature: `ConnectedButtonGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)`. All 9 call sites (`PlanFlowSteps` x3, `FiltersSheet` x3, `HistoryHeader`, `AppearanceScreen`, `SettingsScreen`) compile unchanged.

- [ ] Step 1: Keep the six existing tests (selected state and check icon with tag `connected_button_group_check_icon`, click calls `onSelect(index)`, 48 dp height, 3 segments in 280 dp at font scale 2, RTL order, disabled ignores clicks). Add: `selecting_another_segment_animates_the_shape` (read the segment's shape at two frames with the test clock paused: the shape at the midpoint differs from both the start and end shape); `reduced_motion_changes_shape_instantly`.
- [ ] Step 2: Implement with `Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween), modifier = modifier.selectableGroup())` and one `ToggleButton(checked = isSelected, onCheckedChange = { onSelect(index) }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes() | connectedMiddleButtonShapes() | connectedTrailingButtonShapes(), colors = ToggleButtonDefaults.toggleButtonColors(...) mapped to the palette: container `surface2`, content `muted`, checked container `primary`, checked content `onPrimary`; disabled alpha 0.38 as today, `modifier = Modifier.weight(1f).semantics { role = Role.RadioButton; selected = isSelected }.defaultMinSize(minHeight = 48.dp)`)`. The label stays inside `ForceLtr`. The check icon appears with `AnimatedVisibility(visible = isSelected, enter = fadeIn + expandHorizontally, exit = fadeOut + shrinkHorizontally)` using the motion scheme; it keeps the test tag. One option gives a fully rounded single segment. Verify the exact alpha29 signatures in the jar before coding (`ToggleButton`, `ToggleButtonDefaults`, `ButtonGroupDefaults.connected*ButtonShapes`); they are `@ExperimentalMaterial3ExpressiveApi`.
- [ ] Step 3: Run the whole `:ui:design` tests and `SettingsScreenTest`, `PlanFlowScreenTest`, `FiltersSheetTest`, `HistoryScreenTest`, `AppearanceScreenTest` (the picker is used in all).
- [ ] Step 4: Commit `feat(design): the unit picker morphs between shapes using the library toggle button`.

### Task T8: Motion across the app

**Files:** `ui/design/.../states/LoadingState.kt`, `ui/design/.../navbar/CompactNavBar.kt` and `PlusButton.kt`, `apps/AppsScreen.kt`, `plans/PlansScreen.kt`, `history/History.kt`, `home/HomeScreen.kt`, icon-button call sites (`common/ScreenHeader.kt`, `apps/AppDetail.kt`, `plans/PlansBento.kt`, `plans/PlanEditorScreen.kt`, `home/HomeSections.kt`), `settings/AppearanceScreen.kt`. Tests next to each.

**Interfaces:**
- Produces `ui/design/.../motion/MotionSpecs.kt`: `@Composable fun rememberEmberbyteMotion(): EmberbyteMotion` with `spatial<T>()`, `effects<T>()`, `fastSpatial<T>()`, `fastEffects<T>()`, each returning `snap()` (or `tween(90)` for effects) when `LocalReduceMotion` is true, otherwise the matching `MaterialTheme.motionScheme` spec. All new animations in this task use it.
- Produces `ui/design/.../shapes/MorphShape.kt`: `fun morphShape(morph: Morph, progress: Float): Shape` (a `Shape` that draws `morph.toPath(progress)` scaled to the size; `graphics-shapes` is already on the classpath through the library).

Work items (each is its own commit):

- [ ] **8a. Lists animate.** In `AppsScreen`, `PlansScreen` (active and archived), `HistoryScreen` rows and `HomeScreen` prompts: stable `key` on every `items(...)` and `Modifier.animateItem(fadeInSpec = effects(), placementSpec = spatial(), fadeOutSpec = effects())`. Test: `removing_an_item_keeps_the_others_and_does_not_crash` (state change in a Compose test) and `reduced_motion_lists_use_snap_specs`.
- [ ] **8b. Plans "+" button.** In `CompactNavBar` the `trailing` slot is wrapped in `AnimatedVisibility(visible, enter = scaleIn + fadeIn + expandHorizontally, exit = reverse)` and the bar offset follows `animateDpAsState` (motion `spatial()`), so the bar slides left smoothly instead of jumping 34 dp. The last non-null `trailing` content is remembered so the exit animation can run. Existing test `trailing_plus_button_sits_to_the_right_and_is_56dp` keeps passing once settled; add `bar_does_not_jump_when_the_trailing_button_appears` (the bar's x moves across at least 3 frames).
- [ ] **8c. Loading.** `LoadingState` uses `ContainedLoadingIndicator`/`LoadingIndicator` with `palette.primary` and `palette.surface2`, keeps the static icon for reduced motion and the `contentDescription = label` semantics. Test: `StatesEverywhereTest` still passes; add `loading_state_has_the_label_for_talkback`.
- [ ] **8d. Press morph on icon buttons.** The 8 `IconButton` call sites use `shapes = IconButtonDefaults.shapes()` (pressed shape morph) with colours from the palette; tap target stays 48 dp. Test: `icon_buttons_keep_48dp_and_their_content_description`.
- [ ] **8e. Theme card morph.** In `AppearanceScreen.ThemeCard` the 46 dp swatch animates between `MaterialShapes.Circle` (unselected) and `MaterialShapes.Cookie9Sided` (selected) with `morphShape(Morph(circle, cookie), progress)` where `progress` comes from `animateFloatAsState(if (isSelected) 1f else 0f, spatial())`; selected border and check stay. Test: `selected_theme_swatch_has_a_different_shape_than_unselected` (semantic key or tag reading the progress) and the existing `ThemeGalleryTest`, `AppearanceScreenTest`.
- [ ] **8f. Consistency sweep.** Replace the remaining ad-hoc specs found by `grep -rn "tween(\|spring(" app/src/main ui/design/src/main` outside `BackTransitions.kt`/`PillSpring.kt`/`CompactNavBar.kt` (for example `WavyBar`'s `animateFloatAsState`, `MorphingNumber`) with `rememberEmberbyteMotion()` specs. The navbar springs and the number morph keep their own physics; only constants move.
- [ ] Final step: run `ThemeSweepTest`, `StatesEverywhereTest`, the accessibility tests and `NoHardcodedColorsTest`.

### Task T9: Release 1.0.2

**Files:** `app/build.gradle.kts`, `ReleaseApkVerificationTest.kt`, `CHANGELOG.md`, `fastlane/metadata/android/en-US/changelogs/3.txt`, `docs/release/RELEASE-NOTES-1.0.2.md`, `README.md` (only if a feature line changes).

- [ ] Step 1: `ReleaseApkVerificationTest`: replace the literals `"1.0.1"` and `2` with the build's values (`BuildConfig.VERSION_NAME`, `BuildConfig.VERSION_CODE`, or `-P` properties passed into the test task), and add `version_name_matches_the_newest_changelog_entry`.
- [ ] Step 2: Bump `versionName = "1.0.2"`, `versionCode = 3`.
- [ ] Step 3: `CHANGELOG.md` entry dated the release day: fixed (tab switching plays the back animation; a slightly slidey tap on a tab is no longer ignored), changed (one back and push motion, unit picker morphs, lists and loading animate), added (Updates row, local navigation log).
- [ ] Step 4: `3.txt` (what F-Droid shows, under 500 characters) and `RELEASE-NOTES-1.0.2.md` (include the certificate fingerprint, the known limits, and the sentence that the app still has no INTERNET permission).
- [ ] Step 5: Follow the per-release checklist in `RELEASE-PLAN-2026-Q4.md` (tag on `main`, release workflow, draft, fingerprint check, publish, check website, Obtainium and Orion, install over 1.0.1).

### Task T10: Clean-up commit (must be the LAST commit on the branch)

These planning documents are on the branch only so the maintainer and Omar can work from the same spec. They are internal and must not reach `main`.

**Files:** Delete `docs/release/RELEASE-PLAN-1.0.2-spec.md` and `docs/release/RELEASE-PLAN-1.0.2-report.md`.

- [ ] Step 1: Only after T0 to T9 are merged into the branch, audited and green: `git rm docs/release/RELEASE-PLAN-1.0.2-spec.md docs/release/RELEASE-PLAN-1.0.2-report.md`.
- [ ] Step 2: Confirm nothing else internal is tracked: `git ls-files docs/release | grep RELEASE-PLAN` prints nothing (the Q4 calendar `RELEASE-PLAN-2026-Q4.md` was never committed).
- [ ] Step 3: Commit `chore: remove the internal 1.0.2 planning documents`. No other change in this commit, and no commit after it.
- [ ] Step 4: Merge to `main` with a **squash merge**, so `main`'s history never contains the two documents. A regular merge would keep them in history even though the last commit deletes them.

---

## Device checklist (real phone, 1.0.1 APK first for comparison)

1. Home to Apps to Plans to Apps to Home by tapping, 20 times, fast and slow: always lands on the tapped tab, never a back-style animation.
2. Same with slightly sliding taps; then a real drag across tabs.
3. Open a plan, an app detail, Settings, Appearance, Licences: push and the back gesture (full swipe, half swipe cancelled, fast flick, both edges). Everything returns to full size; the navbar slides back in with the screen.
4. New plan: back from step 3 to step 2 to step 1 with the gesture; steps slide the right way; data kept; leave from step 1 with data shows "Discard plan?".
5. MB, GB, TB (and the other eight pickers): the shape morphs on tap, on press, check icon animates; TalkBack reads "selected".
6. Settings: the Updates row text matches how the APK was installed (Obtainium, Orion, browser); long-press the version, paste the log into notes, it has events.
7. Animations off in developer options: everything is instant and nothing is stuck. RTL (Arabic) and font scale 2 once each.
8. After the release: install 1.0.2 over 1.0.1, data intact.

## Self-review

- Coverage of the maintainer's list: tab-transition fix (T2), back rework (T4, T5), motion work (T8), picker swap (T7), Updates row (T6), logging for the stuck tap (T1, plus T3 as the defensive fix for the leading hypothesis). Release paperwork (T9), test environment (T0).
- Known open point: the stuck tap is not reproduced. T1 and the random-sequence test in T2 are how it gets caught; if either shows a failing sequence, add a task before release.
- Risks: alpha library signatures (verify in the jar before each task that touches `ToggleButton`, `LoadingIndicator`, `IconButtonDefaults.shapes`); a large update in three days (the audit gates the release, not the calendar); `NavHost` seek behaviour with `predictivePop*` can only be judged on a device.
