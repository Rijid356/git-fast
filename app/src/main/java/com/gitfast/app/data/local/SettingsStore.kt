package com.gitfast.app.data.local

import android.content.Context
import com.gitfast.app.data.model.DistanceUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var autoPauseEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PAUSE_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_PAUSE_ENABLED, value).apply()
        }

    var distanceUnit: DistanceUnit
        get() {
            val name = prefs.getString(KEY_DISTANCE_UNIT, DistanceUnit.MILES.name)
            return try {
                DistanceUnit.valueOf(name ?: DistanceUnit.MILES.name)
            } catch (e: Exception) {
                DistanceUnit.MILES
            }
        }
        set(value) {
            prefs.edit().putString(KEY_DISTANCE_UNIT, value.name).apply()
        }

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) {
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()
        }

    var autoLapEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LAP_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_LAP_ENABLED, value).apply()
        }

    var homeArrivalEnabled: Boolean
        get() = prefs.getBoolean(KEY_HOME_ARRIVAL_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_HOME_ARRIVAL_ENABLED, value).apply()
        }

    var homeLatitude: Double?
        get() {
            val raw = prefs.getString(KEY_HOME_LATITUDE, null) ?: return null
            return raw.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_HOME_LATITUDE, value.toString()).apply()
            } else {
                prefs.edit().remove(KEY_HOME_LATITUDE).apply()
            }
        }

    var homeLongitude: Double?
        get() {
            val raw = prefs.getString(KEY_HOME_LONGITUDE, null) ?: return null
            return raw.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_HOME_LONGITUDE, value.toString()).apply()
            } else {
                prefs.edit().remove(KEY_HOME_LONGITUDE).apply()
            }
        }

    var homeArrivalRadiusMeters: Int
        get() = prefs.getInt(KEY_HOME_ARRIVAL_RADIUS, 15)
        set(value) {
            prefs.edit().putInt(KEY_HOME_ARRIVAL_RADIUS, value).apply()
        }

    val hasHomeLocation: Boolean
        get() = homeLatitude != null && homeLongitude != null

    var lapStartLatitude: Double?
        get() {
            val raw = prefs.getString(KEY_LAP_START_LATITUDE, null) ?: return null
            return raw.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_LAP_START_LATITUDE, value.toString()).apply()
            } else {
                prefs.edit().remove(KEY_LAP_START_LATITUDE).apply()
            }
        }

    var lapStartLongitude: Double?
        get() {
            val raw = prefs.getString(KEY_LAP_START_LONGITUDE, null) ?: return null
            return raw.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_LAP_START_LONGITUDE, value.toString()).apply()
            } else {
                prefs.edit().remove(KEY_LAP_START_LONGITUDE).apply()
            }
        }

    val hasLapStartPoint: Boolean
        get() = lapStartLatitude != null && lapStartLongitude != null

    fun clearLapStartPoint() {
        prefs.edit()
            .remove(KEY_LAP_START_LATITUDE)
            .remove(KEY_LAP_START_LONGITUDE)
            .apply()
    }

    var healthConnectLastSync: Long
        get() = prefs.getLong(KEY_HC_LAST_SYNC, 0L)
        set(value) {
            prefs.edit().putLong(KEY_HC_LAST_SYNC, value).apply()
        }

    fun clearHomeLocation() {
        prefs.edit()
            .remove(KEY_HOME_LATITUDE)
            .remove(KEY_HOME_LONGITUDE)
            .apply()
    }

    var screenshotOverlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREENSHOT_OVERLAY_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SCREENSHOT_OVERLAY_ENABLED, value).apply()
        }

    companion object {
        const val AUTO_LAP_ANCHOR_RADIUS_METERS = 5
        const val LAP_START_CLUSTER_RADIUS_METERS = 50.0

        private const val PREFS_NAME = "gitfast_settings"
        private const val KEY_AUTO_PAUSE_ENABLED = "auto_pause_enabled"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_LAP_ENABLED = "auto_lap_enabled"
        private const val KEY_HOME_ARRIVAL_ENABLED = "home_arrival_enabled"
        private const val KEY_HOME_LATITUDE = "home_latitude"
        private const val KEY_HOME_LONGITUDE = "home_longitude"
        private const val KEY_HOME_ARRIVAL_RADIUS = "home_arrival_radius"
        private const val KEY_HC_LAST_SYNC = "health_connect_last_sync"
        private const val KEY_LAP_START_LATITUDE = "lap_start_latitude"
        private const val KEY_LAP_START_LONGITUDE = "lap_start_longitude"
        private const val KEY_SCREENSHOT_OVERLAY_ENABLED = "screenshot_overlay_enabled"
    }
}
