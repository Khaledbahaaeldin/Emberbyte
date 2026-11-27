# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-11-27

### Added
- **What's new sheet**: after an update the app shows what changed, once. It is read from a changelog bundled in the app, so it still works completely offline, and you can open it again from Settings.
- **Three-step onboarding**: Welcome, Allow access and Your plan, with every permission in one list and a button that always names the next one.
- **New-plan flow in five calm steps**, and a **plan editor** that is one scrolling form with the preview always visible and a Save button that never scrolls away.
- **What if** on a plan: try +1, +5 or +10 GB and see when the plan would run out, without changing it.
- **Plan insight and renewal timeline** on the Plans page: which plan is running ahead of its pace and what daily amount keeps it lasting, plus the next renewals in a row.
- **Tap a plan to bring it to the front.**
- **Values on every bar** of the Apps, App detail and History charts, and clear date labels across month boundaries.
- **Live tile and Today by network tile** on Home, and a hero with a bigger mark and a live unit badge.

### Changed
- **New look for Home, Apps, App detail, History, Plans and Settings**: lighter headlines, clearer cards, one scrolling screen pattern with a collapsing title, and one Plans button.
- **Text is readable in every theme**: the contrast checks now cover every text role in all 12 themes, in light, dark and AMOLED. Pure black (AMOLED) cards are now distinguishable from the background.
- **Setting up a plan** from Home opens the new-plan flow.
- **Plans archive is gone.** Plans archived earlier are restored as normal plans the first time you open 1.0.3, and deleting a plan really deletes it, with Undo.
- Dialogs share one style (icon, button order, one destructive style) and the passphrase and budget dialogs explain their errors.

### Fixed
- Home, Apps and App detail no longer flash an empty state while the first data loads.
- The largest app is featured in Apps whatever the sort order, the seven day bars always fit their card, and History shows the visible date range.
- The AMOLED switch enables itself when the system turns dark.
- The Estimated tag on the Live tile stays on one line.
- Granting usage access in onboarding no longer ends onboarding at once, so the other permissions are no longer skipped.

## [1.0.2] - 2026-11-12

### Added
- **Updates row** in Settings. It shows which app keeps Emberbyte up to date (Obtainium, Orion Store or F-Droid) and opens it; with any other installer it opens the latest GitHub release. Emberbyte still never goes online by itself, so it cannot tell you when a new version exists.
- **Local navigation log**: long-press the version in Settings to copy the last 300 navigation and tab-tap events to the clipboard. It is kept in memory only and never saved or sent anywhere.

### Changed
- **One back and push motion** for every screen: the screen shrinks, rounds its corners and slides away, and follows your finger during the predictive back gesture.
- **Plan creation**: going back a step now uses the predictive back gesture, and steps slide in the direction you are moving.
- **Unit picker** (MB / GB / TB and the other segmented choices) morphs between shapes using the Material 3 Expressive toggle button.
- **Lists animate**: Apps, Plans, History and the Home prompts fade and slide when items appear, disappear or move.
- **Loading** uses the Material 3 Expressive loading indicator.
- **Icon buttons** change shape while pressed, and the theme swatch morphs between a circle and a cookie when selected.
- The plans "+" button grows in and out, and the navigation bar slides smoothly instead of jumping.
- Animation settings now come from one place, and reduced motion still snaps everything.

### Fixed
- Switching tabs from the navigation bar no longer plays the back animation (for example Apps to Home).
- A tap on a tab that drifted slightly before lifting is no longer ignored, which could leave the Apps tab unresponsive until you went Home.
- Opening a tab from a link while a sub-screen is open no longer leaves a duplicate tab on the back stack.

## [1.0.1] - 2026-10-21

### Changed
- Build with JDK 21 (bytecode still targets Java 17), so the app can be built from source by F-Droid. No changes to features or data.

## [1.0.0] - 2026-10-20

### Added
- **Material 3 Expressive Redesign**:
  - **12 Hand-Tuned Themes**: Three original palettes (*Dusk*, *Ember*, *Monochrome*) and nine community palettes (*Catppuccin*, *Gruvbox*, *Nord*, *Rosé Pine*, *Solarized*, *Dracula*, *Tokyo Night*, *Everforest*, *Kanagawa*), each supporting Light, Dark, and pure-black AMOLED modes with mathematically verified WCAG AA contrast.
  - **Spring Expanding-Pill Navigation Bar**: Physics-based compact navigation bar featuring an expanding active tab pill, continuous gesture dragging with momentum, haptic detents, and real-time coverage-based icon color interpolation.
  - **Four Home-Screen Glance Widgets**: Today (2x2), Plan (2x2), Bento (4x2), and Dashboard (4x4) glance widgets that automatically refresh on data, plan, and theme updates while strictly maintaining a < 1.5 MiB bitmap memory budget.
  - **Flexible Mid-Cycle Plan Creation & Pinning**: Plan creation supports three first-cycle start options (*From phone record*, *Entered usage*, or *Start later*) and plan pinning to ensure your priority plan stays front and center.
  - **Expressive Components**: Wavy progress rings (`WavyRing`) and bars (`WavyBar`), mixed-corner bento tiles (`BentoTile`), vector `BrandMark`, dual-tone `NetworkBar`, responsive `NoticeLine`, segmented `ConnectedButtonGroup`, `ExpressiveSwitch`, and modern accessible modal dialogs (`EmberbyteDialog`).
  - **Four-Page Onboarding Pager**: Clean horizontal pager guiding users through core features, permissions, and notifications with 48 dp touch targets.
  - **Predictive Back Navigation**: Refined Android predictive back transitions across all sub-screens, with `GuardedBack` intercepting only dirty forms while allowing clean forms and back gestures to smoothly pop.
  - **Android 16 Live Updates**: Live notification promotion on Android 16 (API 36) with compact chip text in the status bar, daily dismissal suppression, and a dedicated Live updates management row in Settings.
- **Live Transfer Speed & Sampler**: Real-time foreground service sampling network rates, with a live download speed notification and Android 16+ Promoted Ongoing remaining data indicator.
- **Traffic Totals & Per-App History**: Breakdown of cellular mobile data and Wi-Fi network usage across 2-hour and daily intervals with real application icons.
- **Flexible Data Plans**: Support for custom billing cycles (monthly or every N days), SIM slot binding, add-on quota packs, free data windows (e.g. night data), and usage rollover.
- **Personalized Quota Forecast**: Predictive calculation of cap run-out dates based on a learned hourly consumption curve over the last 28 days.
- **Threshold & Budget Alerts**: Per-plan notifications at thresholds you choose from 50%, 80%, 90% and 100% (default 80% and 100%), plus per-app mobile data budgets.
- **Spike Detector**: Automated detection and alerting for anomalous mobile data consumption compared to a 14-day median baseline.
- **"What If" Scenario Planner**: Projection of quota run-out dates under hypothetical additional daily consumption.
- **Streamed Data Export**: Memory-efficient streaming export of historical usage metrics and plans to CSV and JSON formats.
- **Encrypted Local Backups**: Full backup and restore of plans, budgets, and hourly usage history using AES-256-GCM encryption with Argon2id password-based key derivation (`.emberbyte`).
- **Full Accessibility Compliance**: 120 verified matrix points in `docs/accessibility-audit.md`, full 2.0x font scaling reflow without clipping, RTL layout mirroring, and reduced motion snapping.
- **100% On-Device Privacy**: Operates completely offline without accounts, cloud sync, or `INTERNET` permission.
