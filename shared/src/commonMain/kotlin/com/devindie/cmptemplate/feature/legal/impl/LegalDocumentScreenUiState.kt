package com.devindie.cmptemplate.feature.legal.impl

internal data class LegalDocumentScreenUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val reloadToken: Int = 0,
)
