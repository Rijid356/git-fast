package com.gitfast.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LapDeltaFormattingTest {

    /**
     * Pure function that mirrors the delta formatting logic from ActiveWorkoutViewModel.
     * Extracted here so it can be tested without Android framework dependencies.
     *
     * Logic: if delta < 0 -> "▲ ${delta}s" (faster)
     *        if delta > 0 -> "▼ +${delta}s" (slower)
     *        if delta == 0 -> "= 0s"
     *        if null -> null
     */
    private fun formatLapDelta(deltaSeconds: Int?): String? {
        return deltaSeconds?.let { delta ->
            if (delta < 0) "▲ ${delta}s"
            else if (delta > 0) "▼ +${delta}s"
            else "= 0s"
        }
    }

    @Test
    fun `negative delta formats as faster indicator`() {
        val result = formatLapDelta(-7)
        assertEquals("▲ -7s", result)
    }

    @Test
    fun `positive delta formats as slower indicator`() {
        val result = formatLapDelta(3)
        assertEquals("▼ +3s", result)
    }

    @Test
    fun `zero delta formats as equal indicator`() {
        val result = formatLapDelta(0)
        assertEquals("= 0s", result)
    }

    @Test
    fun `null delta returns null`() {
        val result = formatLapDelta(null)
        assertNull(result)
    }

    @Test
    fun `large negative delta formats correctly`() {
        val result = formatLapDelta(-120)
        assertEquals("▲ -120s", result)
    }

    @Test
    fun `large positive delta formats correctly`() {
        val result = formatLapDelta(45)
        assertEquals("▼ +45s", result)
    }
}
