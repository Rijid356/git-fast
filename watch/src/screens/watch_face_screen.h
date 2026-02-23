#pragma once

#include "screen.h"
#include <XPowersLib.h>

class WatchFaceScreen : public Screen {
public:
    void setPmu(XPowersAXP2101* pmu) { _pmu = pmu; }

    void onEnter() override;
    bool render(LGFX_Sprite& sprite, uint32_t nowMs) override;
    int handleButton(ButtonEvent event) override;

private:
    XPowersAXP2101* _pmu = nullptr;
    uint32_t _lastRenderSec = 0;
};
