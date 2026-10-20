# Contributors

Emberbyte is a small project and everybody who shaped it is listed here, with what their commits in this repository actually contain (taken from `git log`, not from memory).

## Khaled Bahaa Eldin ([@Khaledbahaaeldin](https://github.com/Khaledbahaaeldin))

Project owner and maintainer: product, architecture, specifications, the design of the expressive redesign, reviews and audits, release and store preparation.

## Omar Amin ([@OmarAminAI](https://github.com/OmarAminAI))

Implementation and testing, 16 commits between 10 and 13 October 2026.

**In the released app:**
- History: the charts now open on the newest period instead of the oldest (`fix(history): open the charts on the newest period`).
- Alerts and launch: made the alert lock test deterministic and added coverage for the new-intent fix on the real activity (`MainActivityNewIntentTest`, `ColdStartLinkTest`, `AlertEvaluatorTest`).
- Home: the 7-day usage row opens at its start (`HomeScreen`, `UsageBarRow` and its test).
- The project's domain glossary, `CONTEXT.md`, which defines the vocabulary used across the code (data entry, plan, cycle and the rest).

**Research that shaped the roadmap (Milestone 5, "Live Lens", not shipped):**
- A userspace packet forwarder spike for a local per-app and per-domain view: parsers for IP, TCP, UDP, DNS and TLS server names written not to trust their input, TCP and UDP relays, the VPN service glue, reproducible measurement scripts, tests that kill surviving mutants and fuzz the parsers, and a written decision record.
- Its measured result (the forwarder stayed far above its CPU budget) is why Live Lens was discarded and why Emberbyte has no VPN and no `INTERNET` permission. That work is kept on the `m5-live-lens` branch as a record.

## Tooling

Parts of the implementation were produced with Gemini (Antigravity); those commits carry a `Co-Authored-By` trailer. Specifications, plans and audits were written with Claude (Anthropic).

## Third-party credits

Theme palettes, fonts and libraries are credited in [NOTICE](NOTICE) with their licences under `licenses/`.
