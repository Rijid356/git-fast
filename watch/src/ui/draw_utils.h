#pragma once

#include <LovyanGFX.hpp>
#include <cstdint>

// Format seconds into MM:SS or H:MM:SS string
void formatTime(uint32_t totalSeconds, char* buf, size_t bufLen);

// Format seconds-per-mile pace into M:SS/mi string
void formatPace(uint16_t secPerMile, char* buf, size_t bufLen);

// Format distance in meters to miles string (e.g., "1.24 mi")
void formatDistance(uint32_t meters, char* buf, size_t bufLen);

// Draw a horizontal divider line
void drawDivider(LGFX_Sprite& sprite, int y, uint16_t color, int marginX = 20);

// Draw battery icon + percentage text at top-right
void drawBattery(LGFX_Sprite& sprite, int percent, bool charging, int x, int y);

// Format number with commas (e.g., 1247 -> "1,247")
void formatWithCommas(uint32_t value, char* buf, size_t bufLen);
