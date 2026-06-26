package com.devindie.cmptemplate.feature.splash.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.usecase.onboarding.HasCompletedOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val initializeApp: InitializeAppUseCase,
    private val hasCompletedOnboarding: HasCompletedOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashScreenUiState())
    val uiState: StateFlow<SplashScreenUiState> = _uiState.asStateFlow()

    init {
        runStartup()
    }

    fun onRetryClick() {
        runStartup()
    }

    private fun runStartup() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = SplashPhase.Loading,
                    errorMessage = null,
                    postStartupDestination = null,
                )
            }
            val result =
                coroutineScope {
                    val initDeferred = async { initializeApp() }
                    val minDisplayDeferred = async { delay(MIN_DISPLAY_MS) }
                    minDisplayDeferred.await()
                    initDeferred.await()
                }
            result
                .onSuccess {
                    val destination =
                        if (hasCompletedOnboarding()) {
                            SplashPostStartupDestination.Main
                        } else {
                            SplashPostStartupDestination.Onboarding
                        }
                    _uiState.update { it.copy(postStartupDestination = destination) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = SplashPhase.Error,
                            errorMessage = error.message ?: "Unable to start the app",
                        )
                    }
                }
        }
    }

    companion object {
        const val MIN_DISPLAY_MS: Long = 1_000L
    }
}
