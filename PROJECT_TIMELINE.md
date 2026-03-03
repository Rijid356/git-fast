# Project Timeline

## Phase: Ideation & First Build
**Date:** Feb 16, 2026 to Feb 17, 2026
**Duration:** 2 days

git-fast started as a fitness tracking app with an RPG twist — turn real workouts into character progression. The idea was born from wanting to gamify dog walks and runs with XP, levels, and stat progression. Within 24 hours of creating the repo, the app had GPS tracking, dog walk recording, crash recovery, and a working character sheet.

### Screenshots
<img src="docs/screenshots/history/01-home-crash-recovery-dialog.png" alt="Home — Crash Recovery Dialog" width="180"> <img src="docs/screenshots/history/02-dog-walk-detail-first-test-walk.png" alt="First Test Walk Detail" width="180"> <img src="docs/screenshots/history/03-home-screen-with-recent-walk.png" alt="Home Screen v1" width="180"> <img src="docs/screenshots/history/04-history-dog-walks-filter.png" alt="History — Dog Walks Filter" width="180"> <img src="docs/screenshots/history/05-dog-walk-detail-with-gps-map.png" alt="Dog Walk GPS Map" width="180"> <img src="docs/screenshots/history/06-character-sheet-level1-empty.png" alt="Character Sheet — Level 1" width="180"> <img src="docs/screenshots/history/07-settings-screen-early.png" alt="Settings Screen" width="180">

### Highlights
- GPS workout tracking with route visualization from day one
- Dog walk and run workout types
- RPG character sheet with stats (SPD/END/CON)
- Crash recovery dialog for interrupted workouts
- Dark retro terminal UI theme

### Metrics
- **Time to First Working App:** 1 day
- **Screens Built:** 6 (Home, History, Detail, Character Sheet, Settings, Active Workout)
- **GPS Points Tracked:** 521 on first walk

---

## Phase: XP System & Lap Mode
**Date:** Feb 18, 2026 to Feb 18, 2026
**Duration:** 1 day

Day 2 brought the core RPG mechanics to life. Added an XP system that rewards workouts, a lap mode with ghost runner comparison, phase breakdowns (warmup/laps/cooldown), and the iconic PressStart2P pixel font. The character sheet went from empty to earning real XP from completed workouts.

### Screenshots
<img src="docs/screenshots/history/08-walk-complete-save-form.png" alt="Walk Complete Form" width="180"> <img src="docs/screenshots/history/09-home-screen-pixel-font-xp.png" alt="Home Screen v2 — Pixel Font" width="180"> <img src="docs/screenshots/history/10-character-sheet-first-xp-earned.png" alt="Character Sheet — First XP" width="180"> <img src="docs/screenshots/history/11-active-workout-lap-mode-ghost.png" alt="Active Workout — Lap Mode" width="180"> <img src="docs/screenshots/history/12-run-details-phases-lap-chart.png" alt="Run Details — Phases" width="180"> <img src="docs/screenshots/history/13-run-details-lap-table-gps-map.png" alt="Run Details — Laps & Map" width="180"> <img src="docs/screenshots/history/14-character-sheet-level3-achievements.png" alt="Character Sheet — Level 3" width="180">

### Highlights
- XP system with per-workout breakdown
- Lap mode with ghost runner time delta
- Phase breakdown (Warmup → Laps → Cooldown)
- Lap trend chart showing "Getting faster"
- Achievement system (First Mile, First Steps)
- PressStart2P pixel font for retro RPG aesthetic

### Metrics
- **Achievements Unlocked:** 2 of 21
- **Max Level Reached:** 3 (184 XP)
- **Lap Mode Features:** Ghost runner, delta tracking, trend chart

---

## Phase: Character Profiles & Pixel Art
**Date:** Feb 18, 2026 to Feb 20, 2026
**Duration:** 3 days

The character sheet evolved into a dual-profile system with pixel art avatars. Added separate tabs for the user (runner) and their dog (Juniper), each with their own level, stats, XP, and achievements. Walk completion form got weather chips, route selection, and energy level tracking.

### Screenshots
<img src="docs/screenshots/history/15-character-sheet-v2-pixel-avatar-me.png" alt="Character Sheet v2 — Me Tab" width="180"> <img src="docs/screenshots/history/16-character-sheet-v2-juniper-tab.png" alt="Character Sheet v2 — Juniper Tab" width="180"> <img src="docs/screenshots/history/17-run-details-phone-view.png" alt="Run Details (revisited)" width="180"> <img src="docs/screenshots/history/18-walk-complete-form-with-selections.png" alt="Walk Complete — With Selections" width="180">

### Highlights
- Dual character profiles (Me + Juniper the dog)
- Pixel art avatars for runner and dog
- Streak tracking with XP multiplier
- Dog-specific achievements (0/6 category)
- Weather, route, and energy tracking on walk completion

### Metrics
- **Character Profiles:** 2 (Runner + Dog)
- **Achievement Categories:** Runner (21) + Dog (6)
- **Streak Multiplier:** 1.1x at 2-day streak

---

## Phase: Sprint Detection & Health Integration
**Date:** Feb 25, 2026 to Feb 25, 2026
**Duration:** 1 day

Added Dog Run as a new workout type with automatic sprint detection — the app detects when you're running fast and tracks sprint segments separately. Integrated with Android Health Connect for body composition syncing. Added Roborazzi screenshot testing for CI and navigate-away handling for active workouts.

### Highlights
- Dog Run workout type with auto sprint detection
- Live speed display during workouts
- Health Connect integration for body composition data
- Screenshot testing with Roborazzi in CI pipeline
- Navigate-away protection with return banner
- Android Lint integration in CI

### Metrics
- **PRs Merged (this phase):** 12
- **New Workout Type:** Dog Run with sprint tracking
- **CI Checks Added:** Lint + Screenshot verification

---

## Phase: Adventure Log & Weekly Stats
**Date:** Feb 26, 2026 to Feb 28, 2026
**Duration:** 3 days

The most feature-rich phase. Added Juniper's Adventure Log — a dog walk event logger with emoji map markers and a radial event wheel. Built a weekly summary card for the home screen. Introduced exercise data model with RPG stat integration, soreness check-in with toughness tracking, and status bar chronometer. The app narrative was rewritten for an epic RPG tone.

### Screenshots
<img src="docs/screenshots/history/19-exercise-model-home.png" alt="Home — Exercise Model" width="180"> <img src="docs/screenshots/history/20-exercise-model-character-sheet-me.png" alt="Character Sheet — Exercise Stats" width="180"> <img src="docs/screenshots/history/21-exercise-model-character-sheet-juniper.png" alt="Character Sheet — Juniper Exercise" width="180"> <img src="docs/screenshots/history/22-dog-walk-event-strip.png" alt="Dog Walk Event Strip" width="180"> <img src="docs/screenshots/history/23-dog-run-event-strip.png" alt="Dog Run Event Strip" width="180"> <img src="docs/screenshots/history/24-dog-run-sprinting-event-strip.png" alt="Dog Run Sprinting" width="180"> <img src="docs/screenshots/history/25-character-sheet-juniper-after-events.png" alt="Juniper — After Events" width="180"> <img src="docs/screenshots/history/26-stop-confirmation-dialog.png" alt="Stop Confirmation Dialog" width="180"> <img src="docs/screenshots/history/27-back-confirmation-dialog.png" alt="Back Confirmation Dialog" width="180"> <img src="docs/screenshots/history/28-dog-walk-detail-narrative-rewrite.png" alt="Dog Walk Detail — Epic Narrative" width="180"> <img src="docs/screenshots/history/29-sprint-mph-display-fix.png" alt="Sprint MPH Display" width="180">

### Highlights
- Dog walk event logger (Juniper's Adventure Log) with emoji map markers
- Radial event wheel FAB replacing the event strip
- Weekly summary card on home screen
- Exercise data model with RPG stat integration
- Soreness check-in system with toughness stat
- Status bar chronometer showing workout time
- Epic narrative rewrite across the app

### Metrics
- **Total PRs Merged:** 100
- **Checkpoints Completed:** 23
- **Screenshot Comparisons:** 6 dated folders
- **Event Types:** Dog walk events with emoji markers

---

## Phase: Security Hardening & Test Coverage
**Date:** Mar 1, 2026 to Mar 2, 2026
**Duration:** 2 days

Shifted focus from features to foundations. Ran a full OWASP Top 10 security audit and fixed every finding — removed PII logging, hardcoded coordinates, disabled Android backup, added Firestore security rules, enabled R8 with ProGuard, and hardened CI. Wrote 114 new unit tests across 8 packages, pushing coverage from patchy to solid. Dependency bumps via Dependabot, custom notification icon, and CI fixes for fork PRs.

### Highlights
- Full OWASP security audit with all findings resolved
- Firestore security rules and R8/ProGuard obfuscation enabled
- 114 new unit tests across 8 packages (mappers, repos, settings)
- CI hardened: Dependabot support, fallback google-services.json
- Custom running man notification icon
- Character sheet polish and activity goals page removed

### Metrics
- **PRs Merged (this phase):** 16
- **New Unit Tests:** 114
- **Security Findings Fixed:** All (PII logging, hardcoded coords, backup, sign-out cleanup)
- **Dependencies Updated:** 6 (via Dependabot)

---

## Phase: Observability, Optimization & New Features
**Date:** Mar 2, 2026 to Mar 3, 2026
**Duration:** 2 days

Added production-grade observability with Crashlytics, Timber structured logging, and persistent file logging. Optimized performance by fixing O(n^2) GPS processing, adding batch DAO queries, and deduplicating UI. Built multi-park lap start points that auto-save locations. Added FOR and STR stats to the character sheet. Shipped an in-app screenshot capture overlay for field testing, and restructured the screenshot directory for long-term organization.

### Highlights
- Crashlytics crash reporting, Timber logging, persistent file logs
- Performance optimization: batch queries, O(n^2) GPS fix, UI dedup
- Dog walk narrative persisted to database
- Multi-park lap start points with auto-save
- FOR and STR stats displayed on character sheet
- In-app screenshot capture with floating overlay button
- Screenshot directory restructured (current/ + history/)
- T-Watch S3 firmware docs extracted to watch/CLAUDE.md

### Metrics
- **Total PRs Merged:** 130
- **PRs Merged (this phase):** 14
- **Observability:** Crashlytics + Timber + file logging
- **New RPG Stats:** FOR (Fortitude) and STR (Strength) on character sheet
