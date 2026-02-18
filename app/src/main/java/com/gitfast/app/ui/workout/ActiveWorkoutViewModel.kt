package com.gitfast.app.ui.workout

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.ui.detail.LapTrend
import com.gitfast.app.util.LapAnalyzer
import com.gitfast.app.util.PermissionManager
import com.gitfast.app.util.XpCalculator
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class WorkoutUiState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isAutoPaused: Boolean = false,
    val keepScreenOn: Boolean = true,
    val workoutId: String? = null,
    val elapsedTimeFormatted: String = "00:00",
    val distanceFormatted: String = "0.00 mi",
    val currentPaceFormatted: String? = null,
    val averagePaceFormatted: String? = null,
    val gpsPointCount: Int = 0,
    val isWorkoutComplete: Boolean = false,
    val isDiscarded: Boolean = false,
    val activityType: ActivityType = ActivityType.RUN,
    // Phase tracking
    val phase: PhaseType = PhaseType.WARMUP,
    val phaseLabel: String = "WARMUP",
    // Lap tracking
    val lapCount: Int = 0,
    val currentLapNumber: Int = 0,
    val currentLapTimeFormatted: String = "00:00",
    val lastLapTimeFormatted: String? = null,
    val lastLapDeltaSeconds: Int? = null,
    val lastLapDeltaFormatted: String? = null,
    val bestLapTimeFormatted: String? = null,
    val averageLapTimeFormatted: String? = null,
    // Ghost runner
    val ghostLapTimeFormatted: String? = null,
    val ghostDeltaSeconds: Int? = null,
    val ghostDeltaFormatted: String? = null,
)

data class WorkoutSummaryStats(
    val time: String = "00:00",
    val distance: String = "0.00 mi",
    val pace: String = "-- /mi",
    val points: String = "0",
    val lapCount: Int = 0,
    val bestLapTime: String? = null,
    val bestLapNumber: Int? = null,
    val trendLabel: String? = null,
    val xpEarned: Int = 0,
    val achievementNames: List<String> = emptyList(),
    val streakDays: Int = 0,
    val streakMultiplier: Double = 1.0,
)

data class GhostSource(
    val workoutId: String,
    val date: Instant,
    val bestLapSeconds: Int,
    val lapCount: Int,
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    application: Application,
    private val permissionManager: PermissionManager,
    private val settingsStore: SettingsStore,
    private val workoutRepository: WorkoutRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _permissionState = MutableStateFlow(permissionManager.checkPermissions())
    val permissionState: StateFlow<PermissionManager.PermissionState> = _permissionState.asStateFlow()

    private var _lastSummaryStats = WorkoutSummaryStats()
    val lastSummaryStats: WorkoutSummaryStats get() = _lastSummaryStats

    private var _lastWorkoutId: String? = null
    val lastWorkoutId: String? get() = _lastWorkoutId

    private var _didDiscard = false

    private var activityType: ActivityType = ActivityType.RUN

    private val _ghostSources = MutableStateFlow<List<GhostSource>>(emptyList())
    val ghostSources: StateFlow<List<GhostSource>> = _ghostSources.asStateFlow()

    fun setActivityType(type: ActivityType) {
        activityType = type
        if (type == ActivityType.RUN) loadGhostSources()
    }

    fun loadGhostSources() {
        viewModelScope.launch {
            val workouts = workoutRepository.getRecentWorkoutsWithLaps(5)
            _ghostSources.value = workouts.mapNotNull { workout ->
                val lapsPhase = workout.phases.find { it.type == PhaseType.LAPS }
                val laps = lapsPhase?.laps ?: return@mapNotNull null
                if (laps.isEmpty()) return@mapNotNull null
                val bestLap = laps.minByOrNull { it.durationMillis ?: Long.MAX_VALUE } ?: return@mapNotNull null
                val bestSeconds = ((bestLap.durationMillis ?: 0L) / 1000).toInt()
                if (bestSeconds <= 0) return@mapNotNull null
                GhostSource(
                    workoutId = workout.id,
                    date = workout.startTime,
                    bestLapSeconds = bestSeconds,
                    lapCount = laps.size,
                )
            }
        }
    }

    fun selectGhost(workoutId: String?) {
        if (workoutId == null) {
            stateManager?.setGhostLap(null)
            return
        }
        val source = _ghostSources.value.find { it.workoutId == workoutId } ?: return
        stateManager?.setGhostLap(source.bestLapSeconds)
    }

    private var stateManager: WorkoutStateManager? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? WorkoutService.WorkoutBinder ?: return
            stateManager = binder.getStateManager()
            isBound = true
            collectWorkoutState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stateManager = null
            isBound = false
        }
    }

    fun bindService() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (isBound) {
            val context = getApplication<Application>()
            context.unbindService(serviceConnection)
            isBound = false
            stateManager = null
        }
    }

    fun refreshPermissions() {
        _permissionState.value = permissionManager.checkPermissions()
    }

    fun startWorkout() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_START
            putExtra(WorkoutService.EXTRA_ACTIVITY_TYPE, activityType.name)
        }
        context.startForegroundService(intent)
    }

    fun pauseWorkout() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeWorkout() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun stopWorkout() {
        snapshotSummaryStats()
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun discardWorkout() {
        _didDiscard = true
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_DISCARD
        }
        context.startService(intent)
    }

    fun startLaps() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_START_LAPS
        }
        context.startService(intent)
    }

    fun markLap() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_MARK_LAP
        }
        context.startService(intent)
    }

    fun endLaps() {
        val context = getApplication<Application>()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_END_LAPS
        }
        context.startService(intent)
    }

    private fun snapshotSummaryStats() {
        val state = _uiState.value
        _lastWorkoutId = state.workoutId
        val manager = stateManager

        val lapDurations = manager?.getLapDurations() ?: emptyList()
        val trend = if (lapDurations.size >= 3) {
            LapAnalyzer.calculateTrend(lapDurations)
        } else null

        // Calculate XP preview from current state
        val trackingState = manager?.workoutState?.value
        val xpEarned = if (trackingState != null) {
            XpCalculator.calculateXp(
                distanceMeters = trackingState.distanceMeters,
                durationMillis = trackingState.elapsedSeconds * 1000L,
                activityType = trackingState.activityType,
                lapCount = trackingState.lapCount,
                hasWarmup = true,
                hasCooldown = trackingState.phase == PhaseType.COOLDOWN,
                hasLaps = trackingState.lapCount > 0,
            ).totalXp
        } else 0

        _lastSummaryStats = WorkoutSummaryStats(
            time = state.elapsedTimeFormatted,
            distance = state.distanceFormatted,
            pace = state.averagePaceFormatted ?: "-- /mi",
            points = state.gpsPointCount.toString(),
            lapCount = state.lapCount,
            bestLapTime = state.bestLapTimeFormatted,
            bestLapNumber = manager?.getBestLapNumber(),
            trendLabel = trend?.let {
                when (it) {
                    LapTrend.GETTING_FASTER -> "Getting faster \u25B2"
                    LapTrend.GETTING_SLOWER -> "Getting slower \u25BC"
                    LapTrend.CONSISTENT -> "Consistent pace \u2500"
                    LapTrend.TOO_FEW_LAPS -> null
                }
            },
            xpEarned = xpEarned,
        )
    }

    private fun collectWorkoutState() {
        val manager = stateManager ?: return

        viewModelScope.launch {
            manager.workoutState.collect { state ->
                val wasActive = _uiState.value.isActive
                val isNowInactive = !state.isActive && state.workoutId == null
                val completed = wasActive && isNowInactive

                if (completed) {
                    val achievements = stateManager?.lastUnlockedAchievements?.value ?: emptyList()
                    val streakDays = stateManager?.lastSaveStreakDays?.value ?: 0
                    val streakMultiplier = stateManager?.lastSaveStreakMultiplier?.value ?: 1.0
                    _lastSummaryStats = _lastSummaryStats.copy(
                        achievementNames = achievements.map { "${it.title} (+${it.xpReward} XP)" },
                        streakDays = streakDays,
                        streakMultiplier = streakMultiplier,
                    )
                }

                val bestLap = stateManager?.getBestLapDuration()
                val avgLap = stateManager?.getAverageLapDuration()

                val unit = settingsStore.distanceUnit
                _uiState.value = WorkoutUiState(
                    isActive = state.isActive,
                    isPaused = state.isPaused,
                    isAutoPaused = state.isAutoPaused,
                    keepScreenOn = settingsStore.keepScreenOn,
                    workoutId = state.workoutId,
                    activityType = state.activityType,
                    elapsedTimeFormatted = formatElapsedTime(state.elapsedSeconds),
                    distanceFormatted = formatDistance(state.distanceMeters, unit),
                    currentPaceFormatted = state.currentPaceSecondsPerMile?.let { formatPace(it, unit) },
                    averagePaceFormatted = state.averagePaceSecondsPerMile?.let { formatPace(it, unit) },
                    gpsPointCount = _uiState.value.gpsPointCount,
                    isWorkoutComplete = completed && !_didDiscard,
                    isDiscarded = completed && _didDiscard,
                    phase = state.phase,
                    phaseLabel = when (state.phase) {
                        PhaseType.WARMUP -> "WARMUP"
                        PhaseType.LAPS -> "LAP ${state.currentLapNumber}"
                        PhaseType.COOLDOWN -> "COOLDOWN"
                    },
                    lapCount = state.lapCount,
                    currentLapNumber = state.currentLapNumber,
                    currentLapTimeFormatted = formatElapsedTime(state.currentLapElapsedSeconds),
                    lastLapTimeFormatted = state.lastLapDurationFormatted,
                    lastLapDeltaSeconds = state.lastLapDeltaSeconds,
                    lastLapDeltaFormatted = state.lastLapDeltaSeconds?.let { delta ->
                        if (delta < 0) "\u25B2 ${delta}s"
                        else if (delta > 0) "\u25BC +${delta}s"
                        else "= 0s"
                    },
                    bestLapTimeFormatted = bestLap?.let { formatElapsedTime(it) },
                    averageLapTimeFormatted = avgLap?.let { formatElapsedTime(it) },
                    ghostLapTimeFormatted = state.ghostLapDurationSeconds?.let { formatElapsedTime(it) },
                    ghostDeltaSeconds = state.ghostDeltaSeconds,
                    ghostDeltaFormatted = state.ghostDeltaSeconds?.let { delta ->
                        if (delta < 0) "\u25B2 ${delta}s"
                        else if (delta > 0) "\u25BC +${delta}s"
                        else "= 0s"
                    },
                )
            }
        }

        viewModelScope.launch {
            manager.gpsPoints.collect { points ->
                _uiState.value = _uiState.value.copy(gpsPointCount = points.size)
            }
        }
    }
}
