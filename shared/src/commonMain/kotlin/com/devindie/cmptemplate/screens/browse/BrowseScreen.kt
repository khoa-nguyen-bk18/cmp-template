package com.devindie.cmptemplate.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * State-holder entry for the Stitch "Browse" / Product Listing tab (project 17128375841121903851).
 */
@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    onCardClick: (CollectibleCard) -> Unit = {},
    viewModel: BrowseViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BrowseScreen(
        state = state,
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
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onCardClick: (CollectibleCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    BrowseScreenContent(
        state = state,
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

@Composable
private fun BrowseScreenContent(
    state: BrowseScreenUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onCardClick: (CollectibleCard) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme
    var searchText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.componentGap),
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                onSearchQueryChange(it)
            },
            modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.semantics {
                        contentDescription = "${category.label} filter"
                    },
                )
            }
        }
        when {
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            state.cards.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No cards match your search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.componentGap),
                ) {
                    items(state.cards, key = { it.id }) { card ->
                        BrowseCardRow(
                            card = card,
                            onClick = { onCardClick(card) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BrowseScreenPreview() {
    AppTheme {
        BrowseScreen(
            state = BrowseScreenUiState(
                searchQuery = "",
                cards = listOf(
                    CollectibleCard(
                        id = 1,
                        name = "Charizard ex",
                        setName = "Obsidian Flames",
                        condition = "NM",
                        priceDisplay = "$189.99",
                        quantity = 2,
                        category = BrowseCategory.Pokemon,
                    ),
                ),
                isLoading = false,
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onCardClick = {},
        )
    }
}
