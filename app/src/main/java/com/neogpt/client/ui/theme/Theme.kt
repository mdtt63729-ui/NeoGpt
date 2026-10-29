package com.neogpt.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.neogpt.client.AppContainer
import com.neogpt.client.domain.model.ThemeMode

private val DarkScheme = darkColorScheme(
    primary = Paper,
    onPrimary = Ink,
    primaryContainer = DarkSurfaceContainerHigh,
    onPrimaryContainer = DarkOnSurface,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceContainer,
    onSecondaryContainer = DarkOnSurface,
    tertiary = DarkOnSurfaceVariant,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkBackground,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkBackground,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkBackground,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorRed,
    onError = Paper,
    scrim = DarkExtended.scrim,
)

private val LightScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = LightSurfaceContainerHigh,
    onPrimaryContainer = LightOnSurface,
    secondary = LightOnSurfaceVariant,
    onSecondary = Paper,
    secondaryContainer = LightSurfaceContainer,
    onSecondaryContainer = LightOnSurface,
    tertiary = LightOnSurfaceVariant,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightBackground,
    inverseSurface = LightOnSurface,
    inverseOnSurface = LightBackground,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorRed,
    onError = Paper,
    scrim = LightExtended.scrim,
)

val LocalNeoColors = staticCompositionLocalOf { DarkExtended }

object NeoTheme {
    val colors: NeoExtendedColors
        @Composable get() = LocalNeoColors.current
}

@Composable
fun NeoGptTheme(
    container: AppContainer,
    content: @Composable () -> Unit,
) {
    val preferences by container.preferences.preferences.collectAsState(
        initial = com.neogpt.client.domain.model.AppPreferences()
    )
    val darkTheme = when (preferences.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalNeoColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NeoTypography,
            shapes = NeoShapes,
            content = content,
        )
    }
}
