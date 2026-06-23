package com.devindie.cmptemplate.feature.collection.impl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CollectionViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionScreenUiState())
    val uiState: StateFlow<CollectionScreenUiState> = _uiState

}
