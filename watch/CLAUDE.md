# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the T-Watch S3 firmware.

## Overview

PlatformIO project for T-Watch S3 (ESP32-S3). Arduino framework. Currently a working splash-screen stub — future phases add BLE integration with the git-fast Android app.

## Build & Flash

Config: `platformio.ini` | Board: `esp32-s3-devkitc-1` | Framework: Arduino

Dependencies: XPowersLib (AXP2101 PMU), LovyanGFX (ST7789 display).

### Flash Pipeline (CLI-based)

1. Enter download mode: hold BOOT button (inside case) while plugging USB, or pyserial DTR toggle
2. Erase: `python -m esptool --chip esp32s3 --port COM3 erase_flash`
3. Flash ALL 4 files: bootloader(0x0) + partitions(0x8000) + boot_app0(0xE000) + firmware(0x10000)
4. boot_app0.bin: `~/.platformio/packages/framework-arduinoespressif32/tools/partitions/boot_app0.bin`
5. Boot: `python -m esptool --chip esp32s3 --port COM3 --before no_reset --after watchdog_reset run`

Use `python -m esptool` (v4.11.0 global pip), NOT PlatformIO bundled (v4.5.1). COM port is typically COM3.

## Hardware

| Component | Details |
|-----------|---------|
| PMU | AXP2101 on I2C (SDA=10, SCL=11, addr=0x34) |
| Display | ST7789 240x240 (SCLK=18, MOSI=13, CS=12, DC=38, BL=45) |
| LovyanGFX | memory_height=320, offset_rotation=2, invert=true, 40MHz write |
| Power rails | ALDO1-4 at 3300mV, BLDO2 at 3300mV (ALDO2 = display power) |

**Note:** `board_build.arduino.memory_type = qio_opi` crashes — use defaults.

## Pin Map

| Function | GPIO | Notes |
|----------|------|-------|
| PDM Mic Clock | **GPIO0** | STRAPPING PIN — never set PAD_HOLD! |
| PDM Mic Data | GPIO47 | |
| I2C SDA | GPIO10 | PMU, accel, touch, RTC |
| I2C SCL | GPIO11 | |
| Display SCLK | GPIO18 | SPI3_HOST |
| Display MOSI | GPIO13 | |
| Display CS | GPIO12 | |
| Display DC | GPIO38 | |
| Display BL | GPIO45 | Backlight control |
| PMU IRQ | **GPIO21** | AXP2101 N_IRQ, active-low |
| Accel IRQ | GPIO14 | BMA423 |
| Touch IRQ | GPIO16 | FT6236 |
| RTC IRQ | GPIO17 | PCF8563 |
| Side Button | PMU PKEY | NOT a GPIO — triggers PMU interrupt |
| BOOT Button | GPIO0 | Inside back case, pulls LOW |

## Critical Safety Rules

- **NEVER set PAD_HOLD / gpio_hold_en() on GPIO0** — it's a strapping pin
- **NEVER call gpio_deep_sleep_hold_en()** without excluding GPIO0
- **NEVER call disableLongPressShutdown() or disableIRQ(ALL)** on PMU
- **ALWAYS clear PAD_HOLD register in setup()** as first action: `REG_WRITE(RTC_CNTL_PAD_HOLD_REG, 0)`
- **ALWAYS include OTA update capability** once initial firmware is stable
- Use `--after watchdog_reset` with esptool (not hard_reset) for ESP32-S3 USB-Serial/JTAG
