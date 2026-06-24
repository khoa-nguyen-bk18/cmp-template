package com.devindie.cmptemplate.feature.splash.impl

data class SplashScreenUiState(
    val phase: SplashPhase = SplashPhase.Loading,
    val errorMessage: String? = null,
    val isStartupComplete: Boolean = false,
)

enum class SplashPhase {
    Loading,
    Error,
}
