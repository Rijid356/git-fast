package com.gitfast.app.ui.theme

import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GitFastDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkBackground,
    primaryContainer = NeonGreenDim,
    onPrimaryContainer = OnDarkBackground,
    secondary = CyanAccent,
    onSecondary = DarkBackground,
    secondaryContainer = CyanAccent.copy(alpha = 0.2f),
    onSecondaryContainer = CyanAccent,
    tertiary = AmberAccent,
    onTertiary = DarkBackground,
    tertiaryContainer = AmberAccent.copy(alpha = 0.2f),
    onTertiaryContainer = AmberAccent,
    error = ErrorRed,
    onError = DarkBackground,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurface.copy(alpha = 0.7f),
    outline = OutlineGray,
    outlineVariant = OutlineGray.copy(alpha = 0.5f),
)

private val GitFastShapes = Shapes(
    extraSmall = RectangleShape,
    small = RectangleShape,
    medium = RectangleShape,
    large = RectangleShape,
    extraLarge = RectangleShape,
)

@Composable
fun GitFastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GitFastDarkColorScheme,
        typography = GitFastTypography,
        shapes = GitFastShapes,
        content = content,
    )
}
