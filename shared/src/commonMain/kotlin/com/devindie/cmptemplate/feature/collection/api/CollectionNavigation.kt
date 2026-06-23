package com.devindie.cmptemplate.feature.collection.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.browse.impl.BrowseScreen
import com.devindie.cmptemplate.feature.collection.impl.CollectionScreen

fun NavGraphBuilder.collectionDestination(onNavigateToCardDetail: (Long) -> Unit) {
    composable<MainRoute.Collection> {
        CollectionScreen(
            modifier = Modifier.fillMaxSize(),
            onCardClick = { card -> onNavigateToCardDetail(card.id) },
        )
    }
}
