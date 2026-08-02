package com.devindie.cmptemplate.feature.auth.impl.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.feature.auth.api.OtpVerificationRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun OtpVerificationScreen(
    route: OtpVerificationRoute,
    onAuthComplete: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpVerificationViewModel = koinViewModel { parametersOf(route) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.authComplete.collect {
            onAuthComplete()
        }
    }

    OtpVerificationContent(
        state = state,
        onBackClick = onBackClick,
        onDigitsChange = viewModel::onDigitsChange,
        onClearClick = viewModel::onClearClick,
        onResendClick = viewModel::onResendClick,
        modifier = modifier,
    )
}
