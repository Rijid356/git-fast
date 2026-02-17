package com.gitfast.app

import com.gitfast.app.util.formatDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFormatterTest {

    @Test
    fun `formatDistance 0 meters returns 0 point 00 mi`() {
        assertEquals("0.00 mi", formatDistance(0.0))
    }

    @Test
    fun `formatDistance 1609 point 34 meters returns 1 point 00 mi`() {
        assertEquals("1.00 mi", formatDistance(1609.34))
    }

    @Test
    fun `formatDistance 804 point 67 meters returns 0 point 50 mi`() {
        assertEquals("0.50 mi", formatDistance(804.67))
    }

    @Test
    fun `formatDistance 5150 meters returns 3 point 20 mi`() {
        assertEquals("3.20 mi", formatDistance(5150.0))
    }
}
