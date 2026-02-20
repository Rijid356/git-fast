package com.gitfast.app

import com.gitfast.app.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSummaryRouteTest {

    @Test
    fun `createRoute includes workoutId when provided`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "10:00",
            distance = "1.00 mi",
            pace = "10:00 /mi",
            steps = "50",
            workoutId = "abc-123",
        )
        assertTrue(route.contains("workoutId=abc-123"))
    }

    @Test
    fun `createRoute omits workoutId when null`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "10:00",
            distance = "1.00 mi",
            pace = "10:00 /mi",
            steps = "50",
            workoutId = null,
        )
        assertFalse(route.contains("workoutId="))
    }

    @Test
    fun `createRoute URL-encodes workoutId with special characters`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "10:00",
            distance = "1.00 mi",
            pace = "10:00 /mi",
            steps = "50",
            workoutId = "abc 123",
        )
        assertTrue(route.contains("workoutId=abc+123") || route.contains("workoutId=abc%20123"))
        assertFalse(route.contains("workoutId=abc 123"))
    }

    @Test
    fun `createRoute includes all parameters together`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "30:00",
            distance = "3.10 mi",
            pace = "9:41 /mi",
            steps = "150",
            lapCount = 5,
            bestLapTime = "5:30",
            bestLapNumber = 3,
            trendLabel = "Getting faster",
            workoutId = "workout-uuid-456",
        )
        assertTrue(route.contains("lapCount=5"))
        assertTrue(route.contains("bestLapNumber=3"))
        assertTrue(route.contains("workoutId=workout-uuid-456"))
    }

    @Test
    fun `route pattern includes workoutId placeholder`() {
        assertTrue(Screen.WorkoutSummary.route.contains("{workoutId}"))
    }

    @Test
    fun `Detail createRoute produces correct path`() {
        val route = Screen.Detail.createRoute("my-workout-id")
        assertEquals("detail/my-workout-id", route)
    }
}
