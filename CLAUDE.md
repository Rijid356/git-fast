# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**git-fast** is a native Android running/workout tracker app built with Kotlin. It tracks GPS during runs and dog walks, calculates distance/pace in real-time, and stores workout history locally with Room. Includes RPG gamification (XP, leveling, achievements, character stats), ghost runner features, Health Connect body composition sync, and Firebase auth. Future phases add T-Watch S3 BLE integration.

Package: `com.gitfast.app` | Min SDK 26 | Target/Compile SDK 35 | JVM 17

Toolchain: AGP 8.13.2 | Kotlin 2.1.0 | KSP 2.1.0-1.0.29 | Hilt 2.53.1 | Compose BOM 2024.12.01

## Build & Test Commands

```bash
./gradlew assembleDebug                                  # Build debug APK
./gradlew testDebugUnitTest                              # Run all unit tests
./gradlew connectedDebugAndroidTest                      # Run instrumented tests (requires device/emulator)
./gradlew test --tests "*.DistanceCalculatorTest"        # Run a single test class
./gradlew test --tests "*.DistanceCalculatorTest.test*"  # Run a single test method
./gradlew lintDebug                                      # Run Android Lint
./gradlew koverHtmlReportDebug                           # Generate code coverage report
./gradlew koverXmlReportDebug                            # Generate XML coverage report (for CI)
./gradlew koverVerifyDebug                               # Verify coverage thresholds (35% line, 33% branch)
./gradlew recordRoborazziDebug                           # Record golden screenshots
./gradlew verifyRoborazziDebug                           # Verify screenshots against golden
./gradlew generateScreenshotComposites                   # Record + capture HTML composites as PNGs (depends on recordRoborazziDebug)
```

Testing stack: JUnit 4.13.2, MockK 1.13.13, Robolectric 4.14.1, coroutines-test 1.7.3. `unitTests.isReturnDefaultValues = true` and `isIncludeAndroidResources = true` are set in build config. Instrumented tests use Espresso 3.6.1, Compose UI testing, and Room testing 2.6.1.

Screenshot testing via Roborazzi 1.59.0 — base class `FullScreenScreenshotTestBase` with 5% cross-platform font tolerance. Golden snapshots in `app/src/test/snapshots/screens/`. Tests in `app/src/test/java/com/gitfast/app/screenshots/screens/`.

Code coverage via Kover 0.9.7 — excludes Hilt/Room/Compose generated code, DI modules, entity/model data classes, migrations, Firebase/auth wrappers. Verification thresholds: 35% line coverage, 33% branch coverage (enforced by `koverVerifyDebug`).

Android Lint is configured with `abortOnError = true`, `warningsAsErrors = false`. Suppressed checks: GradleDependency, NewerVersionAvailable, AndroidGradlePluginVersion, OldTargetApi, LockedOrientationActivity, ObsoleteSdkInt, UseKtx, InlinedApi, DiscouragedApi, TestManifestGradleConfiguration.

## CI

GitHub Actions (`.github/workflows/ci.yml`) runs on PRs to main with concurrency cancellation per branch:

| Step | Level | Notes |
|------|-------|-------|
| Build debug APK | **BLOCK** | |
| Unit tests | **BLOCK** | Failure reports uploaded as artifact |
| Android Lint | **WARN** | `continue-on-error`, HTML report uploaded |
| Coverage verify | **WARN** | `continue-on-error`, XML report uploaded |
| Screenshot verify | **WARN** | `continue-on-error`, diffs uploaded |

Uses Java 17 (temurin). `google-services.json` decoded from `GOOGLE_SERVICES_JSON` secret. Instrumented tests are **not** run in CI.

Quality gates config: `.claude/quality-gates.json` mirrors the CI levels (build/test BLOCK, lint/coverage WARN).

## File Manifest

See `FILE_MANIFEST.md` for a one-liner-per-file reference of every source file in the project. Read it once at session start for full codebase context without reading individual files.

## Architecture

### Data Flow

```
GpsTracker (FusedLocation) → WorkoutStateManager (in-memory StateFlow) → WorkoutService (foreground LifecycleService)
                                                                              ↓
                                                                        WorkoutSaveManager → WorkoutDao → Room DB (v8)
```

- `WorkoutService` is intent-controlled: `ACTION_START`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_STOP`, `ACTION_DISCARD`, `ACTION_START_LAPS`, `ACTION_MARK_LAP`, `ACTION_END_LAPS`
- UI binds via `WorkoutBinder` to observe `WorkoutStateManager` state

### Critical Binding Pattern

`WorkoutStateManager` is a **plain `@Singleton` class** (not a ViewModel) — injected into both `WorkoutService` and `ActiveWorkoutViewModel`. The ViewModel connects to the service via `ServiceConnection`/`WorkoutBinder` to get the shared instance. `ActiveWorkoutViewModel` extends `AndroidViewModel` (needs Application context for service binding).

`WorkoutService.isRunning` is a `@Volatile` companion object boolean — the cross-thread service-running flag.

### Navigate-Away from Active Workout

Users can navigate away from an active workout without stopping it. `ActiveWorkoutBanner` (shown in `MainActivity`) displays live time/distance at the top of the app; tapping it returns to the workout screen. BackHandler offers "Go Home", "Stop Workout", or "Cancel". Double-start guard in `ActiveWorkoutViewModel` prevents duplicate service starts.

### Crash Recovery

`WorkoutStateStore` (SharedPrefs: `"workout_state"`) persists active workout ID. `HomeViewModel.init` calls `checkForIncompleteWorkout()` which checks `workoutStateStore.hasActiveWorkout() && !WorkoutService.isRunning`. A recovery dialog is shown if true. Service uses `START_REDELIVER_INTENT` for intent re-delivery on kill.

### Two-Layer Data Model

- **Room entities** (`data/local/entity/`) — flat DB schema, `Long` timestamps
- **Domain models** (`data/model/`) — `java.time.Instant`, nested relationships, computed properties
- **Mappers** (`data/local/mappers/WorkoutMappers.kt`) — extension functions for bidirectional entity↔domain conversion

Enums (`WorkoutStatus`, `PhaseType`, `ActivityType`, etc.) are shared by both layers, stored in `data/model/`. Room stores enums as their `.name` string (TEXT columns) via `Converters.kt`.

### DI Structure (Hilt)

- `DatabaseModule` — Room database, WorkoutDao, WorkoutRepository, WorkoutSaveManager, WorkoutStateStore, CharacterDao, CharacterRepository (all `@Singleton`)
- `ServiceModule` — GpsTracker, WorkoutStateManager, PermissionManager, AutoPauseDetector, SettingsStore (all `@Singleton`)
- `AppModule` — placeholder for future app-wide bindings

### Database Schema (Room v8)

Tables: `workouts` → `workout_phases` → `laps`, `workouts` → `gps_points`, `route_tags`, `character_profiles`, `xp_transactions`, `unlocked_achievements`, `body_comp_entries`. Cascade deletes on workout relationships. `exportSchema = true` (schemas in `app/schemas/`).

Migration history (`data/local/migrations/`): v1→v2 dog walk fields, v2→v3 route tags, v3→v4 character/XP tables, v4→v5 achievements table, v5→v6 profileId on XP/achievements + Juniper profile seed, v6→v7 splitLatitude/splitLongitude on laps, v7→v8 `body_comp_entries` table + `vitalityStat` column on `character_profiles`.

`WorkoutDao.saveWorkoutTransaction()` is a `@Transaction` DAO method with upsert semantics (update if exists, insert if not).

### Health Connect Integration

`HealthConnectManager` (`data/healthconnect/`) is a `@Singleton` that reads from Android Health Connect: WeightRecord, BodyFatRecord, LeanBodyMassRecord, BoneMassRecord, BasalMetabolicRateRecord, HeightRecord. Uses `androidx.health.connect:connect-client:1.1.0-alpha11`.

`BodyCompRepository` (`data/sync/`) syncs Health Connect data to the local `body_comp_entries` table. `CharacterSheetViewModel` wires the VIT stat to body composition data. Settings screen includes `HealthConnectSection` for permission management.

### Navigation

Compose Navigation with sealed `Screen` class in `navigation/GitFastNavGraph.kt`. Routes: Home, Workout (activityType param), WorkoutSummary, DogWalkSummary, History, Detail/{workoutId}, Settings, Goals, Analytics, RouteOverlay, RoutePerformance, PersonalRecords, Trends, BodyComp, CharacterSheet.

**Gotcha**: `WorkoutSummary` passes data as URL-encoded query params (not Parcelables). Achievements are pipe-delimited (`|`) in the URL. Always use `URLEncoder`/`URLDecoder` when building/parsing these routes.

### RPG System

Dual character profiles: id=1 (user/Ryan) and id=2 (Juniper, the dog). Both earn XP on dog walks; each has independent levels, stats, and achievements.

- `CharacterRepository.awardXp()` is **idempotent** — checks for existing `XpTransactionEntity` with `(workoutId, profileId)` before inserting. Achievement XP uses `"achievement:<id>"` as workoutId to prevent double-awarding.
- `StatsCalculator` uses bracket interpolation (1-99 RPG-style stats) with an `inverted` flag for pace (lower = better).
- `AchievementDef` enum has a `profileId` field — most default to 1, Juniper-specific ones are profileId=2.
- Streak multiplier: Day 1 = 1.0x, +0.1x/day, capped at 1.5x (Day 5+). Streak counts today OR yesterday (1-day grace).

### Ghost Runner

In-memory only (fields on `WorkoutStateManager`). Auto-ghost = best lap so far; external ghost = selected from previous workout. Ghost delta positive = behind, negative = ahead. All state wiped after workout stop.

### Key Behavioral Details

- **Auto-lap discard**: Laps under 5 seconds are discarded and distance merged into previous lap (in `discardMicroLap()`).
- **SettingsStore** uses synchronous SharedPreferences (no Flow). Settings take effect on next workout start, not mid-workout.
- **GPS callbacks** run on `Looper.getMainLooper()`. The `callbackFlow` in `GpsTracker.startTracking()` uses `trySend()`.
- **LapAnalyzer.calculateTrend()** requires ≥3 laps for trend analysis (returns `TOO_FEW_LAPS` otherwise).
- **Database name**: `"gitfast-database"`.

### Theme

Material3 dark theme with pixel art aesthetic: neon green primary (#39FF14), cyan secondary (#58A6FF), near-black background (#0D1117). `PressStart2P` pixel font for all text styles. `HighContrastText = Color.White` for outdoor readability. `AmberAccent (#F0883E)` for warnings/PR markers.

### Maps

Google Maps with dark style JSON (`res/raw/map_style_dark.json`). `MAPS_API_KEY` must be set in `local.properties` (read via manual Properties loading in `app/build.gradle.kts`, NOT `gradle.properties`). Key is injected into the manifest via `manifestPlaceholders`.

### Firebase & Auth

Firebase BOM 34.9.0 with firebase-auth and firebase-firestore. Google Sign-In via Credential Manager (`credentials:1.5.0-beta01`) and `googleid:1.1.1`. Auth wrappers in `com.gitfast.app.auth` package. CI requires `GOOGLE_SERVICES_JSON` secret (base64-encoded `google-services.json`).

### Permissions & Services

Manifest declares: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `ACTIVITY_RECOGNITION`. `WorkoutService` has `foregroundServiceType="location"`. `MainActivity` is locked to portrait orientation.

### Screenshot Composites

`screenshots/` directory contains a Playwright + Node.js pipeline for generating full-screen HTML composites with Pixel 9 phone frames. `capture.mjs` orchestrates; journey HTML files in `screenshots/journeys/`. Output goes to `app/src/test/snapshots/composites/`. Triggered via `./gradlew generateScreenshotComposites`.

## watch/ — T-Watch S3 Firmware

PlatformIO project (`watch/platformio.ini`): ESP32-S3 with `espressif32@6.10.0`, `esp32-s3-devkitc-1` board, Arduino framework. Dependencies: XPowersLib (AXP2101 PMU), LovyanGFX (ST7789 display). Currently a working splash-screen stub. See root-level memory notes for flash pipeline details.

## Checkpoint Specs

Detailed specs for each development phase live in `.claude/specs/` (00-14 plus architecture docs). Always read the relevant spec before implementing a checkpoint — they contain exact code, file paths, and test requirements.

## UI Change Workflow

**Any plan that modifies UI MUST include before/after screenshots.** This is mandatory, not optional.

1. **Before**: Record current golden screenshots for affected screens (`./gradlew recordRoborazziDebug`)
2. **Show the "before"** to the user as part of the plan discussion
3. **Mock up the "after"** — describe or sketch the proposed UI change so the user can approve before implementation
4. **After implementing**: Re-record screenshots and show "before vs after" side by side
5. Use existing screenshot tests in `app/src/test/java/com/gitfast/app/screenshots/screens/`

This applies to all UI work: new screens, layout changes, adding/removing/moving elements, theming changes, etc.

## Conventions

- JUnit 4 for all tests (not JUnit 5)
- Test names use backtick syntax: `` `descriptive test name` ``
- Unit tests: `app/src/test/`; Instrumented tests: `app/src/androidTest/`
- Commit messages: `Checkpoint N: description`
- Branches: `checkpoint-N-kebab-case-description`
- Coroutines test: `kotlinx-coroutines-test:1.7.3`
- Single-module Gradle project (`:app` only)
