package com.devindie.cmptemplate.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsScreenModel
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeSettingsScreen: ObserveSettingsScreenUseCase,
    private val updateSetting: UpdateSettingUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingsScreenUiState> =
        observeSettingsScreen()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SettingsScreenModel(sections = emptyList()),
            )

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun onSettingChanged(key: SettingKey, value: SettingValue) {
        viewModelScope.launch {
            updateSetting(key, value)
                .onFailure { error -> _messages.emit(error.message ?: "Could not save setting") }
        }
    }
}
