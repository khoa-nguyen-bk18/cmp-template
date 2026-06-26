package com.devindie.cmptemplate.feature.apppromotion.api

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun AppPromotionSettingsSection(
    modifier: Modifier = Modifier,
    client: AppPromotionClient = koinInject(),
) {
    val actions = rememberAppPromotionActions(client)
    Text(
        text = "Support",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    ListItem(
        headlineContent = { Text("Rate this app") },
        leadingContent = {
            Icon(Icons.Outlined.StarOutline, contentDescription = null)
        },
        modifier = Modifier.clickable { actions.requestInAppReview() },
    )
    HorizontalDivider()
    ListItem(
        headlineContent = { Text("Share with friends") },
        leadingContent = {
            Icon(Icons.Outlined.Share, contentDescription = null)
        },
        modifier = Modifier.clickable { actions.shareApp() },
    )
}
