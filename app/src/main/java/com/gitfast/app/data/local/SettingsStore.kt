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

    var autoLapAnchorRadiusMeters: Int
        get() = prefs.getInt(KEY_AUTO_LAP_ANCHOR_RADIUS, 15)
        set(value) {
            prefs.edit().putInt(KEY_AUTO_LAP_ANCHOR_RADIUS, value).apply()
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
        get() = prefs.getInt(KEY_HOME_ARRIVAL_RADIUS, 30)
        set(value) {
            prefs.edit().putInt(KEY_HOME_ARRIVAL_RADIUS, value).apply()
        }

    val hasHomeLocation: Boolean
        get() = homeLatitude != null && homeLongitude != null

    fun clearHomeLocation() {
        prefs.edit()
            .remove(KEY_HOME_LATITUDE)
            .remove(KEY_HOME_LONGITUDE)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "gitfast_settings"
        private const val KEY_AUTO_PAUSE_ENABLED = "auto_pause_enabled"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_LAP_ENABLED = "auto_lap_enabled"
        private const val KEY_AUTO_LAP_ANCHOR_RADIUS = "auto_lap_anchor_radius"
        private const val KEY_HOME_ARRIVAL_ENABLED = "home_arrival_enabled"
        private const val KEY_HOME_LATITUDE = "home_latitude"
        private const val KEY_HOME_LONGITUDE = "home_longitude"
        private const val KEY_HOME_ARRIVAL_RADIUS = "home_arrival_radius"
    }
}
