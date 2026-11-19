# Emberbyte 1.0.2 UI audit: findings, reproduction and solutions

**Status:** working document on the `release/1.0.3` branch only. It is removed by the final cleanup commit and never reaches `main`.
**Build audited:** 1.0.2 (versionCode 3), debug-signed copy of the release build, Android 17 emulator (1080 x 2400, 420 dpi, so 1 dp = 2.625 px).
**Method:** six reviewers in two rounds drove the app on the emulator, recorded transitions, measured pixels with Python/PIL, and traced every finding to source. Contrast numbers were measured from screenshots and recomputed from `GeneratedPalettes.kt`; they agree to two decimals.
**Companion document:** `RELEASE-PLAN-1.0.3-spec.md` turns every finding below into a task with files, signatures, tests and acceptance criteria.

---

## How to read this document

Each finding has the same parts:

- **ID and severity.** P0 = unreadable or broken for many users. P1 = clearly unpolished or wrong. P2 = nice to fix. P3 = idea.
- **What is wrong** in plain words.
- **Reproduce** steps you can follow on the emulator or a phone.
- **Root cause** with a source reference (verified unless it says "needs device check").
- **Solution** concrete enough to implement.
- **Confidence:** *Confirmed* (measured or seen by 2+ reviewers, and the cause checked in source), *Single* (one reviewer, cause cited), *Needs device check* (could not be settled on the emulator).

Findings are grouped by theme (A to H). The cross-reference column in the index maps each ID back to the reviewer reports (`F-` flows, `V2-`/V visual, `R` robustness, `M`/`M2-` motion) kept in the scratchpad `audit` folder.

### Reproduction prerequisites

1. Install the 1.0.2 debug build. Grant usage access, notifications and phone state (on the emulator: `adb shell appops set io.github.khaledbahaaeldin.emberbyte GET_USAGE_STATS allow`, `adb shell pm grant io.github.khaledbahaaeldin.emberbyte android.permission.POST_NOTIFICATIONS`).
2. Create one plan named **Home_Plan**: 20 GB, monthly, day 1, not tied to a SIM, default alerts. Most findings reproduce with this plan.
3. Useful switches (restore them afterwards):
   - Font size: `adb shell settings put system font_scale 2.0` (1.0 restores).
   - Display size: `adb shell wm density 560` (320 for small; `adb shell wm density reset`).
   - Dark mode: `adb shell cmd uimode night yes` (`no` restores).
   - Reduced motion: set the three animation scales to 0 (`window_animation_scale`, `transition_animation_scale`, `animator_duration_scale`).
   - **Slow motion for animation checks:** set the same three scales to 4 or 10, then record with `adb shell screenrecord` and cut frames with `ffmpeg -i clip.mp4 -vf fps=6 f_%03d.png`. The emulator's recorder is variable-rate, so count distinct frames, never frames per second.
   - Right-to-left: `adb shell cmd locale set-app-locales io.github.khaledbahaaeldin.emberbyte --locales ar` (clear it afterwards with an empty list).
4. Themes: Settings > Appearance. Twelve palettes, each Light, Dark and (Dark only) AMOLED.

---

## Index

| ID | Sev | Area | Finding |
|---|---|---|---|
| A1 | P0 | Colour | Error colour is the dark-theme pastel in light themes (1.25:1) |
| A2 | P0 | Colour | Plan status text uses the accent container colour as text (1.02:1) |
| A3 | P1 | Colour | Other palette text pairs fail AA (muted, links, legends, forecast tile) |
| A4 | P2 | Colour | Monochrome has no state colours (Safe/Warn/Exceeded) |
| B1 | P1 | Layout | Content scrolls under the status bar (Settings, editor, History) or hard-clips (Home, Apps) |
| B2 | P1 | Layout | Connected button groups break words and deform the selected pill |
| B3 | P2 | Layout | Large font: list rows lose their names |
| B4 | P2 | Layout | Plan options menu opens on top of the card text |
| B5 | P1 | Layout | Kebab overlaps the ring on the small plan tile |
| B6 | P2 | Layout | Navbar: merges with cards, no way back at list end, offset under reduced motion |
| B7 | P2 | Layout | Home: detached History bars, dead-looking tiles, ambiguous hero |
| B8 | P2 | Layout | Onboarding page 1 is top-heavy; Skip and button styles |
| C1 | P1 | Data | "Most data used" shows the wrong app when sorted by name |
| C2 | P1 | Data | App detail daily chart clips its first bar |
| C3 | P1 | Data | Budget reads "0 B of 1 GB" for an app that used 600 MB |
| C4 | P2 | Data | Hero and Apps totals differ (785 MB vs 776 MB) |
| C5 | P1 | Data | History Day view: 7 bars, 14-day totals |
| C6 | P1 | Data | History bars have no selected state, split bar has no legend |
| C7 | P2 | Data | History Month: layout jump and unequal stat tiles |
| C8 | P1 | Data | History charts show no numbers on or above the bars (tester report) |
| C9 | P1 | Data | The selected day is barely marked (tester report; extends C6) |
| C10 | P1 | Data | Day, Week and Month bar labels follow three different schemes (tester report) |
| D1 | P1 | Forms | New-plan numeric fields: wrong keyboard, silent invalid input |
| D2 | P1 | Forms | Step counter changes from "of 4" to "of 5" |
| D3 | P1 | Forms | Continue and Back buttons do not read as primary/secondary |
| D4 | P1 | Forms | Review step is bare |
| D5 | P1 | Forms | Plan editor: five selection styles, stock widgets, labels differ from the flow |
| D6 | P1 | Forms | Save is mid-page, not sticky |
| D7 | P1 | Forms | Alert thresholds: none selected, no explanation |
| D8 | P1 | Forms | Free window asks for a raw package name |
| D9 | P1 | Forms | Archived plan: no unarchive, not distinct, no undo |
| D10 | P2 | Forms | Duplicate "New plan", inconsistent titles, bare options menu |
| D11 | P2 | Forms | What-if planner is two plain lines and marks the form dirty |
| D12 | P1 | Forms | Budget dialog: unlabeled chips, accepts "0-", no confirm on Remove |
| E1 | P2 | Apps | Active filters are heavy 48 dp chips with bare labels |
| E2 | P2 | Apps | Filters sheet polish |
| E3 | P2 | Apps | Search field and Filters button differ in height and style |
| E4 | P2 | Apps | App detail alignment, empty half, 0% ring |
| F1 | P2 | Settings | Letter placeholders and three tile shapes in one list |
| F2 | P2 | Settings | AMOLED row: placeholder tile, lone dot, different name |
| F3 | P2 | Settings | AMOLED switch can stay disabled |
| F4 | P2 | Settings | AMOLED loses all card structure |
| F5 | P2 | Settings | Row text columns do not line up |
| F6 | P2 | Dialogs | Export dialog has only a filled Cancel; passphrase errors incomplete |
| F7 | P2 | Settings | Updates row is a four-line paragraph |
| F8 | P2 | Dialogs | Dialog family is inconsistent |
| G1 | P1 | Motion | Home, Apps, App detail show a false empty state on first paint |
| G2 | P1 | Motion | `MorphingNumber` is never used |
| G3 | P1 | Motion | Wavy bars and rings never animate in |
| G4 | P1 | Motion | Conditional blocks hard-cut across the app |
| G5 | P1 | Motion | Bar chart height and selection colour snap |
| G6 | P2 | Motion | Theme or dark-mode change flips every colour in one frame |
| G7 | P2 | Motion | Guarded back cancel snaps instead of springing |
| G8 | P2 | Motion | Plan-flow progress is stock and un-animated |
| G9 | P2 | Motion | Dialog inline errors pop in; no content motion |
| G10 | P2 | Motion | Icon placeholders hard-swap to real icons |
| G11 | P3 | Motion | Startup placeholder cuts to the app |
| G12 | P2 | Motion | Press feedback is ripple only |
| G13 | P2 | Motion | Plus button has an entrance but no exit |
| G14 | P1 | Motion | Plan editor conditional fields appear in one frame |
| G15 | P3 | Motion | Onboarding: no entrance, dot colour snaps |
| G16 | P3 | Motion | Appearance selection and enabled/disabled snaps |
| G17 | P2 | Motion | List items not keyed, so inserts shift siblings |
| G18 | P2 | Motion | Licences ignores reduced motion |
| G19 | P3 | Motion | Ideas: container transform, pull-to-refresh, Expressive menus |
| H1 | P1 | System | Theme passes no typography or shape scale to `MaterialExpressiveTheme` |
| H2 | P2 | System | 12 corner radii and 66 hard-coded text sizes |
| H3 | P2 | System | Two parallel colour systems |
| H4 | P3 | System | Strings are literals; no localisation |
| H5 | P3 | System | Dead `LicensesScreen.kt` |
| H6 | P2 | System | Stock widgets make the editor look like another app |
| H7 | P3 | System | No adaptive layout for tablets and foldables |

---

## A. Colour and contrast

### A1. P0. Error colour is unreadable in light themes
*Confidence: Confirmed (4 reviewers, measured).*

**What is wrong.** Every error and destructive surface uses a pale pink on a light lavender dialog: contrast **1.25:1** in Dusk light and **1.28:1** in Ember light (AA needs 4.5:1). Affected: the budget limit error ("Enter a number greater than zero"), the passphrase mismatch, the "Remove" budget button, the titles "Discard changes?" and "Delete plan?", and the error outline of text fields. In Monochrome light the error colour is black, so errors look like normal text. Other palettes also fail on dialog error text: Gruvbox dark 2.56, Nord dark 2.11, Solarized dark 2.25, Dracula dark 2.91, Tokyo Night light 2.34, Everforest light 2.51 (full table in A3).

**Reproduce.**
1. Set Settings > Appearance to Dusk and Light.
2. Open Apps > any app > Set a budget. Type `0` in Limit, tap Save. The error text is almost invisible.
3. Settings > Data and backup > Create backup. Type two different passphrases. The mismatch message is barely visible.
4. Open a plan editor, change a field, press back: "Discard changes?" has a washed-out title.

**Root cause.** `ui/design/.../theme/palette/GeneratedPalettes.kt:35` (DuskLight) and `:69` (EmberLight) set `danger = Color(0xFFFFB4AB)`. That is the Material dark-scheme error tint. The only `0xFFFFB4AB` hits in the file are lines 18, 35, 52, 69, so only the two light palettes are wrong in that way. `PaletteMapping.kt:41-44, 77-80` maps `danger` to both `error` and `errorContainer`. `MonochromeLight` sets `danger = 0xFF000000`.

**Solution.**
1. In the palette source (`tools/palettes/palettes.json`) give each light palette a real error: M3 baseline `#BA1A1A` on container `#FFDAD6`, on-error `#FFFFFF`. Keep `#FFB4AB` only in dark palettes.
2. Add a contrast guard `ensureContrast(foreground, background, minRatio = 4.5f)` used when mapping `danger` into `colorScheme.error` so any palette that still falls short is nudged (darkened in light, lightened in dark) instead of shipping unreadable.
3. For Monochrome, use a dedicated red and add a non-colour cue (leading error icon, bold) so errors are not colour-only.
4. Register the missing pair in the existing `ContrastPairs` gate (danger as text on surface2 and on the page, 4.5:1) so `PaletteContrastTest` fails for any palette that regresses.

**Acceptance.** Dusk light error text on the dialog surface is at least 4.5:1; the palette contrast test passes for all 24 palette and mode combinations.

---

### A2. P0. Plan status text uses an accent container colour as text
*Confidence: Confirmed (4 reviewers, measured).*

**What is wrong.** On plan cards and the editor preview, the line "Won't run out this cycle" is pale mint on lavender: **1.02:1** in Dusk and Ember light. It is the key message of the card. The same sentence on the Home Forecast tile is readable (dark text on a mint tile). In nine of twelve light palettes the pair is below 3:1.

**Reproduce.** Light theme (Dusk). Open the Plans tab. The status line under the plan name is invisible. The same line in dark mode is legible (11.85:1).

**Root cause.** `ui/design/.../plan/PlanCard.kt:96-98`: `val textColor = if (isExceeded) palette.danger else palette.accent`. `palette.accent` is a *container* colour (pastel in light themes) used here as text on `surface2`. `PlansBento.kt` uses the same pattern in the small tile.

**Solution.**
1. Add a text role: `palette.onAccentContainer`-style `statusText` computed with `ensureContrast(accent, surface2, 4.5f)`, or simply use `palette.text` for the sentence and keep the accent as a leading dot or tonal chip.
2. Same fix for the other places that use `accent` as text (Settings "Keep measuring" and similar). Other uses of `accent` as fills and icons are fine (`HomeSections.kt:264`, `History.kt:329`, `AppRow.kt:160`).
3. Register the pair "accent used as text on surface2" in `ContrastPairs` (`ui/design/.../theme/palette/ContrastPairs.kt`) so the existing `PaletteContrastTest` fails for it today and guards it afterwards.

**Acceptance.** The status line is at least 4.5:1 on every light and dark palette on the plan card and the editor preview.

---

### A3. P1. More palette text pairs fail AA
*Confidence: Confirmed (computed from source, matched to device pixels).*

**What is wrong.** Besides A1 and A2, the table below lists pairs below 4.5:1 (FAIL(lg) = passes only as large text, 3 to 4.5).

| Palette | Mode | Plan status | Dialog error | Muted on surface2 | Link on surface2 |
|---|---|---|---|---|---|
| Dusk | Light | **1.02** | **1.25** | 5.20 | 4.74 |
| Ember | Light | **1.02** | **1.28** | 5.63 | 3.95 |
| Catppuccin | Light | **2.17** | 3.52 | 4.05 | 3.51 |
| Gruvbox | Light / Dark | **2.83** / 4.19 | 5.02 / **2.56** | 3.80 / 4.05 | 3.57 / 5.20 |
| Nord | Light / Dark | **1.51** / 4.23 | 3.03 / **2.11** | 5.46 / 6.39 | **2.98** / 4.31 |
| Rosé Pine | Light | **2.37** | 3.50 | 4.10 | 3.16 |
| Solarized | Light / Dark | **2.34** / 3.24 | 3.37 / **2.25** | 4.09 / 3.60 | **2.68** / **2.82** |
| Dracula | Dark | 6.67 | **2.91** | 4.71 | 3.79 |
| Tokyo Night | Light | 3.14 | **2.34** | 3.97 | **2.67** |
| Everforest | Light / Dark | **2.05** / 4.08 | **2.51** / 3.43 | 4.04 / 3.96 | **2.67** / 4.70 |
| Kanagawa | Light | **2.64** | 3.28 | 4.38 | 4.91 |

Also: Home Forecast tile detail line fails on Gruvbox light (3.76); "Mobile 19 KB" muted text fails on Gruvbox light, Catppuccin and Solarized dark; the App detail legend "Mobile 0 B" (2.98:1) and "Wi-Fi" (3.41:1) fail on Solarized light, and the yellow legend text on Dusk (about 1.7:1). Dark Dusk, Ember, Monochrome, Rosé Pine and Tokyo Night pass every tested element.

**Reproduce.** Pick the palette and mode, open Home, Plans, App detail and any dialog; sample colours with a screenshot tool or compare with the table. The computation is exact: `ratio = (L1 + 0.05) / (L2 + 0.05)` with WCAG relative luminance.

**Root cause.** A contrast gate already exists (`ui/design/.../theme/palette/ContrastPairs.kt` with `PaletteContrastTest.every_pair_passes_in_every_theme_and_mode`) and it passes, because it only registers pairs for the roles used as designed: text, muted, and `onAccent`/`onDanger` on their fills, and "marks" at 3:1 on the background. It does **not** register `accent` or `danger` used as *text* on `surface2`, `primary` used as a link on `surface2`, or `muted` on `surface2`, and the code uses them that way (`PlanCard.kt:98`, dialogs, "See all", legends). The test is green while the screen is unreadable.

**Solution.** One systemic fix, not 24 hand-edits: (1) register the missing pairs in `ContrastPairs` (accent text on surface2, danger text on surface2 and on the page, primary link on surface2, muted on surface2) and watch the test go red for the palettes in the table; (2) fix the roles, either by a text-safe role derived with `ensureContrast` (preserve hue, nudge lightness) when the colour scheme is built, or by changing the call sites to use `text`/`onAccent` on a tonal chip (see spec T1).

**Acceptance.** The contrast test reports zero failing pairs for text roles in all 24 combinations; large-text exceptions are documented in the test.

---

### A4. P2. Monochrome state colours are indistinguishable
*Confidence: Needs device check.*

**What is wrong.** In Monochrome, `danger` is black or near-white and `accent` is grey, so ring states Safe, Warn and Exceeded rely on shape and text alone.

**Reproduce.** Choose Monochrome; edit Home_Plan to a 1 MB cap so it is exceeded; compare the plan card ring and text with Safe.

**Solution.** Give Monochrome a dedicated muted red and amber for state only, or add a state glyph (check, warning, error icon) next to the percentage so state is never colour-only. Verify with a plan in each state.

---

## B. Layout and insets

### B1. P1. Content scrolls under the status bar, or hard-clips
*Confidence: Confirmed (4 reviewers, screenshots).*

**What is wrong.** On Settings, the Plan editor, Appearance and History, scrolled content passes under the clock and battery with no scrim: the clock overlaps the "Decimal" and "None" chips. On Home, Apps and Licences the content is cut off flush at the status-bar edge instead. The editor's "Edit plan" title and back arrow scroll away. Two different behaviours for the same job, neither polished.

**Reproduce.** Settings: scroll down 2 screens; look at the top 300 px. Plan editor: scroll the form; the chips collide with the clock. Home: scroll up and see the row cut at the status-bar edge.

**Root cause.** `common/ScreenHeader.kt:32` applies `statusBarsPadding()` inside the list, while `contentPadding` has no top inset (`SettingsScreen.kt:266`, `PlanEditorScreen.kt` near line 153, `History.kt:268`). Home, Apps, Licences put `Modifier.statusBarsPadding()` on the LazyColumn itself (`HomeScreen.kt:91`, `AppsScreen.kt:137`, `LicencesScreen.kt:112`), which clips.

**Solution.** One pattern for every scrolling screen: a shared screen wrapper with a pinned (or collapsing) Material 3 Expressive top app bar and a status-bar scrim (24 dp gradient, background to transparent). Use the flexible app bar for sub-screens with a title and back arrow. Remove per-screen `statusBarsPadding()` workarounds.

**Acceptance.** On every scrolling screen the top 24 dp below the status bar never shows text under the clock, in light and dark; titles stay visible on sub-screens.

---

### B2. P1. Connected button groups break words and deform the selected pill
*Confidence: Confirmed (3 reviewers, screenshots at four conditions).*

**What is wrong.** The Filters sheet Period group ("Today", "7 days", "30 days", "Cycle") breaks "Today" as "Tod/ay" at font 1.3, as "Toda/y" even at default font on a 411 dp wide phone, and reads "To d…" and "Cycl/e" at 2.0. At display size 560 the selected chip collapses into a "T…" circle. Appearance Mode wraps "Syste/m" and "Decimal (GB)". At font 1.5 the segments become different heights (218 px vs 134 px). During the picker morph a selected "30 days" wraps to two lines and the sheet height jumps about 14 dp.

**Reproduce.** Font size 1.3 (or `wm size 720x1280` with `wm density 280`). Apps > Filters. Look at Period. Repeat with font 2.0 and with `wm density 560`.

**Root cause.** `ConnectedButtonGroup` gives every segment `Modifier.weight(1f)` (equal widths) and its label `maxLines = 2` with ellipsis (`ConnectedButtonGroup.kt`). Four equal segments cannot hold their words at accessibility sizes. Used at `FiltersSheet.kt:95`, `AppearanceScreen.kt:119`, `SettingsScreen.kt:347`, `History`, plan editor.

**Solution.** Make the group width-aware:
1. Measure each label's intrinsic width. If the sum plus gaps fits, weight segments by their intrinsic width (not equally).
2. If it does not fit, lay the group out in two rows (`FlowRow` with connected shapes recalculated per row) or make it horizontally scrollable.
3. Force `softWrap = false`, `maxLines = 1`, and give every segment the same height (`IntrinsicSize.Min`).
4. At `fontScale > 1.3`, drop the leading check icon to save width.

**Acceptance.** At font 2.0 and density 560 no label is broken mid-word, all segments in a row have equal height, and the selected shape stays a pill.

---

### B3. P2. Large font: list rows lose their names
*Confidence: Confirmed.*

**What is wrong.** At font 2.0 app names truncate ("Private Co…", "Google Play…") because the value pill claims width first.

**Reproduce.** Font 2.0; open Home and Apps.

**Solution.** At `fontScale > 1.3` stack the value under the name, or give the name up to two lines and cap the value pill with a maximum width (`Modifier.widthIn(max = ...)`). Names already use `weight`; the value needs the cap (`UsageBarRow`, `AppRow`).

---

### B4. P2. Plan options menu opens on top of the card text
*Confidence: Confirmed.*

**What is wrong.** The three-item dropdown (Pin, Edit, Archive) opens anchored over the card ("14 days left"), same lavender as the card, so it barely lifts, and has no icons.

**Reproduce.** Plans tab > the kebab on a plan card.

**Solution.** Offset the menu below the kebab, aligned to the end edge; use an Expressive menu with icons, a divider before Archive and an error tint for destructive items.

---

### B5. P1. Kebab overlaps the ring on the small plan tile
*Confidence: Single (cause in layout).*

**What is wrong.** On the half-width plan tile the three-dot button sits on the wavy ring's right edge; the tile also loses "days left" and the forecast.

**Reproduce.** Plans tab with two plans so one renders as a small tile.

**Solution.** Move the kebab to the top-right corner outside the ring, or put the ring below the title. Keep "days left" visible at the small size by shortening the label.

---

### B6. P2. Navbar details
*Confidence: Confirmed for the first two; the third needs a device check.*

1. **The pill merges with surface2 cards.** When visible over cards its bottom lip is a ghost shape behind the selected chip. Give it a 1 dp `outlineVariant` stroke or a raised container with 3 dp elevation.
2. **No way back at the end of a list.** The pill hides on scroll down and, once the list ends, only reappears by scrolling up. At the end of a list show it again.
3. **Offset with animations off.** With the animation scale set to 0 the pill appeared left of centre and clipped on Home, Apps and Plans (`anim0/01_home.png`). The cause is likely the snapped placement path in `CompactNavBar.kt` / `PillSpring.kt`. This was booted with scale already 0; **toggle the setting while the app is open** to confirm before fixing. This is the accessibility path, so treat it as high priority once confirmed.

Also: at rest on Apps the pill covers the last row ("Chr"); bottom padding is 120 dp on Apps/History/Settings and 180 dp on Home/Plans (`HomeScreen.kt:92`, `PlansScreen.kt:61`). The plan editor's 112 dp is the tightest (12 dp above the pill zone); make it 120 dp.

---

### B7. P2. Home polish
*Confidence: Single.*

- The History card heading sits in a tile but the seven bars sit outside it on the page background, so the card looks empty and the chart detached.
- Forecast, Live and Today-by-network tiles look tappable but are not.
- The hero "693 MB" next to "20.0 GB left of 20.0 GB" reads contradictory (today's usage vs plan usage). Label the hero ("Today").
- Top-apps rows: the value pill has auto width, so bars end at different x (805, 795, 795 px). Fix the value column width.
- The Live tile has dead space under the speed.

---

### B8. P2. Onboarding polish
*Confidence: Single (page 1 screenshot and source).*

Page 1 is top-heavy: content ends around y = 1260 px, then about 800 px of empty space above the dots. "Skip" on pages 1 to 3 jumps to the permission page, while the last page's secondary says "Skip for now": rename the first "Skip intro". There is no Back button. Page 3's "Set up a plan" is a `FilledTonalButton` coloured like primary (`OnboardingPages.kt:266`): use `Button`. Centre the copy block vertically or enlarge the hero art (`OnboardingScreen.kt:208-271`).
Not tested on device: onboarding cannot be restarted without clearing data.

---

## C. Data and logic visible to the user

### C1. P1. "Most data used" shows the wrong app
*Confidence: Confirmed (screenshot, cause verified).*

**What is wrong.** After Filters > Sort by Name the featured card still says "Most data used" but shows "Android system 4.90 KB (3% of total)" while another app used 69.4 KB in the same list. In the default sort the featured app also duplicates the first row beneath it.

**Reproduce.** Apps > Filters > Sort by Name > Apply.

**Root cause.** `apps/AppsViewModel.kt:105`: `val featuredRow = if (text.isEmpty() && rows.isNotEmpty()) rows.first() else null`. `rows` follow the chosen sort, so the first row is the alphabetical first.

**Solution.** Pick the maximum by usage: `rows.maxByOrNull { it.mobileBytes + it.wifiBytes }`, or hide the card when the sort is not by usage. Consider dropping that app from the list below to avoid duplication.

---

### C2. P1. App detail daily chart clips its first bar
*Confidence: Confirmed (2 reviewers).*

**What is wrong.** On "Daily usage" the Wednesday bar is 30 px wide of 126 px expected and its label is missing: the seven fixed-width slots overflow the card's padding. The "today" bar uses `palette.accent` (an olive/green container) and reads as an error colour.

**Reproduce.** Apps > any app > look at the Daily usage chart.

**Root cause.** `AppDetail.kt:466-500` lays out `UsageBarRow` with fixed 126 px slots inside a padded `BentoTile`.

**Solution.** `Row(Modifier.fillMaxWidth())` with `Modifier.weight(1f)` per day and label every slot; use `primary` for today and a quieter container for the rest.

---

### C3. P1. Budget says "0 B of 1 GB today" for an app that used 600 MB
*Confidence: Needs device check.*

Probably usage is counted only since the budget was created, but the card does not say so. Either count the whole period or label it "Since you set this budget". Check the budget model against the intended use case before deciding.

---

### C4. P2. Hero and Apps totals differ
*Confidence: Needs device check.*

Home showed 785 MB while Apps showed "776 MB in total" for the same Today filter. Probably mobile plus Wi-Fi against an app-only sum (system traffic not attributed to an app). Decide which number each screen shows and label it.

---

### C5. P1. History Day view: 7 bars but 14-day totals
*Confidence: Single (recording and screenshots).*

**What is wrong.** The hero "1.39 GB" and Total "1.39 GB" cover 14 days; the chart shows seven bars (about 965 MB). The other seven are behind a horizontal scroll with no affordance. The hero and Total tile repeat the same number.

**Reproduce.** Home > History card (or the History screen) > Day.

**Solution.** Add a range label ("4 to 17 Nov"), peek the next bar at the edge with a fade, and say what the hero totals ("Last 14 days"). Remove the duplicate number.

---

### C6. P1. History and Home bars have no selected state; split bar has no legend
*Confidence: Single; the selected-state half is confirmed again by a tester (see C9).*

Tapping a bar changes the hero to that day and inserts a "Tuesday / Show total" row plus a Mobile/Wi-Fi split bar with no legend; all bars keep the same colour; the layout jumps 230 px down. On Home the weekly bars are tappable (48 dp slots) and nothing happens.
**Solution.** Tint the selected bar `primary` and dim the others (`onSurfaceVariant` at 38%), add Mobile/Wi-Fi legend dots, reserve the row height so nothing jumps, and give Home bars either the same behaviour or no click handler.

---

### C7. P2. History Month: jump and unequal tiles
*Confidence: Single.*

Switching to Month removes the trend line so the chart jumps up about 65 to 100 px, and "Average per active month" wraps so the two tiles differ in height (220 vs 273 px). **Solution.** Reserve the trend row (show "No previous month"), use `IntrinsicSize.Max` for the tile pair, shorten the label to "Avg / active month".

---

### C8. P1. History charts show no numbers
*Confidence: Confirmed by a tester on a physical phone (Discord #dev-feedback, dark Ember theme) and by the source.*

**What is wrong.** In History (Day, Week and Month) each bar is a plain shape with a label underneath, but no value anywhere: you cannot read how much without tapping every bar. The only number is the hero, and only for the selected bar. The weekly bars on Home are the same. The tester's words: "those charts need numbers".

**Reproduce.** Home > History card (or the History screen) > Day, then Week and Month. No MB or GB value is drawn on or above any bar. The spoken description does contain the value (`History.kt:103-112`), so TalkBack users hear a number that sighted users cannot read.

**Root cause.** `historyBars` builds `BarUi(label, point.totalBytes, description)` and `UsageBarRow` renders only `bar.label` (`UsageBarRow.kt` around lines 146-148 and 199). There is no value text in the layout.

**Solution.**
1. Draw a compact value above each bar in the user's unit setting (`62 MB`, `1.4 GB`; no decimals at 10 or more units, one decimal below), `labelSmall`, `muted` colour (`text` for the selected bar). Use the same formatter as the rest of the app (`ByteUnits`) so Decimal and Binary settings apply.
2. When 12 bars do not leave room for every value (Week and Month on a narrow phone, or font scale above 1.3), show values for the selected bar, the highest bar and today only, and offer a "Show values" chip that reveals all of them in a scrolling row, so nothing overlaps or truncates.
3. Add a small baseline hint ("max 3.2 GB") so bar heights are readable.
4. Hide the new value text from the accessibility tree as a separate node (the row description already speaks it) to avoid double announcements.

**Acceptance.** Every visible bar in Day, Week and Month shows its value at font 1.0 on a 360 dp phone, or the reduced set at larger sizes; no value overlaps a neighbour at font 2.0.

---

### C9. P1. The selected day is barely marked
*Confidence: Confirmed by the tester; extends C6.*

**What is wrong.** Tapping a bar switches the hero and adds a "Saturday / Show total" row, but the bar itself looks almost like the others, so you cannot see which one you picked. In the tester's screenshot the selected bar differs from its neighbours mainly by height. The tester's words: "the selected day should be more marked".

**Reproduce.** History > Day > tap any bar > compare it with the other bars.

**Root cause.** `UsageBarRow` has no selected style beyond the row semantics (see C6), and the selection colour is not animated either (motion finding G5).

**Solution.** Make selection unmistakable with several cues at once:
1. Selected bar filled `primary` with an Expressive shape (full-radius capsule); all others `primaryContainer` at 38% or `surface2`.
2. Selected bar's value and label in bold `text`; the rest muted.
3. A small marker (dot) under the label and a tonal pill behind the selected label.
4. Animate colour and height changes with the spatial spring (task T10, finding G5); snap under reduced motion.
5. Keep the layout from jumping when the "Saturday / Show total" row appears (C6).
6. Today's bar gets a ring so "today" and "selected" are different cues.

**Acceptance.** In a screenshot the selected bar is distinguishable from the others at a glance by colour (contrast at least 3:1 against unselected bars) and label weight, in every theme and in AMOLED.

---

### C10. P1. Bar labels follow three different schemes
*Confidence: Source confirmed; the tester's wording is ambiguous, see below.*

**What the tester asked.** "Why days start from 1 but weeks not". The screenshot shows Day labels 9, 10, 11, 12, 13, 14.

**What the code does.**
- **Day:** the label is the bare day of the month, `date.dayOfMonth.toString()` (`History.kt:105`), for the last 14 days. Over a month boundary the row reads "28, 29, 30, 1, 2, 3": the numbers restart at 1 with no month, weekday or separator, so it looks like a counter that resets, and a reader cannot tell the "1" is the next month.
- **Week:** the label is `d MMM` of the **Monday** that starts the week (`History.kt:108`, for example "3 Nov"), for the last 12 weeks. It is a date, not a week number, and it never restarts at 1.
- **Month:** the short month name ("Nov").

So the three scales use three vocabularies (a bare number, a date, a month name), and only Day can "restart from 1". The tester most likely means this mismatch. **Confirm with the tester before coding** whether they meant (a) Day labels restarting at 1 at a month change, or (b) the Week chart lacking a numbering such as "Week 1". The solution below covers both.

**Reproduce.** History > Day on a range that crosses a month boundary (set the emulator date, or use data from the last days of a month). Then switch to Week and Month and compare.

**Solution.** One labelling rule across scales, built from the real dates:
1. **Day:** weekday initial over the day number ("S" over "9"); at a month boundary show the month on the first bar of the new month ("1 Dec") with a thin separator.
2. **Week:** the Monday-to-Sunday range ("3 to 9 Nov"), collapsing to "3 Nov" when narrow; never a bare number.
3. **Month:** short month name; add the year only at a year boundary ("Jan 27").
4. Use the same wording in the content descriptions and in the hero subtitle so the selected bar names itself ("Saturday 14 Nov").
5. Formats come from `DateTimeFormatter` with the user's locale, and words from `strings.xml` (also helps localisation, H4).

**Acceptance.** Across a month boundary the Day labels make the change visible; Week and Month labels read as dates in the same style; labels never truncate at font 2.0 (they may reduce to the initial and number).

---

## D. Plans, forms and dialogs

### D1. P1. New-plan numeric fields: text keyboard, silent invalid input
*Confidence: Confirmed (source and recording).*

**What is wrong.** Data allowance, Day of month and Days per cycle open a QWERTY keyboard; "1.5.2" or "401" is accepted; Continue stays disabled with no message.

**Reproduce.** Plans > New plan > step with the cap > tap the field.

**Root cause.** `plans/flow/PlanFlowSteps.kt` has no `KeyboardOptions` (0 matches), while `PlanEditorScreen.kt:201, 309` use `KeyboardType.Decimal`.

**Solution.** `KeyboardOptions(keyboardType = Decimal or Number, imeAction = Next)`; inline `supportingText` errors ("Enter a number greater than zero", "Day must be 1 to 31"); keep Continue enabled so a tap explains what is missing.

---

### D2. P1. Step counter changes while typing
*Confidence: Needs device check.*

"Step 3 of 4" became "Step 3 of 5" with the keyboard open and a day entered, then back to 4 when the keyboard closed. Probably a conditional step in `PlanFlowViewModel`. The total must never change while editing: derive it from the plan type chosen earlier, not from the field being edited.

---

### D3. P1. Continue and Back buttons do not read as primary and secondary
*Confidence: Single.*

Disabled Continue is flat grey, enabled Continue is pale lavender with dark text (it looks disabled), the final "Create plan" is solid primary. Cancel and Back are faint outlined buttons (border about 1.4:1). **Solution.** Enabled Continue = filled `Button`; disabled = clearly greyed; Back = text button with a visible label (`PlanFlowScreen.kt`).

---

### D4. P1. Review step is bare
*Confidence: Single.*

Three plain lines (only Name bold) and a blank screen: no SIM, no next renewal date, no start option. **Solution.** A `BentoTile` summary reusing the plan card preview with label/value rows and "Renews 17 Dec".

---

### D5. P1. Plan editor: five selection styles, stock widgets, different labels
*Confidence: Confirmed (2 reviewers).*

**What is wrong.** One long form with radios, separately outlined unit chips (MB/GB/TB, Rollover, Alerts), a connected group with a hard outline (Cycle), outlined full-width buttons for dates and times with no label or icon, and plain headings, all on the bare background. The new-plan flow uses a tonal `ConnectedButtonGroup` for the same choices. Labels differ: "Data allowance" vs "Cap", "Days per cycle" vs "Number of days", "Day of month (1-31)".

**Reproduce.** Plans > a plan > Edit; compare with Plans > New plan.

**Root cause.** The editor was built from stock Material components (`PlanEditorScreen.kt`: 11 `OutlinedTextField`, 8 `RadioButton` across editor, budget dialog and flow).

**Solution.** One `ConnectedButtonGroup` for every single choice (units, cycle, rollover; alerts as multi-select), group sections in `BentoTile`s, `OutlinedTextField` with a trailing unit for Cap, leading calendar and clock icons on the date and time rows, one vocabulary shared with the flow (store the strings once).

---

### D6. P1. Save is mid-page and scrolls away
*Confidence: Confirmed.*

Save sits halfway down the form above "Danger zone"; the title scrolls away; a long form needs a sticky bottom bar. **Solution.** `Scaffold` with a pinned small top bar and a `bottomBar` holding Save and Discard (see B1).

---

### D7. P1. Alert thresholds: nothing selected, no explanation
*Confidence: Single.*

50, 80, 90, 100% chips with none checked and no helper text; unclear whether it is multi-select or empty means off. The budget dialog's "Warn at" defaults to 80%. **Solution.** Helper text ("Notify me when the plan reaches…"), default 80% and 100% checked as in the product spec, and say what "none" means.

---

### D8. P1. Free window asks for a raw package name
*Confidence: Single.*

A user cannot know `com.whatsapp`. Seven weekday chips wrap 4 + 3 and are all pre-selected; delete icons float with no card around the row; add-on rows the same. **Solution.** An app picker (the Apps list already exists) and a card per add-on or window.

---

### D9. P1. Archived plan: no unarchive, not distinct, no undo
*Confidence: Confirmed.*

An archived card has no kebab, looks like an active one (tiny "Archived" label, still shows the green forecast); the editor's Danger zone still offers "Archive plan"; Delete is a plain purple text button; archiving is instant with no snackbar or Undo. **Solution.** Kebab on archived cards with Unarchive and Delete; 60% alpha plus an "Archived" chip; "Archive" becomes "Unarchive" in the editor; Snackbar "Plan archived · Undo"; destructive tonal error button with an icon in the Danger zone.

---

### D10. P2. Duplicate "New plan", inconsistent titles, bare menu
*Confidence: Single.*

"New plan" appears twice on Plans (top-right button and the navbar "+"): keep one. Page titles are regular weight on Plans, History, Settings and Edit plan but bold on Apps and Today: choose one style.

---

### D11. P2. What-if planner
*Confidence: Needs device check for the dirty flag and the vanishing preview.*

The result is two plain lines ("Now: Won't run out this cycle", "With extra: runs out Sat"). "Sat" is ambiguous (which date?). The unit chips are outlined while the cap uses MB/GB/TB. Typing in it marks the form dirty so leaving shows "You edited this plan". The Preview card disappeared after scrolling to the top and typing. **Solution.** Its own bento card with an inline result chip and a full date; keep it out of the plan's dirty state.

---

### D12. P1. Budget dialog
*Confidence: Confirmed.*

Unit chips MB/GB have no label while Period and Warn-at do; they are separate outlined buttons, not the connected group used in Filters; the numeric field accepts "-" and spaces ("0-" was accepted); the error appears only after Save and shifts the layout by 6 px; "Remove budget" is instant with no confirm or undo; Cancel is black text while other dialogs use purple or a filled button. **Solution.** Connected groups with labels, numeric filtering (`Decimal` plus input filter), inline error with reserved height, confirm or Snackbar Undo on Remove, and one dialog button style (see F8).

---

## E. Apps and filters

### E1. P2. Active filters are heavy chips with bare labels
48 dp filled chips with labels like "Cycle", "Mobile", "Name", "System apps" that mean nothing alone; the last chip is clipped with no fade; no "Clear all"; the Filters button shows no badge. **Solution.** 32 dp tonal `InputChip`s labelled "Period: Cycle", "Sort: Name", a trailing "Reset", a badge dot on Filters.

### E2. P2. Filters sheet polish
Reset is a low-contrast outline (border about 1.4:1) and nothing confirms it; the Show-system-apps row uses a different type setting from the labels; 130 px gaps above the title and buttons make it loose. The sheet scrolls, opens fully expanded, and the draft/Apply/Reset model is good: keep it. Use the Expressive sheet top shape.

### E3. P2. Search and Filters row
Search is 64 dp tall outlined, Filters 48 dp tonal. Align height and style. The empty search state repeats itself ("No apps match" and "No apps match "goozzq".") and still shows "0 B in total".

### E4. P2. App detail
Back arrow, icon and title use three different x offsets; "App settings" is inset 74 px against content at 42 or 84; the 0% budget ring looks broken (dark outlined circle in a grey scalloped track); the lower 55% of the screen is empty.

---

## F. Settings

### F1. P2. Letter placeholders and three tile shapes
Appearance rows ("A", "B") and Data rows ("C", "J", "B", "R") use letters instead of icons; "B" means AMOLED black on one card and Backup on another. Tile shapes vary (Cookie, Squircle, Burst, Sunny) in one list. **Root cause.** `iconText = title.firstOrNull()` defaults in `SettingsGroups.kt:53, 133, 253`; `DataSection.kt:106-146`. **Solution.** Real icons (`Palette`, `DarkMode`, `FileDownload`, `DataObject`, `Backup`, `Restore`), one shape per list (Cookie for leading tiles, Burst/Sunny only for group headers), one tint rule (container at 12% primary or surface2, glyph primary).

### F2. P2. AMOLED row on Settings
Placeholder "B" tile (`SettingsScreen.kt:535-544`, `iconColor = palette.surface`, pale on pale); the grey dot is the switch's off thumb on a track the same colour as the card; the title differs from the Appearance screen ("AMOLED black / Pure black backgrounds in dark theme" vs "Pure black (AMOLED)" with "Dark mode only"). The Appearance card's mini swatches always show five dark presets even in light themes and never the selected one when it is beyond the first five (`SettingsScreen.kt:~507`). **Solution.** Icon, off-state track and outline (`uncheckedTrackColor = surfaceVariant`, `uncheckedBorderColor = outline`), one name and subtitle in both places, swatches that include the selected theme.

### F3. P2. AMOLED switch can stay disabled
*Needs device check.* On the Appearance screen with Mode = System, when the device turns dark at runtime the switch stays greyed ("Dark mode only"), probably because `AppearanceViewModel.toUiState` (lines 40-53) computes `effectiveDark` via `isSystemDark()` only when the settings flow emits. The Settings-card switch is never gated, so the two disagree. **Solution.** Derive from `isSystemInDarkTheme()` (or never disable; store the preference and apply it only in dark).

### F4. P2. AMOLED loses card structure
`PaletteMapping.kt:124` sets `surface` and `background` to black, so surface cards disappear into the page. **Solution.** A slightly raised surface (#0B0B0B to #101010) or a 1 dp `outlineVariant` border on cards in AMOLED.

### F5. P2. Row text columns do not line up
Leading offsets 199, 210, 215, 220, 242 px for the label column across rows; the Appearance sub-line starts at x = 79 while its title starts at 179. **Solution.** One leading slot width (48 dp) from a shared row component.

### F6. P2. Export and passphrase dialogs
Export usage: three plain text rows with no ripple affordance and a single filled "Cancel" as the most prominent button. Create-backup: no show/hide icon, no warning that a lost passphrase cannot be recovered, Create disabled without a reason until both fields match and meet the minimum length; the mismatch error is the A1 pink and does not turn the field outline red (the budget dialog does with `isError = true`). **Solution.** Radio rows or `ConnectedButtonGroup` plus an explicit Export button, text Cancel; `isError` and `supportingText` on both fields; show/hide icon; a one-line warning about the passphrase.

### F7. P2. Updates row
A four-line paragraph; tapping launches the browser. **Solution.** One line plus an external-link glyph, mentioning that it opens a browser. The Licences title sits 20 px lower than other sub-screen titles. The version long-press log hook is undiscoverable by design.

### F8. P2. Dialog family
Discard (red cookie icon, red title, destructive pill with warning glyph) is excellent. Create backup (lock icon, primary title), Set budget (no icon, `BudgetEditorDialog.kt:42`) and Export usage (no icon) differ. **Solution.** One icon policy, one button order (dismiss left, confirm right), one destructive style.

---

## G. Loading and motion

> Measuring method for all of G: slow animations to 4x or 10x, record, and **count distinct in-between frames**. A real animation shows many; a cut shows none. Already animated and good (do not break): navbar pill spring and drag-follow, Plus entrance, picker morph, switch thumb, dialog scale and fade, push and pop with the predictive card, Licences expand, Plans loader and fade-in, theme swatch morph, tab cross-fade (verified at 10x: Home fades out as Apps fades in).

### G1. P1. False empty state on first paint
*Confidence: Single (recorded), cause plausible.* After launch Home shows "0 B", "Set up a plan", "No connection", "No per-app data yet" for about 1.9 s, then swaps to the real content in one frame. Apps shows "0 B in total" with a blank list for 4 frames, then the whole list pops in. App detail shows the raw package name and "No daily usage" for about 0.4 s. **Reproduce.** Force-stop the app, launch it, record 6 s. **Solution.** Do not render "empty" until the first query has returned: `UiState.Loading` with `ContainedLoadingIndicator`, then a fade-in, as Plans does (Plans is the model to copy).

### G2. P1. `MorphingNumber` is never used
*Confidence: Confirmed (no call sites in `app/src/main` or `ui/design/src/main`).* The signature Expressive number morph is dead code: the Home hero, Speed tile and History/Apps totals are plain text. **Solution.** Hero (`HomeSections.kt:149`), live Speed tile, History totals (`History.kt:316+`), Apps total.

### G3. P1. Wavy bars and rings never animate in
`WavyBar.kt:53`, `WavyRing.kt:114`, `OnboardingPages.kt:228` start at their target value. **Solution.** `Animatable(0f)` started in `LaunchedEffect`, grow with the spatial spring on first composition, stagger rings in the Plans bento.

### G4. P1. Conditional blocks hard-cut across the app
Only three screen-level `AnimatedVisibility`/`AnimatedContent` exist. Cuts: Home chips, gap/spike/data-plan tiles, top-apps `when` (`HomeSections.kt:96, 128, 317`, `HomeScreen.kt:131, 159, 193`); Apps filter chips row, clear-search X, featured card, empty states (`AppsScreen.kt:167, 250, 278, 312`); editor preview, what-if, cycle kind, rollover cap, add-on lists, danger zone (`PlanEditorScreen.kt:181, 186, 256, 357, 440, 714`); Plans state swap (`PlansScreen.kt:79`); plan-flow step label and CTA (`PlanFlowScreen.kt:190, 220`); Data section busy and message (`DataSection.kt:148, 160`); App detail budget, spikes, chart (`AppDetail.kt:477, 500, 556`). **Solution.** Two wrappers, `EmberbyteAnimatedVisibility` (expandVertically plus fadeIn with `spatial()`/`effects()`, none under reduced motion) and `EmberbyteCrossfade`, applied everywhere above.

### G5. P1. Bar chart snaps
`UsageBarRow.kt` near line 126: height and selection colour change instantly on data, granularity or selection. **Solution.** `animateDpAsState` and `animateColorAsState` with the motion specs, grow-in on first show.

### G6. P2. Theme or dark-mode switch is a one-frame flip
`EmberbyteTheme.kt`. **Solution.** Animate the palette tokens or cross-fade the root over 200 to 300 ms (effects spec); snap under reduced motion.

### G7. P2. Guarded back cancel snaps
`GuardedBack.kt:44-47, 78`: on gesture cancel and dialog dismiss `shrink` goes straight back to 1. **Solution.** `Animatable` progress with `BackTransitions.cancelSpring` (the plan flow already does this).

### G8. P2. Plan-flow progress is stock
`PlanFlowScreen.kt:227` uses `LinearProgressIndicator` (flat, un-animated) while Home uses the wavy bar; step label and the Continue-to-Create button cut. **Solution.** `LinearWavyProgressIndicator` with an animated value and label.

### G9. P2. Dialog inline errors pop; no content motion
`BudgetEditorDialog.kt:167, 191`, `PassphraseDialogs.kt:115, 131`, `EmberbyteDialog.kt:93`. The dialog window does animate (scale and fade over 7 frames in, 5 out) but with no spring. **Solution.** `AnimatedVisibility` for error rows with reserved space; consider the spatial spring for the window.

### G10. P2. Icon placeholder to real icon is a hard swap
Letter placeholders ("G", "P") swap to real icons when loaded (`HomeScreen.kt:55`, `AppsScreen.kt` icon map). **Solution.** `Crossfade` with the effects spec.

### G11. P3. Startup placeholder cuts to the app
`EmberbyteApp.kt:98`. **Solution.** Fade the placeholder out.

### G12. P2. Press feedback is ripple only
Cards, rows and the Plus button have no pressed shape morph (`BentoTile.kt`, `AppRow.kt`, `PlusButton.kt`). Material 3 Expressive expects a morph to a smaller corner radius on a spatial spring, as the icon buttons now do. **Solution.** Shared press modifier using `rememberEmberbyteMotion().spatial()`.

### G13. P2. Plus button has no exit
Good entrance (about 10 frames of scale and expand), but it vanishes in the same frame as the page cut. **Solution.** Mirror the entrance on exit; keep the last trailing content while it plays.

### G14. P1. Plan editor conditional fields
Switching Monthly to Every N days, and choosing rollover Capped, insert or replace fields in one frame (about 80 dp for Capped), and the content below jumps. The segmented control itself morphs fine. **Solution.** G4 wrappers plus `animateContentSize` on the form column.

### G15. P3. Onboarding motion
No page entrance or parallax, dot colour snaps (`OnboardingScreen.kt:196`), bottom bar swap (`:64-115`) and granted rows (`OnboardingPages.kt:439`) cut.

### G16. P3. Appearance selection and enabled/disabled
Selection border and check badge pop (`AppearanceScreen.kt:219, 256`); disabled/enabled alpha snaps (`SettingsGroups.kt:182-210`, `AppearanceScreen.kt:314`). Use `animateFloatAsState`/`animateColorAsState`.

### G17. P2. List items not keyed everywhere
Plans and Apps rows have keys and `animateItem` (verified), but Home and parts of Apps use un-keyed `item {}` (`HomeScreen.kt:131-193`, `AppsScreen.kt:141-309`), so an insert shifts siblings abruptly.

### G18. P2. Licences ignores reduced motion
`LicencesScreen.kt:288, 336` use default specs. Use `rememberEmberbyteMotion()`. Navigation transitions read `MaterialTheme.motionScheme` directly and branch on reduced motion manually, which works but is a second path.

### G19. P3. Ideas
Container transform from a tile or row to its detail screen, pull-to-refresh, an Expressive menu, a static wave phase made alive.

---

## H. Design system

### H1. P1. The theme passes no typography or shapes
*Confidence: Confirmed (source).* `EmberbyteTheme.kt:86` calls `MaterialExpressiveTheme(colorScheme = scheme, motionScheme = MotionScheme.expressive(), content = content)`. No `typography`, no `shapes`. Consequences: all 176 `MaterialTheme.typography` uses are stock; **0 uses of any `*Emphasized` style**; the 7 uses of `MaterialTheme.shapes` get stock 4/8/12/16/28 dp. Roboto Flex's expressive faces exist only in `EmberbyteType` (41 uses: hero numbers and titles), so body copy is not expressive. **Solution.** Build an Emberbyte `Typography` (with Emphasized variants) and a `Shapes` scale (for example extraSmall 8, small 12, medium 16, large 24, extraLarge 32, largeIncreased 28, extraLargeIncreased 36) and pass both.

### H2. P2. Radii and sizes are not on a scale
31 raw `RoundedCornerShape(` sites using 12 radii (10, 12, 14, 16, 20, 22, 24, 26, 28, 30, 32, 36 dp), plus 8 `mixedCorners` literals; 66 `NN.sp` literals and 23 `typography.X.copy(fontWeight=…)` overrides that Emphasized styles would replace. **Solution.** Migrate to `AppShape` tokens and typography roles; add a test that fails on new raw radii outside the token file.

### H3. P2. Two parallel colour systems
Colours are clean (no hard-coded hex outside palettes; only `Color.Transparent` and `BrandMark.kt:40 Color.White`), but `MaterialTheme.colorScheme` is read directly in 50 places (`PlanEditorScreen` 15, `LicensesScreen` 5, `UsageBarRow` 4, `BudgetEditorDialog` 4) while `LocalEmberbytePalette` is used 124 times. **Solution.** Pick one for UI code (the palette) and let the scheme feed only library components.

### H4. P3. Strings are literals
About 170 literal UI strings against 20 `stringResource` uses, no `values-xx`, no `localeConfig`. RTL layout itself is excellent (everything mirrors, numbers stay left-to-right) but all text stays English. **Solution.** Externalise incrementally (the editor and flow first, since D5 touches them) and declare `localeConfig`.

### H5. P3. Dead `LicensesScreen.kt`
`settings/LicensesScreen.kt` (90 lines) is only imported at `EmberbyteApp.kt:66`; the routed screen is `LicencesScreen.kt` (398 lines, `EmberbyteApp.kt:303, 313`). Delete the dead file and its import.

### H6. P2. Stock widgets
Stock `OutlinedTextField` (small corners) in the editor (11) and flow (5) against 16 dp fields in Apps search; stock `LinearProgressIndicator`; stock `RadioButton` (8 uses); outlined full-width buttons; text-only "Archive plan"/"Delete plan". These are the least premium screens. See D5 and the adoption table in the spec.

### H7. P3. No adaptive layout
`ContentWidth.kt` caps content at 600 dp, so wide screens show a stretched phone layout and bottom sheets stay 500 px wide. **Solution.** `WindowSizeClass` driving `WideNavigationRail` and a two-pane Plans list/editor; cap sheets at 560 dp.

---

## What is already excellent (do not break)

Home hero (number plus cookie unit badge, wavy bar); Forecast tile and Today-by-network bar; navbar pill spring, drag-follow and Plus entrance; the picker morph and check mark on every connected group; theme gallery (circle to cookie, haptics row); dialog shapes, heavy titles and pill actions; the Discard and Delete dialogs (scalloped icon, tonal destructive button with glyph); Licences list with search; RTL mirroring and `ForceLtr` for numbers; IME handling (navbar hides, fields stay above the keyboard, sheet scrolls); 320 dpi and 411 dp phone layouts; reduced-motion plumbing; palette tooling (measured colours equal generated tokens); accessibility labels on bars and rows; almost no hard-coded colours outside palettes.

## What was not tested

Onboarding on a fresh install (needs cleared data; judged from source and one screenshot); plan cards in Exceeded and Warn states and real data volumes (the seeded plan is Safe); widgets and the live notification; TalkBack; tablet landscape; 3-button navigation; file-picker flows (CSV, JSON, backup); live toggling of the animation scale; and absolute smoothness, because the emulator renders in software and its recorder is variable-rate. **Gesture feel and real-device smoothness need a phone.** One claim was checked and rejected: tab switches are not hard cuts (verified at 10x slow motion).
