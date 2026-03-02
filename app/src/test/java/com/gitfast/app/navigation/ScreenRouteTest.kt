package com.gitfast.app.navigation

import com.gitfast.app.data.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.net.URLEncoder

class ScreenRouteTest {

    @Test
    fun `Workout createRoute encodes activity type`() {
        val route = Screen.Workout.createRoute(ActivityType.RUN)
        assertEquals("workout?activityType=RUN", route)
    }

    @Test
    fun `Workout createRoute encodes dog walk activity type`() {
        val route = Screen.Workout.createRoute(ActivityType.DOG_WALK)
        assertEquals("workout?activityType=DOG_WALK", route)
    }

    @Test
    fun `Detail createRoute includes workoutId`() {
        val route = Screen.Detail.createRoute("abc-123")
        assertEquals("detail/abc-123", route)
    }

    @Test
    fun `DogWalkSummary createRoute includes workoutId`() {
        val route = Screen.DogWalkSummary.createRoute("walk-456")
        assertEquals("dog_walk_summary/walk-456", route)
    }

    @Test
    fun `WorkoutSummary createRoute encodes all parameters`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "25:30",
            distance = "3.14 mi",
            pace = "8:07 /mi",
            steps = "4,521",
            lapCount = 3,
            bestLapTime = "7:45",
            bestLapNumber = 2,
            trendLabel = "Improving",
            workoutId = "w-001",
            xpEarned = 150,
            achievements = listOf("First Run", "Speed Demon"),
            streakDays = 5,
        )

        assertTrue(route.startsWith("workout_summary/"))
        assertTrue(route.contains("lapCount=3"))
        assertTrue(route.contains("bestLapNumber=2"))
        assertTrue(route.contains("xpEarned=150"))
        assertTrue(route.contains("streakDays=5"))

        // Verify URL-encoded values decode correctly
        val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
        assertTrue(route.contains(enc("25:30")))
        assertTrue(route.contains(enc("3.14 mi")))
        assertTrue(route.contains(enc("8:07 /mi")))
        assertTrue(route.contains("bestLapTime=${enc("7:45")}"))
        assertTrue(route.contains("trendLabel=${enc("Improving")}"))
        assertTrue(route.contains("workoutId=${enc("w-001")}"))
    }

    @Test
    fun `WorkoutSummary createRoute with special characters in time and pace`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "1:05:30",
            distance = "10.5 mi",
            pace = "10:03 /mi",
            steps = "12,345",
        )

        val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
        assertTrue(route.contains(enc("1:05:30")))
        assertTrue(route.contains(enc("10:03 /mi")))
        assertTrue(route.contains(enc("12,345")))
    }

    @Test
    fun `WorkoutSummary createRoute with achievements pipe-delimited`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "20:00",
            distance = "2.5 mi",
            pace = "8:00 /mi",
            steps = "3000",
            achievements = listOf("First Run", "Speed Demon", "Marathon Prep"),
        )

        val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
        val expectedAchievements = enc("First Run|Speed Demon|Marathon Prep")
        assertTrue(route.contains("achievements=$expectedAchievements"))
    }

    @Test
    fun `WorkoutSummary createRoute with empty achievements omits param`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "20:00",
            distance = "2.5 mi",
            pace = "8:00 /mi",
            steps = "3000",
            achievements = emptyList(),
        )

        assertFalse(route.contains("achievements="))
    }

    @Test
    fun `WorkoutSummary createRoute with default optional params`() {
        val route = Screen.WorkoutSummary.createRoute(
            time = "15:00",
            distance = "1.5 mi",
            pace = "10:00 /mi",
            steps = "2000",
        )

        assertTrue(route.contains("lapCount=0"))
        assertTrue(route.contains("xpEarned=0"))
        assertTrue(route.contains("streakDays=0"))
        assertFalse(route.contains("bestLapTime="))
        assertFalse(route.contains("bestLapNumber="))
        assertFalse(route.contains("trendLabel="))
        assertFalse(route.contains("workoutId="))
        assertFalse(route.contains("achievements="))
    }
}
