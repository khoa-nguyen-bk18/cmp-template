package com.devindie.cmptemplate.feature.apppromotion.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class AppPromotionActions internal constructor(
    private val client: AppPromotionClient,
    private val scope: CoroutineScope,
) {
    fun requestInAppReview() {
        scope.launch { client.requestInAppReview() }
    }

    fun shareApp() {
        scope.launch { client.shareApp() }
    }
}

@Composable
fun rememberAppPromotionActions(
    client: AppPromotionClient = koinInject(),
): AppPromotionActions {
    val scope = rememberCoroutineScope()
    return remember(client, scope) { AppPromotionActions(client, scope) }
}
