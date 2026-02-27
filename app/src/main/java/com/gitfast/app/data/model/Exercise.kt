package com.gitfast.app.data.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val equipment: Equipment,
    val category: ExerciseCategory,
    val primaryMuscles: Set<MuscleGroup>,
    val secondaryMuscles: Set<MuscleGroup> = emptySet(),
    val difficulty: Difficulty,
    val isUnilateral: Boolean = false,
    val hasWeight: Boolean = false,
)
