package com.jasongrech.carlocator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// The app commits to one look rather than following system light/dark — same
// call Discord itself makes for most of its own users.
private val CarLocatorColorScheme = darkColorScheme(
    background = Bg,
    surface = Surface,
    surfaceVariant = SurfaceRaised,
    primary = Accent,
    onPrimary = OnAccent,
    secondary = Success,
    onSecondary = TextPrimary,
    error = Danger,
    onError = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Border
)

private val CarLocatorShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CarLocatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarLocatorColorScheme,
        typography = AppTypography,
        shapes = CarLocatorShapes,
        content = content
    )
}
