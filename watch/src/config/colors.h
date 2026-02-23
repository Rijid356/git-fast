#pragma once

#include <cstdint>

// --- RGB565 color palette matching Android git-fast theme ---
// Conversion: ((R & 0xF8) << 8) | ((G & 0xFC) << 3) | (B >> 3)

namespace Colors {
    // Primary
    constexpr uint16_t PRIMARY       = 0x47E0;  // #39FF14 neon green
    constexpr uint16_t PRIMARY_DIM   = 0x2E62;  // #2BCC10 dimmed green

    // Secondary
    constexpr uint16_t SECONDARY     = 0x5D3F;  // #58A6FF cyan accent

    // Background / Surface
    constexpr uint16_t BG_BLACK      = 0x0000;  // #000000
    constexpr uint16_t BG_DARK       = 0x0882;  // #0D1117 dark background
    constexpr uint16_t SURFACE       = 0x18E4;  // #161B22 dark surface
    constexpr uint16_t SURFACE_VAR   = 0x2124;  // #21262D surface variant

    // Text
    constexpr uint16_t TEXT_PRIMARY  = 0xFFFF;  // #FFFFFF high contrast
    constexpr uint16_t TEXT_BODY     = 0xE73C;  // #E6EDF3 body text
    constexpr uint16_t TEXT_DIM      = 0x8C71;  // #8B949E dimmed text

    // Accents
    constexpr uint16_t AMBER         = 0xF447;  // #F0883E amber/warning
    constexpr uint16_t ERROR_RED     = 0xF8A9;  // #F85149 error red
    constexpr uint16_t SUCCESS_GREEN = 0x47E0;  // same as PRIMARY

    // UI
    constexpr uint16_t OUTLINE       = 0x31A7;  // #30363D outline gray
    constexpr uint16_t DIVIDER       = 0x31A7;  // same as outline
}
