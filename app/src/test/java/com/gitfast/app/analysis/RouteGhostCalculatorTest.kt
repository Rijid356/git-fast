package com.gitfast.app.analysis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RouteGhostCalculatorTest {

    /** Create a precise linear profile with enough points that interpolation is near-exact. */
    private fun linearProfile(totalDistanceMeters: Double, totalSeconds: Int): DistanceTimeProfile {
        val n = 1001
        val distances = DoubleArray(n) { i -> totalDistanceMeters * i.toDouble() / (n - 1) }
        val elapsed = IntArray(n) { i -> (totalSeconds.toLong() * i / (n - 1)).toInt() }
        return DistanceTimeProfile(
            distances = distances,
            elapsedSeconds = elapsed,
            totalDistanceMeters = totalDistanceMeters,
            totalSeconds = totalSeconds,
        )
    }

    private fun assertDeltaEquals(expected: Int, actual: Int?, tolerance: Int = 1) {
        checkNotNull(actual) { "Expected delta $expected but was null" }
        assertTrue(
            "Expected delta $expected +/- $tolerance but got $actual",
            abs(expected - actual) <= tolerance,
        )
    }

    @Test
    fun `empty profiles returns null delta`() {
        val result = RouteGhostCalculator.calculateDelta(100.0, 60, emptyList())
        assertNull(result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `single profile - exactly on pace`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(500.0, 300, listOf(profile))
        assertDeltaEquals(0, result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `single profile - behind ghost (positive delta)`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(500.0, 350, listOf(profile))
        assertDeltaEquals(50, result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `single profile - ahead of ghost (negative delta)`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(500.0, 250, listOf(profile))
        assertDeltaEquals(-50, result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `multiple profiles - averages interpolated times`() {
        val profile1 = linearProfile(1000.0, 600)
        val profile2 = linearProfile(1000.0, 400)
        val result = RouteGhostCalculator.calculateDelta(500.0, 280, listOf(profile1, profile2))
        assertDeltaEquals(30, result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `partial exhaustion - some profiles exceeded`() {
        val profile1 = linearProfile(1000.0, 600)
        val profile2 = linearProfile(400.0, 240)
        val result = RouteGhostCalculator.calculateDelta(500.0, 300, listOf(profile1, profile2))
        assertDeltaEquals(0, result.deltaSeconds)
        assertTrue(result.isExhausted)
    }

    @Test
    fun `full exhaustion - all profiles exceeded`() {
        val profile1 = linearProfile(400.0, 240)
        val profile2 = linearProfile(300.0, 180)
        val result = RouteGhostCalculator.calculateDelta(500.0, 350, listOf(profile1, profile2))
        assertNull(result.deltaSeconds)
        assertTrue(result.isExhausted)
    }

    @Test
    fun `at distance zero - delta equals current elapsed`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(0.0, 5, listOf(profile))
        assertDeltaEquals(5, result.deltaSeconds)
        assertFalse(result.isExhausted)
    }

    @Test
    fun `delta sign correctness - negative means ahead`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(500.0, 200, listOf(profile))
        assertTrue(result.deltaSeconds!! < 0)
    }

    @Test
    fun `delta sign correctness - positive means behind`() {
        val profile = linearProfile(1000.0, 600)
        val result = RouteGhostCalculator.calculateDelta(500.0, 400, listOf(profile))
        assertTrue(result.deltaSeconds!! > 0)
    }
}
