package com.neogpt.client.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
