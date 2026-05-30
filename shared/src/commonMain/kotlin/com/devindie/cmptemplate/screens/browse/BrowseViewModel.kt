package com.devindie.cmptemplate.screens.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.usecase.browse.EnsureBrowseCatalogSeededUseCase
import com.devindie.cmptemplate.domain.usecase.browse.ObserveBrowseCardsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModel(
    private val observeBrowseCards: ObserveBrowseCardsUseCase,
    private val ensureBrowseCatalogSeeded: EnsureBrowseCatalogSeededUseCase,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(BrowseCategory.All)
    private val catalogReady = MutableStateFlow(false)

    val uiState: StateFlow<BrowseScreenUiState> =
        combine(searchQuery, selectedCategory, catalogReady) { query, category, ready ->
            Triple(query, category, ready)
        }.flatMapLatest { (query, category, ready) ->
            if (!ready) {
                flowOf(
                    BrowseScreenUiState(
                        searchQuery = query,
                        selectedCategory = category,
                        isLoading = true,
                    ),
                )
            } else {
                flow {
                    observeBrowseCards(
                        BrowseCardsQuery(query = query, category = category),
                    ).collect { cards ->
                        emit(
                            BrowseScreenUiState(
                                searchQuery = query,
                                selectedCategory = category,
                                cards = cards,
                                isLoading = false,
                            ),
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BrowseScreenUiState(),
        )

    init {
        viewModelScope.launch {
            ensureBrowseCatalogSeeded()
            catalogReady.update { true }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun onCategorySelected(category: BrowseCategory) {
        selectedCategory.update { category }
    }
}
