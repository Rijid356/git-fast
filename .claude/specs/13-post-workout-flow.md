# Checkpoint 13: Post-Workout Flow Fix

## Goal
Fix the broken "VIEW DETAILS" button on the workout summary screen and improve the post-workout navigation flow for both runs and dog walks.

## What Was Fixed

### Problem
- The "VIEW DETAILS" button on WorkoutSummaryScreen did nothing (contained a `// TODO` comment since CP5)
- Dog walk saves returned directly to Home with no way to view the detail screen
- The `workoutId` was available in the completion callback but never passed to the summary route

### Solution
Thread `workoutId` through the WorkoutSummary navigation route as a query parameter, and change the dog walk post-save flow to navigate to the detail screen.

## Files Modified (3)

| File | Change |
|------|--------|
| `navigation/GitFastNavGraph.kt` | Added `workoutId` query param to WorkoutSummary route, pass it from `onWorkoutComplete`, extract and wire to Detail navigation. Changed DogWalkSummary `onSaved` to navigate to Detail instead of Home. |
| `ui/workout/WorkoutSummaryScreen.kt` | Added `workoutId: String?` parameter. VIEW DETAILS button only shown when workoutId is non-null. |
| `ui/dogwalk/DogWalkSummaryScreen.kt` | Changed `onSaved` callback from `() -> Unit` to `(workoutId: String) -> Unit`, passes viewModel.workoutId. |

## Files Created (2)

| File | Purpose |
|------|---------|
| `WorkoutSummaryRouteTest.kt` | 6 tests for route generation with workoutId |
| `.claude/specs/13-post-workout-flow.md` | This spec |

## User Flow Changes

### Before (Runs)
1. Complete run → Summary screen → "VIEW DETAILS" does nothing → "DONE" → Home

### After (Runs)
1. Complete run → Summary screen → "VIEW DETAILS" → Detail screen (map, phases, laps) → Back → Home

### Before (Dog Walks)
1. Complete walk → Metadata form → "SAVE WALK" → Home (no detail view)

### After (Dog Walks)
1. Complete walk → Metadata form → "SAVE WALK" → Detail screen (map, metadata, route comparison) → Back → Home

## Design Decisions
- `workoutId` added as a query parameter (not path parameter) to avoid breaking the existing URL structure
- VIEW DETAILS button hidden (not disabled) when workoutId is null — graceful degradation
- Dog walk post-save navigates to Detail with `popUpTo(Home)` so back button returns to Home cleanly
- Run summary VIEW DETAILS also uses `popUpTo(Home)` for consistent back-stack behavior
