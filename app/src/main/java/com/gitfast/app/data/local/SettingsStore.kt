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

    companion object {
        private const val PREFS_NAME = "gitfast_settings"
        private const val KEY_AUTO_PAUSE_ENABLED = "auto_pause_enabled"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_LAP_ENABLED = "auto_lap_enabled"
        private const val KEY_AUTO_LAP_ANCHOR_RADIUS = "auto_lap_anchor_radius"
    }
}
