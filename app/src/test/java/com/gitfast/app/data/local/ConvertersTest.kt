package com.gitfast.app.data.local

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEventType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    fun `WorkoutStatus roundtrip for all values`() {
        for (status in WorkoutStatus.values()) {
            val str = converters.fromWorkoutStatus(status)
            assertEquals(status.name, str)
            assertEquals(status, converters.toWorkoutStatus(str))
        }
    }

    @Test
    fun `PhaseType roundtrip for all values`() {
        for (type in PhaseType.values()) {
            val str = converters.fromPhaseType(type)
            assertEquals(type.name, str)
            assertEquals(type, converters.toPhaseType(str))
        }
    }

    @Test
    fun `ActivityType roundtrip for all values`() {
        for (type in ActivityType.values()) {
            val str = converters.fromActivityType(type)
            assertEquals(type.name, str)
            assertEquals(type, converters.toActivityType(str))
        }
    }

    @Test
    fun `WeatherCondition roundtrip for all values`() {
        for (condition in WeatherCondition.values()) {
            val str = converters.fromWeatherCondition(condition)
            assertEquals(condition.name, str)
            assertEquals(condition, converters.toWeatherCondition(str))
        }
    }

    @Test
    fun `WeatherCondition null handling`() {
        assertNull(converters.fromWeatherCondition(null))
        assertNull(converters.toWeatherCondition(null))
    }

    @Test
    fun `WeatherTemp roundtrip for all values`() {
        for (temp in WeatherTemp.values()) {
            val str = converters.fromWeatherTemp(temp)
            assertEquals(temp.name, str)
            assertEquals(temp, converters.toWeatherTemp(str))
        }
    }

    @Test
    fun `WeatherTemp null handling`() {
        assertNull(converters.fromWeatherTemp(null))
        assertNull(converters.toWeatherTemp(null))
    }

    @Test
    fun `EnergyLevel roundtrip for all values`() {
        for (level in EnergyLevel.values()) {
            val str = converters.fromEnergyLevel(level)
            assertEquals(level.name, str)
            assertEquals(level, converters.toEnergyLevel(str))
        }
    }

    @Test
    fun `EnergyLevel null handling`() {
        assertNull(converters.fromEnergyLevel(null))
        assertNull(converters.toEnergyLevel(null))
    }

    @Test
    fun `DogWalkEventType roundtrip for all values`() {
        for (type in DogWalkEventType.values()) {
            val str = converters.fromDogWalkEventType(type)
            assertEquals(type.name, str)
            assertEquals(type, converters.toDogWalkEventType(str))
        }
    }
}
