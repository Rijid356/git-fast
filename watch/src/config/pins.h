#pragma once

// --- T-Watch S3 GPIO Pin Assignments ---

// I2C (PMU + sensors)
#define PIN_I2C_SDA     10
#define PIN_I2C_SCL     11

// SPI Display (ST7789)
#define PIN_LCD_SCLK    18
#define PIN_LCD_MOSI    13
#define PIN_LCD_CS      12
#define PIN_LCD_DC      38
#define PIN_LCD_BL      45

// Side button
#define PIN_BUTTON      0

// Touch (FT6336, for future use)
#define PIN_TOUCH_SDA   10  // shared I2C
#define PIN_TOUCH_SCL   11
#define PIN_TOUCH_INT   16
#define PIN_TOUCH_RST   -1

// Motor
#define PIN_MOTOR       4

// Microphone (PDM)
#define PIN_MIC_DATA    2
#define PIN_MIC_CLK     3

// IR
#define PIN_IR_TX       17

// RTC PCF8563 shares I2C bus (addr 0x51)
// BMA423 shares I2C bus (addr 0x18 or 0x19)
