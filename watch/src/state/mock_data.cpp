#include "mock_data.h"
#include <Arduino.h>
#include <cstdlib>

void MockWorkout::start(WorkoutData& data) {
    data.reset();
    data.isActive = true;
    data.isPaused = false;
    data.phase = PhaseType::WARMUP;
    data.activityType = ActivityType::RUN;
    data.currentLapNumber = 0;

    _running = true;
    _finished = false;
    _startMs = millis();
    _lastTickMs = _startMs;
    _phaseStartMs = _startMs;
    _lapStartMs = 0;
    _basePace = 512;  // ~8:32/mi
}

void MockWorkout::update(WorkoutData& data, uint32_t nowMs) {
    if (!_running || _finished) return;

    // Update elapsed time (1 second per real second)
    uint32_t totalElapsed = nowMs - _startMs;
    data.elapsedSeconds = totalElapsed / 1000;

    uint32_t phaseElapsed = nowMs - _phaseStartMs;

    // Simulate distance accumulation (~3.1 meters/sec = ~8:32/mi pace)
    float metersPerMs = 3.1f / 1000.0f;
    data.distanceMeters = (uint32_t)(totalElapsed * metersPerMs);

    // Simulate pace with slight variance
    int variance = (rand() % 20) - 10;  // +/- 10 sec
    data.currentPaceSecPerMile = _basePace + variance;
    data.averagePaceSecPerMile = _basePace;

    // Phase transitions
    switch (data.phase) {
        case PhaseType::WARMUP:
            if (phaseElapsed >= WARMUP_DURATION_MS) {
                transitionToLaps(data, nowMs);
            }
            break;

        case PhaseType::LAPS:
            // Update current lap elapsed
            if (_lapStartMs > 0) {
                data.currentLapElapsedSeconds = (nowMs - _lapStartMs) / 1000;
            }
            // Auto-lap
            if (_nextAutoLapMs > 0 && nowMs >= _nextAutoLapMs) {
                markLap(data, nowMs);
            }
            // Phase end
            if (phaseElapsed >= LAPS_DURATION_MS) {
                transitionToCooldown(data, nowMs);
            }
            break;

        case PhaseType::COOLDOWN:
            // Slow down pace during cooldown
            data.currentPaceSecPerMile = 720 + variance;  // ~12:00/mi
            if (phaseElapsed >= COOLDOWN_DURATION_MS) {
                stop(data);
            }
            break;
    }

    _lastTickMs = nowMs;
}

void MockWorkout::markLap(WorkoutData& data, uint32_t nowMs) {
    if (data.phase != PhaseType::LAPS) return;

    uint32_t lapDuration = (nowMs - _lapStartMs) / 1000;
    if (lapDuration < 2) return;  // ignore micro-laps

    data.lapCount++;

    // Calculate delta vs previous lap (simulate variance)
    int16_t delta = (rand() % 7) - 3;  // -3 to +3 seconds
    data.lastLapDeltaSeconds = delta;

    // Track best lap
    if (data.bestLapSeconds == 0 || lapDuration < data.bestLapSeconds) {
        data.bestLapSeconds = lapDuration;
    }

    // Start new lap
    data.currentLapNumber = data.lapCount + 1;
    data.currentLapElapsedSeconds = 0;
    _lapStartMs = nowMs;
    _nextAutoLapMs = nowMs + AUTO_LAP_INTERVAL_MS + (rand() % 4000) - 2000;  // +/- 2s jitter
}

void MockWorkout::transitionToLaps(WorkoutData& data, uint32_t nowMs) {
    data.phase = PhaseType::LAPS;
    data.currentLapNumber = 1;
    data.lapCount = 0;
    _phaseStartMs = nowMs;
    _lapStartMs = nowMs;
    _nextAutoLapMs = nowMs + AUTO_LAP_INTERVAL_MS;
}

void MockWorkout::transitionToCooldown(WorkoutData& data, uint32_t nowMs) {
    data.phase = PhaseType::COOLDOWN;
    data.currentLapNumber = 0;
    _phaseStartMs = nowMs;
    _lapStartMs = 0;
    _nextAutoLapMs = 0;
}

void MockWorkout::stop(WorkoutData& data) {
    data.isActive = false;
    data.isPaused = false;
    _running = false;
    _finished = true;
}
