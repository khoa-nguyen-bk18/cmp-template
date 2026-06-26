package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingScreenUiState(
    val pages: List<OnboardingPage> = OnboardingPages.default,
    val currentPageIndex: Int = 0,
    val isStartupComplete: Boolean = false,
)

data class OnboardingPage(val title: String, val body: String, val icon: ImageVector)
