package com.devindie.cmptemplate.feature.main.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionSettingsSection

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "settings") {
            ListItem(
                modifier = Modifier.clickable(onClick = onNavigateToSettings),
                headlineContent = { Text("Settings") },
                supportingContent = { Text("App preferences and display options") },
            )
            HorizontalDivider()
        }
        item(key = "app-promotion") {
            AppPromotionSettingsSection()
        }
    }
}
