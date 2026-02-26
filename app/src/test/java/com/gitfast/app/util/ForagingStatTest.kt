package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForagingStatTest {

    @Test
    fun `zero events returns minimum stat of 1`() {
        assertEquals(1, StatsCalculator.calculateForaging(0))
    }

    @Test
    fun `negative events returns minimum stat of 1`() {
        assertEquals(1, StatsCalculator.calculateForaging(-5))
    }

    @Test
    fun `5 events yields stat around 10`() {
        val stat = StatsCalculator.calculateForaging(5)
        assertEquals(10, stat)
    }

    @Test
    fun `20 events yields stat around 25`() {
        val stat = StatsCalculator.calculateForaging(20)
        assertEquals(25, stat)
    }

    @Test
    fun `50 events yields stat around 50`() {
        val stat = StatsCalculator.calculateForaging(50)
        assertEquals(50, stat)
    }

    @Test
    fun `100 events yields stat around 75`() {
        val stat = StatsCalculator.calculateForaging(100)
        assertEquals(75, stat)
    }

    @Test
    fun `200 events yields stat at or near 99`() {
        val stat = StatsCalculator.calculateForaging(200)
        assertEquals(99, stat)
    }

    @Test
    fun `300 events does not exceed 99`() {
        val stat = StatsCalculator.calculateForaging(300)
        assertTrue("Stat should not exceed 99, got $stat", stat <= 99)
    }

    @Test
    fun `1 event yields a stat between 1 and 10`() {
        val stat = StatsCalculator.calculateForaging(1)
        assertTrue("1 event stat should be >= 1, got $stat", stat >= 1)
        assertTrue("1 event stat should be <= 10, got $stat", stat <= 10)
    }

    @Test
    fun `stat increases monotonically`() {
        var prevStat = 0
        for (count in listOf(1, 5, 10, 20, 50, 100, 200)) {
            val stat = StatsCalculator.calculateForaging(count)
            assertTrue(
                "Stat should increase: $count events -> $stat, prev was $prevStat",
                stat >= prevStat
            )
            prevStat = stat
        }
    }

    @Test
    fun `intermediate values interpolate smoothly`() {
        // Between 5->10 and 20->25, 12 events should give a value between 10 and 25
        val stat = StatsCalculator.calculateForaging(12)
        assertTrue("12 events should be >= 10, got $stat", stat >= 10)
        assertTrue("12 events should be <= 25, got $stat", stat <= 25)
    }
}
