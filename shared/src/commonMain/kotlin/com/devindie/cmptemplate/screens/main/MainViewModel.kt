package com.devindie.cmptemplate.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<MainEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onDestinationSelected(destination: MainDestination) {
        if (destination == _uiState.value.selectedDestination) return

        _uiState.update { it.copy(selectedDestination = destination) }
        viewModelScope.launch {
            _events.send(MainEvent.NavigateToTab(destination))
        }
    }

    /** Syncs shell state when [NavHost] route changes (e.g. restored back stack). */
    fun onRouteChanged(destination: MainDestination) {
        _uiState.update { current ->
            if (current.selectedDestination == destination) return@update current
            current.copy(selectedDestination = destination)
        }
    }

    fun onCartClick() = Unit
}
