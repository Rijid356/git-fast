#include "splash_screen.h"
#include "../config/colors.h"
#include "../config/display_config.h"

void SplashScreen::onEnter() {
    _enterMs = millis();
    _finished = false;
    _lastBarWidth = -1;
}

bool SplashScreen::render(LGFX_Sprite& sprite, uint32_t nowMs) {
    uint32_t elapsed = nowMs - _enterMs;

    if (elapsed >= SPLASH_DURATION_MS) {
        _finished = true;
    }

    // Loading bar progress
    float progress = min(1.0f, (float)elapsed / SPLASH_DURATION_MS);
    int barMaxW = 160;
    int barW = (int)(barMaxW * progress);

    // Only redraw if bar width changed (or first frame)
    if (barW == _lastBarWidth) return false;
    _lastBarWidth = barW;

    // Clear
    sprite.fillScreen(Colors::BG_BLACK);

    // "git-fast" title
    sprite.setTextColor(Colors::PRIMARY);
    sprite.setTextDatum(lgfx::middle_center);
    sprite.setFont(&fonts::Font4);
    sprite.drawString("git-fast", SCREEN_W / 2, 100);

    // Subtitle
    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.drawString("run tracker", SCREEN_W / 2, 130);

    // Loading bar background
    int barH = 4;
    int barX = (SCREEN_W - barMaxW) / 2;
    int barY = 160;
    sprite.fillRect(barX, barY, barMaxW, barH, Colors::SURFACE_VAR);

    // Loading bar fill
    if (barW > 0) {
        sprite.fillRect(barX, barY, barW, barH, Colors::PRIMARY);
    }

    return true;
}

int SplashScreen::handleButton(ButtonEvent event) {
    // Skip splash on any button press
    if (event == ButtonEvent::SHORT_PRESS || event == ButtonEvent::LONG_PRESS) {
        _finished = true;
        return (int)ScreenId::WATCH_FACE;
    }
    return -1;
}
