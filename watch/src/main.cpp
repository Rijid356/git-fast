#include <Arduino.h>
#include <Wire.h>
#include <XPowersLib.h>

#define LGFX_USE_V1
#include <LovyanGFX.hpp>

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
        cfg.freq_read = 16000000;
        cfg.pin_sclk = 18;
        cfg.pin_mosi = 13;
        cfg.pin_miso = -1;
        cfg.pin_dc = 38;
        _bus.config(cfg);
        _panel.setBus(&_bus);

        // Panel
        auto pcfg = _panel.config();
        pcfg.pin_cs = 12;
        pcfg.pin_rst = -1;
        pcfg.pin_busy = -1;
        pcfg.panel_width = 240;
        pcfg.panel_height = 240;
        pcfg.memory_width = 240;
        pcfg.memory_height = 320;
        pcfg.offset_x = 0;
        pcfg.offset_y = 0;
        pcfg.offset_rotation = 2;
        pcfg.invert = true;
        _panel.config(pcfg);

        // Backlight
        auto lcfg = _light.config();
        lcfg.pin_bl = 45;
        lcfg.freq = 1000;
        lcfg.pwm_channel = 3;
        lcfg.invert = false;
        _light.config(lcfg);
        _panel.setLight(&_light);

        setPanel(&_panel);
    }
};

LGFX display;
XPowersAXP2101 pmu;

void setup() {
    // Init I2C for PMU (SDA=10, SCL=11)
    Wire.begin(10, 11);

    // Init AXP2101 PMU and enable power rails
    if (pmu.begin(Wire, AXP2101_SLAVE_ADDRESS, 10, 11)) {
        pmu.setALDO1Voltage(3300);
        pmu.enableALDO1();
        pmu.setALDO2Voltage(3300);  // Display power
        pmu.enableALDO2();
        pmu.setALDO3Voltage(3300);
        pmu.enableALDO3();
        pmu.setALDO4Voltage(3300);
        pmu.enableALDO4();
        pmu.setBLDO2Voltage(3300);
        pmu.enableBLDO2();
    }

    // Init display
    delay(100);  // Let power stabilize
    display.init();
    display.setBrightness(200);

    // Draw splash screen
    display.fillScreen(TFT_BLACK);
    display.setTextColor(0x47E0);  // #39FF14 neon green (RGB565)
    display.setTextDatum(lgfx::middle_center);
    display.setFont(&fonts::Font4);
    display.drawString("git-fast", 120, 120);
}

void loop() {
    delay(1000);
}
