# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
