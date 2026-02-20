# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**git-fast** is a native Android running/workout tracker app built with Kotlin. It tracks GPS during runs and dog walks, calculates distance/pace in real-time, and stores workout history locally with Room. Includes RPG gamification (XP, leveling, achievements, character stats) and ghost runner features. Future phases add T-Watch S3 BLE integration.

Package: `com.gitfast.app` | Min SDK 26 | Target/Compile SDK 35 | JVM 17

Toolchain: AGP 8.13.2 | Kotlin 2.1.0 | KSP 2.1.0-1.0.29 | Hilt 2.53.1 | Compose BOM 2024.12.01

## Build & Test Commands

```bash
./gradlew assembleDebug                                  # Build debug APK
./gradlew testDebugUnitTest                              # Run all unit tests
./gradlew connectedDebugAndroidTest                      # Run instrumented tests (requires device/emulator)
./gradlew test --tests "*.DistanceCalculatorTest"        # Run a single test class
./gradlew test --tests "*.DistanceCalculatorTest.test*"  # Run a single test method
./gradlew koverHtmlReportDebug                           # Generate code coverage report
```

Testing stack: JUnit 4.13.2, MockK 1.13.13, Robolectric 4.14.1, coroutines-test 1.7.3. `unitTests.isReturnDefaultValues = true` and `isIncludeAndroidResources = true` are set in build config. Instrumented tests use Espresso 3.6.1, Compose UI testing, and Room testing 2.6.1.

Code coverage via Kover 0.9.7 — excludes Hilt/Room/Compose generated code, DI modules, entity/model data classes, and migrations. No lint tools configured; uses `kotlin.code.style=official` only.

## CI

GitHub Actions (`.github/workflows/ci.yml`) runs on PRs to main: builds debug APK and runs unit tests with Java 17 (temurin). Uploads test failure reports as artifacts. Instrumented tests are **not** run in CI (device/emulator required).

## Architecture

### Data Flow

```
GpsTracker (FusedLocation) → WorkoutStateManager (in-memory StateFlow) → WorkoutService (foreground LifecycleService)
                                                                              ↓
                                                                        WorkoutSaveManager → WorkoutDao → Room DB (v7)
```

- `WorkoutService` is intent-controlled: `ACTION_START`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_STOP`, `ACTION_DISCARD`, `ACTION_START_LAPS`, `ACTION_MARK_LAP`, `ACTION_END_LAPS`
- UI binds via `WorkoutBinder` to observe `WorkoutStateManager` state

### Critical Binding Pattern

`WorkoutStateManager` is a **plain `@Singleton` class** (not a ViewModel) — injected into both `WorkoutService` and `ActiveWorkoutViewModel`. The ViewModel connects to the service via `ServiceConnection`/`WorkoutBinder` to get the shared instance. `ActiveWorkoutViewModel` extends `AndroidViewModel` (needs Application context for service binding).

`WorkoutService.isRunning` is a `@Volatile` companion object boolean — the cross-thread service-running flag.

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

### Database Schema (Room v7)

Tables: `workouts` → `workout_phases` → `laps`, `workouts` → `gps_points`, `route_tags`, `character_profiles`, `xp_transactions`, `unlocked_achievements`. Cascade deletes on workout relationships. `exportSchema = true` (schemas in `app/schemas/`).

Migration history (`data/local/migrations/`): v1→v2 dog walk fields, v2→v3 route tags, v3→v4 character/XP tables, v4→v5 achievements table, v5→v6 profileId on XP/achievements + Juniper profile seed, v6→v7 splitLatitude/splitLongitude on laps.

`WorkoutDao.saveWorkoutTransaction()` is a `@Transaction` DAO method with upsert semantics (update if exists, insert if not).

### Navigation

Compose Navigation with sealed `Screen` class in `navigation/GitFastNavGraph.kt`. Routes: Home, Workout (activityType param), WorkoutSummary, DogWalkSummary, History, Detail/{workoutId}, Settings, CharacterSheet.

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

### Permissions & Services

Manifest declares: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `ACTIVITY_RECOGNITION`. `WorkoutService` has `foregroundServiceType="location"`. `MainActivity` is locked to portrait orientation.

## watch/ — T-Watch S3 Firmware

PlatformIO project (`watch/platformio.ini`): ESP32-S3 with `espressif32@6.10.0`, `esp32-s3-devkitc-1` board, Arduino framework. Dependencies: XPowersLib (AXP2101 PMU), LovyanGFX (ST7789 display). Currently a working splash-screen stub. See root-level memory notes for flash pipeline details.

## Checkpoint Specs

Detailed specs for each development phase live in `.claude/specs/` (00-14 plus architecture docs). Always read the relevant spec before implementing a checkpoint — they contain exact code, file paths, and test requirements.

## Conventions

- JUnit 4 for all tests (not JUnit 5)
- Test names use backtick syntax: `` `descriptive test name` ``
- Unit tests: `app/src/test/`; Instrumented tests: `app/src/androidTest/`
- Commit messages: `Checkpoint N: description`
- Branches: `checkpoint-N-kebab-case-description`
- Coroutines test: `kotlinx-coroutines-test:1.7.3`
- Single-module Gradle project (`:app` only)
