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
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.location.GpsTracker
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : LifecycleService() {

    @Inject lateinit var gpsTracker: GpsTracker
    @Inject lateinit var workoutStateManager: WorkoutStateManager
    @Inject lateinit var workoutRepository: WorkoutRepository

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
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> startWorkout()
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> stopWorkout()
        }

        return START_REDELIVER_INTENT
    }

    private fun startWorkout() {
        val workoutId = workoutStateManager.startWorkout()

        startForeground(NOTIFICATION_ID, buildNotification("Workout started"))

        lifecycleScope.launch {
            workoutRepository.saveWorkout(
                workout = WorkoutEntity(
                    id = workoutId,
                    startTime = Instant.now().toEpochMilli(),
                    endTime = null,
                    totalSteps = 0,
                    distanceMeters = 0.0,
                    status = WorkoutStatus.ACTIVE,
                    activityType = ActivityType.RUN,
                    dogName = null,
                    notes = null,
                    weatherCondition = null,
                    weatherTemp = null,
                    energyLevel = null,
                    routeTag = null
                ),
                phases = emptyList(),
                laps = emptyList(),
                gpsPoints = emptyList()
            )
        }

        gpsCollectionJob = lifecycleScope.launch {
            gpsTracker.startTracking().collect { point ->
                workoutStateManager.addGpsPoint(point)
                Log.d(
                    "WorkoutService",
                    "GPS: ${point.latitude}, ${point.longitude} " +
                        "accuracy=${point.accuracy}m"
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
        workoutStateManager.pauseWorkout()
        gpsCollectionJob?.cancel()
        updateNotification("Workout paused")

        lifecycleScope.launch {
            val existing = workoutRepository.getActiveWorkout() ?: return@launch
            workoutRepository.updateWorkout(existing.copy(status = WorkoutStatus.PAUSED))
        }
    }

    private fun resumeWorkout() {
        workoutStateManager.resumeWorkout()

        gpsCollectionJob = lifecycleScope.launch {
            gpsTracker.startTracking().collect { point ->
                workoutStateManager.addGpsPoint(point)
            }
        }

        updateNotification("Tracking workout")

        lifecycleScope.launch {
            val existing = workoutRepository.getActiveWorkout() ?: return@launch
            workoutRepository.updateWorkout(existing.copy(status = WorkoutStatus.ACTIVE))
        }
    }

    private fun stopWorkout() {
        gpsCollectionJob?.cancel()
        timerJob?.cancel()

        val snapshot = workoutStateManager.stopWorkout()

        lifecycleScope.launch {
            val existingWorkout = workoutRepository.getActiveWorkout()
            if (existingWorkout != null) {
                workoutRepository.updateWorkout(
                    existingWorkout.copy(
                        endTime = snapshot.endTime.toEpochMilli(),
                        distanceMeters = snapshot.totalDistanceMeters,
                        status = WorkoutStatus.COMPLETED
                    )
                )

                val gpsEntities = snapshot.gpsPoints.mapIndexed { index, point ->
                    GpsPointEntity(
                        workoutId = snapshot.workoutId,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        timestamp = point.timestamp.toEpochMilli(),
                        accuracy = point.accuracy,
                        sortIndex = index
                    )
                }
                workoutRepository.saveGpsPoints(gpsEntities)

                val phase = WorkoutPhaseEntity(
                    id = UUID.randomUUID().toString(),
                    workoutId = snapshot.workoutId,
                    type = PhaseType.WARMUP,
                    startTime = snapshot.startTime.toEpochMilli(),
                    endTime = snapshot.endTime.toEpochMilli(),
                    distanceMeters = snapshot.totalDistanceMeters,
                    steps = 0
                )
                workoutRepository.savePhase(phase)
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
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

        val content = text ?: run {
            val distanceMiles = DistanceCalculator.metersToMiles(state.distanceMeters)
            val paceText = state.currentPaceSecondsPerMile?.let { formatPace(it) }

            if (distanceMiles >= 0.01 && paceText != null) {
                "${"%.2f".format(distanceMiles)} mi \u2022 $paceText"
            } else {
                "Tracking workout \u2022 $elapsed"
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
