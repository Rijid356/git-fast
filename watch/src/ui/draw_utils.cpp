#include "draw_utils.h"
#include "../config/colors.h"
#include <cstdio>

void formatTime(uint32_t totalSeconds, char* buf, size_t bufLen) {
    uint32_t h = totalSeconds / 3600;
    uint32_t m = (totalSeconds % 3600) / 60;
    uint32_t s = totalSeconds % 60;
    if (h > 0) {
        snprintf(buf, bufLen, "%u:%02u:%02u", h, m, s);
    } else {
        snprintf(buf, bufLen, "%02u:%02u", m, s);
    }
}

void formatPace(uint16_t secPerMile, char* buf, size_t bufLen) {
    if (secPerMile == 0 || secPerMile > 3600) {
        snprintf(buf, bufLen, "--:--/mi");
        return;
    }
    uint16_t m = secPerMile / 60;
    uint16_t s = secPerMile % 60;
    snprintf(buf, bufLen, "%u:%02u/mi", m, s);
}

void formatDistance(uint32_t meters, char* buf, size_t bufLen) {
    // Convert meters to miles (1 mile = 1609.34 meters)
    float miles = meters / 1609.34f;
    snprintf(buf, bufLen, "%.2f mi", miles);
}

void drawDivider(LGFX_Sprite& sprite, int y, uint16_t color, int marginX) {
    sprite.drawFastHLine(marginX, y, 240 - 2 * marginX, color);
}

void drawBattery(LGFX_Sprite& sprite, int percent, bool charging, int x, int y) {
    // Battery outline: 24x12 px
    int bw = 24, bh = 12;
    sprite.drawRect(x, y, bw, bh, Colors::TEXT_DIM);
    sprite.fillRect(x + bw, y + 3, 3, 6, Colors::TEXT_DIM);  // terminal nub

    // Fill based on percentage
    uint16_t fillColor;
    if (percent <= 20) {
        fillColor = Colors::ERROR_RED;
    } else if (charging) {
        fillColor = Colors::SECONDARY;
    } else {
        fillColor = Colors::PRIMARY;
    }

    int fillW = (bw - 4) * percent / 100;
    if (fillW > 0) {
        sprite.fillRect(x + 2, y + 2, fillW, bh - 4, fillColor);
    }

    // Percentage text
    char buf[8];
    snprintf(buf, sizeof(buf), "%d%%", percent);
    sprite.setFont(&fonts::Font0);
    sprite.setTextColor(Colors::TEXT_DIM);
    sprite.setTextDatum(lgfx::middle_left);
    sprite.drawString(buf, x + bw + 6, y + bh / 2);
}

void formatWithCommas(uint32_t value, char* buf, size_t bufLen) {
    char raw[16];
    snprintf(raw, sizeof(raw), "%u", value);
    int len = strlen(raw);
    int commas = (len - 1) / 3;
    int total = len + commas;

    if ((size_t)total >= bufLen) {
        snprintf(buf, bufLen, "%u", value);
        return;
    }

    int src = len - 1;
    int dst = total;
    buf[dst--] = '\0';
    int count = 0;
    while (src >= 0) {
        buf[dst--] = raw[src--];
        count++;
        if (count == 3 && src >= 0) {
            buf[dst--] = ',';
            count = 0;
        }
    }
}
