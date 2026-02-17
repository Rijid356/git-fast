package com.gitfast.app

import com.gitfast.app.util.DateFormatter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.TimeZone

class DateFormatterTest {

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun `shortDate formats correctly`() {
        // 2024-03-15 10:30:00 UTC is a Friday
        val instant = Instant.parse("2024-03-15T10:30:00Z")
        assertEquals("Fri, Mar 15", DateFormatter.shortDate(instant))
    }

    @Test
    fun `timeOfDay formats AM correctly`() {
        val instant = Instant.parse("2024-03-15T09:05:00Z")
        assertEquals("9:05 AM", DateFormatter.timeOfDay(instant))
    }

    @Test
    fun `timeOfDay formats PM correctly`() {
        val instant = Instant.parse("2024-03-15T14:30:00Z")
        assertEquals("2:30 PM", DateFormatter.timeOfDay(instant))
    }

    @Test
    fun `monthYear formats correctly`() {
        val instant = Instant.parse("2024-03-15T10:30:00Z")
        assertEquals("March 2024", DateFormatter.monthYear(instant))
    }

    @Test
    fun `relativeDate today returns Today`() {
        val now = Instant.now()
        assertEquals("Today", DateFormatter.relativeDate(now))
    }

    @Test
    fun `relativeDate yesterday returns Yesterday`() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)
        assertEquals("Yesterday", DateFormatter.relativeDate(yesterday))
    }

    @Test
    fun `relativeDate two days ago returns short date format`() {
        val twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS)
        val expected = DateFormatter.shortDate(twoDaysAgo)
        assertEquals(expected, DateFormatter.relativeDate(twoDaysAgo))
    }
}
