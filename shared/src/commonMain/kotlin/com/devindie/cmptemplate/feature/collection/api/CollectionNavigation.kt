package com.devindie.cmptemplate.feature.collection.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.collection.impl.CollectionScreen

fun EntryProviderScope<NavKey>.collectionEntry(onNavigateToCardDetail: (Long) -> Unit) {
    entry<MainRoute.Collection> {
        CollectionScreen(
            modifier = Modifier.fillMaxSize(),
            onCardClick = { card -> onNavigateToCardDetail(card.id) },
        )
    }
}
