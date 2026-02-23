#pragma once

#define LGFX_USE_V1
#include <LovyanGFX.hpp>
#include "pins.h"

// --- LovyanGFX Display Config (T-Watch S3: ST7789 240x240) ---
class LGFX : public lgfx::LGFX_Device {
    lgfx::Panel_ST7789 _panel;
    lgfx::Bus_SPI _bus;
    lgfx::Light_PWM _light;

public:
    LGFX(void) {
        // SPI bus
        auto cfg = _bus.config();
        cfg.spi_host = SPI3_HOST;
        cfg.spi_mode = 0;
        cfg.freq_write = 40000000;
        cfg.freq_read  = 16000000;
        cfg.pin_sclk = PIN_LCD_SCLK;
        cfg.pin_mosi = PIN_LCD_MOSI;
        cfg.pin_miso = -1;
        cfg.pin_dc   = PIN_LCD_DC;
        _bus.config(cfg);
        _panel.setBus(&_bus);

        // Panel
        auto pcfg = _panel.config();
        pcfg.pin_cs       = PIN_LCD_CS;
        pcfg.pin_rst      = -1;
        pcfg.pin_busy     = -1;
        pcfg.panel_width  = 240;
        pcfg.panel_height = 240;
        pcfg.memory_width  = 240;
        pcfg.memory_height = 320;
        pcfg.offset_x = 0;
        pcfg.offset_y = 0;
        pcfg.offset_rotation = 2;
        pcfg.invert = true;
        _panel.config(pcfg);

        // Backlight
        auto lcfg = _light.config();
        lcfg.pin_bl      = PIN_LCD_BL;
        lcfg.freq         = 1000;
        lcfg.pwm_channel  = 3;
        lcfg.invert       = false;
        _light.config(lcfg);
        _panel.setLight(&_light);

        setPanel(&_panel);
    }
};

// Display dimensions
constexpr int SCREEN_W = 240;
constexpr int SCREEN_H = 240;
