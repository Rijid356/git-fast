# Project Stats

## Overview
- **Project:** git-fast
- **Started:** 2026-02-17
- **Last Active:** 2026-02-28
- **Tech Stack:** Kotlin, Jetpack Compose, Android (API 26+), Room, Hilt
- **Key Dependencies:** androidx-compose-bom, hilt-android, room-runtime, room-ktx, play-services-location, kotlinx-coroutines, material3, maps-compose, health-connect-client

## Activity Log

| Date | Summary | Files Changed | Commits | Lines (+/-) |
|------|---------|--------------|---------|-------------|
| 2026-02-17 | Checkpoints 0-3: Project skeleton, data layer, GPS service, real-time calculations | 52 | 4 | +3965/-0 |
| 2026-02-17 | Schema update: Add dog walk activity type, enums, RouteTagEntity, metadata fields, tests | 19 | 1 | +811/-17 |
| 2026-02-17 | Checkpoint 4: Active workout screen with live stats, permissions, navigation | 16 | 1 | +1020/-150 |
| 2026-02-17 | Checkpoint 5: Save completed workouts, crash recovery, summary screen, tests | 17 | 1 | +818/-91 |
| 2026-02-17 | Checkpoint 6: Workout history screen, date formatting, home preview, tests | 12 | 1 | +706/-9 |
| 2026-02-17 | Checkpoint 7: Workout detail screen with Google Maps route display, tests | 10 | 1 | +700/-70 |
| 2026-02-17 | Checkpoint 8: Lap tracking with phase flow (warmup/laps/cooldown), lap UI, tests | 17 | 1 | +748/-79 |
| 2026-02-17 | Checkpoint 9: Lap analysis with phase breakdown, trend chart, lap table, tests | 19 | 1 | +1084/-0 |
| 2026-02-17 | Checkpoint 10: Dog walk activity type with post-walk metadata, route comparison, tests | 29 | 1 | +1536/-83 |
| 2026-02-17 | Checkpoint 11: Auto-pause based on GPS speed for running workouts | 20 | 1 | +512/-26 |
| 2026-02-17 | Checkpoint 12: Settings screen with distance unit, auto-pause, and keep-screen-on preferences | 13 | 1 | +501/-11 |
| 2026-02-17 | Checkpoint 13: Post-workout flow fix — VIEW DETAILS button and dog walk navigation | 5 | 1 | +159/-19 |
| 2026-02-17 | Checkpoint 14: Post-test-walk bug fixes — crash recovery, active workout resume, split home sections, map hardening | 7 | 1 | +65/-10 |
| 2026-02-17 | Checkpoint 15: Derived character stats (SPD/END/CON) with bracket interpolation, Room migration v3→v4, stat bars UI, 20 unit tests | 14 | 1 | +758/-3 |
| 2026-02-17 | Add 32 ViewModel unit tests (MockK), fix XpCalculatorTest distance precision, CI fixes | 10 | 5 | +727/-11 |
| 2026-02-17 | Final XpCalculatorTest fix: use 1610.0m for reliable mile conversion, merge PR #25 | 1 | 1 | +9/-9 |
| 2026-02-18 | Full retro pixel art UI overhaul: Press Start 2P font, square corners, pixel app icon | 17 | 3 | +135/-102 |
| 2026-02-18 | Ghost runner: lap-based ghost comparison during workouts with auto/manual ghost selection, 10 unit tests | 11 | 1 | +430/-15 |
| 2026-02-18 | Streaks and multipliers: daily workout streak tracking with XP multiplier bonuses (1.0x-1.5x), streak UI on home/character/summary screens, 16 unit tests | 16 | 4 | +479/-20 |
| 2026-02-18 | Field test improvements: GPS lap pins, micro-lap discard, auto-lap detection, portrait lock, bigger text, sunlight readability, button fixes | 20 | 1 | +217/-47 |
| 2026-02-19 | Replace distance-based auto-lap with GPS start/finish anchor detection, configurable radius (10-25m), leave-and-return + cooldown logic, 8 unit tests | 9 | 2 | +268/-29 |
| 2026-02-19 | Update CLAUDE.md with comprehensive architecture docs: RPG system, ghost runner, crash recovery, DB v7 migration history, navigation gotchas, toolchain versions, watch/ firmware section | 1 | 1 | +58/-16 |
| 2026-02-19 | Dog walk UI polish: "START DOG WALK" text + cyan button color, raised active-walk buttons (96dp), notification shows elapsed time instead of pace | 3 | 1 | +12/-8 |
| 2026-02-19 | Enhanced workout notification: BigTextStyle expandable view, activity-aware content (run vs dog walk), Pause/Resume action buttons, separate 3s notification timer, 10 unit tests | 5 | 1 | +285/-37 |
| 2026-02-20 | Fix dog walk button reverting to "START RUN" after service binds — collectWorkoutState() was clobbering activityType with state manager default | 1 | 1 | +5/-1 |
| 2026-02-20 | Redesign app icon (side-profile running man, multi-color palette) and character sheet sprites (JRPG Hero runner, tri-color Aussie dog), add monochrome icon layer | 5 | 1 | +722/-682 |
| 2026-02-20 | Dog walk summary polish (title/color/padding/routes/windy) + real-time notification chronometer | 6 | 2 | +33/-35 |
| 2026-02-20 | Tap-to-expand stat breakdowns on Character Sheet: animated panels showing formula, inputs, brackets, decay notes for SPD/END/CON | 4 | 1 | +284/-32 |
| 2026-02-20 | Home arrival auto-pause: GPS geofence pauses workout at home with Stop/Resume notification, Settings UI for home location capture, 9 unit tests | 10 | 2 | +531/-10 |
| 2026-02-20 | Checkpoint 15: Analytics hub screen with lifetime stats, 6 section cards for future analytics features, navigation from HomeScreen, getLongestStreak(), 12 unit tests | 10 | 4 | +621/-0 |
| 2026-02-20 | Checkpoint 16: Route overlay comparison — GPS trace visualization on map with color-coded polylines, route tag selector, interactive zoom/pan, trace legend, 7 unit tests | 7 | 2 | +770/-2 |
| 2026-02-21 | Checkpoint 17: Route performance table — session history per route tag with trend summary banner, personal best highlighting, delta comparisons, tap-to-detail navigation, 11 unit tests | 7 | 1 | +929/-6 |
| 2026-02-21 | Checkpoint 18: Personal records dashboard — all-time bests for runs (fastest pace, longest run/duration, best lap), dog walks (longest walk/duration, most steps), overall (streak, totals), tap-to-detail, 15 unit tests | 13 | 2 | +846/-1 |
| 2026-02-21 | Fix empty route tag dropdowns (upsert save + fallback query from workout records), add long-press to delete lap from workout history with confirmation dialog | 15 | 2 | +126/-24 |
| 2026-02-21 | Remove duplicate time from notification text (chronometer handles it), show elapsed time in status bar even when paused | 3 | 1 | +16/-22 |
| 2026-02-21 | Firebase cloud backup: Google Sign-In, Firestore bidirectional sync, fire-and-forget post-workout push, Cloud Backup settings UI, 36 unit tests | 20 | 1 | +1592/-5 |
| 2026-02-22 | Checkpoint 19: Weekly & monthly trends — TrendsCalculator with ISO week/month grouping, period-over-period comparison cards with delta badges, pixel bar charts, activity filtering (ALL/RUNS/WALKS), 17 unit tests | 9 | 0 | +~880/-0 |
| 2026-02-23 | Remove google-services.json from git tracking, add to .gitignore, CI decodes from GitHub secret | 3 | 1 | +6/-47 |
| 2026-02-23 | Ship analytics trends: PR #66 merged — TrendsCalculator, pixel bar charts, TrendsScreen, navigation, tests | 9 | 1 | +1228/-1 |
| 2026-02-23 | Increase test coverage 33.8%→35.4% line, 32.7%→33.9% branch: 28 new tests across StatsCalculator, WorkoutSaveManager, HomeViewModel, DetailViewModel; raised Kover thresholds to 32%/28% | 5 | 0 | +437/-15 |
| 2026-02-24 | Easy-win test coverage 35.8%→36.9% line: 81 new tests — SettingsViewModel (22), CharacterRepository (24), WorkoutRepository +13, WorkoutStateManager branches (22); merged PR #74 | 10 | 1 | +1411/-0 |
| 2026-02-24 | GitHub issue workflow: created issues #72 (Roborazzi) and #73 (Weight Tracking) from plan files, added Plans→GitHub Issues bidirectional sync rule to global CLAUDE.md | 0 | 0 | +0/-0 |
| 2026-02-24 | Remove home arrival radius selector, hardcode 15m (~50ft): dropped HomeArrivalRadiusItem composable, removed setHomeArrivalRadius from ViewModel/UiState, updated tests; merged PR #87 | 4 | 1 | +2/-68 |
| 2026-02-24 | FirestoreSync test coverage 0%→100%: 29 new tests covering all push/pull methods, auth guards, error handling, edge cases; overall 36.9%→38.9% line; merged PR #88 | 2 | 1 | +700/-5 |
| 2026-02-24 | Checkpoint 15: Daily activity goals & activity rings — 3-ring visualization (minutes/distance/active days) on home screen, Goals Settings screen with AHA defaults, date-range DAO queries, 9 new tests; Phase 5 milestone + 4 skeleton issues created; merged PR #94 | 19 | 1 | +920/-12 |
| 2026-02-26 | Health Connect body comp tracking: 7-agent parallel implementation — HC data layer, Body Comp screen with trend charts, VIT stat + weigh-in XP + achievements, Character Sheet VIT bar, Home weight display, Settings HC permissions, 873 tests passing; merged PR #97 | 53 | 15 | +4024/-20 |
| 2026-02-26 | Settings cleanup + UX fixes: remove anchor radius selector (hardcode 5m), remove distance unit selector (hardcode Miles), Body Comp "Sync Now" button on empty state, Cloud Backup sign-in error feedback, 2 new tests; merged PR #98 | 19 | 1 | +132/-185 |
| 2026-02-26 | Roborazzi screenshot testing: 12 golden screenshots across 5 composables, ScreenshotTestBase with GitFastTheme wrapper, 5% cross-platform threshold, CI verify (WARN) + diff artifacts; merged PR #99 | 23 | 4 | +319/-0 |
| 2026-02-27 | Body Comp sync feedback: SyncResult sealed class, snackbar messages for all sync outcomes, simplified Settings sync, updated tests; PR #101 | 6 | 1 | +68/-31 |
| 2026-02-28 | Android Lint fixes + CI integration: fix 2 UnrememberedMutableState errors, 6 DefaultLocale warnings, CredentialManagerMisuse, 6 ModifierParameter reorders; lint config in build.gradle.kts, CI lint step, quality-gates.json | 13 | 1 | +63/-22 |
| 2026-02-28 | Wire VIT stat to Health Connect data: inject BodyCompRepository + HealthConnectManager into CharacterSheetViewModel, load real weigh-in count and body fat trend in init; merged PR #103 | 2 | 1 | +55/-9 |
| 2026-02-28 | Full-screen screenshot composites: 15 Roborazzi test files generating 21 Pixel 9 screenshots, Canvas-based Google Maps mocks, HTML phone frame journeys with CSS Pixel 9 bezels, Playwright composite capture, Gradle integration | 57 | 1 | +2817/-0 |
| 2026-03-02 | Checkpoint 15: Weekly summary card on home screen — "THIS WEEK" card with active min/distance/workouts deltas vs last week, active days progress bar, WeeklyMetrics model, 6 weekly repo methods, nested combine in ViewModel; merged PR #122 | 16 | 1 | +318/-0 |
| 2026-03-02 | Fix Firestore sync: add totalDistanceMeters/activeDurationMs/averagePaceMs aggregate fields to workout push, fix missing vitalityStat in CharacterProfile mapper; merged PR #123 | 4 | 1 | +37/-2 |
| 2026-03-03 | Fix Dog Run button color consistency (amber on workout screen to match home), add lap start point GPS auto-start feature, 4 new DOG_RUN screenshot tests, micro-lap threshold 5s→30s; merged PR #124 | 18 | 1 | +356/-20 |
| 2026-03-03 | Auto-start laps test suite: 12 tests for GPS-based warmup→laps transition, endLaps lapCount fix, lessons learned entry; merged PR #125 | 4 | 1 | +244/-6 |
| 2026-03-03 | Dog Walk Event Logger (Issue #95): DogWalkEventType enum, DogWalkEventStrip UI, WorkoutStateManager event tracking, DogWalkNarrativeGenerator, EventTimeline/EventRouteMap detail views, Forage stat, 7 Juniper achievements, Room v8→v9 migration, Firestore sync, settings auto-start laps, 63 new tests, updated screenshots; merged PR #126 | 64 | 5 | +2314/-32 |
| 2026-03-04 | Clean up CLAUDE.md: remove 10+ hardcoded library versions, replace growing lists with file pointers, fix stale DB version/migration/routes/specs references; clean up project settings.local.json (105→48 entries); merged PR #127 | 1 | 1 | +16/-19 |
| 2026-03-05 | Show workout chronometer in status bar; merged PR #128 | — | 1 | — |
| 2026-03-05 | Archive 18 historical screenshots (Feb 17-20) with annotated README, dog walk event improvements, new achievements and narrative tests; merged PR #129 | 28 | 1 | +205/-21 |
| 2026-03-06 | Polish stop/back-handler workout dialogs: custom pixel-art styled modals with big color-coded buttons, square corners, centered overlay; 2 new screenshot tests with before/after comparisons; global UI-change hook; merged PR #131 | 7 | 1 | +449/-60 |
| 2026-03-06 | Checkpoint 22: Soreness check-in + TGH stat — daily soreness logging (11 muscle groups, 3 intensities), Toughness RPG stat with 30-day bracket interpolation, 4 RECOVERY achievements, soreness XP, home "Feeling sore?" card, Room v9→v10 migration, 38 new tests; merged PR #132 | 42 | 4 | +1584/-19 |
| 2026-03-07 | Checkpoint 23: Exercise data model — ~80-exercise catalog (bodyweight/dumbbell/pull-up bar), ExerciseSession/Set domain+entity models, Room v10→v11 migration, STR stat with bracket interpolation, session XP formula, 6 FITNESS achievements, unified streak calc, 69 new tests; PR #133 | 32 | 3 | +1990/-4 |
| 2026-02-26 | Dog walk detail emoji markers + history cleanup: emoji map markers (🍖💩🐾⚡ etc.) on RouteMap/EventRouteMap via Canvas bitmap, event pins on detail screen map, remove redundant "Juniper" from history subtitle; merged PR #134 | 5 | 1 | +59/-10 |
| 2026-02-27 | Fix event wheel centering (onGloballyPositioned + single Popup), remove Zoomies event type, update achievements/narrative/tests, before/after comparisons; merged PR #135 | 17 | 1 | +131/-121 |
| 2026-02-27 | Organize screenshot comparisons into dated subfolders, update /ui-diff skill for YYYY-MM-DD folder format, clean up .gitignore; merged PR #136 | 13 | 1 | +5/-1 |
| 2026-02-28 | Fix sprint stuck on null GPS speed (timeout safety nets) and show MPH during sprinting (SpeedDisplay hero + sprint timer below), 12 new AutoSprintDetector tests; PR #139 | 5 | 1 | +248/-9 |
| 2026-02-28 | Epic narrative rewrite + remove redundant dog name: dynamic RPG narrative engine with randomized word pools, combo prefixes (Berserker mode/Diplomacy maxed/Legendary foraging), remove 🐕 Juniper metadata row from detail screen, HUMAN_FRIEND event type commit, 3 new tests; merged PR #138 | 8 | 5 | +523/-116 |

## Totals
- **Total Sessions:** 68
- **Total Commits:** 116
- **Total Files Changed:** 943
- **Total Lines Added:** 44019
- **Total Lines Removed:** 2537
