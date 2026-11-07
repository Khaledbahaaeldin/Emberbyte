# 1.0.2 investigation report: update notice, back animation, motion

Written 2026-11-07 for the release on Tuesday 10 Nov. Local only (the `RELEASE-PLAN-*.md` name is git-excluded). No product code was changed.

How to read the evidence labels:

- **PROVEN**: read directly in the source, or in the compiled bytecode of the pinned libraries.
- **LIKELY**: follows from proven facts, but I did not see it run.
- **OPEN**: cannot be settled without the phone or a reproduction.

## Summary

| # | Request | Finding | Status |
|---|---|---|---|
| 1 | Update notice | The app has no INTERNET permission by design, and the release gate fails if it is added. A GitHub check is a product decision, not a task. A permission-free alternative exists. | PROVEN |
| 2a | Back animation looks wrong | Three separate implementations of "back", none driven properly by the gesture. | PROVEN (causes), LIKELY (visual effect) |
| 2b | Apps → Home plays a back animation | Cause found: the tab tap is a pop plus a no-op push, so Navigation's `isPop` flag stays true. | PROVEN (flag), LIKELY (behaviour) |
| 2c | Plans → Apps sometimes ignores the tap | Not reproduced. Two ranked hypotheses and a plan to catch it. | OPEN |
| 3 | UI feels stiff, shapes do not morph | The MB/GB/TB picker is hand-built and swaps its shape instantly. One component serves 9 places. The pinned library already has the expressive replacement. | PROVEN |

One blocker for working on this: local `testDebugUnitTest` now fails with `Java heap space` on this PC, even for an existing trivial test (`NavTransitionsTest`) and with a 5 GB daemon. It is not caused by the code under investigation. Fix it before the work starts (see the end).

---

## 1. Update notification

### What blocks the obvious design

- The manifest declares no INTERNET permission, and that is the headline promise: README, website, posters, emails, F-Droid and Orion texts.
- `ReleaseApkVerificationTest` fails the build if the release APK declares INTERNET (lines about 171 to 174).
- Looking for a new GitHub release needs the network. There is no way to do it without the permission.

So "notify whenever a new release appears" costs the promise. That is your decision, not a coding detail.

### Options

| Option | Notification when a release exists? | Cost | Verdict |
|---|---|---|---|
| A. Add INTERNET and check GitHub (opt-in) | Yes | Breaks the headline claim. Manifest, release gate, README, website, posters, F-Droid and Orion texts and the review emails all change. Every outreach message already sent becomes wrong. | Not recommended |
| B. Two builds: a "github" flavour with the check, a "store" flavour without | Only in one build | The promise becomes "depends on the build", which is weaker than "none". Two APKs, more CI. | Not recommended |
| C. Permission-free **Updates** row in Settings (see below) | No push, but the user is pointed to the right updater | Small. No permission. | **Recommended for 1.0.2** |
| D. Local reminder ("check for updates" once a month, opt-in) | A reminder, not a real notice | Small. Nags even when there is nothing new. | Optional add-on to C |

Obtainium and Orion Store already poll GitHub releases and notify their users, so most of the people who need a push already get one.

### Option C in detail (all permission-free)

- Show the installed version (`BuildConfig.VERSION_NAME`).
- Detect who installed the app: `PackageManager.getInstallSourceInfo(packageName).installingPackageName` on API 30 and later, `getInstallerPackageName` on API 29 (minSdk is 29).
  - Installed by Obtainium, Orion Store or F-Droid: show "Updates come from <that app>". Orion's package id has to be read from its manifest before it goes in the code.
  - Anything else (browser, file manager, adb): show a **Check for a new version** row that opens `https://github.com/Khaledbahaaeldin/Emberbyte/releases/latest` in the browser, plus a one-line tip about Obtainium with the deep link already in the README.
- `QUERY_ALL_PACKAGES` is already granted, so this needs no new permission or `<queries>` entry.
- Be honest in the text: the app cannot know whether a release exists, because it never goes online.

### If you decide to accept INTERNET (option A), the design

Off by default; one `GET https://api.github.com/repos/Khaledbahaaeldin/Emberbyte/releases/latest` every 24 hours through WorkManager (already a dependency), ETag cached, no identifiers, a separate notification channel, a version compare, then the same row as C. The unauthenticated API limit is 60 requests per hour per IP, which is fine at one per day. The work is the claims clean-up, not the code.

---

## 2. Back animation

### 2a. Why the back animation looks wrong (PROVEN unless noted)

The app has **three independent implementations of "what back looks like"**, and none of them follows the finger properly.

1. **NavHost pop transitions** (`nav/BackTransitions.kt`, wired in `EmberbyteApp.kt`): scale to 0.90, a small slide, and a fade. They are `tween(300, FastOutSlowInEasing)`.
   - Navigation Compose 2.10.2 has dedicated `predictivePopEnterTransition` and `predictivePopExitTransition` parameters (I found them in the compiled `NavHost`). The app passes neither, so the gesture is driven by the 300 ms tween specs.
   - LIKELY: the gesture progress is mapped onto the tween's easing curve, so the screen moves faster than the finger at the start. The fade only begins at 220 ms of 300 ms, so the screen stays fully opaque until about three quarters of the swipe, then vanishes.
2. **`PredictiveBackSubScreen`** adds its own corner radius and shadow from a separate 300 ms `animateFloat` keyed to the NavHost transition state. LIKELY: it is timed, not gesture-driven, so it lags or jumps relative to the scale.
3. **`GuardedBack`** (dirty forms) re-creates the shrink by hand with a `PredictiveBackHandler`.

Other findings:

- **Unit bug:** `popExit` uses `targetOffsetX = { SLIDE_DP * 3 * sign }`. That lambda returns **pixels**, so the slide is 84 px (about 28 dp on a phone), not the intended distance. Visually minor, but it is wrong.
- **Push and pop are different families:** entering a sub-screen uses the tab cross-fade (`enterTransition`), leaving it uses scale and slide. The two do not read as one motion.
- **The navbar is conditionally composed** (`if (isTopLevel)` in `EmberbyteApp.kt`). It disappears instantly when you push and slides in only after the pop commits, so its own `AnimatedVisibility` exit never runs. LIKELY: this is a visible second animation after back.
- **Plan flow steps** use `BackHandler(enabled = state.step > 1)`: no gesture preview at all when you go back a step. The step change itself is a plain `fadeIn() togetherWith fadeOut()` with default specs, not the motion scheme.

Direction (not implemented): one transition family from the motion scheme for both push and pop, pass the `predictivePop*` parameters, drive corner radius and shadow from the gesture progress instead of a timer, delete the hand-made shrink in `GuardedBack` in favour of the same source, and keep the navbar composed with its own animated visibility.

### 2b. Apps → Home plays the back animation (cause found)

The mechanism, from the compiled `ComposeNavigator`:

- It keeps an `isPop` flag. `popBackStack()` sets it to **true**; `navigate()` sets it to **false**. The NavHost picks `popEnter/popExit` whenever the flag is true.
- A tab tap runs `navigate(route) { popUpTo(Home) { saveState = true }; launchSingleTop = true; restoreState = true }`.
- For the **Home** tab: `popUpTo(Home)` pops Apps (flag becomes true). Then `launchSingleTop` finds Home already on top, so no new entry is pushed and `navigate()` is never called on the navigator. The flag stays **true**, so Apps → Home animates as a back.
- For Apps → Plans there is a real push after the pop, so the flag is reset to false and the tab transition plays. That matches what you see: only the trip *to Home* looks like a back.

The same options are used by `openDeepLink` for tab links, so widgets and `emberbyte://home` links behave the same way.

Confidence: the flag behaviour is PROVEN from bytecode; the visible result is LIKELY. I wrote a throwaway Robolectric test to confirm it, but local tests fail with the heap error (see the end).

Direction: decide the transition by the two routes involved. If both the leaving and the arriving route are top-level tabs, always use the tab transition, whatever `isPop` says. A directional slide by tab order (Home, Apps, Plans) would also feel better than a plain fade.

### 2c. Plans → Apps sometimes ignores the tap (not reproduced)

I could not find a deterministic cause by reading. Ranked hypotheses:

1. **A tap that becomes a drag (LIKELY for the intermittent part).** `CompactNavBar` has a parent pointer handler. Once the finger moves more than the touch slop it treats the gesture as a drag and consumes it, so the cell's own click never fires. At the end it picks a landing tab from the pill position and velocity, which for a short slide is still the current tab, so nothing navigates and nothing tells you why. A tap with a little finger movement on a non-selected tab is swallowed.
2. **Rapid taps during a transition (OPEN).** The pop animation lasts 300 ms and the pill spring bounces for longer (damping 0.58). Navigation Compose has had cases where a navigation issued during a running transition ends up on the wrong entry. I have no evidence for or against it here.

Why Plans is special: it is the only tab with a trailing `PlusButton`. The bar row is centred, so on Plans the whole bar jumps left by about 34 dp, with no animation. It does not change hit areas, but it is a visible glitch and part of the picture.

"Unless I go to Home first" suggests something that persists, which hypothesis 1 does not explain on its own. To settle it I need the exact sequence from you when it happens (what you did before: opened Settings, a plan editor, used the + button, scrolled the list?) and whether the pill moved. I would also add a temporary log in the tab click handler and the destination listener, and a random-sequence navigation test that replays the exact tab options. I wrote that test; it needs working local tests.

---

## 3. Motion and the Material 3 Expressive library

### Where things stand (PROVEN)

- `MaterialExpressiveTheme` with `MotionScheme.expressive()` is set at the root, so stock components animate by themselves.
- The app's own components do not. Animated containers (`AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `Crossfade`, shared transitions, `animateItem`) appear in only **5 files**: `BackTransitions.kt`, `PlanFlowScreen.kt`, `LicencesScreen.kt`, `CompactNavBar.kt`, `MorphingNumber.kt`. `animateItem`, `animateContentSize` and shared transitions appear **nowhere**.
- No expressive library component is used anywhere (no `ButtonGroup`, `ToggleButton`, `LoadingIndicator`, wavy indicators, floating toolbar).

### The MB/GB/TB picker

`ui/design/controls/ConnectedButtonGroup.kt` is hand-built. The shape is chosen from `isSelected` on every frame (a `RoundedCornerShape` swap), the colour is passed straight to `Surface`, and the check icon is added and removed from a `Row`. There is no `animate*AsState` anywhere in it, so selection simply jumps. That is exactly the "it just becomes" you see.

It is used in **9 places**: the plan flow steps (cap unit, cycle kind, entered unit), Filters sheet (3), History header, Appearance and Settings. Replacing this one component upgrades all of them.

The library already has the real thing. In the pinned `material3:1.5.0-alpha29` I confirmed `ButtonGroup`, `ToggleButton` with `ToggleButtonShapes`, and `ButtonGroupDefaults.connectedLeadingButtonShapes / MiddleButtonShapes / TrailingButtonShapes`. `ToggleButton` animates between its resting, pressed and checked shapes through an internal animated-shape state (`rememberAnimatedShape` is in the jar), so the morph comes for free.

### What the library gives us (verified present in alpha29)

| Library piece | Replaces or adds | Notes |
|---|---|---|
| `ButtonGroup` + `ToggleButton` with connected shapes | `ConnectedButtonGroup` (9 uses) | Highest value, lowest risk |
| `LoadingIndicator` / `ContainedLoadingIndicator` | `LoadingState` (plain circular indicator) | Morphing polygons, the expressive loading look |
| `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` | Hand-drawn `WavyBar` (123 lines) and `WavyRing` (222 lines) | Evaluate first: the budget ring may need custom behaviour |
| `IconButtonDefaults` shapes, `IconToggleButtonShapes` | Plain icon buttons | Press-morph on every icon button |
| `ButtonDefaults` shapes | Buttons | Press-morph |
| `SplitButton` | A possible "Add plan" menu | Optional |
| `HorizontalFloatingToolbar`, `FloatingActionButtonMenu` | The separate `PlusButton` that shifts the bar | Would remove the 34 dp jump; moderate work |
| `ListItem` shapes, `MenuGroupShapes` / `MenuItemShapes` | Hand-built settings rows and menus | Later |
| `carousel` package | Theme gallery or onboarding | Nice for the 12 themes |
| `SwipeToDismissBox` | Swipe to delete a plan | Needs an undo, so a feature |
| `MaterialShapes` + graphics-shapes `Morph` (library present) | Your example: morph the app tile shapes on press or selection | `Morph(start, end).toPath(progress)` |
| `ShortNavigationBar` | Standard navbar | **Keep your spring pill**: it is the signature piece and better than the stock bar |

Also from Compose itself, no extra library: `Modifier.animateItem()` for lists (add, remove, reorder), `animateContentSize`, `SharedTransitionLayout` for a Material container transform from an Apps row to App detail, and `motion.fastSpatialSpec()` / `defaultEffectsSpec()` in every custom animation so everything shares one motion language.

Cautions: these APIs are `@ExperimentalMaterial3ExpressiveApi` and the library is an alpha, so signatures can move between alphas. Keep the version pinned and upgrade deliberately.

---

## 4. Suggested split

| When | Items |
|---|---|
| **1.0.2 (10 Nov)** | 2b tab transitions (small, proven cause), swap `ConnectedButtonGroup` for `ButtonGroup`/`ToggleButton` (one component, 9 places), Updates row without INTERNET (option C), animate the Plans `PlusButton` appearance, the pixel/dp bug, plus the 2c log and test so it is caught |
| **1.1.0 (1 Dec)** | Rework predictive back (one family, `predictivePop*`, gesture-driven corners), `animateItem` and step transitions on the motion scheme, `LoadingIndicator`, icon and button press morphs, shared transition into App detail, maybe the floating toolbar |

Each item needs a check before it ships: a unit test that picks the tab transition from the two routes; a Compose test that the selected `ToggleButton` has the checked shape; a real-phone run of the tab sequence and of the back gesture with the 1.0.1 APK for comparison.

## Decisions needed from you

1. Update notice: option C (recommended), or accept INTERNET (option A) with the claims clean-up?
2. Confirm the 1.0.2 scope above, or move items between 1.0.2 and 1.1.0.
3. For 2c: the exact steps before it happened, if you can remember them.

## Blocker: local tests

`./gradlew :app:testDebugUnitTest --tests "*nav.NavTransitionsTest*"` ends with `Java heap space`. I tried a daemon restart, 5 GB and `--no-build-cache`. The cause is not found. Local JDK is 17 while CI and F-Droid use 21, which may matter. It needs diagnosing before any of the above can be verified locally.
