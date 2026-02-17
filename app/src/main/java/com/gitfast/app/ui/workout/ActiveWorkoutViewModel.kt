package com.gitfast.app.ui.workout

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.util.PermissionManager
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val workoutId: String? = null,
    val elapsedTimeFormatted: String = "00:00",
    val distanceFormatted: String = "0.00 mi",
    val currentPaceFormatted: String? = null,
    val averagePaceFormatted: String? = null,
    val gpsPointCount: Int = 0,
    val isWorkoutComplete: Boolean = false,
    val isDiscarded: Boolean = false,
)

data class WorkoutSummaryStats(
    val time: String = "00:00",
    val distance: String = "0.00 mi",
    val pace: String = "-- /mi",
    val points: String = "0",
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    application: Application,
    private val permissionManager: PermissionManager,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _permissionState = MutableStateFlow(permissionManager.checkPermissions())
    val permissionState: StateFlow<PermissionManager.PermissionState> = _permissionState.asStateFlow()

    private var _lastSummaryStats = WorkoutSummaryStats()
    val lastSummaryStats: WorkoutSummaryStats get() = _lastSummaryStats

    private var _didDiscard = false

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

    private fun snapshotSummaryStats() {
        val state = _uiState.value
        _lastSummaryStats = WorkoutSummaryStats(
            time = state.elapsedTimeFormatted,
            distance = state.distanceFormatted,
            pace = state.averagePaceFormatted ?: "-- /mi",
            points = state.gpsPointCount.toString(),
        )
    }

    private fun collectWorkoutState() {
        val manager = stateManager ?: return

        viewModelScope.launch {
            manager.workoutState.collect { state ->
                val wasActive = _uiState.value.isActive
                val isNowInactive = !state.isActive && state.workoutId == null
                val completed = wasActive && isNowInactive

                _uiState.value = WorkoutUiState(
                    isActive = state.isActive,
                    isPaused = state.isPaused,
                    workoutId = state.workoutId,
                    elapsedTimeFormatted = formatElapsedTime(state.elapsedSeconds),
                    distanceFormatted = formatDistance(state.distanceMeters),
                    currentPaceFormatted = state.currentPaceSecondsPerMile?.let { formatPace(it) },
                    averagePaceFormatted = state.averagePaceSecondsPerMile?.let { formatPace(it) },
                    gpsPointCount = _uiState.value.gpsPointCount,
                    isWorkoutComplete = completed && !_didDiscard,
                    isDiscarded = completed && _didDiscard,
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
