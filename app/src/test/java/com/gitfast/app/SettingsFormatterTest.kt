package com.gitfast.app

import com.gitfast.app.data.model.DistanceUnit
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatPace
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsFormatterTest {

    // --- Distance unit-aware formatting ---

    @Test
    fun `formatDistance in miles returns mi suffix`() {
        assertEquals("1.00 mi", formatDistance(1609.34, DistanceUnit.MILES))
    }

    @Test
    fun `formatDistance in kilometers returns km suffix`() {
        assertEquals("1.61 km", formatDistance(1609.34, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `formatDistance 0 meters in kilometers returns 0 point 00 km`() {
        assertEquals("0.00 km", formatDistance(0.0, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `formatDistance 5000 meters in kilometers returns 5 point 00 km`() {
        assertEquals("5.00 km", formatDistance(5000.0, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `formatDistance in miles matches legacy single-arg version`() {
        assertEquals(formatDistance(1609.34), formatDistance(1609.34, DistanceUnit.MILES))
    }

    // --- Pace unit-aware formatting ---

    @Test
    fun `formatPace in miles returns per mi label`() {
        assertEquals("10:00 /mi", formatPace(600, DistanceUnit.MILES))
    }

    @Test
    fun `formatPace in kilometers converts from per-mile to per-km`() {
        // 600 seconds/mile ÷ 1.60934 ≈ 372 seconds/km → 6:12 /km
        assertEquals("6:12 /km", formatPace(600, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `formatPace 480 seconds per mile in km`() {
        // 480 / 1.60934 ≈ 298 → 4:58 /km
        assertEquals("4:58 /km", formatPace(480, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `formatPace in miles matches legacy single-arg version`() {
        assertEquals(formatPace(600), formatPace(600, DistanceUnit.MILES))
    }

    // --- DistanceUnit enum ---

    @Test
    fun `DistanceUnit has two values`() {
        assertEquals(2, DistanceUnit.entries.size)
    }

    @Test
    fun `DistanceUnit valueOf round-trips`() {
        assertEquals(DistanceUnit.MILES, DistanceUnit.valueOf("MILES"))
        assertEquals(DistanceUnit.KILOMETERS, DistanceUnit.valueOf("KILOMETERS"))
    }
}
