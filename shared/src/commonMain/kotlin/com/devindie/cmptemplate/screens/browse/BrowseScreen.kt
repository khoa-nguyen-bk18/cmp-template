package com.devindie.cmptemplate.screens.browse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.ui.theme.AppSpacing
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
internal fun BrowseScreen(
    modifier: Modifier = Modifier,
    onCardClick: (CollectibleCard) -> Unit = {},
    viewModel: BrowseViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedCards = viewModel.pagedCards.collectAsLazyPagingItems()

    BrowseScreen(
        state = state,
        pagedCards = pagedCards,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onCardClick = onCardClick,
        modifier = modifier,
    )
}

/**
 * Previewable UI for the browse inventory list — no ViewModel or DI.
 */
@Composable
fun BrowseScreen(
    state: BrowseScreenUiState,
    pagedCards: LazyPagingItems<CollectibleCard>,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onCardClick: (CollectibleCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    BrowseScreenContent(
        state = state,
        pagedCards = pagedCards,
        onSearchQueryChange = onSearchQueryChange,
        onCategorySelected = onCategorySelected,
        onCardClick = onCardClick,
        modifier = modifier.fillMaxSize().testTag("browse_screen"),
        contentPadding = PaddingValues(
            start = spacing.screenMargin,
            end = spacing.screenMargin,
            top = spacing.spaceSm,
            bottom = spacing.spaceMd,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseScreenContent(
    state: BrowseScreenUiState,
    pagedCards: LazyPagingItems<CollectibleCard>,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onCardClick: (CollectibleCard) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme
    var searchText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    val isRefreshing =
        pagedCards.loadState.refresh is LoadState.Loading && pagedCards.itemCount > 0
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex >= SCROLL_TO_TOP_FIRST_VISIBLE_INDEX_THRESHOLD
        }
    }

    Box(modifier = modifier) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize().testTag("browse_pull_refresh"),
            state = pullState,
            isRefreshing = isRefreshing,
            onRefresh = { pagedCards.refresh() },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(spacing.componentGap),
            ) {
                item(key = "search") {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            onSearchQueryChange(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                            .testTag("browse_search_field")
                            .semantics { contentDescription = "Search inventory" },
                        placeholder = { Text("Search cards…") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                        ),
                    )
                }
                item(key = "categories") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.spaceSm),
                    ) {
                        items(BrowseCategory.filters, key = { it.name }) { category ->
                            val selected = state.selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { onCategorySelected(category) },
                                label = { Text(category.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.secondary.copy(alpha = 0.35f),
                                    selectedLabelColor = colorScheme.onSurface,
                                ),
                                modifier = Modifier
                                    .testTag("browse_category_${category.name}")
                                    .semantics {
                                        contentDescription = "${category.label} filter"
                                    },
                            )
                        }
                    }
                }
                when (val refreshState = pagedCards.loadState.refresh) {
                    is LoadState.Loading -> {
                        if (pagedCards.itemCount == 0) {
                            item(key = "initial_loading") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 240.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = colorScheme.primary)
                                }
                            }
                        } else {
                            browseCardItems(
                                pagedCards = pagedCards,
                                onCardClick = onCardClick,
                                spacing = spacing,
                                primaryColor = colorScheme.primary,
                            )
                        }
                    }
                    is LoadState.Error -> {
                        item(key = "refresh_error") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 240.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(spacing.spaceSm),
                                ) {
                                    Text(
                                        text = refreshState.error.message ?: "Unable to load catalog",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.error,
                                        textAlign = TextAlign.Center,
                                    )
                                    Button(onClick = { pagedCards.refresh() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                    is LoadState.NotLoading -> {
                        if (pagedCards.itemCount == 0) {
                            item(key = "empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 240.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No cards match your search",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            browseCardItems(
                                pagedCards = pagedCards,
                                onCardClick = onCardClick,
                                spacing = spacing,
                                primaryColor = colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showScrollToTop && pagedCards.itemCount > 0,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(spacing.spaceMd),
            enter = scaleIn(
                animationSpec = tween(durationMillis = 200),
                initialScale = 0f,
                transformOrigin = TransformOrigin(1f, 1f),
            ),
            exit = scaleOut(
                animationSpec = tween(durationMillis = 150),
                targetScale = 0f,
                transformOrigin = TransformOrigin(1f, 1f),
            ),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .testTag("browse_scroll_to_top")
                    .semantics { contentDescription = "Scroll to top" },
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                )
            }
        }
    }
}

private fun LazyListScope.browseCardItems(
    pagedCards: LazyPagingItems<CollectibleCard>,
    onCardClick: (CollectibleCard) -> Unit,
    spacing: AppSpacing,
    primaryColor: Color,
) {
    items(
        count = pagedCards.itemCount,
        key = pagedCards.itemKey { it.id },
    ) { index ->
        pagedCards[index]?.let { card ->
            BrowseCardRow(
                card = card,
                onClick = { onCardClick(card) },
            )
        }
    }
    when (pagedCards.loadState.append) {
        is LoadState.Loading -> {
            item(key = "append_loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(spacing.spaceSm),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
        }
        is LoadState.Error -> {
            item(key = "append_error") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(spacing.spaceSm),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = { pagedCards.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
        else -> Unit
    }
}

@Preview
@Composable
private fun BrowseScreenPreview() {
    val previewCards =
        listOf(
            CollectibleCard(
                id = 1,
                name = "Charizard ex",
                setName = "Obsidian Flames",
                condition = "NM",
                priceDisplay = "$189.99",
                quantity = 2,
                category = BrowseCategory.Pokemon,
            ),
        )
    val lazyPagingItems =
        flowOf(PagingData.from(previewCards)).collectAsLazyPagingItems()

    AppTheme {
        BrowseScreen(
            state = BrowseScreenUiState(),
            pagedCards = lazyPagingItems,
            onSearchQueryChange = {},
            onCategorySelected = {},
            onCardClick = {},
        )
    }
}
