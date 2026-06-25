package com.devindie.cmptemplate.feature.settings.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.feature.settings.impl.SettingsContent
import com.devindie.cmptemplate.feature.settings.impl.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        state = state,
        messages = viewModel.messages,
        onBack = onBack,
        onSettingChanged = viewModel::onSettingChanged,
        modifier = modifier,
    )
}
