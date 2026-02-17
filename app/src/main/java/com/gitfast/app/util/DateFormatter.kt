package com.gitfast.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatter {

    private val shortDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val timeOfDayFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    private fun zone(): ZoneId = ZoneId.systemDefault()

    fun shortDate(instant: Instant): String {
        return shortDateFormatter.format(instant.atZone(zone()))
    }

    fun timeOfDay(instant: Instant): String {
        return timeOfDayFormatter.format(instant.atZone(zone()))
    }

    fun monthYear(instant: Instant): String {
        return monthYearFormatter.format(instant.atZone(zone()))
    }

    fun relativeDate(instant: Instant): String {
        val zone = zone()
        val date = instant.atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> shortDate(instant)
        }
    }
}
