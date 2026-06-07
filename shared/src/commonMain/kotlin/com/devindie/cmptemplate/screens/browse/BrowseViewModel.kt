package com.devindie.cmptemplate.screens.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val SEARCH_QUERY_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class BrowseViewModel(
    private val pagerFactory: BrowseCardPagerFactory,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(BrowseCategory.All)

    /** Debounced + distinct query drives the pager; [searchQuery] stays immediate for the text field. */
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
        combine(searchQuery, selectedCategory) { displayQuery, category ->
            BrowseScreenUiState(
                searchQuery = displayQuery,
                selectedCategory = category,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BrowseScreenUiState(),
        )

    val pagedCards: Flow<PagingData<CollectibleCard>> =
        combine(debouncedSearchQuery, selectedCategory) { query, category ->
            BrowseCardsQuery(query = query, category = category)
        }.flatMapLatest { browseQuery ->
            pagerFactory.pages(browseQuery)
        }.cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun onCategorySelected(category: BrowseCategory) {
        selectedCategory.update { category }
    }
}
