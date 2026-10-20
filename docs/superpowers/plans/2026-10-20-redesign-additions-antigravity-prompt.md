# Prompt for Antigravity: the additions (predictive back and the live-update pill)

> Paste everything below the line into Antigravity, in the project directory, ONLY after Claude has approved STOP S5. It continues the same run on the same branch with three more tasks and one more stop.

---

# 0. Where we are

You are continuing the **Emberbyte expressive redesign** on branch **`redesign`**. Everything in `docs/superpowers/plans/2026-10-17-redesign-antigravity-prompt.md` still applies **unchanged**: the truth rules, the rules block of the plan, the `awk` brief extraction, the implementer and tester templates, the gate, one emulator only, `capture_png.py` and `check_pngs.py` for every screenshot, never push, merge or tag. Read sections 1, 3 and 4 of that prompt again before you start.

The user tried the app on their phone (a Galaxy S24+ on One UI 8.5) and asked for two refinements. They are written as **PART 7** of `docs/superpowers/plans/2026-10-17-emberbyte-redesign-m7-m11.md` (tasks **F1, F2, F3**), with **contract section 14** (`docs/superpowers/specs/2026-10-01-datakb-api-contract.md`) as the exact reference and two playable mockups: `docs/design/expressive/mockups/16-predictive-back.html` and `17-live-notification.html`. Open both mockups in a browser and read their source before you start.

| Task | What |
|---|---|
| F1 | Refined predictive back animation (screen shrinks, rounds, follows the finger; the previous screen waits behind it); no screen intercepts back unconditionally any more; dirty forms guard back with the same preview |
| F2 | Live-update promotion on Android 16 (API 36.0 through the extra, 36.1 through the builder), chip text per state, a "Live updates" row in Settings, dismissal respected |
| F3 | Docs, audit rows, end-to-end proof, then STOP S6 |

# 1. Rules for this part

1. Briefs come only from the `awk` command of the main prompt (`N=F1`, `F2`, `F3`). The extractor stops at the next `### Task` or `# PART` line, so the F3 brief ends at the end of the file.
2. Do not touch `docs/superpowers/` (Claude's files; the check of the main prompt, section 1.1, applies, including `grep -c "## 13. Milestones 7 to 11"` and now also `grep -c "## 14. Additions after the design audit"`, both must print 1).
3. **Never touch a physical phone.** The user's Galaxy S24+ is the only device that can show Samsung's pill and Now Bar, and only the user and Claude test it. Your F2 device proof is the emulator plus the written checklist (`docs/redesign-f2-phone-checklist.md`).
4. Lint: no new warning. The count at S5 is the baseline (0 errors; the S5 count after the lint fixes). `verifyReleaseApk` stays green.
5. Truth rules as before: show every new test failing first, computed counts from `tools/test-diff.py`, cited tests checked with `tools/check-cited-tests.py`, `CANNOT-VERIFY` where true (the pill and the Now Bar cannot be seen on the emulator; say so, do not claim them).
6. F1 and F2 touch different files (navigation and screens versus notification and settings); do them in order, one commit each, then F3.
7. Use the contract's exact names and the constants of the mockups; where a stock Compose or Navigation helper already does something (rounded corners during predictive back, the sheet back animation), use it and say so in the report.

# 2. Stop

After F3, make sure `git status` shows nothing modified apart from git-ignored files, nothing is pushed, then print the S6 message of task F3 and **wait**. Do not audit yourself, merge, push or tag.

Start by reading, confirm in one short message, and begin task F1.
