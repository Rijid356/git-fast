#pragma once

#include "workout_data.h"
#include <cstdint>

// Simulated workout that auto-plays through WARMUP -> LAPS -> COOLDOWN -> stop.
// Produces realistic data for UI testing without BLE.
class MockWorkout {
public:
    void start(WorkoutData& data);
    void update(WorkoutData& data, uint32_t nowMs);
    void markLap(WorkoutData& data, uint32_t nowMs);
    void stop(WorkoutData& data);

    bool isRunning() const { return _running; }
    bool isFinished() const { return _finished; }

private:
    void transitionToLaps(WorkoutData& data, uint32_t nowMs);
    void transitionToCooldown(WorkoutData& data, uint32_t nowMs);

    bool     _running = false;
    bool     _finished = false;
    uint32_t _startMs = 0;
    uint32_t _lastTickMs = 0;
    uint32_t _phaseStartMs = 0;
    uint32_t _lapStartMs = 0;

    // Simulated pace variance
    uint16_t _basePace = 512;  // ~8:32/mi

    // Auto-lap timing
    uint32_t _nextAutoLapMs = 0;

    static constexpr uint32_t WARMUP_DURATION_MS   = 15000;   // 15s warmup
    static constexpr uint32_t LAPS_DURATION_MS     = 60000;   // 60s of laps
    static constexpr uint32_t COOLDOWN_DURATION_MS = 15000;   // 15s cooldown
    static constexpr uint32_t AUTO_LAP_INTERVAL_MS = 20000;   // auto-lap every ~20s
};
