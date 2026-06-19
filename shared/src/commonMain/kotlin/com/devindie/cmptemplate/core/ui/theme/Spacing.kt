package com.devindie.cmptemplate.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppSpacing(
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceMd: Dp = 16.dp,
    val spaceLg: Dp = 24.dp,
    val componentGap: Dp = 12.dp,
    val screenMargin: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

val AppSpacingDefaults = AppSpacing()
