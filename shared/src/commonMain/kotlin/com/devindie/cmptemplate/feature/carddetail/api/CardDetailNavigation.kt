package com.devindie.cmptemplate.feature.carddetail.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.devindie.cmptemplate.feature.carddetail.impl.CardDetailBottomSheet
import kotlinx.serialization.Serializable
@Serializable
internal data class CardDetailRoute(val cardId: Long)

fun NavGraphBuilder.cardDetailDestination(storeName: String, onDismiss: () -> Unit) {
    dialog<CardDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CardDetailRoute>()
        CardDetailBottomSheet(
            cardId = route.cardId,
            storeName = storeName,
            onDismiss = onDismiss,
        )
    }
}

fun NavHostController.navigateToCardDetail(cardId: Long) {
    navigate(CardDetailRoute(cardId = cardId))
}
