package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(private val completeOnboarding: CompleteOnboardingUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingScreenUiState())
    val uiState: StateFlow<OnboardingScreenUiState> = _uiState.asStateFlow()

    fun onPageChanged(pageIndex: Int) {
        _uiState.update { it.copy(currentPageIndex = pageIndex) }
    }

    fun onNextClick() {
        val lastIndex = _uiState.value.pages.lastIndex
        if (_uiState.value.currentPageIndex < lastIndex) {
            _uiState.update { it.copy(currentPageIndex = it.currentPageIndex + 1) }
        }
    }

    fun onGetStartedClick() {
        if (_uiState.value.currentPageIndex != _uiState.value.pages.lastIndex) return
        viewModelScope.launch {
            completeOnboarding()
            _uiState.update { it.copy(isStartupComplete = true) }
        }
    }
}
