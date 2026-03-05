# File Manifest

Quick-reference for every source file in git-fast. Read this once for full project context.

All paths relative to `app/src/main/java/com/gitfast/app/` unless noted otherwise.

## Root

| File | Purpose |
|------|---------|
| `GitFastApp.kt` | Hilt `@HiltAndroidApp` application class |
| `MainActivity.kt` | Single activity hosting Compose nav graph and active workout banner |

## auth/

| File | Purpose |
|------|---------|
| `GoogleAuthManager.kt` | Google Sign-In via Credential Manager + Firebase Auth linking |

## di/

| File | Purpose |
|------|---------|
| `AppModule.kt` | Placeholder Hilt module for future app-wide bindings |
| `ServiceModule.kt` | Provides GpsTracker, StepTracker, WorkoutStateManager, PermissionManager, AutoPauseDetector |
| `DatabaseModule.kt` | Provides Room DB, DAOs, repositories, Firestore sync, Health Connect manager |
| `FirebaseModule.kt` | Provides Firebase Auth and Firestore singleton instances |

## service/

| File | Purpose |
|------|---------|
| `WorkoutService.kt` | Foreground LifecycleService for GPS/step tracking, intent-controlled actions, notifications |
| `WorkoutStateManager.kt` | `@Singleton` in-memory state for phases, laps, ghost runner, auto-lap; shared via binder |
| `AutoPauseDetector.kt` | Analyzes GPS speed to trigger auto-pause/resume during workouts |
| `NotificationContent.kt` | Builds foreground notification content (title, collapsed/expanded text) |

## location/

| File | Purpose |
|------|---------|
| `GpsTracker.kt` | Flow-based FusedLocationProvider tracking (2s interval, 3m displacement) |
| `StepTracker.kt` | Flow-based TYPE_STEP_COUNTER sensor reader |

## analysis/

| File | Purpose |
|------|---------|
| `DistanceTimeProfile.kt` | Cumulative distance-time profile from GPS points with binary search interpolation |
| `RouteComparisonAnalyzer.kt` | Compares current dog walk to previous same-route walks with delta formatting |
| `RouteGhostCalculator.kt` | Calculates route ghost delta by comparing current walk against historical profiles |

## util/

| File | Purpose |
|------|---------|
| `DistanceCalculator.kt` | Haversine GPS distance calculation; m↔mi↔km unit conversions |
| `PaceCalculator.kt` | Average pace, current pace (rolling window), segment pace |
| `TimeFormatter.kt` | Formats elapsed time (HH:MM:SS) and pace (MM:SS /mi or /km) |
| `DisplayFormatter.kt` | Formats distance with unit label (miles vs kilometers) |
| `LapAnalyzer.kt` | Lap analysis with trend detection (faster/slower/consistent); requires ≥3 laps |
| `PhaseAnalyzer.kt` | Formats workout phase breakdown (warmup/laps/cooldown) |
| `PersonalRecordsCalculator.kt` | Computes PRs: fastest pace, longest run/walk, best lap, streak records |
| `TrendsCalculator.kt` | Groups workouts by week/month; calculates period summaries and deltas |
| `XpCalculator.kt` | XP from distance/duration/laps/weather/streak; leveling formula |
| `StatsCalculator.kt` | RPG stats (SPD/END/CON/VIT) via bracket interpolation (1-99 scale) |
| `StreakCalculator.kt` | Current/longest streak tracking; multiplier 1.0x–1.5x with 1-day grace |
| `AchievementDef.kt` | Enum defining 30+ achievements with XP rewards, icons, categories, profileId |
| `AchievementChecker.kt` | Evaluates achievement unlock conditions for user and Juniper profiles |
| `PermissionManager.kt` | Checks location, notification, activity recognition permissions |
| `DogWalkNarrativeGenerator.kt` | Template-based narrative from dog walk events with special combos |
| `ScreenCaptureManager.kt` | PixelCopy screen capture + MediaStore gallery save + Room screenshot tracking |

## navigation/

| File | Purpose |
|------|---------|
| `GitFastNavGraph.kt` | Sealed `Screen` routes; Compose NavHost with 16 destinations |

## data/model/

| File | Purpose |
|------|---------|
| `Workout.kt` | Domain model for completed workout with nested phases, GPS, metadata |
| `WorkoutPhase.kt` | Domain model for phase containing laps and aggregated distance/steps |
| `Lap.kt` | Domain model for lap with duration, distance, pace, split coordinates |
| `GpsPoint.kt` | Domain model for GPS coordinate with timestamp, speed, accuracy |
| `CharacterProfile.kt` | Domain model for RPG character (XP, level, stats) |
| `CharacterStats.kt` | Data class for stat values: speed, endurance, consistency, vitality (1-99) |
| `XpTransaction.kt` | Domain model for XP award with reason, amount, timestamp |
| `UnlockedAchievement.kt` | Domain model for achievement with unlock timestamp and XP awarded |
| `BodyCompReading.kt` | Domain model for Health Connect body composition reading |
| `DailyActivityMetrics.kt` | Daily totals with goals and computed progress percentages |
| `WeeklyMetrics.kt` | Weekly summary totals with previous-week comparison data |
| `ActivityType.kt` | Enum: RUN, DOG_WALK |
| `PhaseType.kt` | Enum: WARMUP, LAPS, COOLDOWN |
| `WorkoutStatus.kt` | Enum: ACTIVE, PAUSED, COMPLETED |
| `DistanceUnit.kt` | Enum: MILES, KILOMETERS |
| `EnergyLevel.kt` | Enum: LOW, NORMAL, HYPER (dog energy during walk) |
| `WeatherCondition.kt` | Enum: SUNNY, CLOUDY, RAINY, SNOWY, WINDY |
| `WeatherTemp.kt` | Enum: HOT, WARM, MILD, COOL, COLD |
| `DogWalkEventType.kt` | Enum: 7 event types (SNACK_FOUND, POOP, PEE, etc.) with displayName, icon, category |
| `DogWalkEvent.kt` | Domain model: dog walk event with type, timestamp, GPS coordinates |
| `EventCategory.kt` | Enum: FORAGING, BATHROOM, ENERGY, SOCIAL (categories for event types) |
| `MuscleGroup.kt` | Enum: 11 muscle groups (CHEST through CALVES) with displayName |
| `SorenessIntensity.kt` | Enum: MILD, MODERATE, SEVERE with XP bonus values |
| `SorenessLog.kt` | Domain model: daily soreness log with muscle groups, intensity, notes |
| `Equipment.kt` | Enum: BODYWEIGHT, PULL_UP_BAR, DUMBBELL with displayName |
| `Difficulty.kt` | Enum: BEGINNER, INTERMEDIATE, ADVANCED with displayName |
| `ExerciseCategory.kt` | Enum: PUSH, PULL, LEGS, CORE, FULL_BODY, CARDIO with displayName |
| `Exercise.kt` | Domain model: exercise definition with equipment, category, muscles, difficulty |
| `ExerciseCatalog.kt` | In-memory catalog of ~80 exercises (bodyweight, pull-up bar, dumbbell) with lookup methods |
| `ExerciseSession.kt` | Domain model: exercise session with start/end time, sets, notes, XP |
| `ExerciseSet.kt` | Domain model: single exercise set with reps, weight, duration, warmup flag |

## data/local/

| File | Purpose |
|------|---------|
| `GitFastDatabase.kt` | Room database v15: 15 entity tables, DAOs, migration chain, schema export |
| `ExerciseDao.kt` | DAO for exercise sessions/sets: insert, query by date, counts, @Transaction save |
| `SorenessDao.kt` | DAO for soreness logs: insert, update, observe by date, count queries |
| `WorkoutDao.kt` | DAO for workouts, phases, laps, GPS points, route tags; `@Transaction` upsert |
| `CharacterDao.kt` | DAO for character profiles, XP transactions, unlocked achievements |
| `BodyCompDao.kt` | DAO for body composition entries (weight, fat%, BMR, height) |
| `Converters.kt` | Room type converters for Instant and enums (stored as TEXT) |
| `LapStartPointDao.kt` | DAO for saved lap start GPS points: insert, getAll, deleteAll, observeCount |
| `ScreenshotDao.kt` | DAO for screenshots: insert, getAll, getByWorkoutId, getRecent, deleteById |
| `SettingsStore.kt` | SharedPrefs wrapper for app settings (auto-pause, units, goals, home location, screenshot overlay) |
| `WorkoutStateStore.kt` | SharedPrefs for crash recovery — persists active workout ID |

## data/local/entity/

| File | Purpose |
|------|---------|
| `WorkoutEntity.kt` | Room entity: workout record (id, timestamps, distance, status, activityType) |
| `WorkoutPhaseEntity.kt` | Room entity: workout phase with FK to workout, cascade delete |
| `LapEntity.kt` | Room entity: lap with split coordinates, duration, distance |
| `GpsPointEntity.kt` | Room entity: dense GPS track point with speed, accuracy, sort index |
| `RouteTagEntity.kt` | Room entity: named dog walk route (name, createdAt, lastUsed) |
| `CharacterProfileEntity.kt` | Room entity: character level, XP, 7 stats (STR added in v11) |
| `XpTransactionEntity.kt` | Room entity: XP audit log; idempotent key = (workoutId, profileId) |
| `UnlockedAchievementEntity.kt` | Room entity: achievement unlock with composite key (achievementId, profileId) |
| `BodyCompEntry.kt` | Room entity: Health Connect body comp reading (weight, fat, BMR, height) |
| `DogWalkEventEntity.kt` | Room entity: dog walk event with FK to workout, GPS, cascade delete |
| `LapStartPointEntity.kt` | Room entity: saved lap start GPS point for multi-park auto-start |
| `SorenessLogEntity.kt` | Room entity: soreness log with comma-separated muscle groups, intensity, date index |
| `ExerciseSessionEntity.kt` | Room entity: exercise session with start/end time, notes, XP awarded |
| `ExerciseSetEntity.kt` | Room entity: exercise set with FK to session, reps, weight, warmup flag, cascade delete |
| `ScreenshotEntity.kt` | Room entity: screenshot metadata (timestamp, filename, galleryUri, workoutId, screenRoute) |

## data/local/mappers/

| File | Purpose |
|------|---------|
| `WorkoutMappers.kt` | Entity↔domain conversion for Workout, WorkoutPhase, Lap, GpsPoint |
| `BodyCompMappers.kt` | Entity↔domain conversion for BodyCompEntry with kg↔lbs and BMI calc |
| `DogWalkEventMappers.kt` | Entity↔domain conversion for DogWalkEvent with Instant↔Long |
| `SorenessMappers.kt` | Entity↔domain conversion for SorenessLog with date↔millis mapping |
| `ExerciseMappers.kt` | Entity↔domain conversion for ExerciseSession and ExerciseSet with Instant↔Long |

## data/local/migrations/

| File | Purpose |
|------|---------|
| `Migration_1_2.kt` | v1→v2: Add speed column to gps_points |
| `Migration_2_3.kt` | v2→v3: Create character_profiles and xp_transactions tables |
| `Migration_3_4.kt` | v3→v4: Add stat columns (speed, endurance, consistency) to character_profiles |
| `Migration_4_5.kt` | v4→v5: Create unlocked_achievements table |
| `Migration_5_6.kt` | v5→v6: Add profileId to XP/achievements; seed Juniper profile (id=2) |
| `Migration_6_7.kt` | v6→v7: Add splitLatitude/splitLongitude to laps |
| `Migration_7_8.kt` | v7→v8: Create body_comp_entries table; add vitalityStat to character_profiles |
| `Migration_8_9.kt` | v8→v9: Create dog_walk_events table; add foragingStat to character_profiles |
| `Migration_9_10.kt` | v9→v10: Create soreness_logs table; add toughnessStat to character_profiles |
| `Migration_10_11.kt` | v10→v11: Create exercise_sessions/exercise_sets tables; add strengthStat to character_profiles |
| `Migration_11_12.kt` | v11→v12: Add narrativeDescription column to workouts table |
| `Migration_12_13.kt` | v12→v13: Create lap_start_points table for multi-park auto-start |
| `Migration_13_14.kt` | v13→v14: Create screenshots table for in-app capture tracking |
| `Migration_14_15.kt` | v14→v15: Transform soreness_logs to per-muscle intensity format (CHEST:MODERATE) |

## data/repository/

| File | Purpose |
|------|---------|
| `WorkoutRepository.kt` | Query interface for workouts; filters by activity type, date ranges |
| `WorkoutSaveManager.kt` | Saves completed workouts; awards XP, checks achievements, updates streaks |
| `CharacterRepository.kt` | Character profile queries; idempotent `awardXp()` with duplicate check |
| `BodyCompRepository.kt` | Syncs Health Connect data to local DB; checks body comp achievements |
| `SorenessRepository.kt` | Soreness CRUD: observe today's log, create/update, 30-day history queries |
| `ExerciseRepository.kt` | Exercise session CRUD: save with sets, query by date/count, 30-day volume for STR stat |

## data/healthconnect/

| File | Purpose |
|------|---------|
| `HealthConnectManager.kt` | Reads Weight, BodyFat, LeanBodyMass, BoneMass, BMR, Height from Health Connect |

## data/sync/

| File | Purpose |
|------|---------|
| `FirestoreSync.kt` | Bidirectional Firestore sync (push/pull workouts, character, settings) |
| `FirestoreMappers.kt` | Domain models ↔ Firestore document map conversion |
| `SyncStatus.kt` | Sealed class (Idle/Syncing/Success/Error) with SharedPrefs persistence |

## ui/theme/

| File | Purpose |
|------|---------|
| `Color.kt` | Neon green (#39FF14), cyan (#58A6FF), amber (#F0883E), crimson (#FF6B6B), near-black palette |
| `Type.kt` | PressStart2P pixel font typography for all Material3 text styles |
| `Theme.kt` | GitFastTheme: Material3 dark color scheme, rectangle shapes |

## ui/components/

| File | Purpose |
|------|---------|
| `ActiveWorkoutBanner.kt` | Pulsing top banner showing live workout time/distance; tap to return |
| `ActivityRings.kt` | Animated concentric rings for daily activity goal progress |
| `ActivityTypeChips.kt` | Filter chips for activity type selection (All, Runs, Dog Walks) |
| `SectionHeader.kt` | Reusable styled section header text composable |
| `KeepScreenOn.kt` | Composable side-effect to keep screen awake during workouts |
| `ScreenshotOverlay.kt` | Draggable floating camera button for in-app screenshot capture |

## ui/home/

| File | Purpose |
|------|---------|
| `HomeScreen.kt` | Main hub: character header, activity rings, quick-start buttons, recent workouts |
| `HomeViewModel.kt` | Loads profile, streak, daily metrics, recent workouts; crash recovery check |
| `WeeklySummaryCard.kt` | "THIS WEEK" card with stats, deltas vs last week, active days bar |
| `RecentWorkoutsSection.kt` | Compact cards showing last 3 workouts with distance, duration, pace, XP |

## ui/workout/

| File | Purpose |
|------|---------|
| `ActiveWorkoutScreen.kt` | Root workout screen: service binding, permissions, back handler, navigate-away |
| `ActiveWorkoutViewModel.kt` | Orchestrates workout state, UI formatting, ghost selection, service binding |
| `RouteGhostRow.kt` | VS AVG row showing route ghost delta with color coding for dog walks |
| `WorkoutContent.kt` | Phase-specific layout (warmup/laps/cooldown) with stats and controls |
| `WorkoutControls.kt` | Activity-specific button layouts (start/pause/resume/stop/lap) |
| `StatGrid.kt` | 2×2 grid: time, distance, pace, steps for active workout |
| `PaceDisplay.kt` | Large current pace with paused-state animation |
| `LapIndicator.kt` | Current lap time + delta vs best/ghost with color coding |
| `LapPhaseContent.kt` | Lap phase hero: current lap time, ghost comparison, lap list |
| `RecordingIndicator.kt` | Top-right status: REC pulse, WALKING, or PAUSED |
| `PermissionRequestContent.kt` | Full-screen permission request with rationale cards |
| `DogWalkEventStrip.kt` | Horizontally scrollable event buttons with tap to log, long-press undo, badges |
| `WorkoutSummaryScreen.kt` | Post-workout: XP awarded, streak, achievements, stat summary |

## ui/dogwalk/

| File | Purpose |
|------|---------|
| `DogWalkSummaryScreen.kt` | Post-walk form: route tag, weather, energy, notes, route comparison |
| `DogWalkSummaryViewModel.kt` | Route tag CRUD, weather/energy selection, workout save orchestration |
| `RouteTagSelector.kt` | Chip selector for route tags with inline new-tag creation |
| `EnergySelector.kt` | Flow row of energy level chips (Low, Normal, Hyper) |
| `WeatherSelector.kt` | Weather condition + temperature chips with emoji labels |
| `EventTimeline.kt` | Chronological event list with icons, names, and time offsets |
| `EventRouteMap.kt` | Google Maps with GPS polyline and colored event markers by category |

## ui/history/

| File | Purpose |
|------|---------|
| `HistoryScreen.kt` | Scrollable workout list grouped by month with activity type filter |
| `HistoryViewModel.kt` | Filters workouts by activity type; groups by month-year |
| `WorkoutHistoryItem.kt` | Data class + mapper: Workout domain → UI display item |

## ui/detail/

| File | Purpose |
|------|---------|
| `DetailScreen.kt` | Full workout detail: map, phases, laps, route comparison |
| `DetailViewModel.kt` | Loads workout by ID; computes lap/phase analysis, route comparison |
| `DetailModels.kt` | UI data classes: WorkoutDetailItem, LatLngPoint, RouteBounds |
| `RouteMap.kt` | Google Maps with dark style JSON, GPS polyline trace, markers |
| `LapTrendChart.kt` | Canvas chart: lap pace trend with average line and color coding |
| `PhaseBreakdownSection.kt` | Table of warmup/laps/cooldown with duration, distance, pace |
| `LapAnalysisSection.kt` | Lap summary, trend indicator, trend chart, interactive lap table |
| `LapAnalysisModels.kt` | Data classes for lap display, trend types (FASTER/SLOWER/CONSISTENT) |
| `LapTable.kt` | Interactive table with long-press delete, best/worst highlighting |
| `RouteComparisonSection.kt` | Current dog walk vs previous same-route walks comparison table |

## ui/character/

| File | Purpose |
|------|---------|
| `CharacterSheetScreen.kt` | Tabbed view: user + Juniper profiles, stats, XP history, achievements |
| `CharacterSheetViewModel.kt` | Loads character data; calculates VIT stat from Health Connect body comp |

## ui/soreness/

| File | Purpose |
|------|---------|
| `SorenessLogScreen.kt` | Full screen: muscle group chips, intensity chips, notes, save with XP toast |
| `SorenessLogViewModel.kt` | Handles save with XP calculation, TGH stat update, achievement checking |
| `MuscleGroupSelector.kt` | Reusable FlowRow + FilterChip composable for multi-select muscle groups |

## ui/settings/

| File | Purpose |
|------|---------|
| `SettingsScreen.kt` | Settings UI: workout toggles, backup, Health Connect |
| `SettingsViewModel.kt` | Manages toggle state, sync triggers, auth state, HC permissions |
| `HealthConnectSection.kt` | Health Connect connection status, sync button, latest weight display |
| `CloudBackupSection.kt` | Google Sign-In card, sync status, last synced timestamp |

## ui/analytics/

| File | Purpose |
|------|---------|
| `AnalyticsHubScreen.kt` | Grid of 6 analytics cards linking to sub-screens |
| `AnalyticsHubViewModel.kt` | Total distance, duration, streak stats for hub display |

### ui/analytics/bodycomp/

| File | Purpose |
|------|---------|
| `BodyCompScreen.kt` | Weight + body fat charts with 30/60/90 day period selector, sync button |
| `BodyCompViewModel.kt` | Health Connect sync, period charting, weight delta calculation |

### ui/analytics/records/

| File | Purpose |
|------|---------|
| `PersonalRecordsScreen.kt` | Sectioned PR list: fastest pace, longest run/walk, best lap, streaks |
| `PersonalRecordsViewModel.kt` | Calculates personal records for runs, walks, and overall stats |
| `PersonalRecordsModels.kt` | Data classes for record items and sections |

### ui/analytics/trends/

| File | Purpose |
|------|---------|
| `TrendsScreen.kt` | Weekly/monthly comparison with distance + workout count bar charts |
| `TrendsViewModel.kt` | Groups workouts by period; calculates deltas and formatted display |
| `TrendsModels.kt` | Enums for period/filter; data classes for comparison and chart bars |
| `PixelBarChart.kt` | Canvas bar chart with value labels, current-bar highlighting |

### ui/analytics/routeoverlay/

| File | Purpose |
|------|---------|
| `RouteOverlayScreen.kt` | Multi-route map overlay with dropdown route tag selector |
| `RouteOverlayViewModel.kt` | Loads routes by tag; overlays multiple GPS traces with colors |
| `RouteOverlayModels.kt` | Data classes for trace display and overlay UI state |

### ui/analytics/routeperformance/

| File | Purpose |
|------|---------|
| `RoutePerformanceScreen.kt` | Route-by-route stats table with PB marking and trend summary |
| `RoutePerformanceViewModel.kt` | Calculates per-route stats, trend analysis, personal bests |
| `RoutePerformanceModels.kt` | Performance row and trend summary data classes |

---

## Tests (directory-level summaries)

All paths relative to `app/src/test/java/com/gitfast/app/`.

| Directory | Files | Coverage |
|-----------|-------|----------|
| _(root)_ | 36 | ViewModels, domain logic, entity/mapper tests, UI state mapping |
| `analysis/` | 3 | RouteComparisonAnalyzer, DistanceTimeProfile, RouteGhostCalculator tests |
| `data/healthconnect/` | 1 | HealthConnectManager mocked reads |
| `data/model/` | 3 | DailyActivityMetrics, dog walk domain model, DogWalkEventType |
| `data/local/mappers/` | 1 | DogWalkEventMappers round-trip and field mapping |
| `data/repository/` | 4 | BodyCompRepository, CharacterRepository, SorenessRepository, ExerciseRepository |
| `data/sync/` | 3 | FirestoreMappers, FirestoreSync, SyncStatusStore |
| `screenshots/` | 9 | Component screenshots (StatGrid, PaceDisplay, LapTable, ActiveWorkoutBanner, ActivityRings) + base classes |
| `screenshots/screens/` | 17 | Full-screen Roborazzi golden tests for every major screen (Detail, RouteOverlay, Permission, etc.) |
| `service/` | 6 | AutoPauseDetector, home arrival, WorkoutStateManager edge cases, events, auto-start laps |
| `ui/analytics/` | 5 | BodyComp, Records, RouteOverlay, RoutePerformance, Trends ViewModels |
| `ui/home/` | 1 | HomeViewModel goals integration |
| `ui/settings/` | 1 | SettingsViewModel |
| `util/` | 17 | Achievement checking, stats calc, streak, XP, lap analysis, trends, narrative, foraging |

**Total: ~104 unit test files**

### Instrumented Tests (`app/src/androidTest/`)

| File | Purpose |
|------|---------|
| `GitFastDatabaseTest` | Room DB operations and migration verification |
| `HomeScreenTest` | Compose UI interaction tests |
| `NavigationTest` | Compose Navigation route verification |
| `WorkoutRepositoryTest` | DAO integration on real device |
| `WorkoutStateStoreTest` | SharedPreferences integration |

---

## Build & Config

| File | Purpose |
|------|---------|
| `build.gradle.kts` (root) | Root Gradle config with plugin versions |
| `app/build.gradle.kts` | AGP, Kotlin, Compose, Hilt, Room, Firebase, Roborazzi, Kover config |
| `settings.gradle.kts` | Single-module project (`:app` only) |
| `.github/workflows/ci.yml` | CI: build → test (BLOCK) → lint/coverage/screenshots (WARN) |
| `.claude/quality-gates.json` | Local mirror of CI gate levels |

## Resources (`app/src/main/res/`)

| Path | Purpose |
|------|---------|
| `raw/map_style_dark.json` | Google Maps dark theme styling |
| `font/press_start_2p_regular.ttf` | Primary pixel art font |
| `font/jetbrains_mono_*.ttf` | Monospace fallback fonts |
| `drawable/ic_camera_pixel.xml` | Pixel-art camera icon for screenshot overlay |
| `drawable/ic_notification.xml` | Workout notification icon |
| `drawable/ic_pause.xml`, `ic_play.xml`, `ic_stop.xml` | Notification action icons |
| `values/strings.xml` | UI strings |
| `values/themes.xml` | Base theme color definitions |

## Screenshots Pipeline (`screenshots/`)

| File | Purpose |
|------|---------|
| `capture.mjs` | Playwright orchestrator for HTML → PNG composites with Pixel 9 frames |
| `package.json` | Playwright dependencies |
| `journeys/*.html` | 8 HTML journey files (home, run, dog-walk, history, analytics, settings, rpg) |

## Watch Firmware (`watch/`)

| File | Purpose |
|------|---------|
| `platformio.ini` | ESP32-S3 config (espressif32@6.10.0, Arduino framework) |
| `src/main.cpp` | Splash-screen stub (AXP2101 PMU + ST7789 display init) |
