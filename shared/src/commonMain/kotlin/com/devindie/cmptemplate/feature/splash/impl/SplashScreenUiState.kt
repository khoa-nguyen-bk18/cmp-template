package com.devindie.cmptemplate.feature.splash.impl

data class SplashScreenUiState(
    val phase: SplashPhase = SplashPhase.Loading,
    val errorMessage: String? = null,
    val postStartupDestination: SplashPostStartupDestination? = null,
)

enum class SplashPhase {
    Loading,
    Error,
}

sealed interface SplashPostStartupDestination {
    data object Main : SplashPostStartupDestination

    data object Onboarding : SplashPostStartupDestination
}
