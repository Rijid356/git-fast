#include "display_manager.h"
#include "../config/colors.h"

void DisplayManager::init(LGFX& lcd) {
    _lcd = &lcd;
    _sprite.createSprite(SCREEN_W, SCREEN_H);
    _sprite.setColorDepth(16);
    _sprite.fillScreen(Colors::BG_BLACK);
}

void DisplayManager::setScreen(Screen* screen) {
    if (_currentScreen) {
        _currentScreen->onExit();
    }
    _currentScreen = screen;
    if (_currentScreen) {
        _currentScreen->onEnter();
    }
}

void DisplayManager::update(uint32_t nowMs) {
    if (!_currentScreen || !_lcd) return;

    bool dirty = _currentScreen->render(_sprite, nowMs);
    if (dirty) {
        _sprite.pushSprite(_lcd, 0, 0);
    }
}
