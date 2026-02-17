package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpCalculatorTest {

    @Test
    fun `minimum XP of 5 for very short workout`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 10.0,
            durationMillis = 30_000L,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
        )
        assertEquals(5, result.totalXp)
    }

    @Test
    fun `run earns 10 XP per mile`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34, // 1 mile
            durationMillis = 600_000L, // 10 min
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
        )
        // 10 XP for 1 mile + 5 XP for 10 min = 15
        assertEquals(15, result.totalXp)
    }

    @Test
    fun `walk earns 8 XP per mile`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34, // 1 mile
            durationMillis = 1200_000L, // 20 min
            activityType = ActivityType.DOG_WALK,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
        )
        // 8 XP for 1 mile + 10 XP for 20 min = 18
        assertEquals(18, result.totalXp)
    }

    @Test
    fun `duration XP awards 5 per 10 minutes`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 0.0,
            durationMillis = 1800_000L, // 30 min
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
        )
        // 0 distance XP + 15 duration XP (30 min / 10 * 5)
        assertEquals(15, result.totalXp)
    }

    @Test
    fun `lap bonus awards 20 XP per lap`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 3,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = true,
        )
        // 10 distance + 5 duration + 60 laps = 75
        assertEquals(75, result.totalXp)
    }

    @Test
    fun `all phases bonus adds 15 XP`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 1,
            hasWarmup = true,
            hasCooldown = true,
            hasLaps = true,
        )
        // 10 distance + 5 duration + 20 lap + 15 all-phases = 50
        assertEquals(50, result.totalXp)
    }

    @Test
    fun `no all phases bonus without cooldown`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 1,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = true,
        )
        // 10 distance + 5 duration + 20 lap = 35 (no all-phases bonus)
        assertEquals(35, result.totalXp)
    }

    @Test
    fun `rainy weather gives 1_25x multiplier`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
            weatherCondition = WeatherCondition.RAINY,
        )
        // base = 15, * 1.25 = 18
        assertEquals(18, result.totalXp)
    }

    @Test
    fun `hot weather gives 1_1x multiplier`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
            weatherTemp = WeatherTemp.HOT,
        )
        // base = 15, * 1.1 = 16
        assertEquals(16, result.totalXp)
    }

    @Test
    fun `rainy weather overrides hot temperature multiplier`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = true,
            hasCooldown = false,
            hasLaps = false,
            weatherCondition = WeatherCondition.RAINY,
            weatherTemp = WeatherTemp.HOT,
        )
        // base = 15, max(1.25, 1.1) = 1.25, 15 * 1.25 = 18
        assertEquals(18, result.totalXp)
    }

    @Test
    fun `breakdown contains descriptive entries`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1609.34,
            durationMillis = 600_000L,
            activityType = ActivityType.RUN,
            lapCount = 2,
            hasWarmup = true,
            hasCooldown = true,
            hasLaps = true,
        )
        assertTrue(result.breakdown.any { it.contains("miles") })
        assertTrue(result.breakdown.any { it.contains("min active") })
        assertTrue(result.breakdown.any { it.contains("laps") })
        assertTrue(result.breakdown.any { it.contains("full workout") })
    }

    // --- Leveling tests ---

    @Test
    fun `xpForLevel returns 0 for level 1`() {
        assertEquals(0, XpCalculator.xpForLevel(1))
    }

    @Test
    fun `xpForLevel returns 50 for level 2`() {
        assertEquals(50, XpCalculator.xpForLevel(2))
    }

    @Test
    fun `xpForLevel returns 150 for level 3`() {
        assertEquals(150, XpCalculator.xpForLevel(3))
    }

    @Test
    fun `xpForLevel increases with each level`() {
        var prev = XpCalculator.xpForLevel(1)
        for (level in 2..10) {
            val current = XpCalculator.xpForLevel(level)
            assertTrue("Level $level XP ($current) should be > level ${level-1} XP ($prev)", current > prev)
            prev = current
        }
    }

    @Test
    fun `levelForXp returns 1 for 0 XP`() {
        assertEquals(1, XpCalculator.levelForXp(0))
    }

    @Test
    fun `levelForXp returns correct level at exact boundary`() {
        assertEquals(2, XpCalculator.levelForXp(50))
        assertEquals(3, XpCalculator.levelForXp(150))
    }

    @Test
    fun `levelForXp returns correct level between boundaries`() {
        assertEquals(2, XpCalculator.levelForXp(100))
        assertEquals(1, XpCalculator.levelForXp(49))
    }

    @Test
    fun `levelForXp and xpForLevel are consistent`() {
        for (level in 1..20) {
            val xp = XpCalculator.xpForLevel(level)
            assertEquals("levelForXp(xpForLevel($level)) should be $level", level, XpCalculator.levelForXp(xp))
        }
    }

    @Test
    fun `xpCostForLevel returns 50 times level`() {
        assertEquals(50, XpCalculator.xpCostForLevel(1))
        assertEquals(100, XpCalculator.xpCostForLevel(2))
        assertEquals(500, XpCalculator.xpCostForLevel(10))
    }
}
