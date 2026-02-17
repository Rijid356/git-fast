package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.PaceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class WorkoutStateManager @Inject constructor() {

    private val _workoutState = MutableStateFlow(WorkoutTrackingState())
    val workoutState: StateFlow<WorkoutTrackingState> = _workoutState.asStateFlow()

    private val _gpsPoints = MutableStateFlow<List<GpsPoint>>(emptyList())
    val gpsPoints: StateFlow<List<GpsPoint>> = _gpsPoints.asStateFlow()

    private var workoutId: String? = null
    private var workoutStartTime: Instant? = null
    private var pauseStartTime: Instant? = null
    private var totalPausedDuration: Long = 0L
    private var gpsPointIndex: Int = 0

    fun startWorkout(): String {
        val id = UUID.randomUUID().toString()
        workoutId = id
        workoutStartTime = Instant.now()
        totalPausedDuration = 0L
        gpsPointIndex = 0
        _gpsPoints.value = emptyList()

        _workoutState.value = WorkoutTrackingState(
            isActive = true,
            isPaused = false,
            workoutId = id,
            elapsedSeconds = 0,
            distanceMeters = 0.0,
            currentPaceSecondsPerMile = null,
            averagePaceSecondsPerMile = null
        )
        return id
    }

    fun pauseWorkout() {
        pauseStartTime = Instant.now()
        _workoutState.value = _workoutState.value.copy(isPaused = true)
    }

    fun resumeWorkout() {
        pauseStartTime?.let { pauseStart ->
            totalPausedDuration += Instant.now().toEpochMilli() - pauseStart.toEpochMilli()
        }
        pauseStartTime = null
        _workoutState.value = _workoutState.value.copy(isPaused = false)
    }

    fun stopWorkout(): WorkoutSnapshot {
        val endTime = Instant.now()
        val snapshot = WorkoutSnapshot(
            workoutId = workoutId ?: "",
            startTime = workoutStartTime ?: endTime,
            endTime = endTime,
            gpsPoints = _gpsPoints.value,
            totalDistanceMeters = _workoutState.value.distanceMeters,
            totalPausedDurationMillis = totalPausedDuration
        )

        workoutId = null
        workoutStartTime = null
        pauseStartTime = null
        totalPausedDuration = 0L
        gpsPointIndex = 0
        _gpsPoints.value = emptyList()
        _workoutState.value = WorkoutTrackingState()

        return snapshot
    }

    fun addGpsPoint(point: GpsPoint) {
        if (_workoutState.value.isPaused) return

        val updatedPoints = _gpsPoints.value + point
        _gpsPoints.value = updatedPoints
        gpsPointIndex++

        // Calculate running distance by adding the distance from
        // the previous point to the new point.
        val distanceMeters = if (updatedPoints.size >= 2) {
            val prev = updatedPoints[updatedPoints.size - 2]
            val segmentDistance = DistanceCalculator.haversineMeters(
                prev.latitude, prev.longitude,
                point.latitude, point.longitude
            )
            _workoutState.value.distanceMeters + segmentDistance
        } else {
            0.0
        }

        val elapsedSeconds = _workoutState.value.elapsedSeconds

        _workoutState.value = _workoutState.value.copy(
            distanceMeters = distanceMeters,
            currentPaceSecondsPerMile = PaceCalculator.currentPace(updatedPoints),
            averagePaceSecondsPerMile = PaceCalculator.averagePace(
                elapsedSeconds, distanceMeters
            )
        )
    }

    fun updateElapsedTime() {
        val start = workoutStartTime ?: return
        if (_workoutState.value.isPaused) return

        val now = Instant.now()
        val totalElapsed = now.toEpochMilli() - start.toEpochMilli()
        val activeElapsed = totalElapsed - totalPausedDuration
        _workoutState.value = _workoutState.value.copy(
            elapsedSeconds = (activeElapsed / 1000).toInt()
        )
    }
}

data class WorkoutTrackingState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val workoutId: String? = null,
    val elapsedSeconds: Int = 0,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerMile: Int? = null,
    val averagePaceSecondsPerMile: Int? = null
)

data class WorkoutSnapshot(
    val workoutId: String,
    val startTime: Instant,
    val endTime: Instant,
    val gpsPoints: List<GpsPoint>,
    val totalDistanceMeters: Double,
    val totalPausedDurationMillis: Long
)
