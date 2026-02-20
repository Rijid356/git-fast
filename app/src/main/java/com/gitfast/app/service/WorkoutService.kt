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
import com.gitfast.app.location.StepTracker
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
    @Inject lateinit var stepTracker: StepTracker
    @Inject lateinit var workoutStateManager: WorkoutStateManager
    @Inject lateinit var workoutRepository: WorkoutRepository
    @Inject lateinit var workoutSaveManager: WorkoutSaveManager
    @Inject lateinit var workoutStateStore: WorkoutStateStore
    @Inject lateinit var autoPauseDetector: AutoPauseDetector
    @Inject lateinit var settingsStore: SettingsStore

    private var gpsCollectionJob: Job? = null
    private var stepCollectionJob: Job? = null
    private var timerJob: Job? = null
    private var notificationTimerJob: Job? = null

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

        private const val RC_OPEN_APP = 0
        private const val RC_PAUSE = 1
        private const val RC_RESUME = 2

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

        startForeground(NOTIFICATION_ID, buildNotification(workoutStateManager.workoutState.value))

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

        stepCollectionJob = lifecycleScope.launch {
            var isFirstReading = true
            stepTracker.startTracking().collect { sensorSteps ->
                if (isFirstReading) {
                    workoutStateManager.initStepBaseline(sensorSteps)
                    isFirstReading = false
                } else {
                    workoutStateManager.updateStepCount(sensorSteps)
                }
            }
        }

        timerJob = lifecycleScope.launch {
            while (isActive) {
                workoutStateManager.updateElapsedTime()
                delay(1_000L)
            }
        }

        notificationTimerJob = lifecycleScope.launch {
            while (isActive) {
                updateNotification()
                delay(3_000L)
            }
        }
    }

    private fun pauseWorkout() {
        autoPauseDetector.reset()
        workoutStateManager.pauseWorkout()
        gpsCollectionJob?.cancel()
        stepCollectionJob?.cancel()
        updateNotification()
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

        stepCollectionJob = lifecycleScope.launch {
            stepTracker.startTracking().collect { sensorSteps ->
                workoutStateManager.updateStepCount(sensorSteps)
            }
        }

        updateNotification()
    }

    private fun stopWorkout() {
        gpsCollectionJob?.cancel()
        stepCollectionJob?.cancel()
        timerJob?.cancel()
        notificationTimerJob?.cancel()

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
        stepCollectionJob?.cancel()
        timerJob?.cancel()
        notificationTimerJob?.cancel()

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
            updateNotification()
        }
        if (result.shouldAutoResume && state.isAutoPaused) {
            workoutStateManager.autoResumeWorkout()
            updateNotification()
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

    private fun buildActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, WorkoutService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(state: WorkoutTrackingState): Notification {
        val content = buildNotificationContent(state)

        val openAppIntent = PendingIntent.getActivity(
            this,
            RC_OPEN_APP,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(content.title)
            .setContentText(content.collapsedText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.expandedText))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)

        // Show a real-time chronometer in the notification and status bar
        if (!state.isPaused) {
            builder
                .setUsesChronometer(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis() - state.elapsedSeconds * 1000L)
        } else {
            builder
                .setUsesChronometer(false)
                .setShowWhen(false)
        }

        if (state.isPaused) {
            builder.addAction(
                R.drawable.ic_play,
                "Resume",
                buildActionPendingIntent(ACTION_RESUME, RC_RESUME)
            )
        } else {
            builder.addAction(
                R.drawable.ic_pause,
                "Pause",
                buildActionPendingIntent(ACTION_PAUSE, RC_PAUSE)
            )
        }

        return builder.build()
    }

    private fun updateNotification() {
        val state = workoutStateManager.workoutState.value
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(state))
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
