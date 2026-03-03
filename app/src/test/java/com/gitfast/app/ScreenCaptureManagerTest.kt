package com.gitfast.app

import com.gitfast.app.util.ScreenCaptureManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenCaptureManagerTest {

    @Test
    fun `generateFilename produces expected format`() {
        // 2026-03-03 14:15:22 UTC
        val timestamp = 1772626522000L
        val filename = ScreenCaptureManager.generateFilename(timestamp)

        assertTrue(filename.startsWith("git-fast_"))
        assertTrue(filename.endsWith(".png"))
        // Format: git-fast_YYYYMMDD_HHMMSS.png
        val regex = Regex("""^git-fast_\d{8}_\d{6}\.png$""")
        assertTrue("Filename '$filename' does not match expected pattern", regex.matches(filename))
    }

    @Test
    fun `generateFilename uses consistent format for same timestamp`() {
        val timestamp = 1700000000000L
        val first = ScreenCaptureManager.generateFilename(timestamp)
        val second = ScreenCaptureManager.generateFilename(timestamp)

        assertEquals(first, second)
    }

    @Test
    fun `generateFilename produces different names for different timestamps`() {
        val filename1 = ScreenCaptureManager.generateFilename(1700000000000L)
        val filename2 = ScreenCaptureManager.generateFilename(1700000001000L)

        assertTrue(filename1 != filename2)
    }
}
