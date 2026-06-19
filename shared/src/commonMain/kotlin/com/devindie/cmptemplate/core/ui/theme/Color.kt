package com.devindie.cmptemplate.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand overrides from Stitch (project 17128375841121903851)
internal val BrandPrimary = Color(0xFF121C24)
internal val BrandSecondary = Color(0xFF76A094)
internal val BrandNeutral = Color(0xFF667085)
internal val BrandTertiarySurface = Color(0xFFF2F4F7)

// Light palette — sourced from Stitch namedColors + FIDELITY overrides
internal val LightPrimary = BrandPrimary
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFF131D25)
internal val LightOnPrimaryContainer = Color(0xFF7B858F)
internal val LightSecondary = Color(0xFF3D665B)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFBDE9DB)
internal val LightOnSecondaryContainer = Color(0xFF426A60)
internal val LightTertiary = Color(0xFF191C1E)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFF191C1E)
internal val LightOnTertiaryContainer = Color(0xFF818487)
internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF93000A)
internal val LightBackground = Color(0xFFF9F9FF)
internal val LightOnBackground = Color(0xFF111C2D)
internal val LightSurface = Color(0xFFF9F9FF)
internal val LightOnSurface = Color(0xFF111C2D)
internal val LightSurfaceVariant = Color(0xFFD9E3FB)
internal val LightOnSurfaceVariant = Color(0xFF44474B)
internal val LightOutline = Color(0xFF74777B)
internal val LightOutlineVariant = Color(0xFFC4C7CB)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLow = Color(0xFFF0F3FF)
internal val LightSurfaceContainer = Color(0xFFE8EEFF)
internal val LightSurfaceContainerHigh = Color(0xFFDFE8FF)
internal val LightSurfaceContainerHighest = Color(0xFFD9E3FB)
internal val LightInverseSurface = Color(0xFF273143)
internal val LightInverseOnSurface = Color(0xFFECF0FF)
internal val LightInversePrimary = Color(0xFFBDC8D3)

// Dark palette — derived from Stitch inverse / fixed roles
internal val DarkPrimary = Color(0xFFBDC8D3)
internal val DarkOnPrimary = Color(0xFF131D25)
internal val DarkPrimaryContainer = Color(0xFF3E4851)
internal val DarkOnPrimaryContainer = Color(0xFFD9E4EF)
internal val DarkSecondary = Color(0xFFA4CFC2)
internal val DarkOnSecondary = Color(0xFF00201A)
internal val DarkSecondaryContainer = Color(0xFF254E44)
internal val DarkOnSecondaryContainer = Color(0xFFC0ECDE)
internal val DarkTertiary = Color(0xFFC4C7CA)
internal val DarkOnTertiary = Color(0xFF191C1E)
internal val DarkTertiaryContainer = Color(0xFF44474A)
internal val DarkOnTertiaryContainer = Color(0xFFE0E3E6)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)
internal val DarkBackground = Color(0xFF273143)
internal val DarkOnBackground = Color(0xFFECF0FF)
internal val DarkSurface = Color(0xFF273143)
internal val DarkOnSurface = Color(0xFFECF0FF)
internal val DarkSurfaceVariant = Color(0xFF44474B)
internal val DarkOnSurfaceVariant = Color(0xFFC4C7CB)
internal val DarkOutline = Color(0xFF8E9196)
internal val DarkOutlineVariant = Color(0xFF44474B)
internal val DarkSurfaceContainerLowest = Color(0xFF1A2332)
internal val DarkSurfaceContainerLow = Color(0xFF273143)
internal val DarkSurfaceContainer = Color(0xFF2E3A4D)
internal val DarkSurfaceContainerHigh = Color(0xFF384357)
internal val DarkSurfaceContainerHighest = Color(0xFF434E62)
internal val DarkInverseSurface = Color(0xFFECF0FF)
internal val DarkInverseOnSurface = Color(0xFF273143)
internal val DarkInversePrimary = Color(0xFF556069)

internal val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

internal val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)
