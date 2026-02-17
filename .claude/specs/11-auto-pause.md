# Checkpoint 11: Auto-Pause

## Goal

Add auto-pause/resume functionality to running workouts based on GPS speed data. When the runner stops (e.g., at a traffic light), the workout automatically pauses. When they start moving again, it auto-resumes. This gives more accurate workout stats with zero manual interaction.

## Scope

- Auto-pause/resume based on GPS speed threshold (0.5 m/s)
- Runs only (dog walks excluded — frequent intentional stops)
- Default ON, toggleable via SettingsStore
- Visual distinction: "AUTO-PAUSED" button label vs "RESUME" for manual pause
- GPS stays alive during auto-pause (unlike manual pause) so detector can sense movement

## Key Design Decision

Auto-pause keeps GPS collection running. Manual pause cancels `gpsCollectionJob`. During auto-pause, `addGpsPoint()` still rejects points (via `isPaused=true` check), but the GPS flow continues feeding points to `AutoPauseDetector` for resume detection.

## New Files

| File | Purpose |
|------|---------|
| `app/.../service/AutoPauseDetector.kt` | Speed analysis with debounced pause/resume triggers |
| `app/.../data/local/SettingsStore.kt` | SharedPreferences wrapper for app settings |
| `app/.../data/local/migrations/Migration_1_2.kt` | Room migration: add `speed` column to `gps_points` |
| `app/src/test/.../service/AutoPauseDetectorTest.kt` | Unit tests for detection logic |

## Modified Files

| File | Change |
|------|--------|
| `GpsPoint.kt` | Added `speed: Float? = null` |
| `GpsPointEntity.kt` | Added `speed: Float? = null` column |
| `WorkoutMappers.kt` | Map `speed` in both directions |
| `GitFastDatabase.kt` | Version 1 → 2 |
| `DatabaseModule.kt` | Added `MIGRATION_1_2` to Room builder |
| `GpsTracker.kt` | Capture `location.speed` |
| `WorkoutStateManager.kt` | Added `isAutoPaused` state, `autoPauseWorkout()`, `autoResumeWorkout()` |
| `WorkoutService.kt` | Inject detector/settings, feed GPS to detector, handle auto-pause/resume |
| `ServiceModule.kt` | Provide `AutoPauseDetector` and `SettingsStore` |
| `ActiveWorkoutViewModel.kt` | Map `isAutoPaused` to UI state |
| `WorkoutControls.kt` | Show "AUTO-PAUSED" on resume button |
| `WorkoutContent.kt` | Pass `isAutoPaused` to controls |

## AutoPauseDetector Design

```
Constants:
  SPEED_THRESHOLD = 0.5 m/s (~1.1 mph)
  PAUSE_WINDOW    = 5 seconds (sustained stillness)
  RESUME_WINDOW   = 3 seconds (resume is snappier)
  RETENTION       = 10 seconds (sliding window)
  MIN_POINTS      = 3 (minimum for pause trigger)

analyzePoint(point, isCurrentlyAutoPaused) → {shouldPause, shouldResume}
  - Maintains sliding window of recent points (last 10s)
  - Pause: ALL points with speed in last 5s below threshold (min 3 points)
  - Resume: ANY point in last 3s above threshold
  - Returns no-op if speed data unavailable

reset() → clears sliding window
```

## Verification

```bash
./gradlew testDebugUnitTest    # All unit tests pass
./gradlew assembleDebug         # Build succeeds with migration
```
