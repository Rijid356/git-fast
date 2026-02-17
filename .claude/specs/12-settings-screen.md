# Checkpoint 12: Settings Screen

## Goal
Add a Settings screen with user preferences for distance unit (miles/km), auto-pause toggle, and keep-screen-on toggle. Wire preferences through the app so formatters and UI respect user choices.

## What Was Built

### Data Layer
- **`DistanceUnit` enum** (`data/model/DistanceUnit.kt`): `MILES`, `KILOMETERS`
- **`SettingsStore` expanded** (`data/local/SettingsStore.kt`): Added `distanceUnit` (default MILES) and `keepScreenOn` (default true) alongside existing `autoPauseEnabled`

### Utility Layer — Unit-Aware Formatters
- **`DisplayFormatter.kt`**: Added `formatDistance(meters, DistanceUnit)` overload using `metersToKm()`
- **`DistanceCalculator.kt`**: Added `metersToKm(meters)` conversion
- **`TimeFormatter.kt`**: Added `formatPace(secondsPerMile, DistanceUnit)` overload — converts seconds-per-mile to seconds-per-km when displaying kilometers

### Settings UI
- **`SettingsViewModel`** (`ui/settings/SettingsViewModel.kt`): Hilt ViewModel exposing `SettingsUiState` with toggle methods that persist to `SettingsStore`
- **`SettingsScreen`** (`ui/settings/SettingsScreen.kt`): Material 3 screen with sections:
  - Workout: auto-pause toggle, distance unit toggle (tap to switch)
  - Display: keep-screen-on toggle
  - About: app name

### Navigation
- Added `Screen.Settings` route to `GitFastNavGraph.kt`
- Added gear icon button to `HomeScreen` top-right corner

### Preference Wiring
- **`ActiveWorkoutViewModel`**: Injects `SettingsStore`, uses `formatDistance(meters, unit)` and `formatPace(seconds, unit)` for unit-aware display
- **`ActiveWorkoutScreen`**: `KeepScreenOn()` composable gated on `uiState.keepScreenOn`

## Files Created (4)
| File | Purpose |
|------|---------|
| `data/model/DistanceUnit.kt` | Miles/Kilometers enum |
| `ui/settings/SettingsViewModel.kt` | Settings state management |
| `ui/settings/SettingsScreen.kt` | Material 3 settings UI |
| `.claude/specs/12-settings-screen.md` | This spec |

## Files Modified (7)
| File | Change |
|------|--------|
| `data/local/SettingsStore.kt` | Added `distanceUnit` and `keepScreenOn` properties |
| `util/DistanceCalculator.kt` | Added `metersToKm()` |
| `util/DisplayFormatter.kt` | Added unit-aware `formatDistance()` overload |
| `util/TimeFormatter.kt` | Added unit-aware `formatPace()` overload |
| `navigation/GitFastNavGraph.kt` | Added `Screen.Settings` route + composable |
| `ui/home/HomeScreen.kt` | Added settings gear icon button |
| `ui/workout/ActiveWorkoutViewModel.kt` | Inject SettingsStore, use unit-aware formatters |
| `ui/workout/ActiveWorkoutScreen.kt` | Gate KeepScreenOn on setting |

## Tests
- `SettingsFormatterTest.kt`: 11 tests covering unit-aware formatDistance, unit-aware formatPace, km conversion accuracy, legacy compatibility, DistanceUnit enum

## Design Decisions
- **PaceCalculator always returns seconds-per-mile** internally. Conversion to /km happens at display time in `formatPace(secondsPerMile, unit)` using division by 1.60934.
- **Legacy single-arg `formatDistance()` and `formatPace()` preserved** for backward compatibility — they default to miles.
- **DistanceUnit stored as string** in SharedPreferences via `name`/`valueOf` for robustness.
- **Settings gear icon overlays** the home screen content via Box + Alignment.TopEnd rather than adding a top app bar (preserving the centered splash layout).
