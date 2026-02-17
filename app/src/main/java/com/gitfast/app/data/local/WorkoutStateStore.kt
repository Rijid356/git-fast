package com.gitfast.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkoutStateStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setActiveWorkout(workoutId: String, startTime: Long) {
        prefs.edit()
            .putString(KEY_WORKOUT_ID, workoutId)
            .putLong(KEY_START_TIME, startTime)
            .apply()
    }

    fun clearActiveWorkout() {
        prefs.edit()
            .remove(KEY_WORKOUT_ID)
            .remove(KEY_START_TIME)
            .apply()
    }

    fun getActiveWorkoutId(): String? {
        return prefs.getString(KEY_WORKOUT_ID, null)
    }

    fun getActiveWorkoutStartTime(): Long? {
        return if (prefs.contains(KEY_START_TIME)) {
            prefs.getLong(KEY_START_TIME, 0L)
        } else {
            null
        }
    }

    fun hasActiveWorkout(): Boolean {
        return prefs.contains(KEY_WORKOUT_ID)
    }

    companion object {
        private const val PREFS_NAME = "workout_state"
        private const val KEY_WORKOUT_ID = "active_workout_id"
        private const val KEY_START_TIME = "active_workout_start_time"
    }
}
