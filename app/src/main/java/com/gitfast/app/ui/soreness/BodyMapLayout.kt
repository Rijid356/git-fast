package com.gitfast.app.ui.soreness

import androidx.compose.ui.geometry.Rect
import com.gitfast.app.data.model.MuscleGroup

data class BodyZone(
    val muscleGroup: MuscleGroup,
    val relativeRect: Rect,
    val label: String,
)

/**
 * Front body zones (6 muscle groups).
 * Coordinates are normalized 0..1 and scaled to the canvas at render time.
 * Bilateral muscles (biceps, forearms, quads) have paired left/right zones.
 */
val FRONT_ZONES: List<BodyZone> = listOf(
    // Shoulders — wide strip across upper torso
    BodyZone(MuscleGroup.SHOULDERS, Rect(0.15f, 0.10f, 0.85f, 0.20f), "SHLDR"),
    // Chest — upper torso center
    BodyZone(MuscleGroup.CHEST, Rect(0.26f, 0.20f, 0.74f, 0.33f), "CHEST"),
    // Biceps — left and right upper arms
    BodyZone(MuscleGroup.BICEPS, Rect(0.06f, 0.20f, 0.24f, 0.38f), "BIC"),
    BodyZone(MuscleGroup.BICEPS, Rect(0.76f, 0.20f, 0.94f, 0.38f), "BIC"),
    // Core — lower torso / abdomen
    BodyZone(MuscleGroup.CORE, Rect(0.26f, 0.33f, 0.74f, 0.48f), "CORE"),
    // Forearms — left and right lower arms
    BodyZone(MuscleGroup.FOREARMS, Rect(0.02f, 0.39f, 0.20f, 0.55f), "FARM"),
    BodyZone(MuscleGroup.FOREARMS, Rect(0.80f, 0.39f, 0.98f, 0.55f), "FARM"),
    // Quads — left and right thighs
    BodyZone(MuscleGroup.QUADS, Rect(0.22f, 0.50f, 0.48f, 0.74f), "QUAD"),
    BodyZone(MuscleGroup.QUADS, Rect(0.52f, 0.50f, 0.78f, 0.74f), "QUAD"),
)

/**
 * Back body zones (5 muscle groups).
 * Same coordinate system as front zones.
 */
val BACK_ZONES: List<BodyZone> = listOf(
    // Back — upper and middle torso
    BodyZone(MuscleGroup.BACK, Rect(0.22f, 0.10f, 0.78f, 0.35f), "BACK"),
    // Triceps — left and right upper arms
    BodyZone(MuscleGroup.TRICEPS, Rect(0.06f, 0.20f, 0.20f, 0.40f), "TRI"),
    BodyZone(MuscleGroup.TRICEPS, Rect(0.80f, 0.20f, 0.94f, 0.40f), "TRI"),
    // Glutes — hip area
    BodyZone(MuscleGroup.GLUTES, Rect(0.22f, 0.36f, 0.78f, 0.50f), "GLUT"),
    // Hamstrings — left and right back of thighs
    BodyZone(MuscleGroup.HAMSTRINGS, Rect(0.22f, 0.52f, 0.48f, 0.74f), "HAM"),
    BodyZone(MuscleGroup.HAMSTRINGS, Rect(0.52f, 0.52f, 0.78f, 0.74f), "HAM"),
    // Calves — left and right lower legs
    BodyZone(MuscleGroup.CALVES, Rect(0.24f, 0.76f, 0.46f, 0.94f), "CALF"),
    BodyZone(MuscleGroup.CALVES, Rect(0.54f, 0.76f, 0.76f, 0.94f), "CALF"),
)
