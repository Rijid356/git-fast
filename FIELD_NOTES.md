# Field Notes

## 2026-03-04 — Dog Walk (Park route)

**Workout ID:** _(not pulled — Firebase CLI lacks Firestore read commands)_
**Conditions:** Park route with route ghost enabled

### Observations
- Route ghost was selected before starting the walk (park route tag)
- "VS AVG" time difference row never appeared during the walk
- User took an in-app screenshot during the walk but it's only saved to device gallery, not synced to Firebase

### Bugs / Issues
- [ ] **Route ghost "VS AVG" row never shows during dog walk** — Root cause found: `WorkoutStateManager.startWorkout()` (line 257) creates a brand new `WorkoutTrackingState` that doesn't include `routeGhostActive`, so it resets to `false`. The backing field stays `true` (so delta calculations still run), but the state flow field stays `false`, and `WorkoutContent.kt:174` checks `uiState.routeGhostActive` to decide whether to render `RouteGhostRow`. Fix: add `routeGhostActive = routeGhostActive` to the `WorkoutTrackingState` constructor in `startWorkout()`.

### Feature Ideas
- [ ] **Pixelated camera icon for screenshot overlay** — Current icon is a camera emoji (📷). Replace with a pixel-art camera drawable to match the Press Start 2P pixel aesthetic.
- [ ] **Sync screenshots to Firebase** — In-app screenshots currently save to device gallery (MediaStore) and Room DB only. Need to upload to Firebase Storage/R2 and store download URL in Firestore so screenshots can be accessed remotely (e.g., for field test sessions).

### Data Notes
- Firebase CLI doesn't support `firestore:get` — need alternative approach for field test data pulls
- Route ghost profiles were loaded (backing field confirmed active), delta calculation was running, but UI was hidden

---

## 2026-03-02 — Dog Walk (field observation)

**Workout ID:** _(not pulled — Firebase token refresh issue)_
**Conditions:** N/A — general observations from recent walks

### Observations
- Narrative description generated after dog walk save is not visible when viewing the walk from history
- MPH display stays active for several seconds after stopping walking

### Bugs / Issues
- [x] **Dog walk narrative not displayed in history detail view** (fixed PR #158) — `DogWalkNarrativeGenerator` creates a narrative in `DogWalkSummaryViewModel` (line 92-93) and it's shown on the immediate `DogWalkSummaryScreen` (lines 158-171). However, the narrative is **never persisted** to the database — `WorkoutDetailItem` in `DetailModels.kt` has no `narrativeDescription` field, and `DetailScreen.kt` doesn't display it. Fix: either (a) persist the narrative to the workout entity/Room DB so it can be loaded in the detail screen, or (b) re-generate it on-the-fly from stored dog walk events when opening the detail view. Option (b) is simpler — just call `DogWalkNarrativeGenerator.generateNarrative()` with the events loaded from history.
- [x] **MPH stays active too long after stopping movement** (fixed PR #158) — `WorkoutStateManager.kt:660-668` uses a 5-point smoothing window (`SPEED_SMOOTHING_WINDOW = 5`, line 652) with GPS updates every 2 seconds. This means ~10 seconds of speed data is averaged, and the display won't clear until 5 consecutive null-speed points arrive. When the user stops walking, the last known speed persists in the UI. Fix: add a time-based decay — if the most recent GPS point is older than N seconds (e.g., 3-4 seconds) or if the user hasn't moved beyond a threshold distance, zero out the displayed speed immediately rather than waiting for the smoothing window to flush.

### Feature Ideas
- (none this session)

### Data Notes
- Firebase data pull blocked by token refresh issue in this session — workout data not reviewed
- Both issues are reproducible from general use patterns, not specific to one workout

---

## 2026-02-28 — Dog Walk (1.53 mi)

**Workout ID:** `61972b8c-9f2a-4c12-9520-fb18626525c7`
**Conditions:** Cool, Cloudy, Park route

### Observations
- 34:41 walk, 3151 steps, 647 GPS points
- 3 events logged (2x snack found, 1x leash pull)
- Sprint detection triggered during walk but got stuck — wouldn't auto-end
- MPH display disappears during sprinting, user wants to see speed while sprinting

### Bugs / Issues
- [ ] **Sprint gets stuck when GPS speed data becomes null** — `AutoSprintDetector.analyzeForEnd()` (line 62-72) requires 3+ GPS points with non-null speed in the 5-second window to end a sprint. If FusedLocationProvider stops reporting speed (which happened — all 647 stored points have null speed), the sprint can never auto-end. It stays active until user pauses or stops the workout. Fix: add a max sprint duration timeout (e.g., 60-90 seconds) and/or fall back to distance-based speed calculation when raw GPS speed is null. Also consider ending sprint if no non-null speed points received for N seconds.
- [ ] **MPH display hidden during sprinting** — `WorkoutContent.kt:117-121` replaces `SpeedDisplay` (MPH + pace) with `SprintDisplay` (pulsing sprint timer) when sprint is active. User wants to continue seeing current speed in MPH while sprinting. Fix: integrate MPH into the sprint display, e.g., show sprint timer as hero with MPH as a secondary readout below it, or add MPH to the sprint stat row.

### Feature Ideas
- (none this session)

### Data Notes
- All 647 GPS points in Firestore have null speed values — FusedLocationProvider speed reporting is unreliable for walking pace
- No sprint laps were persisted in the workout data, despite sprinting occurring during the walk (likely discarded as micro-sprint or lost during the stuck state)
- 3 dog walk events all have valid GPS coordinates

---

## 2026-02-26 — Dog Walk (0.83 mi)

**Workout ID:** `22059fd6-caec-4218-9b90-e5b634fdbebb`
**Conditions:** Cool, Cloudy, Nighttime route

### Observations
- Juniper was HYPER energy, 5 events logged (deep sniff, 2x leash pull, zoomies, friendly dog)
- All dog walk events correctly stored GPS coordinates in Firestore
- 366 GPS points captured over 28 min walk

### Bugs / Issues
- [ ] **Dog walk detail screen missing event markers on map** — `DetailScreen.kt:248` uses `RouteMap` (route-only polyline) instead of `EventRouteMap` (which includes color-coded event pins). After saving a dog walk, user navigates to DetailScreen where they see the EVENT LOG text list above the map (line 218-220, `DogWalkEventSection`) but the map itself only shows the green GPS route with no event pins. Fix: swap `RouteMap` for `EventRouteMap` in `DetailScreen.kt` when `activityType.isDogActivity`, passing `dogWalkEvents` and GPS points. `EventRouteMap.kt` already handles rendering — just needs to be wired into the detail view. The `DogWalkSummaryScreen` correctly uses `EventRouteMap` but user only briefly sees it before navigating to detail.
- [ ] **Remove "Juniper" from dog walk history subtitle** — `WorkoutHistoryItem.kt:30` displays `dogName · routeTag` (e.g., "Juniper · Nighttime") but since dogName is always "Juniper", it's redundant. Should only show the `routeTag` for dog activities.

### Feature Ideas
- (none this session)

### Data Notes
- Dog walk events in Firestore all have non-null lat/lng — GPS capture at event time is working correctly
- Event timestamps are within the workout time range — timing logic is solid

---
