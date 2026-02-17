package com.gitfast.app

import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatterTest {

    @Test
    fun `formatElapsedTime 0 seconds returns 00 colon 00`() {
        assertEquals("00:00", formatElapsedTime(0))
    }

    @Test
    fun `formatElapsedTime 65 seconds returns 01 colon 05`() {
        assertEquals("01:05", formatElapsedTime(65))
    }

    @Test
    fun `formatElapsedTime 3661 seconds returns 1 colon 01 colon 01`() {
        assertEquals("1:01:01", formatElapsedTime(3661))
    }

    @Test
    fun `formatPace 600 seconds per mile returns 10 colon 00 per mi`() {
        assertEquals("10:00 /mi", formatPace(600))
    }

    @Test
    fun `formatPace 495 seconds per mile returns 8 colon 15 per mi`() {
        assertEquals("8:15 /mi", formatPace(495))
    }
}
