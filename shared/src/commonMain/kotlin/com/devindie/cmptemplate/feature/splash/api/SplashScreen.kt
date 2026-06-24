package com.devindie.cmptemplate.feature.splash.api

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
import com.devindie.cmptemplate.feature.splash.impl.SplashContent
import com.devindie.cmptemplate.feature.splash.impl.SplashPhase
import com.devindie.cmptemplate.feature.splash.impl.SplashScreenUiState
import com.devindie.cmptemplate.feature.splash.impl.SplashViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isStartupComplete) {
        if (state.isStartupComplete) {
            onNavigateToMain()
        }
    }

    SplashScreen(
        state = state,
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Composable
fun SplashScreen(
    state: SplashScreenUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SplashContent(
        state = state,
        onRetryClick = onRetryClick,
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    )
}

@Preview
@Composable
private fun SplashScreenLoadingPreview() {
    AppTheme {
        SplashScreen(state = SplashScreenUiState(), onRetryClick = {})
    }
}

@Preview
@Composable
private fun SplashScreenErrorPreview() {
    AppTheme {
        SplashScreen(
            state =
                SplashScreenUiState(
                    phase = SplashPhase.Error,
                    errorMessage = "Unable to start the app",
                ),
            onRetryClick = {},
        )
    }
}
