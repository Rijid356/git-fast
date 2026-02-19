package com.gitfast.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.gitfast.app.MainActivity
import com.gitfast.app.R
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.location.GpsTracker
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.formatElapsedTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : LifecycleService() {

    @Inject lateinit var gpsTracker: GpsTracker
    @Inject lateinit var workoutStateManager: WorkoutStateManager
    @Inject lateinit var workoutRepository: WorkoutRepository
    @Inject lateinit var workoutSaveManager: WorkoutSaveManager
    @Inject lateinit var workoutStateStore: WorkoutStateStore
    @Inject lateinit var autoPauseDetector: AutoPauseDetector
    @Inject lateinit var settingsStore: SettingsStore

    private var gpsCollectionJob: Job? = null
    private var timerJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "workout_tracking"
        const val CHANNEL_NAME = "Workout Tracking"

        const val ACTION_START = "com.gitfast.app.ACTION_START"
        const val ACTION_PAUSE = "com.gitfast.app.ACTION_PAUSE"
        const val ACTION_RESUME = "com.gitfast.app.ACTION_RESUME"
        const val ACTION_STOP = "com.gitfast.app.ACTION_STOP"
        const val ACTION_DISCARD = "com.gitfast.app.ACTION_DISCARD"
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"
        const val ACTION_START_LAPS = "com.gitfast.app.ACTION_START_LAPS"
        const val ACTION_MARK_LAP = "com.gitfast.app.ACTION_MARK_LAP"
        const val ACTION_END_LAPS = "com.gitfast.app.ACTION_END_LAPS"

        @Volatile
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> startWorkout(intent)
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> stopWorkout()
            ACTION_DISCARD -> discardWorkout()
            ACTION_START_LAPS -> workoutStateManager.startLaps()
            ACTION_MARK_LAP -> workoutStateManager.markLap()
            ACTION_END_LAPS -> workoutStateManager.endLaps()
        }

        return START_REDELIVER_INTENT
    }

    private fun startWorkout(intent: Intent? = null) {
        val activityTypeName = intent?.getStringExtra(EXTRA_ACTIVITY_TYPE)
        val activityType = activityTypeName?.let {
            try { ActivityType.valueOf(it) } catch (e: Exception) { ActivityType.RUN }
        } ?: ActivityType.RUN

        val workoutId = workoutStateManager.startWorkout(activityType)

        // Configure auto-lap GPS anchor from settings
        workoutStateManager.setAutoLapConfig(
            enabled = settingsStore.autoLapEnabled,
            anchorRadiusMeters = settingsStore.autoLapAnchorRadiusMeters
        )

        workoutStateStore.setActiveWorkout(workoutId, Instant.now().toEpochMilli())

        startForeground(NOTIFICATION_ID, buildNotification("Workout started"))

        autoPauseDetector.reset()

        gpsCollectionJob = lifecycleScope.launch {
            gpsTracker.startTracking().collect { point ->
                handleAutoPause(point, activityType)
                workoutStateManager.addGpsPoint(point)
                Log.d(
                    "WorkoutService",
                    "GPS: ${point.latitude}, ${point.longitude} " +
                        "accuracy=${point.accuracy}m speed=${point.speed}"
                )
            }
        }

        timerJob = lifecycleScope.launch {
            while (isActive) {
                workoutStateManager.updateElapsedTime()
                updateNotification()
                delay(1_000L)
            }
        }
    }

    private fun pauseWorkout() {
        autoPauseDetector.reset()
        workoutStateManager.pauseWorkout()
        gpsCollectionJob?.cancel()
        updateNotification("Workout paused")
    }

    private fun resumeWorkout() {
        autoPauseDetector.reset()
        workoutStateManager.resumeWorkout()

        gpsCollectionJob = lifecycleScope.launch {
            gpsTracker.startTracking().collect { point ->
                handleAutoPause(point, workoutStateManager.workoutState.value.activityType)
                workoutStateManager.addGpsPoint(point)
            }
        }

        updateNotification("Tracking workout")
    }

    private fun stopWorkout() {
        gpsCollectionJob?.cancel()
        timerJob?.cancel()

        val snapshot = workoutStateManager.stopWorkout()

        lifecycleScope.launch {
            val saveResult = workoutSaveManager.saveCompletedWorkout(snapshot)
            if (saveResult != null) {
                if (saveResult.achievementsUnlocked.isNotEmpty()) {
                    workoutStateManager.setUnlockedAchievements(saveResult.achievementsUnlocked)
                }
                workoutStateManager.setSaveStreakInfo(saveResult.streakDays, saveResult.streakMultiplier)
            }
            workoutStateStore.clearActiveWorkout()

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun discardWorkout() {
        gpsCollectionJob?.cancel()
        timerJob?.cancel()

        workoutStateManager.stopWorkout()
        workoutStateStore.clearActiveWorkout()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleAutoPause(point: GpsPoint, activityType: ActivityType) {
        if (!settingsStore.autoPauseEnabled) return
        if (activityType != ActivityType.RUN) return

        val state = workoutStateManager.workoutState.value
        val result = autoPauseDetector.analyzePoint(point, state.isAutoPaused)

        if (result.shouldAutoPause && !state.isPaused) {
            workoutStateManager.autoPauseWorkout()
            updateNotification("[AUTO-PAUSED]")
        }
        if (result.shouldAutoResume && state.isAutoPaused) {
            workoutStateManager.autoResumeWorkout()
            updateNotification("Tracking workout")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows while git-fast is tracking a workout"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("git-fast")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String? = null) {
        val state = workoutStateManager.workoutState.value
        val elapsed = formatElapsedTime(state.elapsedSeconds)
        val isDogWalk = state.activityType == ActivityType.DOG_WALK

        val content = text ?: run {
            if (state.isAutoPaused) {
                "[AUTO-PAUSED] \u2022 $elapsed"
            } else {
                val distanceMiles = DistanceCalculator.metersToMiles(state.distanceMeters)

                if (distanceMiles >= 0.01) {
                    "${"%.2f".format(distanceMiles)} mi \u2022 $elapsed"
                } else {
                    val label = if (isDogWalk) "Dog walk" else "Tracking workout"
                    "$label \u2022 $elapsed"
                }
            }
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private val binder = WorkoutBinder()

    inner class WorkoutBinder : Binder() {
        fun getStateManager(): WorkoutStateManager = workoutStateManager
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
}
