package com.gitfast.app.data.local

import android.content.Context
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

    companion object {
        private const val PREFS_NAME = "gitfast_settings"
        private const val KEY_AUTO_PAUSE_ENABLED = "auto_pause_enabled"
    }
}
