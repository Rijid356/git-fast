package com.gitfast.app

import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.DarkBackground
import com.gitfast.app.ui.theme.DarkSurface
import com.gitfast.app.ui.theme.DarkSurfaceVariant
import com.gitfast.app.ui.theme.ErrorRed
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.ui.theme.NeonGreenDim
import com.gitfast.app.ui.theme.OnDarkBackground
import com.gitfast.app.ui.theme.OnDarkSurface
import com.gitfast.app.ui.theme.OutlineGray
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {

    @Test
    fun `primary color is neon green`() {
        assertEquals(Color(0xFF39FF14), NeonGreen)
    }

    @Test
    fun `primary dim color is correct`() {
        assertEquals(Color(0xFF2BCC10), NeonGreenDim)
    }

    @Test
    fun `background color is near-black`() {
        assertEquals(Color(0xFF0D1117), DarkBackground)
    }

    @Test
    fun `surface color is correct`() {
        assertEquals(Color(0xFF161B22), DarkSurface)
    }

    @Test
    fun `surface variant color is correct`() {
        assertEquals(Color(0xFF21262D), DarkSurfaceVariant)
    }

    @Test
    fun `on-background color is correct`() {
        assertEquals(Color(0xFFE6EDF3), OnDarkBackground)
    }

    @Test
    fun `on-surface color is correct`() {
        assertEquals(Color(0xFFE6EDF3), OnDarkSurface)
    }

    @Test
    fun `secondary color is cyan`() {
        assertEquals(Color(0xFF58A6FF), CyanAccent)
    }

    @Test
    fun `tertiary color is amber`() {
        assertEquals(Color(0xFFF0883E), AmberAccent)
    }

    @Test
    fun `error color is soft red`() {
        assertEquals(Color(0xFFF85149), ErrorRed)
    }

    @Test
    fun `outline color is correct`() {
        assertEquals(Color(0xFF30363D), OutlineGray)
    }
}
