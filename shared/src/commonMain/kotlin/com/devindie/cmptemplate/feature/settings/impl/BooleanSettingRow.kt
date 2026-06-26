package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel

@Composable
internal fun BooleanSettingRow(
    item: BooleanSettingsItemModel,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(item.title) },
        supportingContent =
            item.description?.let { description ->
                { Text(description) }
            },
        trailingContent = {
            Switch(
                checked = item.value,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
