package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.service.WorkoutTrackingState
import com.gitfast.app.service.buildNotificationContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationContentTest {

    @Test
    fun `title shows Running for active run`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.RUN
        )
        val content = buildNotificationContent(state)
        assertEquals("git-fast \u2022 Running", content.title)
    }

    @Test
    fun `title shows Dog Walk for active dog walk`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.DOG_WALK
        )
        val content = buildNotificationContent(state)
        assertEquals("git-fast \u2022 Dog Walk", content.title)
    }

    @Test
    fun `title shows Paused when paused for run`() {
        val state = WorkoutTrackingState(
            isActive = true,
            isPaused = true,
            activityType = ActivityType.RUN
        )
        val content = buildNotificationContent(state)
        assertEquals("git-fast \u2022 Paused", content.title)
    }

    @Test
    fun `title shows Paused when paused for dog walk`() {
        val state = WorkoutTrackingState(
            isActive = true,
            isPaused = true,
            activityType = ActivityType.DOG_WALK
        )
        val content = buildNotificationContent(state)
        assertEquals("git-fast \u2022 Paused", content.title)
    }

    @Test
    fun `collapsed text for run includes time distance and pace`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.RUN,
            elapsedSeconds = 332, // 05:32
            distanceMeters = 676.0, // ~0.42 mi
            currentPaceSecondsPerMile = 765 // 12:45
        )
        val content = buildNotificationContent(state)
        assertTrue(content.collapsedText.contains("05:32"))
        assertTrue(content.collapsedText.contains("mi"))
        assertTrue(content.collapsedText.contains("12:45 /mi"))
    }

    @Test
    fun `collapsed text for dog walk includes time and distance but no pace`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.DOG_WALK,
            elapsedSeconds = 1395, // 23:15
            distanceMeters = 1335.0, // ~0.83 mi
            currentPaceSecondsPerMile = 900
        )
        val content = buildNotificationContent(state)
        assertTrue(content.collapsedText.contains("23:15"))
        assertTrue(content.collapsedText.contains("mi"))
        assertFalse(content.collapsedText.contains("/mi"))
    }

    @Test
    fun `expanded text for run has all 4 stat lines`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.RUN,
            elapsedSeconds = 332,
            distanceMeters = 676.0,
            currentPaceSecondsPerMile = 765,
            averagePaceSecondsPerMile = 782
        )
        val content = buildNotificationContent(state)
        assertTrue(content.expandedText.contains("Time"))
        assertTrue(content.expandedText.contains("Distance"))
        assertTrue(content.expandedText.contains("Pace"))
        assertTrue(content.expandedText.contains("Avg Pace"))
    }

    @Test
    fun `expanded text for dog walk has only time and distance`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.DOG_WALK,
            elapsedSeconds = 1395,
            distanceMeters = 1335.0,
            currentPaceSecondsPerMile = 900,
            averagePaceSecondsPerMile = 950
        )
        val content = buildNotificationContent(state)
        assertTrue(content.expandedText.contains("Time"))
        assertTrue(content.expandedText.contains("Distance"))
        assertFalse(content.expandedText.contains("Pace"))
        assertFalse(content.expandedText.contains("Avg Pace"))
    }

    @Test
    fun `expanded text for paused state shows paused after time`() {
        val state = WorkoutTrackingState(
            isActive = true,
            isPaused = true,
            activityType = ActivityType.RUN,
            elapsedSeconds = 332,
            distanceMeters = 676.0,
            currentPaceSecondsPerMile = 765
        )
        val content = buildNotificationContent(state)
        assertTrue(content.expandedText.contains("(paused)"))
        // "(paused)" should be on the Time line
        val timeLine = content.expandedText.lines().first { it.contains("Time") }
        assertTrue(timeLine.contains("(paused)"))
    }

    @Test
    fun `null pace shows placeholder`() {
        val state = WorkoutTrackingState(
            isActive = true,
            activityType = ActivityType.RUN,
            elapsedSeconds = 10,
            distanceMeters = 0.0,
            currentPaceSecondsPerMile = null,
            averagePaceSecondsPerMile = null
        )
        val content = buildNotificationContent(state)
        assertTrue(content.collapsedText.contains("-- /mi"))
        assertTrue(content.expandedText.contains("-- /mi"))
    }
}
