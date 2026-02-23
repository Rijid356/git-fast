#pragma once

#include <cstdint>

// Screen IDs
enum class ScreenId : uint8_t {
    SPLASH,
    WATCH_FACE,
    WORKOUT
};

// Workout phase (mirrors Android PhaseType)
enum class PhaseType : uint8_t {
    WARMUP,
    LAPS,
    COOLDOWN
};

// Activity type (mirrors Android ActivityType)
enum class ActivityType : uint8_t {
    RUN,
    DOG_WALK
};

// Button events
enum class ButtonEvent : uint8_t {
    NONE,
    SHORT_PRESS,
    LONG_PRESS
};
