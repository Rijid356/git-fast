package com.gitfast.app.data.local

import androidx.room.TypeConverter
import com.gitfast.app.data.model.PhaseType
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
}
