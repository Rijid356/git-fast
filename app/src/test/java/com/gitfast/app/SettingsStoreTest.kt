package com.gitfast.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    // =========================================================================
    // autoLapEnabled
    // =========================================================================

    @Test
    fun `autoLapEnabled defaults to false`() {
        assertFalse(settingsStore.autoLapEnabled)
    }

    @Test
    fun `autoLapEnabled can be set to true and read back`() {
        settingsStore.autoLapEnabled = true
        assertTrue(settingsStore.autoLapEnabled)
    }

    // =========================================================================
    // homeArrivalEnabled
    // =========================================================================

    @Test
    fun `homeArrivalEnabled defaults to false`() {
        assertFalse(settingsStore.homeArrivalEnabled)
    }

    @Test
    fun `homeArrivalEnabled can be set to true and read back`() {
        settingsStore.homeArrivalEnabled = true
        assertTrue(settingsStore.homeArrivalEnabled)
    }

    // =========================================================================
    // Home location properties
    // =========================================================================

    @Test
    fun `homeLatitude defaults to null`() {
        assertNull(settingsStore.homeLatitude)
    }

    @Test
    fun `homeLatitude and homeLongitude can be set and read back`() {
        settingsStore.homeLatitude = 40.7128
        settingsStore.homeLongitude = -74.0060

        assertEquals(40.7128, settingsStore.homeLatitude!!, 0.0001)
        assertEquals(-74.0060, settingsStore.homeLongitude!!, 0.0001)
    }

    @Test
    fun `homeLatitude can be cleared by setting null`() {
        settingsStore.homeLatitude = 40.7128
        settingsStore.homeLatitude = null

        assertNull(settingsStore.homeLatitude)
    }

    // =========================================================================
    // Lap start location properties
    // =========================================================================

    @Test
    fun `lapStartLatitude and lapStartLongitude can be set and read back`() {
        settingsStore.lapStartLatitude = 34.0522
        settingsStore.lapStartLongitude = -118.2437

        assertEquals(34.0522, settingsStore.lapStartLatitude!!, 0.0001)
        assertEquals(-118.2437, settingsStore.lapStartLongitude!!, 0.0001)
    }

    // =========================================================================
    // Computed properties
    // =========================================================================

    @Test
    fun `hasHomeLocation returns false when no location set`() {
        assertFalse(settingsStore.hasHomeLocation)
    }

    @Test
    fun `hasHomeLocation returns true when both lat and lng set`() {
        settingsStore.homeLatitude = 40.7128
        settingsStore.homeLongitude = -74.0060

        assertTrue(settingsStore.hasHomeLocation)
    }

    @Test
    fun `hasLapStartPoint returns false when no start point set`() {
        assertFalse(settingsStore.hasLapStartPoint)
    }

    @Test
    fun `hasLapStartPoint returns true when both lat and lng set`() {
        settingsStore.lapStartLatitude = 34.0522
        settingsStore.lapStartLongitude = -118.2437

        assertTrue(settingsStore.hasLapStartPoint)
    }

    // =========================================================================
    // Clear methods
    // =========================================================================

    @Test
    fun `clearHomeLocation removes both lat and lng`() {
        settingsStore.homeLatitude = 40.7128
        settingsStore.homeLongitude = -74.0060

        settingsStore.clearHomeLocation()

        assertNull(settingsStore.homeLatitude)
        assertNull(settingsStore.homeLongitude)
        assertFalse(settingsStore.hasHomeLocation)
    }

    @Test
    fun `clearLapStartPoint removes both lat and lng`() {
        settingsStore.lapStartLatitude = 34.0522
        settingsStore.lapStartLongitude = -118.2437

        settingsStore.clearLapStartPoint()

        assertNull(settingsStore.lapStartLatitude)
        assertNull(settingsStore.lapStartLongitude)
        assertFalse(settingsStore.hasLapStartPoint)
    }

    // =========================================================================
    // Int / Long properties
    // =========================================================================

    @Test
    fun `homeArrivalRadiusMeters defaults to 15`() {
        assertEquals(15, settingsStore.homeArrivalRadiusMeters)
    }

    @Test
    fun `homeArrivalRadiusMeters can be set and read back`() {
        settingsStore.homeArrivalRadiusMeters = 30
        assertEquals(30, settingsStore.homeArrivalRadiusMeters)
    }

    @Test
    fun `healthConnectLastSync defaults to 0`() {
        assertEquals(0L, settingsStore.healthConnectLastSync)
    }

    @Test
    fun `healthConnectLastSync can be set and read back`() {
        settingsStore.healthConnectLastSync = 1_700_000_000_000L
        assertEquals(1_700_000_000_000L, settingsStore.healthConnectLastSync)
    }
}
