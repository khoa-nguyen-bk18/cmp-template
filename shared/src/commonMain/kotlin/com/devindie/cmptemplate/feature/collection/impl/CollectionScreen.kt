package com.devindie.cmptemplate.feature.collection.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import org.koin.compose.viewmodel.koinViewModel

/** Lazy list items before the total-count header: search field and category chips. */
private const val BROWSE_SCROLLABLE_HEADER_ITEM_COUNT = 2

/** Lazy list item index of the total-count header; card rows start immediately after. */
private const val BROWSE_LIST_HEADER_ITEM_COUNT = 1

/** Show scroll-to-top after this many card rows have scrolled off (headers excluded). */
private const val SCROLL_TO_TOP_AFTER_CARD_ROWS = 2

private const val SCROLL_TO_TOP_FIRST_VISIBLE_INDEX_THRESHOLD =
    BROWSE_SCROLLABLE_HEADER_ITEM_COUNT +
        BROWSE_LIST_HEADER_ITEM_COUNT +
        SCROLL_TO_TOP_AFTER_CARD_ROWS

/**
 * State-holder entry for the Stitch "Browse" / Product Listing tab (project 17128375841121903851).
 */
@Composable
internal fun CollectionScreen(
    modifier: Modifier = Modifier,
    onCardClick: (CollectibleCard) -> Unit = {},
    viewModel: CollectionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectionScreenContent(
        state = state,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionScreenContent(state: CollectionScreenUiState, modifier: Modifier = Modifier) {
    val pullState = rememberPullToRefreshState()
    val isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    Box(modifier) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            state = pullState,
            isRefreshing = isRefreshing,
            onRefresh = { },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "search") {
                    Text(text = "Search")
                }
                item(key = "categories") {
                    Text(text = "Categories")
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
private fun CollectionScreenPreview() {
    CollectionScreenContent(
        CollectionScreenUiState(),
    )
}
