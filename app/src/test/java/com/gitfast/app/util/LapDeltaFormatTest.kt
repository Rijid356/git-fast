package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LapDeltaFormatTest {

    @Test
    fun `formatDelta negative returns up arrow with negative seconds`() {
        val result = LapAnalyzer.formatDelta(-7)
        assertEquals("\u25B2 -7s", result)
    }

    @Test
    fun `formatDelta positive returns down arrow with positive seconds`() {
        val result = LapAnalyzer.formatDelta(3)
        assertEquals("\u25BC +3s", result)
    }

    @Test
    fun `formatDelta zero returns equals sign`() {
        val result = LapAnalyzer.formatDelta(0)
        assertEquals("= 0s", result)
    }
}
