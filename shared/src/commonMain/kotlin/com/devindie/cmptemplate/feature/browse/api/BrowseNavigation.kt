package com.devindie.cmptemplate.feature.browse.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.browse.impl.BrowseScreen

fun NavGraphBuilder.browseDestination(onNavigateToCardDetail: (Long) -> Unit) {
    composable<MainRoute.Browse> {
        BrowseScreen(
            modifier = Modifier.fillMaxSize(),
            onCardClick = { card -> onNavigateToCardDetail(card.id) },
        )
    }
}
