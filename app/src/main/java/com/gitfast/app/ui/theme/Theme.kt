package com.gitfast.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
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
