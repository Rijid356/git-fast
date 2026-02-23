#include "workout_screen.h"
#include "../config/colors.h"
#include "../config/display_config.h"
#include "../ui/draw_utils.h"

void WorkoutScreen::onEnter() {
    _lastRenderMs = 0;
    _pauseBlinkOn = true;
}

bool WorkoutScreen::render(LGFX_Sprite& sprite, uint32_t nowMs) {
    if (!_data) return false;

    // Throttle render rate
    uint32_t interval = _data->isPaused ? PAUSED_FRAME_INTERVAL_MS : ACTIVE_FRAME_INTERVAL_MS;
    if (nowMs - _lastRenderMs < interval) return false;
    _lastRenderMs = nowMs;

    sprite.fillScreen(Colors::BG_BLACK);

    renderHeader(sprite);
    renderTime(sprite, nowMs);
    renderStats(sprite);

    if (_data->phase == PhaseType::LAPS) {
        renderLapInfo(sprite);
        renderSummaryBar(sprite);
    }

    if (_data->isPaused) {
        renderPauseOverlay(sprite, nowMs);
    }

    return true;
}

void WorkoutScreen::renderHeader(LGFX_Sprite& sprite) {
    // Phase label
    const char* phaseLabel = "";
    switch (_data->phase) {
        case PhaseType::WARMUP:   phaseLabel = "WARMUP"; break;
        case PhaseType::LAPS:     {
            static char lapLabel[16];
            snprintf(lapLabel, sizeof(lapLabel), "LAP %d", _data->currentLapNumber);
            phaseLabel = lapLabel;
            break;
        }
        case PhaseType::COOLDOWN: phaseLabel = "COOLDOWN"; break;
    }

    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::SECONDARY);
    sprite.setTextDatum(lgfx::top_left);

    // Draw at 2x scale for readability
    sprite.setTextSize(2);
    sprite.drawString(phaseLabel, 8, 6);
    sprite.setTextSize(1);

    // Divider
    drawDivider(sprite, 24, Colors::OUTLINE, 4);
}

void WorkoutScreen::renderTime(LGFX_Sprite& sprite, uint32_t nowMs) {
    char timeBuf[12];
    formatTime(_data->elapsedSeconds, timeBuf, sizeof(timeBuf));

    // If paused, blink the time (toggle every 500ms)
    uint16_t timeColor = Colors::PRIMARY;
    if (_data->isPaused) {
        _pauseBlinkOn = ((nowMs / 500) % 2) == 0;
        if (!_pauseBlinkOn) {
            timeColor = Colors::SURFACE_VAR;  // nearly invisible = blink off
        }
    }

    sprite.setFont(&fonts::Font7);
    sprite.setTextColor(timeColor);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(timeBuf, SCREEN_W / 2, 30);
}

void WorkoutScreen::renderStats(LGFX_Sprite& sprite) {
    int statsY = 84;

    // Labels
    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString("DISTANCE", 60, statsY);
    sprite.drawString("PACE", 180, statsY);

    // Values
    char distBuf[16];
    formatDistance(_data->distanceMeters, distBuf, sizeof(distBuf));

    char paceBuf[16];
    formatPace(_data->currentPaceSecPerMile, paceBuf, sizeof(paceBuf));

    sprite.setFont(&fonts::Font4);
    sprite.setTextColor(Colors::TEXT_PRIMARY);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(distBuf, 60, statsY + 14);
    sprite.drawString(paceBuf, 180, statsY + 14);
}

void WorkoutScreen::renderLapInfo(LGFX_Sprite& sprite) {
    int lapY = 132;

    // Current lap time
    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.setTextDatum(lgfx::top_left);
    sprite.drawString("LAP TIME", 8, lapY);

    char lapTimeBuf[12];
    formatTime(_data->currentLapElapsedSeconds, lapTimeBuf, sizeof(lapTimeBuf));

    sprite.setFont(&fonts::Font4);
    sprite.setTextColor(Colors::TEXT_BODY);
    sprite.setTextDatum(lgfx::top_left);
    sprite.drawString(lapTimeBuf, 8, lapY + 14);

    // Delta indicator (only show if we have a previous lap)
    if (_data->lapCount > 0 && _data->lastLapDeltaSeconds != 0) {
        int16_t delta = _data->lastLapDeltaSeconds;
        bool faster = (delta < 0);
        uint16_t deltaColor = faster ? Colors::PRIMARY : Colors::ERROR_RED;

        // Triangle indicator
        int triX = 150;
        int triY = lapY + 20;
        if (faster) {
            // Up triangle (faster)
            sprite.fillTriangle(triX, triY - 6, triX - 5, triY + 2, triX + 5, triY + 2, deltaColor);
        } else {
            // Down triangle (slower)
            sprite.fillTriangle(triX, triY + 6, triX - 5, triY - 2, triX + 5, triY - 2, deltaColor);
        }

        // Delta text
        char deltaBuf[12];
        int absDelta = abs(delta);
        snprintf(deltaBuf, sizeof(deltaBuf), "%c%ds", faster ? '-' : '+', absDelta);
        sprite.setFont(&fonts::Font2);
        sprite.setTextColor(deltaColor);
        sprite.setTextDatum(lgfx::middle_left);
        sprite.drawString(deltaBuf, triX + 10, triY);
    }
}

void WorkoutScreen::renderSummaryBar(LGFX_Sprite& sprite) {
    int barY = 182;
    drawDivider(sprite, barY, Colors::OUTLINE, 4);

    char summary[40];
    if (_data->bestLapSeconds > 0) {
        char bestBuf[12];
        formatTime(_data->bestLapSeconds, bestBuf, sizeof(bestBuf));
        snprintf(summary, sizeof(summary), "%d laps | best: %s", _data->lapCount, bestBuf);
    } else {
        snprintf(summary, sizeof(summary), "%d laps", _data->lapCount);
    }

    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(summary, SCREEN_W / 2, barY + 6);
}

void WorkoutScreen::renderPauseOverlay(LGFX_Sprite& sprite, uint32_t nowMs) {
    // Pulsing "PAUSED" text at bottom
    bool show = ((nowMs / 500) % 2) == 0;
    if (show) {
        sprite.setFont(&fonts::Font2);
        sprite.setTextColor(Colors::AMBER);
        sprite.setTextDatum(lgfx::top_center);
        sprite.drawString("PAUSED", SCREEN_W / 2, 220);
    }
}

int WorkoutScreen::handleButton(ButtonEvent event) {
    // Button handling will be wired in Step 8
    if (event == ButtonEvent::LONG_PRESS) {
        return (int)ScreenId::WATCH_FACE;
    }
    return -1;
}
