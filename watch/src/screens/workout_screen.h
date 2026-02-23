#pragma once

#include "screen.h"
#include "../state/workout_data.h"

class WorkoutScreen : public Screen {
public:
    void setData(WorkoutData* data) { _data = data; }

    void onEnter() override;
    bool render(LGFX_Sprite& sprite, uint32_t nowMs) override;
    int handleButton(ButtonEvent event) override;

private:
    void renderHeader(LGFX_Sprite& sprite);
    void renderTime(LGFX_Sprite& sprite, uint32_t nowMs);
    void renderStats(LGFX_Sprite& sprite);
    void renderLapInfo(LGFX_Sprite& sprite);
    void renderSummaryBar(LGFX_Sprite& sprite);
    void renderPauseOverlay(LGFX_Sprite& sprite, uint32_t nowMs);

    WorkoutData* _data = nullptr;
    uint32_t _lastRenderMs = 0;
    bool     _pauseBlinkOn = true;

    static constexpr uint32_t ACTIVE_FRAME_INTERVAL_MS = 33;   // ~30fps
    static constexpr uint32_t PAUSED_FRAME_INTERVAL_MS = 500;  // blink rate
};
