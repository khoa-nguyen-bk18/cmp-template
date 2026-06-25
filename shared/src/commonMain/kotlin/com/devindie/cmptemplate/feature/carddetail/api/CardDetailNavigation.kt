package com.devindie.cmptemplate.feature.carddetail.api

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.devindie.cmptemplate.feature.carddetail.impl.CardDetailBottomSheet
import kotlinx.serialization.Serializable

@Serializable
internal data class CardDetailRoute(
    val cardId: Long,
) : NavKey

fun EntryProviderScope<NavKey>.cardDetailEntry(
    storeName: String,
    onDismiss: () -> Unit,
) {
    entry<CardDetailRoute>(metadata = DialogSceneStrategy.dialog()) { key ->
        CardDetailBottomSheet(
            cardId = key.cardId,
            storeName = storeName,
            onDismiss = onDismiss,
        )
    }
}
