package com.devindie.cmptemplate.screens.carddetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.model.carddetail.ConditionPricing
import com.devindie.cmptemplate.screens.main.DEFAULT_STORE_NAME
import com.devindie.cmptemplate.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.AppThemeTypography
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Fullscreen bottom sheet entry for the Stitch "Card Details" screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailBottomSheet(
    cardId: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    storeName: String = DEFAULT_STORE_NAME,
    onCartClick: () -> Unit = {},
    viewModel: CardDetailViewModel = koinViewModel { parametersOf(cardId) },
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(cardId) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RectangleShape,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        CardDetailScreen(
            storeName = storeName,
            onBackClick = onDismiss,
            onCartClick = onCartClick,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * State-holder entry for card detail — collects [CardDetailViewModel] state.
 */
@Composable
fun CardDetailScreen(
    storeName: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CardDetailScreen(
        state = state,
        storeName = storeName,
        onBackClick = onBackClick,
        onCartClick = onCartClick,
        onConditionSelected = viewModel::onConditionSelected,
        onAddToCartClick = viewModel::onAddToCartClick,
        onSellYoursClick = viewModel::onSellYoursClick,
        modifier = modifier,
    )
}

/**
 * Previewable UI for card detail — no ViewModel or DI.
 */
@Composable
fun CardDetailScreen(
    state: CardDetailScreenUiState,
    storeName: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onConditionSelected: (CardCondition) -> Unit,
    onAddToCartClick: () -> Unit,
    onSellYoursClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.testTag("card_detail_screen"),
        containerColor = colorScheme.surface,
        topBar = {
            Surface(color = colorScheme.surface) {
                CardDetailTopBar(
                    storeName = storeName,
                    onBackClick = onBackClick,
                    onCartClick = onCartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .appStatusBarsPadding()
                        .padding(
                            horizontal = spacing.screenMargin,
                            vertical = spacing.spaceSm,
                        ),
                )
            }
        },
        bottomBar = {
            state.card?.let { card ->
                CardDetailPurchaseBar(
                    selectedCondition = state.selectedCondition,
                    totalPriceDisplay = state.selectedPriceDisplay,
                    onSellYoursClick = onSellYoursClick,
                    onAddToCartClick = onAddToCartClick,
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = spacing.screenMargin),
                    )
                }
            }

            state.card != null -> {
                CardDetailContent(
                    card = state.card,
                    selectedCondition = state.selectedCondition,
                    onConditionSelected = onConditionSelected,
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: CardDetail,
    selectedCondition: CardCondition,
    onConditionSelected: (CardCondition) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding)
            .padding(
                horizontal = spacing.screenMargin,
                vertical = spacing.spaceMd,
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.spaceLg),
    ) {
        CardHeroSection(
            card = card,
            modifier = Modifier.fillMaxWidth(),
        )
        CardIdentitySection(
            card = card,
            modifier = Modifier.fillMaxWidth(),
        )
        ConditionSelectorSection(
            pricing = card.conditionPricing,
            selectedCondition = selectedCondition,
            onConditionSelected = onConditionSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        if (card.abilitiesText.isNotBlank()) {
            CardAbilitiesSection(
                abilitiesText = card.abilitiesText,
                flavorText = card.flavorText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        MarketInsightsSection(
            marketPriceDisplay = card.marketPriceDisplay,
            buylistPriceDisplay = card.buylistPriceDisplay,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(spacing.spaceSm))
    }
}

@Composable
private fun CardHeroSection(card: CardDetail, modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(3f / 4.2f)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, colorScheme.outlineVariant),
            shadowElevation = 1.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = card.name.take(1).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = spacing.spaceMd, end = spacing.spaceLg)
                .semantics { contentDescription = "Condition ${card.conditionBadgeLabel}" },
            shape = MaterialTheme.shapes.small,
            color = colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, colorScheme.outlineVariant),
        ) {
            Text(
                text = card.conditionBadgeLabel,
                modifier = Modifier.padding(horizontal = spacing.spaceSm, vertical = spacing.spaceXs),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CardIdentitySection(card: CardDetail, modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.componentGap),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.spaceXs),
        ) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
            )
            Text(
                text = "${card.gameName} / ${card.setName}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(spacing.spaceXs),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = colorScheme.surfaceContainerHighest,
                border = BorderStroke(1.dp, colorScheme.outlineVariant),
            ) {
                Text(
                    text = card.rarityLabel,
                    modifier = Modifier.padding(horizontal = spacing.spaceSm, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurface,
                )
            }
            Text(
                text = card.editionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ConditionSelectorSection(
    pricing: List<ConditionPricing>,
    selectedCondition: CardCondition,
    onConditionSelected: (CardCondition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.spaceSm),
    ) {
        Text(
            text = "SELECT CONDITION",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.spaceSm),
        ) {
            pricing.forEach { tier ->
                val selected = tier.condition == selectedCondition
                val buttonColors =
                    if (selected) {
                        ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface,
                        )
                    }
                val buttonModifier =
                    Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription =
                                "${tier.condition.label}, ${tier.priceDisplay}"
                        }

                if (selected) {
                    Button(
                        onClick = { onConditionSelected(tier.condition) },
                        modifier = buttonModifier,
                        shape = RoundedCornerShape(8.dp),
                        colors = buttonColors,
                        contentPadding = PaddingValues(vertical = spacing.spaceSm),
                    ) {
                        ConditionButtonContent(
                            code = tier.condition.code,
                            priceDisplay = tier.priceDisplay,
                            selected = true,
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onConditionSelected(tier.condition) },
                        modifier = buttonModifier,
                        shape = RoundedCornerShape(8.dp),
                        colors = buttonColors,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant),
                        contentPadding = PaddingValues(vertical = spacing.spaceSm),
                    ) {
                        ConditionButtonContent(
                            code = tier.condition.code,
                            priceDisplay = tier.priceDisplay,
                            selected = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionButtonContent(code: String, priceDisplay: String, selected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
        )
        Text(
            text = priceDisplay,
            style = MaterialTheme.typography.labelSmall,
            color =
            if (selected) {
                colorScheme.onPrimary.copy(alpha = 0.85f)
            } else {
                colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CardAbilitiesSection(abilitiesText: String, flavorText: String, modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.spaceSm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "CARD ABILITIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = abilitiesText,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
            )
            if (flavorText.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                ) {
                    Text(
                        text = flavorText,
                        modifier = Modifier.padding(top = spacing.spaceSm),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketInsightsSection(
    marketPriceDisplay: String,
    buylistPriceDisplay: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.spaceSm),
    ) {
        Text(
            text = "MARKET INSIGHTS",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.spaceSm),
        ) {
            MarketInsightCard(
                label = "Market Price",
                priceDisplay = marketPriceDisplay,
                priceColor = colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            MarketInsightCard(
                label = "Buylist Price",
                priceDisplay = buylistPriceDisplay,
                priceColor = colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MarketInsightCard(
    label: String,
    priceDisplay: String,
    priceColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(spacing.componentGap),
            verticalArrangement = Arrangement.spacedBy(spacing.spaceXs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.outline,
            )
            Text(
                text = priceDisplay,
                style = AppThemeTypography.priceDisplay,
                color = priceColor,
            )
        }
    }
}

@Composable
private fun CardDetailPurchaseBar(
    selectedCondition: CardCondition,
    totalPriceDisplay: String,
    onSellYoursClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .appNavigationBarsPadding()
                .padding(
                    horizontal = spacing.screenMargin,
                    vertical = spacing.spaceMd,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.spaceXs)) {
                Text(
                    text = "Total for ${selectedCondition.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = totalPriceDisplay,
                    style = AppThemeTypography.priceDisplay,
                    color = colorScheme.onSurface,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.spaceSm)) {
                OutlinedButton(
                    onClick = onSellYoursClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                ) {
                    Text(
                        text = "Sell Yours",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Button(
                    onClick = onAddToCartClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(spacing.spaceSm))
                    Text(
                        text = "Add to Cart",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CardDetailScreenPreview() {
    AppTheme {
        CardDetailScreen(
            state = CardDetailScreenUiState(
                isLoading = false,
                card = previewCardDetail(),
                selectedCondition = CardCondition.NearMint,
                selectedPriceDisplay = "$5.62",
            ),
            storeName = DEFAULT_STORE_NAME,
            onBackClick = {},
            onCartClick = {},
            onConditionSelected = {},
            onAddToCartClick = {},
            onSellYoursClick = {},
        )
    }
}

private fun previewCardDetail(): CardDetail = CardDetail(
    id = 1,
    name = "Gaeas Touch",
    gameName = "Magic: The Gathering",
    setName = "The Dark",
    rarityLabel = "Uncommon #77/119",
    editionLabel = "Normal Edition",
    imageUrl = null,
    listingCondition = CardCondition.NearMint,
    conditionBadgeLabel = "Near Mint",
    abilitiesText =
    "You may put one additional land in play during each of your turns, " +
        "but that land must be a basic forest.",
    flavorText =
    "\"The forest provides for those who cherish its roots as much as its leaves.\"",
    conditionPricing =
    listOf(
        ConditionPricing(CardCondition.NearMint, "$5.62"),
        ConditionPricing(CardCondition.LightlyPlayed, "$4.85"),
        ConditionPricing(CardCondition.ModeratelyPlayed, "$3.90"),
        ConditionPricing(CardCondition.HeavilyPlayed, "$2.15"),
        ConditionPricing(CardCondition.Damaged, "$1.05"),
    ),
    marketPriceDisplay = "$5.40",
    buylistPriceDisplay = "$3.25",
)
