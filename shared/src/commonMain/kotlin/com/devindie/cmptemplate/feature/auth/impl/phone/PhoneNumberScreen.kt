package com.devindie.cmptemplate.feature.auth.impl.phone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PhoneNumberScreen(
    onNavigateToOtp: (com.devindie.cmptemplate.feature.auth.api.OtpVerificationRoute) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneNumberViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PhoneNumberEvent.NavigateToOtp -> onNavigateToOtp(event.route)
            }
        }
    }

    PhoneNumberContent(
        state = state,
        onCountryDropdownExpandedChange = viewModel::onCountryDropdownExpandedChange,
        onCountrySelected = viewModel::onCountrySelected,
        onLocalDigitsChange = viewModel::onLocalDigitsChange,
        onNextClick = viewModel::onNextClick,
        onSendErrorShown = viewModel::onSendErrorShown,
        modifier = modifier,
    )
}
