package com.gitfast.app

import com.gitfast.app.data.local.Converters
import com.gitfast.app.data.model.ActivityType
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

    // --- WorkoutStatus ---

    @Test
    fun `fromWorkoutStatus converts all values to string`() {
        WorkoutStatus.entries.forEach { status ->
            assertEquals(status.name, converters.fromWorkoutStatus(status))
        }
    }

    @Test
    fun `toWorkoutStatus converts all strings back`() {
        WorkoutStatus.entries.forEach { status ->
            assertEquals(status, converters.toWorkoutStatus(status.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toWorkoutStatus throws on invalid string`() {
        converters.toWorkoutStatus("INVALID")
    }

    // --- PhaseType ---

    @Test
    fun `fromPhaseType converts all values to string`() {
        PhaseType.entries.forEach { type ->
            assertEquals(type.name, converters.fromPhaseType(type))
        }
    }

    @Test
    fun `toPhaseType converts all strings back`() {
        PhaseType.entries.forEach { type ->
            assertEquals(type, converters.toPhaseType(type.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toPhaseType throws on invalid string`() {
        converters.toPhaseType("INVALID")
    }

    // --- ActivityType ---

    @Test
    fun `fromActivityType converts all values to string`() {
        ActivityType.entries.forEach { type ->
            assertEquals(type.name, converters.fromActivityType(type))
        }
    }

    @Test
    fun `toActivityType converts all strings back`() {
        ActivityType.entries.forEach { type ->
            assertEquals(type, converters.toActivityType(type.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toActivityType throws on invalid string`() {
        converters.toActivityType("INVALID")
    }

    // --- WeatherCondition (nullable) ---

    @Test
    fun `fromWeatherCondition converts all values to string`() {
        WeatherCondition.entries.forEach { condition ->
            assertEquals(condition.name, converters.fromWeatherCondition(condition))
        }
    }

    @Test
    fun `fromWeatherCondition returns null for null input`() {
        assertNull(converters.fromWeatherCondition(null))
    }

    @Test
    fun `toWeatherCondition converts all strings back`() {
        WeatherCondition.entries.forEach { condition ->
            assertEquals(condition, converters.toWeatherCondition(condition.name))
        }
    }

    @Test
    fun `toWeatherCondition returns null for null input`() {
        assertNull(converters.toWeatherCondition(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toWeatherCondition throws on invalid string`() {
        converters.toWeatherCondition("INVALID")
    }

    // --- WeatherTemp (nullable) ---

    @Test
    fun `fromWeatherTemp converts all values to string`() {
        WeatherTemp.entries.forEach { temp ->
            assertEquals(temp.name, converters.fromWeatherTemp(temp))
        }
    }

    @Test
    fun `fromWeatherTemp returns null for null input`() {
        assertNull(converters.fromWeatherTemp(null))
    }

    @Test
    fun `toWeatherTemp converts all strings back`() {
        WeatherTemp.entries.forEach { temp ->
            assertEquals(temp, converters.toWeatherTemp(temp.name))
        }
    }

    @Test
    fun `toWeatherTemp returns null for null input`() {
        assertNull(converters.toWeatherTemp(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toWeatherTemp throws on invalid string`() {
        converters.toWeatherTemp("INVALID")
    }

    // --- EnergyLevel (nullable) ---

    @Test
    fun `fromEnergyLevel converts all values to string`() {
        EnergyLevel.entries.forEach { level ->
            assertEquals(level.name, converters.fromEnergyLevel(level))
        }
    }

    @Test
    fun `fromEnergyLevel returns null for null input`() {
        assertNull(converters.fromEnergyLevel(null))
    }

    @Test
    fun `toEnergyLevel converts all strings back`() {
        EnergyLevel.entries.forEach { level ->
            assertEquals(level, converters.toEnergyLevel(level.name))
        }
    }

    @Test
    fun `toEnergyLevel returns null for null input`() {
        assertNull(converters.toEnergyLevel(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toEnergyLevel throws on invalid string`() {
        converters.toEnergyLevel("INVALID")
    }

    // --- Round-trip tests ---

    @Test
    fun `round-trip WorkoutStatus preserves all values`() {
        WorkoutStatus.entries.forEach { status ->
            assertEquals(status, converters.toWorkoutStatus(converters.fromWorkoutStatus(status)))
        }
    }

    @Test
    fun `round-trip nullable types preserve null`() {
        assertNull(converters.toWeatherCondition(converters.fromWeatherCondition(null)))
        assertNull(converters.toWeatherTemp(converters.fromWeatherTemp(null)))
        assertNull(converters.toEnergyLevel(converters.fromEnergyLevel(null)))
    }
}
