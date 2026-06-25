package com.devindie.cmptemplate.feature.main.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(onClick = onNavigateToSettings),
        headlineContent = { Text("Settings") },
        supportingContent = { Text("App preferences and display options") },
    )
}
