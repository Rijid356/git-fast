package com.gitfast.app

import com.gitfast.app.data.local.Converters
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumConverterTest {

    private val converters = Converters()

    // --- ActivityType ---

    @Test
    fun `ActivityType RUN serializes to RUN string`() {
        assertEquals("RUN", converters.fromActivityType(ActivityType.RUN))
    }

    @Test
    fun `ActivityType DOG_WALK serializes to DOG_WALK string`() {
        assertEquals("DOG_WALK", converters.fromActivityType(ActivityType.DOG_WALK))
    }

    @Test
    fun `ActivityType RUN deserializes from RUN string`() {
        assertEquals(ActivityType.RUN, converters.toActivityType("RUN"))
    }

    @Test
    fun `ActivityType DOG_WALK deserializes from DOG_WALK string`() {
        assertEquals(ActivityType.DOG_WALK, converters.toActivityType("DOG_WALK"))
    }

    // --- WeatherCondition ---

    @Test
    fun `WeatherCondition serializes all values correctly`() {
        for (condition in WeatherCondition.entries) {
            assertEquals(condition.name, converters.fromWeatherCondition(condition))
        }
    }

    @Test
    fun `WeatherCondition deserializes all values correctly`() {
        for (condition in WeatherCondition.entries) {
            assertEquals(condition, converters.toWeatherCondition(condition.name))
        }
    }

    @Test
    fun `WeatherCondition null serializes to null`() {
        assertNull(converters.fromWeatherCondition(null))
    }

    @Test
    fun `WeatherCondition null deserializes from null`() {
        assertNull(converters.toWeatherCondition(null))
    }

    // --- WeatherTemp ---

    @Test
    fun `WeatherTemp serializes all values correctly`() {
        for (temp in WeatherTemp.entries) {
            assertEquals(temp.name, converters.fromWeatherTemp(temp))
        }
    }

    @Test
    fun `WeatherTemp deserializes all values correctly`() {
        for (temp in WeatherTemp.entries) {
            assertEquals(temp, converters.toWeatherTemp(temp.name))
        }
    }

    @Test
    fun `WeatherTemp null serializes to null`() {
        assertNull(converters.fromWeatherTemp(null))
    }

    @Test
    fun `WeatherTemp null deserializes from null`() {
        assertNull(converters.toWeatherTemp(null))
    }

    // --- EnergyLevel ---

    @Test
    fun `EnergyLevel serializes all values correctly`() {
        for (level in EnergyLevel.entries) {
            assertEquals(level.name, converters.fromEnergyLevel(level))
        }
    }

    @Test
    fun `EnergyLevel deserializes all values correctly`() {
        for (level in EnergyLevel.entries) {
            assertEquals(level, converters.toEnergyLevel(level.name))
        }
    }

    @Test
    fun `EnergyLevel null serializes to null`() {
        assertNull(converters.fromEnergyLevel(null))
    }

    @Test
    fun `EnergyLevel null deserializes from null`() {
        assertNull(converters.toEnergyLevel(null))
    }
}
