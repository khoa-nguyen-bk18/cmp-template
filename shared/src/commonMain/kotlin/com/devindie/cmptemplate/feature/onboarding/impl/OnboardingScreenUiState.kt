package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class OnboardingScreenUiState(
    val pages: ImmutableList<OnboardingPage> = OnboardingPages.default.toImmutableList(),
    val currentPageIndex: Int = 0,
    val isStartupComplete: Boolean = false,
)

data class OnboardingPage(val title: String, val body: String, val icon: ImageVector)
