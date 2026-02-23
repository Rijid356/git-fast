#pragma once

#include <LovyanGFX.hpp>
#include "../config/display_config.h"
#include "../screens/screen.h"
#include "../state/app_state.h"

class DisplayManager {
public:
    void init(LGFX& lcd);
    void setScreen(Screen* screen);
    Screen* getScreen() { return _currentScreen; }

    // Call every loop iteration. Renders current screen and pushes to display if dirty.
    void update(uint32_t nowMs);

    LGFX_Sprite& getSprite() { return _sprite; }

private:
    LGFX*        _lcd = nullptr;
    LGFX_Sprite  _sprite;
    Screen*      _currentScreen = nullptr;
};
