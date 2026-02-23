#include "watch_face_screen.h"
#include "../config/colors.h"
#include "../config/display_config.h"
#include "../hardware/pmu.h"
#include "../hardware/bma423.h"
#include "../ui/draw_utils.h"
#include <ctime>

void WatchFaceScreen::onEnter() {
    _lastRenderSec = 0;  // force first render
}

bool WatchFaceScreen::render(LGFX_Sprite& sprite, uint32_t nowMs) {
    // Only redraw once per second
    uint32_t currentSec = nowMs / 1000;
    if (currentSec == _lastRenderSec) return false;
    _lastRenderSec = currentSec;

    sprite.fillScreen(Colors::BG_BLACK);

    // --- Battery (top-right) ---
    int batPercent = _pmu ? pmuGetBatteryPercent(*_pmu) : -1;
    bool charging = _pmu ? pmuIsCharging(*_pmu) : false;
    if (batPercent >= 0) {
        drawBattery(sprite, batPercent, charging, 168, 8);
    }

    // --- Time (Font7 = 7-segment, large) ---
    time_t now = time(nullptr);
    struct tm* t = localtime(&now);

    char timeBuf[8];
    snprintf(timeBuf, sizeof(timeBuf), "%02d:%02d", t->tm_hour, t->tm_min);

    sprite.setFont(&fonts::Font7);
    sprite.setTextColor(Colors::PRIMARY);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(timeBuf, SCREEN_W / 2, 36);

    // --- Date ---
    static const char* dayNames[] = {
        "Sunday", "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday"
    };
    static const char* monthNames[] = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    char dateBuf[32];
    snprintf(dateBuf, sizeof(dateBuf), "%s, %s %d",
             dayNames[t->tm_wday], monthNames[t->tm_mon], t->tm_mday);

    sprite.setFont(&fonts::Font2);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(dateBuf, SCREEN_W / 2, 96);

    // --- Divider ---
    drawDivider(sprite, 120, Colors::OUTLINE);

    // --- Steps ---
    uint32_t steps = stepCounterGetSteps();
    char stepBuf[16];
    formatWithCommas(steps, stepBuf, sizeof(stepBuf));
    char stepLine[24];
    snprintf(stepLine, sizeof(stepLine), "%s steps", stepBuf);

    sprite.setFont(&fonts::Font4);
    sprite.setTextColor(Colors::SECONDARY);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString(stepLine, SCREEN_W / 2, 134);

    // --- Divider ---
    drawDivider(sprite, 186, Colors::OUTLINE);

    // --- Branding ---
    sprite.setFont(&fonts::Font2);
    sprite.setTextColor(Colors::PRIMARY_DIM);
    sprite.setTextDatum(lgfx::top_center);
    sprite.drawString("git-fast", SCREEN_W / 2, 200);

    return true;
}

int WatchFaceScreen::handleButton(ButtonEvent event) {
    if (event == ButtonEvent::SHORT_PRESS) {
        return (int)ScreenId::WORKOUT;
    }
    return -1;
}
