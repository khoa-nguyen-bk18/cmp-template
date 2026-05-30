package com.devindie.cmptemplate.screens.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.usecase.browse.EnsureBrowseCatalogSeededUseCase
import com.devindie.cmptemplate.domain.usecase.browse.ObserveBrowseCardsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_QUERY_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class BrowseViewModel(
    private val observeBrowseCards: ObserveBrowseCardsUseCase,
    private val ensureBrowseCatalogSeeded: EnsureBrowseCatalogSeededUseCase,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(BrowseCategory.All)
    private val catalogReady = MutableStateFlow(false)

    /** Debounced + distinct query drives Room; [searchQuery] stays immediate for the text field. */
    private val debouncedSearchQuery =
        searchQuery
            .debounce(SEARCH_QUERY_DEBOUNCE_MS)
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = "",
            )

    val uiState: StateFlow<BrowseScreenUiState> =
        combine(
            searchQuery,
            debouncedSearchQuery,
            selectedCategory,
            catalogReady,
        ) { displayQuery, queryForSearch, category, ready ->
            SearchInputs(
                displayQuery = displayQuery,
                queryForSearch = queryForSearch,
                category = category,
                catalogReady = ready,
            )
        }.flatMapLatest { inputs ->
            if (!inputs.catalogReady) {
                flowOf(
                    BrowseScreenUiState(
                        searchQuery = inputs.displayQuery,
                        selectedCategory = inputs.category,
                        isLoading = true,
                    ),
                )
            } else {
                flow {
                    observeBrowseCards(
                        BrowseCardsQuery(
                            query = inputs.queryForSearch,
                            category = inputs.category,
                        ),
                    ).collect { cards ->
                        emit(
                            BrowseScreenUiState(
                                searchQuery = inputs.displayQuery,
                                selectedCategory = inputs.category,
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

private data class SearchInputs(
    val displayQuery: String,
    val queryForSearch: String,
    val category: BrowseCategory,
    val catalogReady: Boolean,
)
