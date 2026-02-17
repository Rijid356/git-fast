package com.gitfast.app.data.local

import androidx.room.TypeConverter
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus

class Converters {
    @TypeConverter
    fun fromWorkoutStatus(status: WorkoutStatus): String = status.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)

    @TypeConverter
    fun fromPhaseType(type: PhaseType): String = type.name

    @TypeConverter
    fun toPhaseType(value: String): PhaseType = PhaseType.valueOf(value)

    @TypeConverter
    fun fromActivityType(type: ActivityType): String = type.name

    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)

    @TypeConverter
    fun fromWeatherCondition(condition: WeatherCondition?): String? = condition?.name

    @TypeConverter
    fun toWeatherCondition(value: String?): WeatherCondition? = value?.let { WeatherCondition.valueOf(it) }

    @TypeConverter
    fun fromWeatherTemp(temp: WeatherTemp?): String? = temp?.name

    @TypeConverter
    fun toWeatherTemp(value: String?): WeatherTemp? = value?.let { WeatherTemp.valueOf(it) }

    @TypeConverter
    fun fromEnergyLevel(level: EnergyLevel?): String? = level?.name

    @TypeConverter
    fun toEnergyLevel(value: String?): EnergyLevel? = value?.let { EnergyLevel.valueOf(it) }
}
