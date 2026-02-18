package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.util.AchievementDef
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.PaceCalculator
import com.gitfast.app.util.formatElapsedTime
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

    private val _lastUnlockedAchievements = MutableStateFlow<List<AchievementDef>>(emptyList())
    val lastUnlockedAchievements: StateFlow<List<AchievementDef>> = _lastUnlockedAchievements.asStateFlow()

    fun setUnlockedAchievements(achievements: List<AchievementDef>) {
        _lastUnlockedAchievements.value = achievements
    }

    private var workoutId: String? = null
    private var workoutStartTime: Instant? = null
    private var pauseStartTime: Instant? = null
    private var totalPausedDuration: Long = 0L
    private var gpsPointIndex: Int = 0

    // Phase tracking
    private var currentPhase: PhaseType = PhaseType.WARMUP
    private var phaseStartTime: Instant? = null
    private var phaseStartDistance: Double = 0.0
    private var phaseStartSteps: Int = 0

    // Activity type
    private var activityType: ActivityType = ActivityType.RUN

    // Lap tracking
    private var laps: MutableList<LapData> = mutableListOf()
    private var currentLapStartTime: Instant? = null
    private var currentLapStartDistance: Double = 0.0
    private var currentLapNumber: Int = 0

    // Ghost runner
    private var externalGhostLapDuration: Int? = null
    private var useExternalGhost: Boolean = false

    // Phase history (for building entities at save time)
    private var completedPhases: MutableList<PhaseData> = mutableListOf()

    /**
     * In-memory representation of a completed lap.
     * Converted to LapEntity at save time.
     */
    data class LapData(
        val lapNumber: Int,
        val startTime: Instant,
        val endTime: Instant,
        val distanceMeters: Double,
        val steps: Int,
        val gpsStartIndex: Int,
        val gpsEndIndex: Int
    )

    /**
     * In-memory representation of a completed phase.
     * Converted to WorkoutPhaseEntity at save time.
     */
    data class PhaseData(
        val type: PhaseType,
        val startTime: Instant,
        val endTime: Instant,
        val distanceMeters: Double,
        val steps: Int,
        val laps: List<LapData>
    )

    fun setGhostLap(durationSeconds: Int?) {
        externalGhostLapDuration = durationSeconds
        useExternalGhost = durationSeconds != null
        _workoutState.value = _workoutState.value.copy(
            ghostLapDurationSeconds = durationSeconds,
            ghostDeltaSeconds = null,
        )
    }

    fun startWorkout(activityType: ActivityType = ActivityType.RUN): String {
        val id = UUID.randomUUID().toString()
        val now = Instant.now()
        workoutId = id
        this.activityType = activityType
        workoutStartTime = now
        totalPausedDuration = 0L
        gpsPointIndex = 0
        _gpsPoints.value = emptyList()

        // Initialize phase tracking
        currentPhase = PhaseType.WARMUP
        phaseStartTime = now
        phaseStartDistance = 0.0
        laps.clear()
        completedPhases.clear()
        currentLapStartTime = null
        currentLapNumber = 0

        _workoutState.value = WorkoutTrackingState(
            isActive = true,
            isPaused = false,
            workoutId = id,
            elapsedSeconds = 0,
            distanceMeters = 0.0,
            currentPaceSecondsPerMile = null,
            averagePaceSecondsPerMile = null,
            activityType = activityType,
            phase = PhaseType.WARMUP,
            lapCount = 0,
            currentLapNumber = 0,
            currentLapElapsedSeconds = 0,
            lastLapDeltaSeconds = null,
            lastLapDurationFormatted = null
        )
        return id
    }

    fun pauseWorkout() {
        pauseStartTime = Instant.now()
        _workoutState.value = _workoutState.value.copy(isPaused = true, isAutoPaused = false)
    }

    fun resumeWorkout() {
        pauseStartTime?.let { pauseStart ->
            totalPausedDuration += Instant.now().toEpochMilli() - pauseStart.toEpochMilli()
        }
        pauseStartTime = null
        _workoutState.value = _workoutState.value.copy(isPaused = false, isAutoPaused = false)
    }

    fun autoPauseWorkout() {
        pauseStartTime = Instant.now()
        _workoutState.value = _workoutState.value.copy(isPaused = true, isAutoPaused = true)
    }

    fun autoResumeWorkout() {
        pauseStartTime?.let { pauseStart ->
            totalPausedDuration += Instant.now().toEpochMilli() - pauseStart.toEpochMilli()
        }
        pauseStartTime = null
        _workoutState.value = _workoutState.value.copy(isPaused = false, isAutoPaused = false)
    }

    /**
     * Transition from WARMUP to LAPS phase.
     * Closes out the warmup phase and begins tracking laps.
     */
    fun startLaps() {
        val now = Instant.now()
        val currentDistance = _workoutState.value.distanceMeters

        // Close warmup phase
        phaseStartTime?.let { start ->
            completedPhases.add(PhaseData(
                type = PhaseType.WARMUP,
                startTime = start,
                endTime = now,
                distanceMeters = currentDistance - phaseStartDistance,
                steps = 0,
                laps = emptyList()
            ))
        }

        // Begin laps phase
        currentPhase = PhaseType.LAPS
        phaseStartTime = now
        phaseStartDistance = currentDistance
        currentLapStartTime = now
        currentLapStartDistance = currentDistance
        currentLapNumber = 1

        _workoutState.value = _workoutState.value.copy(
            phase = PhaseType.LAPS,
            lapCount = 0,
            currentLapNumber = 1,
            currentLapElapsedSeconds = 0,
            lastLapDeltaSeconds = null
        )
    }

    /**
     * Mark a completed lap and start the next one.
     * Only valid during LAPS phase.
     */
    fun markLap() {
        if (currentPhase != PhaseType.LAPS) return

        val now = Instant.now()
        val currentDistance = _workoutState.value.distanceMeters
        val currentGpsIndex = _gpsPoints.value.size - 1

        val lapStartGpsIndex = if (laps.isEmpty()) {
            // First lap: GPS index when laps phase started
            _gpsPoints.value.indexOfFirst {
                !it.timestamp.isBefore(phaseStartTime)
            }.coerceAtLeast(0)
        } else {
            laps.last().gpsEndIndex + 1
        }

        // Record completed lap
        val completedLap = LapData(
            lapNumber = currentLapNumber,
            startTime = currentLapStartTime ?: now,
            endTime = now,
            distanceMeters = currentDistance - currentLapStartDistance,
            steps = 0,
            gpsStartIndex = lapStartGpsIndex,
            gpsEndIndex = currentGpsIndex.coerceAtLeast(lapStartGpsIndex)
        )
        laps.add(completedLap)

        // Calculate delta vs previous lap
        val deltaSeconds = if (laps.size >= 2) {
            val prevLap = laps[laps.size - 2]
            val prevDuration = prevLap.endTime.toEpochMilli() - prevLap.startTime.toEpochMilli()
            val thisDuration = completedLap.endTime.toEpochMilli() - completedLap.startTime.toEpochMilli()
            ((thisDuration - prevDuration) / 1000).toInt()
        } else null

        // Update auto-ghost (best lap so far) if no external ghost is set
        if (!useExternalGhost) {
            externalGhostLapDuration = getBestLapDuration()
        }

        // Start next lap
        currentLapNumber++
        currentLapStartTime = now
        currentLapStartDistance = currentDistance

        _workoutState.value = _workoutState.value.copy(
            lapCount = laps.size,
            currentLapNumber = currentLapNumber,
            currentLapElapsedSeconds = 0,
            lastLapDeltaSeconds = deltaSeconds,
            lastLapDurationFormatted = formatElapsedTime(
                ((completedLap.endTime.toEpochMilli() - completedLap.startTime.toEpochMilli()) / 1000).toInt()
            ),
            ghostLapDurationSeconds = externalGhostLapDuration,
            ghostDeltaSeconds = null,
        )
    }

    /**
     * Transition from LAPS to COOLDOWN phase.
     * Closes out the laps phase (including the in-progress lap)
     * and begins cooldown tracking.
     */
    fun endLaps() {
        if (currentPhase != PhaseType.LAPS) return

        // If there's an in-progress lap, complete it first
        if (currentLapStartTime != null && currentLapNumber > (laps.lastOrNull()?.lapNumber ?: 0)) {
            markLap()
        }

        val now = Instant.now()
        val currentDistance = _workoutState.value.distanceMeters

        // Close laps phase
        phaseStartTime?.let { start ->
            completedPhases.add(PhaseData(
                type = PhaseType.LAPS,
                startTime = start,
                endTime = now,
                distanceMeters = currentDistance - phaseStartDistance,
                steps = 0,
                laps = laps.toList()
            ))
        }

        // Begin cooldown phase
        currentPhase = PhaseType.COOLDOWN
        phaseStartTime = now
        phaseStartDistance = currentDistance

        _workoutState.value = _workoutState.value.copy(
            phase = PhaseType.COOLDOWN
        )
    }

    /**
     * Get the duration of the fastest lap in seconds.
     * Returns null if no laps completed yet.
     */
    fun getBestLapDuration(): Int? {
        if (laps.isEmpty()) return null
        return laps.minOf {
            ((it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000).toInt()
        }
    }

    /**
     * Get the average lap duration in seconds.
     * Returns null if no laps completed yet.
     */
    fun getAverageLapDuration(): Int? {
        if (laps.isEmpty()) return null
        val totalSeconds = laps.sumOf {
            ((it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000).toInt()
        }
        return totalSeconds / laps.size
    }

    fun getBestLapNumber(): Int? {
        if (laps.isEmpty()) return null
        return laps.minByOrNull {
            it.endTime.toEpochMilli() - it.startTime.toEpochMilli()
        }?.lapNumber
    }

    fun getLapDurations(): List<Int> {
        return laps.map {
            ((it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000).toInt()
        }
    }

    fun stopWorkout(): WorkoutSnapshot {
        val endTime = Instant.now()
        val currentDistance = _workoutState.value.distanceMeters

        // Close current phase
        phaseStartTime?.let { start ->
            completedPhases.add(PhaseData(
                type = currentPhase,
                startTime = start,
                endTime = endTime,
                distanceMeters = currentDistance - phaseStartDistance,
                steps = 0,
                laps = if (currentPhase == PhaseType.LAPS) laps.toList() else emptyList()
            ))
        }

        val snapshot = WorkoutSnapshot(
            workoutId = workoutId ?: "",
            startTime = workoutStartTime ?: endTime,
            endTime = endTime,
            gpsPoints = _gpsPoints.value,
            totalDistanceMeters = currentDistance,
            totalPausedDurationMillis = totalPausedDuration,
            phases = completedPhases.toList(),
            activityType = activityType
        )

        // Reset all state
        workoutId = null
        workoutStartTime = null
        pauseStartTime = null
        totalPausedDuration = 0L
        gpsPointIndex = 0
        activityType = ActivityType.RUN
        currentPhase = PhaseType.WARMUP
        phaseStartTime = null
        laps.clear()
        completedPhases.clear()
        externalGhostLapDuration = null
        useExternalGhost = false
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

        // Also update current lap time if in LAPS phase
        val lapElapsed = if (currentPhase == PhaseType.LAPS && currentLapStartTime != null) {
            ((now.toEpochMilli() - currentLapStartTime!!.toEpochMilli()) / 1000).toInt()
        } else 0

        // Compute ghost delta (positive = behind ghost, negative = ahead)
        val ghostDelta = if (currentPhase == PhaseType.LAPS && externalGhostLapDuration != null) {
            lapElapsed - externalGhostLapDuration!!
        } else null

        _workoutState.value = _workoutState.value.copy(
            elapsedSeconds = (activeElapsed / 1000).toInt(),
            currentLapElapsedSeconds = lapElapsed,
            ghostDeltaSeconds = ghostDelta,
        )
    }
}

data class WorkoutTrackingState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isAutoPaused: Boolean = false,
    val workoutId: String? = null,
    val elapsedSeconds: Int = 0,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerMile: Int? = null,
    val averagePaceSecondsPerMile: Int? = null,
    val activityType: ActivityType = ActivityType.RUN,
    val phase: PhaseType = PhaseType.WARMUP,
    val lapCount: Int = 0,
    val currentLapNumber: Int = 0,
    val currentLapElapsedSeconds: Int = 0,
    val lastLapDeltaSeconds: Int? = null,
    val lastLapDurationFormatted: String? = null,
    val ghostLapDurationSeconds: Int? = null,
    val ghostDeltaSeconds: Int? = null,
)

data class WorkoutSnapshot(
    val workoutId: String,
    val startTime: Instant,
    val endTime: Instant,
    val gpsPoints: List<GpsPoint>,
    val totalDistanceMeters: Double,
    val totalPausedDurationMillis: Long,
    val phases: List<WorkoutStateManager.PhaseData>,
    val activityType: ActivityType
)
