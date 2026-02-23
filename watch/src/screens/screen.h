#pragma once

#include <LovyanGFX.hpp>
#include "../state/app_state.h"
#include "../state/workout_data.h"

// Abstract base class for all screens.
// Each screen renders into a sprite (off-screen buffer) for flicker-free display.
class Screen {
public:
    virtual ~Screen() = default;

    // Called once when the screen becomes active
    virtual void onEnter() {}

    // Called once when the screen is about to be replaced
    virtual void onExit() {}

    // Render the screen into the sprite. Returns true if content changed (needs push).
    virtual bool render(LGFX_Sprite& sprite, uint32_t nowMs) = 0;

    // Handle a button event. Returns a ScreenId if navigation should occur, or -1 to stay.
    virtual int handleButton(ButtonEvent event) { return -1; }
};
