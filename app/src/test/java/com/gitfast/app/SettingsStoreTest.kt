package com.gitfast.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsStoreTest {

    private lateinit var context: Context
    private lateinit var settingsStore: SettingsStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        settingsStore = SettingsStore(context)
    }

    @Test
    fun `autoPauseEnabled defaults to true`() {
        assertTrue(settingsStore.autoPauseEnabled)
    }

    @Test
    fun `autoPauseEnabled can be set to false and read back`() {
        settingsStore.autoPauseEnabled = false
        assertFalse(settingsStore.autoPauseEnabled)
    }

    @Test
    fun `autoPauseEnabled can be toggled back to true`() {
        settingsStore.autoPauseEnabled = false
        settingsStore.autoPauseEnabled = true
        assertTrue(settingsStore.autoPauseEnabled)
    }

    @Test
    fun `distanceUnit defaults to MILES`() {
        assertEquals(DistanceUnit.MILES, settingsStore.distanceUnit)
    }

    @Test
    fun `distanceUnit can be set to KILOMETERS and read back`() {
        settingsStore.distanceUnit = DistanceUnit.KILOMETERS
        assertEquals(DistanceUnit.KILOMETERS, settingsStore.distanceUnit)
    }

    @Test
    fun `keepScreenOn defaults to true`() {
        assertTrue(settingsStore.keepScreenOn)
    }

    @Test
    fun `keepScreenOn can be set to false and read back`() {
        settingsStore.keepScreenOn = false
        assertFalse(settingsStore.keepScreenOn)
    }

    @Test
    fun `distanceUnit falls back to MILES for invalid stored value`() {
        val prefs = context.getSharedPreferences("gitfast_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("distance_unit", "INVALID_UNIT").commit()

        assertEquals(DistanceUnit.MILES, settingsStore.distanceUnit)
    }

    @Test
    fun `settings persist across SettingsStore instances`() {
        settingsStore.autoPauseEnabled = false
        settingsStore.distanceUnit = DistanceUnit.KILOMETERS
        settingsStore.keepScreenOn = false

        val newStore = SettingsStore(context)
        assertFalse(newStore.autoPauseEnabled)
        assertEquals(DistanceUnit.KILOMETERS, newStore.distanceUnit)
        assertFalse(newStore.keepScreenOn)
    }
}
