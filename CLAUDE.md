# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**git-fast** is a native Android running/workout tracker app built with Kotlin. It tracks GPS during runs and dog walks, calculates distance/pace in real-time, and stores workout history locally with Room. Future phases add T-Watch S3 BLE integration and RPG gamification.

Package: `com.gitfast.app` | Min SDK 26 | Target/Compile SDK 35 | JVM 17

## Build & Test Commands

```bash
./gradlew assembleDebug                                  # Build debug APK
./gradlew testDebugUnitTest                              # Run all unit tests
./gradlew connectedDebugAndroidTest                      # Run instrumented tests (requires device/emulator)
./gradlew test --tests "*.DistanceCalculatorTest"        # Run a single test class
./gradlew test --tests "*.DistanceCalculatorTest.test*"  # Run a single test method
```

## Architecture

### Data Flow

```
GpsTracker (FusedLocation) → WorkoutStateManager (in-memory StateFlow) → WorkoutService (foreground LifecycleService)
                                                                              ↓
                                                                        WorkoutSaveManager → WorkoutDao → Room DB (v2)
```

- `WorkoutService` is intent-controlled: `ACTION_START`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_STOP`, `ACTION_DISCARD`, `ACTION_START_LAPS`, `ACTION_MARK_LAP`, `ACTION_END_LAPS`
- UI binds via `WorkoutBinder` to observe `WorkoutStateManager` state
- Crash recovery: `WorkoutStateStore` persists active workout ID to SharedPreferences; `HomeViewModel` checks on init

### Two-Layer Data Model

- **Room entities** (`data/local/entity/`) — flat DB schema, `Long` timestamps
- **Domain models** (`data/model/`) — `java.time.Instant`, nested relationships, computed properties
- **Mappers** (`data/local/mappers/WorkoutMappers.kt`) — bidirectional entity↔domain conversion

Enums (`WorkoutStatus`, `PhaseType`, `ActivityType`, etc.) are shared by both layers, stored in `data/model/`.

### DI Structure (Hilt)

- `DatabaseModule` — Room database, WorkoutDao, WorkoutRepository, WorkoutSaveManager, WorkoutStateStore (all `@Singleton`)
- `ServiceModule` — GpsTracker, WorkoutStateManager, PermissionManager, AutoPauseDetector, SettingsStore (all `@Singleton`)
- `AppModule` — placeholder for future app-wide bindings

### Database Schema (Room v2)

Five tables with cascade deletes: `workouts` → `workout_phases` → `laps`, `workouts` → `gps_points`, plus `route_tags`. Migration support in `data/local/migrations/`. `exportSchema = true`.

### Navigation

Compose Navigation with sealed `Screen` class. Routes: Home, Workout (with activityType param), WorkoutSummary, DogWalkSummary, History, Detail/{workoutId}, Settings.

### Key Features by Domain

- **Workout phases**: WARMUP → LAPS → COOLDOWN with per-lap tracking and trend analysis (`LapAnalyzer`, `PhaseAnalyzer`)
- **Dog walks**: Post-workout metadata (weather, energy, dog name, route tag) with route comparison (`RouteComparisonAnalyzer`)
- **Auto-pause**: `AutoPauseDetector` triggers on GPS speed drop below threshold (RUN only, toggleable in settings)
- **GPS filtering**: High-accuracy FLP, 2s intervals, 3m min displacement, 20m max accuracy

### Theme

Material3 dark theme: neon green primary (#39FF14), cyan secondary (#58A6FF), near-black background (#0D1117). JetBrains Mono for headings.

## Checkpoint Specs

Detailed specs for each development phase live in `.claude/specs/`. Always read the relevant spec before implementing a checkpoint — they contain exact code, file paths, and test requirements.

## Conventions

- JUnit 4 for all tests (not JUnit 5)
- Test names use backtick syntax: `` `descriptive test name` ``
- Unit tests: `app/src/test/`; Instrumented tests: `app/src/androidTest/`
- Commit messages: `Checkpoint N: description`
- Branches: `checkpoint-N-kebab-case-description`
- Coroutines test: `kotlinx-coroutines-test:1.7.3`
