package com.neogpt.client.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal val Ink = Color(0xFF0D0D0D)
internal val Paper = Color(0xFFFFFFFF)

internal val DarkBackground = Color(0xFF0D0D0D)
internal val DarkSurface = Color(0xFF171717)
internal val DarkSurfaceContainer = Color(0xFF212121)
internal val DarkSurfaceContainerHigh = Color(0xFF2F2F2F)
internal val DarkOnSurface = Color(0xFFECECEC)
internal val DarkOnSurfaceVariant = Color(0xFFB4B4B4)
internal val DarkOutline = Color(0xFF3C3C3C)
internal val DarkOutlineVariant = Color(0xFF262626)

internal val LightBackground = Color(0xFFFFFFFF)
internal val LightSurface = Color(0xFFF9F9F9)
internal val LightSurfaceContainer = Color(0xFFF4F4F4)
internal val LightSurfaceContainerHigh = Color(0xFFEDEDED)
internal val LightOnSurface = Color(0xFF0D0D0D)
internal val LightOnSurfaceVariant = Color(0xFF5D5D5D)
internal val LightOutline = Color(0xFFDCDCDC)
internal val LightOutlineVariant = Color(0xFFEDEDED)

internal val ErrorRed = Color(0xFFE5484D)

/**
 * Tokens the Material scheme does not express but the ChatGPT layout needs.
 */
@Immutable
data class NeoExtendedColors(
    val sidebar: Color,
    val composerField: Color,
    val userBubble: Color,
    val assistantText: Color,
    val codeBackground: Color,
    val divider: Color,
    val chipBorder: Color,
    val avatarBackground: Color,
    val avatarForeground: Color,
    val sendEnabled: Color,
    val sendDisabled: Color,
    val onSend: Color,
    val scrim: Color,
    val composerBackground: Color,
    val composerBorder: Color,
    val suggestionTint: Color,
    val voiceAccent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val placeholder: Color,
    val topBarCircle: Color,
)

internal val DarkExtended = NeoExtendedColors(
    sidebar = DarkSurface,
    composerField = DarkSurfaceContainer,
    userBubble = DarkSurfaceContainerHigh,
    assistantText = DarkOnSurface,
    codeBackground = Color(0xFF1B1B1B),
    divider = DarkOutlineVariant,
    chipBorder = DarkOutline,
    avatarBackground = Color(0xFFECECEC),
    avatarForeground = Color(0xFF0D0D0D),
    sendEnabled = Color(0xFFFFFFFF),
    sendDisabled = Color(0xFF2F2F2F),
    onSend = Color(0xFF0D0D0D),
    scrim = Color(0x99000000),
    composerBackground = Color(0xFF2F2F2F),
    composerBorder = Color(0xFF3C3C3C),
    suggestionTint = DarkOnSurfaceVariant,
    voiceAccent = Color(0xFF3566EF),
    accentSoft = Color(0xFF20304A),
    accentText = Color(0xFF9BC1FF),
    placeholder = Color(0xFF9B9B9B),
    topBarCircle = Color(0xFF2A2A2A),
)

internal val LightExtended = NeoExtendedColors(
    sidebar = LightSurface,
    composerField = LightSurfaceContainer,
    userBubble = LightSurfaceContainerHigh,
    assistantText = LightOnSurface,
    codeBackground = Color(0xFFF6F6F6),
    divider = LightOutlineVariant,
    chipBorder = LightOutline,
    avatarBackground = Color(0xFF0D0D0D),
    avatarForeground = Color(0xFFFFFFFF),
    sendEnabled = Color(0xFF0D0D0D),
    sendDisabled = Color(0xFFEDEDED),
    onSend = Color(0xFFFFFFFF),
    scrim = Color(0x66000000),
    composerBackground = Color(0xFFFFFFFF),
    composerBorder = Color(0xFFEBEBEB),
    suggestionTint = Color(0xFF5C5C5C),
    voiceAccent = Color(0xFF3566EF),
    accentSoft = Color(0xFFE8F4FF),
    accentText = Color(0xFF3B82F1),
    placeholder = Color(0xFF8F8F8F),
    topBarCircle = Color(0xFFFFFFFF),
)
