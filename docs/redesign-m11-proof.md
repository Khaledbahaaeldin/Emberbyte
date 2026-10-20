# Milestone 11 Proof — Glance Expressive Widgets Family

This document provides formal proof for Milestone 11 (Tasks E1 through E8) of the Emberbyte Expressive Redesign on branch `redesign`.

## 1. Overview of Delivered Components

Milestone 11 delivered a cohesive, low-memory, Glance-powered widget family adhering to Contract Section 13.7:
1. **Widget Theme & Typography Bitmap Renderer (E1):**
   - Palette resolution supporting active theme presets, system light/dark mode, and AMOLED overrides.
   - Off-screen high-quality typography rendering directly to ARGB_8888 bitmaps, guaranteeing consistent kerning and numeral presentation across launchers without Compose runtime overhead inside RemoteViews.
2. **Shared Widget Data Architecture (E2):**
   - Unified `WidgetData` snapshot engine providing instant access to Today metrics, Plan state, top app usage breakdown, and daily history bars.
3. **Today Widget & Legacy Hero Compatibility (E3):**
   - Modern `TodayWidget` with responsive layout adapting from compact 1x1 to wide 4x2 configurations.
   - `HeroWidgetProvider` backwards compatibility allowing pre-existing home screen placements to seamlessly upgrade without user intervention or launcher crashes.
4. **Plan Status Widget (E4):**
   - Dedicated `PlanWidget` showcasing front plan urgency, cycle progression, remaining data balances, and dormant states.
5. **Asymmetric Bento Widget (E5):**
   - Multi-tile `BentoWidget` offering complementary Today and Plan cards with expressive mixed corner radii.
6. **Full-Featured Dashboard Widget (E6):**
   - Comprehensive 4x4 `DashboardWidget` displaying hero metrics, 7-day usage mini-charts, and top three application usage rows with geometric app shapes and deep links.
7. **Rate-Limiting & Previews (E7):**
   - Synchronous update dispatchers reacting to data ingestion, plan edits, and appearance adjustments with 5-second rate limiting to prevent RemoteViews thrashing.
   - Static XML preview layouts and localized metadata for all widget providers.
   - Enforced 1.5 MiB image memory budget across all widget instances.
8. **End-to-End Device Verification (E8):**
   - Complete visual and behavioral validation across 29 device screenshots on `Violet_API_36`.

## 2. Verification Evidence

### Test Suite Execution
- All unit tests pass across modules (`:app`, `:core:data`, `:core:engine`, `:ui:design`).
- Test count diff: `python tools/test-diff.py 467c41b HEAD --allow docs/redesign-test-changes.md` -> `ADDED 331`.
- Memory budget verification: `AllWidgetsBudgetTest.each_widget_at_its_largest_size_stays_under_1_5_MiB_of_bitmaps` verified passing across all 5 widget types.

### Device Captures
All 29 screenshots are archived under `.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/e8/` and validated with `tools/device/check_pngs.py`:
- 16 theme captures across Dusk dark, Dusk light, Catppuccin light, and Gruvbox AMOLED.
- 5 sizing and degradation captures (compact through 4x4).
- 4 special state captures (permission missing, dormant plan, over-cap warning, legacy Hero receiver).
- 4 accessibility captures (font scale 2.0 and forced RTL).
- Crash count in logcat: 0.
