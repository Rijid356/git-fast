#pragma once

#include <cstdint>
#include "app_state.h"

// Flat struct matching Android WorkoutTrackingState fields.
// Designed for direct BLE deserialization — no strings, no pointers.
struct WorkoutData {
    bool        isActive;                   // workout in progress
    bool        isPaused;                   // currently paused
    PhaseType   phase;                      // WARMUP / LAPS / COOLDOWN
    ActivityType activityType;              // RUN / DOG_WALK

    uint32_t    elapsedSeconds;             // total elapsed time
    uint32_t    distanceMeters;             // x100 for 2 decimal places (124 = 1.24 mi after conversion)
    uint16_t    currentPaceSecPerMile;      // seconds per mile (0 = no pace yet)
    uint16_t    averagePaceSecPerMile;      // seconds per mile

    // Lap data (only meaningful when phase == LAPS)
    uint8_t     lapCount;                   // completed laps
    uint8_t     currentLapNumber;           // 1-based current lap
    uint16_t    currentLapElapsedSeconds;   // seconds into current lap
    int16_t     lastLapDeltaSeconds;        // delta vs previous/ghost lap (+ = slower, - = faster)
    uint16_t    bestLapSeconds;             // best lap duration in seconds

    void reset() {
        isActive = false;
        isPaused = false;
        phase = PhaseType::WARMUP;
        activityType = ActivityType::RUN;
        elapsedSeconds = 0;
        distanceMeters = 0;
        currentPaceSecPerMile = 0;
        averagePaceSecPerMile = 0;
        lapCount = 0;
        currentLapNumber = 0;
        currentLapElapsedSeconds = 0;
        lastLapDeltaSeconds = 0;
        bestLapSeconds = 0;
    }
};
