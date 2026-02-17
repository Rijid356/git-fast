package com.gitfast.app

import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.local.mappers.toEntity
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WorkoutMapperTest {

    @Test
    fun `map WorkoutEntity to domain Workout and verify all fields`() {
        val entity = WorkoutEntity(
            id = "w-1",
            startTime = 1000L,
            endTime = 2000L,
            totalSteps = 500,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED
        )
        val phases = listOf(
            WorkoutPhase(
                id = "p-1",
                type = PhaseType.WARMUP,
                startTime = Instant.ofEpochMilli(1000),
                endTime = Instant.ofEpochMilli(2000),
                distanceMeters = 1609.34,
                steps = 500,
                laps = emptyList()
            )
        )
        val gpsPoints = listOf(
            GpsPoint(
                latitude = 37.7749,
                longitude = -122.4194,
                timestamp = Instant.ofEpochMilli(1000),
                accuracy = 5.0f
            )
        )

        val workout = entity.toDomain(phases, gpsPoints)

        assertEquals("w-1", workout.id)
        assertEquals(Instant.ofEpochMilli(1000), workout.startTime)
        assertEquals(Instant.ofEpochMilli(2000), workout.endTime)
        assertEquals(500, workout.totalSteps)
        assertEquals(1609.34, workout.distanceMeters, 0.001)
        assertEquals(WorkoutStatus.COMPLETED, workout.status)
        assertEquals(1, workout.phases.size)
        assertEquals(1, workout.gpsPoints.size)
    }

    @Test
    fun `map domain Workout back to entity and verify round-trip`() {
        val workout = Workout(
            id = "w-1",
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(2000),
            totalSteps = 500,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            phases = emptyList(),
            gpsPoints = emptyList()
        )

        val entity = workout.toEntity()

        assertEquals("w-1", entity.id)
        assertEquals(1000L, entity.startTime)
        assertEquals(2000L, entity.endTime)
        assertEquals(500, entity.totalSteps)
        assertEquals(1609.34, entity.distanceMeters, 0.001)
        assertEquals(WorkoutStatus.COMPLETED, entity.status)
    }

    @Test
    fun `handle null endTime for active workout`() {
        val entity = WorkoutEntity(
            id = "w-2",
            startTime = 1000L,
            endTime = null,
            totalSteps = 100,
            distanceMeters = 200.0,
            status = WorkoutStatus.ACTIVE
        )

        val workout = entity.toDomain(emptyList(), emptyList())

        assertNull(workout.endTime)
        assertEquals(WorkoutStatus.ACTIVE, workout.status)

        // Round-trip back to entity
        val roundTripped = workout.toEntity()
        assertNull(roundTripped.endTime)
    }

    @Test
    fun `map GpsPointEntity to domain GpsPoint`() {
        val entity = GpsPointEntity(
            id = 1,
            workoutId = "w-1",
            latitude = 37.7749,
            longitude = -122.4194,
            timestamp = 1500L,
            accuracy = 3.5f,
            sortIndex = 0
        )

        val gpsPoint = entity.toDomain()

        assertEquals(37.7749, gpsPoint.latitude, 0.0001)
        assertEquals(-122.4194, gpsPoint.longitude, 0.0001)
        assertEquals(Instant.ofEpochMilli(1500), gpsPoint.timestamp)
        assertEquals(3.5f, gpsPoint.accuracy, 0.01f)
    }

    @Test
    fun `map domain GpsPoint back to entity`() {
        val gpsPoint = GpsPoint(
            latitude = 40.7128,
            longitude = -74.0060,
            timestamp = Instant.ofEpochMilli(2000),
            accuracy = 10.0f
        )

        val entity = gpsPoint.toEntity(workoutId = "w-1", sortIndex = 3)

        assertEquals(0L, entity.id) // default auto-generate
        assertEquals("w-1", entity.workoutId)
        assertEquals(40.7128, entity.latitude, 0.0001)
        assertEquals(-74.0060, entity.longitude, 0.0001)
        assertEquals(2000L, entity.timestamp)
        assertEquals(10.0f, entity.accuracy, 0.01f)
        assertEquals(3, entity.sortIndex)
    }

    @Test
    fun `map WorkoutPhaseEntity to domain WorkoutPhase`() {
        val entity = WorkoutPhaseEntity(
            id = "p-1",
            workoutId = "w-1",
            type = PhaseType.LAPS,
            startTime = 1000L,
            endTime = 5000L,
            distanceMeters = 800.0,
            steps = 300
        )
        val laps = listOf(
            Lap(
                id = "l-1",
                lapNumber = 1,
                startTime = Instant.ofEpochMilli(1000),
                endTime = Instant.ofEpochMilli(3000),
                distanceMeters = 400.0,
                steps = 150
            )
        )

        val phase = entity.toDomain(laps)

        assertEquals("p-1", phase.id)
        assertEquals(PhaseType.LAPS, phase.type)
        assertEquals(Instant.ofEpochMilli(1000), phase.startTime)
        assertEquals(Instant.ofEpochMilli(5000), phase.endTime)
        assertEquals(800.0, phase.distanceMeters, 0.001)
        assertEquals(300, phase.steps)
        assertEquals(1, phase.laps.size)
        assertEquals("l-1", phase.laps[0].id)
    }

    @Test
    fun `map domain WorkoutPhase back to entity`() {
        val phase = WorkoutPhase(
            id = "p-1",
            type = PhaseType.COOLDOWN,
            startTime = Instant.ofEpochMilli(5000),
            endTime = Instant.ofEpochMilli(6000),
            distanceMeters = 200.0,
            steps = 80,
            laps = emptyList()
        )

        val entity = phase.toEntity(workoutId = "w-1")

        assertEquals("p-1", entity.id)
        assertEquals("w-1", entity.workoutId)
        assertEquals(PhaseType.COOLDOWN, entity.type)
        assertEquals(5000L, entity.startTime)
        assertEquals(6000L, entity.endTime)
        assertEquals(200.0, entity.distanceMeters, 0.001)
        assertEquals(80, entity.steps)
    }

    @Test
    fun `map LapEntity to domain Lap`() {
        val entity = LapEntity(
            id = "l-1",
            phaseId = "p-1",
            lapNumber = 2,
            startTime = 3000L,
            endTime = 4000L,
            distanceMeters = 400.0,
            steps = 150
        )

        val lap = entity.toDomain()

        assertEquals("l-1", lap.id)
        assertEquals(2, lap.lapNumber)
        assertEquals(Instant.ofEpochMilli(3000), lap.startTime)
        assertEquals(Instant.ofEpochMilli(4000), lap.endTime)
        assertEquals(400.0, lap.distanceMeters, 0.001)
        assertEquals(150, lap.steps)
    }

    @Test
    fun `map domain Lap back to entity`() {
        val lap = Lap(
            id = "l-1",
            lapNumber = 3,
            startTime = Instant.ofEpochMilli(4000),
            endTime = Instant.ofEpochMilli(5000),
            distanceMeters = 400.0,
            steps = 160
        )

        val entity = lap.toEntity(phaseId = "p-1")

        assertEquals("l-1", entity.id)
        assertEquals("p-1", entity.phaseId)
        assertEquals(3, entity.lapNumber)
        assertEquals(4000L, entity.startTime)
        assertEquals(5000L, entity.endTime)
        assertEquals(400.0, entity.distanceMeters, 0.001)
        assertEquals(160, entity.steps)
    }

    @Test
    fun `map LapEntity with null endTime`() {
        val entity = LapEntity(
            id = "l-2",
            phaseId = "p-1",
            lapNumber = 1,
            startTime = 1000L,
            endTime = null,
            distanceMeters = 100.0,
            steps = 50
        )

        val lap = entity.toDomain()
        assertNull(lap.endTime)

        val roundTripped = lap.toEntity(phaseId = "p-1")
        assertNull(roundTripped.endTime)
    }

    @Test
    fun `map WorkoutPhaseEntity with null endTime`() {
        val entity = WorkoutPhaseEntity(
            id = "p-2",
            workoutId = "w-1",
            type = PhaseType.WARMUP,
            startTime = 1000L,
            endTime = null,
            distanceMeters = 0.0,
            steps = 0
        )

        val phase = entity.toDomain(emptyList())
        assertNull(phase.endTime)

        val roundTripped = phase.toEntity(workoutId = "w-1")
        assertNull(roundTripped.endTime)
    }
}
