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

import androidx.annotation.StringRes
import com.example.R

enum class ThemeMode(@StringRes val labelRes: Int) {
    DARK(R.string.theme_dark),
    LIGHT(R.string.theme_light),
    SYSTEM(R.string.theme_system)
}

@Immutable
data class TelemetryExtendedColors(
    val connected: Color,
    val connectedContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val surfaceElevated: Color,
    val chartGrid: Color,
    val isDark: Boolean
)

val LocalTelemetryColors = staticCompositionLocalOf {
    TelemetryExtendedColors(
        connected = EmeraldSecondaryDark,
        connectedContainer = EmeraldContainerDark,
        warning = AmberTertiaryDark,
        warningContainer = AmberContainerDark,
        surfaceElevated = DarkSurfaceElevated,
        chartGrid = DarkOutline,
        isDark = true
    )
}

private val ArduDarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = CyanOnPrimaryDark,
    primaryContainer = CyanContainerDark,
    onPrimaryContainer = CyanPrimaryDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = EmeraldOnSecondaryDark,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldSecondaryDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberTertiaryDark,
    error = CrimsonErrorDark,
    onError = CrimsonOnErrorDark,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = CrimsonErrorDark,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceElevated
)

private val ArduLightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = CyanOnPrimaryLight,
    primaryContainer = CyanContainerLight,
    onPrimaryContainer = CyanPrimaryLight,
    secondary = EmeraldSecondaryLight,
    onSecondary = EmeraldOnSecondaryLight,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = EmeraldSecondaryLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberTertiaryLight,
    error = CrimsonErrorLight,
    onError = CrimsonOnErrorLight,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = CrimsonErrorLight,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    outlineVariant = LightSurfaceElevated
)

val ArduLinkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ArduLinkTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) ArduDarkColorScheme else ArduLightColorScheme
    val extendedColors = if (useDark) {
        TelemetryExtendedColors(
            connected = EmeraldSecondaryDark,
            connectedContainer = EmeraldContainerDark,
            warning = AmberTertiaryDark,
            warningContainer = AmberContainerDark,
            surfaceElevated = DarkSurfaceElevated,
            chartGrid = DarkOutline.copy(alpha = 0.55f),
            isDark = true
        )
    } else {
        TelemetryExtendedColors(
            connected = EmeraldSecondaryLight,
            connectedContainer = EmeraldContainerLight,
            warning = AmberTertiaryLight,
            warningContainer = AmberContainerLight,
            surfaceElevated = LightSurfaceElevated,
            chartGrid = LightOutline.copy(alpha = 0.65f),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalTelemetryColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ArduLinkTypography,
            shapes = ArduLinkShapes,
            content = content
        )
    }
}
