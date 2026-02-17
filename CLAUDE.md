# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**git-fast** is a native Android running/workout tracker app built with Kotlin. It tracks GPS during runs, calculates distance/pace in real-time, and stores workout history locally. Future phases add T-Watch S3 BLE integration and RPG gamification.

Package: `com.gitfast.app`

## Build & Test Commands

```bash
./gradlew assembleDebug                    # Build debug APK
./gradlew testDebugUnitTest                # Run all unit tests
./gradlew connectedDebugAndroidTest        # Run instrumented tests (requires device/emulator)
./gradlew test --tests "*.DistanceCalculatorTest"  # Run a single test class
```

Note: The repo may not include `gradlew` — if missing, use Android Studio or install the Gradle wrapper.

## Architecture

### Data Flow

```
GpsTracker (FusedLocation) → WorkoutStateManager (in-memory state) → WorkoutService (foreground service)
                                                                          ↓
                                                                    WorkoutRepository → WorkoutDao → Room DB
```

### Two-Layer Data Model

- **Room entities** (`data/local/entity/`) — database schema, use `Long` for timestamps, flat structure
- **Domain models** (`data/model/`) — app logic, use `java.time.Instant`, nested relationships, computed properties
- **Mappers** (`data/local/mappers/WorkoutMappers.kt`) — bidirectional conversion between layers

Enums (`WorkoutStatus`, `PhaseType`) are shared by both layers, stored in `data/model/`.

### DI Structure (Hilt)

- `AppModule` — empty, placeholder for future app-wide bindings
- `DatabaseModule` — Room database, WorkoutDao, WorkoutRepository (all `@Singleton`)
- `ServiceModule` — GpsTracker, WorkoutStateManager, PermissionManager (all `@Singleton`)

### Foreground Service

`WorkoutService` is a `LifecycleService` controlled via intent actions (`ACTION_START`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_STOP`). It owns GPS collection and the timer loop. UI binds to it via `WorkoutBinder` to observe `WorkoutStateManager` state.

### Database Schema

Four tables with cascade deletes: `workouts` → `workout_phases` → `laps`, and `workouts` → `gps_points`. Version 1, `exportSchema = true`.

### Navigation

Compose Navigation with sealed `Screen` class. Routes: Home, Workout, History, Detail/{workoutId}.

## Checkpoint Specs

Detailed specs for each development phase live in `.claude/specs/`. Always read the relevant spec before implementing a checkpoint — they contain exact code, file paths, and test requirements.

## Conventions

- JUnit 4 for all tests (not JUnit 5)
- Test names use backtick syntax: `` `descriptive test name` ``
- Unit tests: `app/src/test/`; Instrumented tests: `app/src/androidTest/`
- Commit messages follow: `Checkpoint N: description` for checkpoint work
- Branches follow: `checkpoint-N-kebab-case-description`
