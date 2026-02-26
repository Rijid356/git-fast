package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutStateManagerEventsTest {

    private lateinit var manager: WorkoutStateManager

    @Before
    fun setUp() {
        manager = WorkoutStateManager()
    }

    @Test
    fun `logDogWalkEvent increments total event count`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)

        assertEquals(1, manager.workoutState.value.dogWalkEventCount)
    }

    @Test
    fun `logDogWalkEvent updates per-type counts`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.POOP, 40.0, -74.0)

        val counts = manager.workoutState.value.dogWalkEventCounts
        assertEquals(2, counts[DogWalkEventType.SNACK_FOUND])
        assertEquals(1, counts[DogWalkEventType.POOP])
    }

    @Test
    fun `undoLastEvent removes the last event of that type`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.1, -74.1)

        val result = manager.undoLastEvent(DogWalkEventType.SNACK_FOUND)

        assertTrue(result)
        assertEquals(1, manager.workoutState.value.dogWalkEventCount)
        assertEquals(1, manager.workoutState.value.dogWalkEventCounts[DogWalkEventType.SNACK_FOUND])
    }

    @Test
    fun `undoLastEvent returns false when no events of that type exist`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)

        val result = manager.undoLastEvent(DogWalkEventType.POOP)

        assertFalse(result)
        assertEquals(1, manager.workoutState.value.dogWalkEventCount)
    }

    @Test
    fun `undoLastEvent only removes the specified type`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.POOP, 40.0, -74.0)

        manager.undoLastEvent(DogWalkEventType.SNACK_FOUND)

        assertEquals(1, manager.workoutState.value.dogWalkEventCount)
        assertEquals(null, manager.workoutState.value.dogWalkEventCounts[DogWalkEventType.SNACK_FOUND])
        assertEquals(1, manager.workoutState.value.dogWalkEventCounts[DogWalkEventType.POOP])
    }

    @Test
    fun `getDogWalkEvents returns copy of events`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.ZOOMIES, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.FRIENDLY_DOG, 40.0, -74.0)

        val events = manager.getDogWalkEvents()
        assertEquals(2, events.size)
        assertEquals(DogWalkEventType.ZOOMIES, events[0].type)
        assertEquals(DogWalkEventType.FRIENDLY_DOG, events[1].type)
    }

    @Test
    fun `events store GPS coordinates`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.DEEP_SNIFF, 40.123, -74.456)

        val event = manager.getDogWalkEvents().first()
        assertEquals(40.123, event.latitude!!, 0.001)
        assertEquals(-74.456, event.longitude!!, 0.001)
    }

    @Test
    fun `events store null GPS when not available`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.DEEP_SNIFF, null, null)

        val event = manager.getDogWalkEvents().first()
        assertEquals(null, event.latitude)
        assertEquals(null, event.longitude)
    }

    @Test
    fun `startWorkout clears previous events`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.POOP, 40.0, -74.0)

        // Start a new workout
        manager.startWorkout(ActivityType.DOG_WALK)

        assertEquals(0, manager.workoutState.value.dogWalkEventCount)
        assertTrue(manager.getDogWalkEvents().isEmpty())
    }

    @Test
    fun `stopWorkout includes events in snapshot`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SQUIRREL_CHASE, 40.0, -74.0)
        manager.logDogWalkEvent(DogWalkEventType.ZOOMIES, 40.0, -74.0)

        val snapshot = manager.stopWorkout()

        assertEquals(2, snapshot.dogWalkEvents.size)
        assertEquals(DogWalkEventType.SQUIRREL_CHASE, snapshot.dogWalkEvents[0].type)
        assertEquals(DogWalkEventType.ZOOMIES, snapshot.dogWalkEvents[1].type)
    }

    @Test
    fun `stopWorkout clears events`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        manager.logDogWalkEvent(DogWalkEventType.SNACK_FOUND, 40.0, -74.0)

        manager.stopWorkout()

        assertEquals(0, manager.workoutState.value.dogWalkEventCount)
        assertTrue(manager.getDogWalkEvents().isEmpty())
    }

    @Test
    fun `event counts in state start at zero`() {
        manager.startWorkout(ActivityType.DOG_WALK)

        assertEquals(0, manager.workoutState.value.dogWalkEventCount)
        assertTrue(manager.workoutState.value.dogWalkEventCounts.isEmpty())
    }
}
