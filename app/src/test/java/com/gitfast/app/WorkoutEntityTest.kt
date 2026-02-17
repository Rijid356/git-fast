package com.gitfast.app

import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutEntityTest {

    @Test
    fun `create WorkoutEntity with all fields populated`() {
        val entity = WorkoutEntity(
            id = "workout-1",
            startTime = 1000L,
            endTime = 2000L,
            totalSteps = 500,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED
        )

        assertEquals("workout-1", entity.id)
        assertEquals(1000L, entity.startTime)
        assertEquals(2000L, entity.endTime)
        assertEquals(500, entity.totalSteps)
        assertEquals(1609.34, entity.distanceMeters, 0.001)
        assertEquals(WorkoutStatus.COMPLETED, entity.status)
    }

    @Test
    fun `create WorkoutEntity with null endTime for active workout`() {
        val entity = WorkoutEntity(
            id = "workout-2",
            startTime = 1000L,
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            status = WorkoutStatus.ACTIVE
        )

        assertNull(entity.endTime)
        assertEquals(WorkoutStatus.ACTIVE, entity.status)
    }

    @Test
    fun `create WorkoutPhaseEntity with all fields populated`() {
        val entity = WorkoutPhaseEntity(
            id = "phase-1",
            workoutId = "workout-1",
            type = PhaseType.WARMUP,
            startTime = 1000L,
            endTime = 2000L,
            distanceMeters = 500.0,
            steps = 200
        )

        assertEquals("phase-1", entity.id)
        assertEquals("workout-1", entity.workoutId)
        assertEquals(PhaseType.WARMUP, entity.type)
        assertEquals(1000L, entity.startTime)
        assertEquals(2000L, entity.endTime)
        assertEquals(500.0, entity.distanceMeters, 0.001)
        assertEquals(200, entity.steps)
    }

    @Test
    fun `create LapEntity with all fields populated`() {
        val entity = LapEntity(
            id = "lap-1",
            phaseId = "phase-1",
            lapNumber = 1,
            startTime = 1000L,
            endTime = 1500L,
            distanceMeters = 400.0,
            steps = 100
        )

        assertEquals("lap-1", entity.id)
        assertEquals("phase-1", entity.phaseId)
        assertEquals(1, entity.lapNumber)
        assertEquals(1000L, entity.startTime)
        assertEquals(1500L, entity.endTime)
        assertEquals(400.0, entity.distanceMeters, 0.001)
        assertEquals(100, entity.steps)
    }

    @Test
    fun `create GpsPointEntity with default id`() {
        val entity = GpsPointEntity(
            workoutId = "workout-1",
            latitude = 37.7749,
            longitude = -122.4194,
            timestamp = 1000L,
            accuracy = 5.0f,
            sortIndex = 0
        )

        assertEquals(0L, entity.id)
        assertEquals("workout-1", entity.workoutId)
        assertEquals(37.7749, entity.latitude, 0.0001)
        assertEquals(-122.4194, entity.longitude, 0.0001)
        assertEquals(1000L, entity.timestamp)
        assertEquals(5.0f, entity.accuracy, 0.01f)
        assertEquals(0, entity.sortIndex)
    }

    @Test
    fun `create GpsPointEntity with explicit id`() {
        val entity = GpsPointEntity(
            id = 42,
            workoutId = "workout-1",
            latitude = 37.7749,
            longitude = -122.4194,
            timestamp = 1000L,
            accuracy = 3.5f,
            sortIndex = 5
        )

        assertEquals(42L, entity.id)
        assertEquals(5, entity.sortIndex)
    }
}
