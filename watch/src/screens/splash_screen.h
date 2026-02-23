#pragma once

#include "screen.h"

class SplashScreen : public Screen {
public:
    void onEnter() override;
    bool render(LGFX_Sprite& sprite, uint32_t nowMs) override;
    int handleButton(ButtonEvent event) override;

    bool isFinished() const { return _finished; }

private:
    uint32_t _enterMs = 0;
    bool     _finished = false;
    int      _lastBarWidth = -1;

    static constexpr uint32_t SPLASH_DURATION_MS = 2000;
};
