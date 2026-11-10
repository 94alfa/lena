package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f),
    error = RedErrorLight,
    onError = Color.White
)

val DarkColors = darkColorScheme(
    primary = Teal300,
    onPrimary = Teal900,
    primaryContainer = Teal800,
    onPrimaryContainer = Teal100,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),
    error = RedErrorDark,
    onError = Color.Black
)

@Immutable
data class ExtraColors(
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val success: Color,
    val successContainer: Color,
    val chartPrimary: Color,
    val chartSecondary: Color,
    val chartWarning: Color
)

val LightExtra = ExtraColors(
    warning = AmberWarningLight,
    warningContainer = AmberContainerLight,
    onWarningContainer = Color(0xFF78350F),
    success = GreenSuccessLight,
    successContainer = GreenContainerLight,
    chartPrimary = ChartSeriesPrimaryLight,
    chartSecondary = ChartSeriesSecondary,
    chartWarning = ChartSeriesWarningLight
)

val DarkExtra = ExtraColors(
    warning = AmberWarningDark,
    warningContainer = AmberContainerDark,
    onWarningContainer = Color(0xFFFEF3C7),
    success = GreenSuccessDark,
    successContainer = GreenContainerDark,
    chartPrimary = ChartSeriesPrimaryDark,
    chartSecondary = ChartSeriesSecondary,
    chartWarning = ChartSeriesWarningDark
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun DopaTapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extraColors = if (darkTheme) DarkExtra else LightExtra

    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

// Alias para manter compatibilidade com códigos existentes
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DopaTapTheme(darkTheme = darkTheme, content = content)
}
