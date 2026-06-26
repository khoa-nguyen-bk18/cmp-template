package com.devindie.cmptemplate.feature.onboarding.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingContent
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingScreenUiState
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isStartupComplete) {
        if (state.isStartupComplete) {
            onNavigateToMain()
        }
    }

    OnboardingScreen(
        state = state,
        onPageChanged = viewModel::onPageChanged,
        onNextClick = viewModel::onNextClick,
        onGetStartedClick = viewModel::onGetStartedClick,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingScreenUiState,
    onPageChanged: (Int) -> Unit,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingContent(
        state = state,
        onPageChanged = onPageChanged,
        onNextClick = onNextClick,
        onGetStartedClick = onGetStartedClick,
        modifier =
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            state = OnboardingScreenUiState(),
            onPageChanged = {},
            onNextClick = {},
            onGetStartedClick = {},
        )
    }
}
