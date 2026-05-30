package com.devindie.cmptemplate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacingDefaults,
        LocalPriceDisplayStyle provides PriceDisplayStyle,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

val LocalPriceDisplayStyle = staticCompositionLocalOf { PriceDisplayStyle }

object AppThemeTypography {
    val priceDisplay: TextStyle
        @Composable get() = LocalPriceDisplayStyle.current
}
