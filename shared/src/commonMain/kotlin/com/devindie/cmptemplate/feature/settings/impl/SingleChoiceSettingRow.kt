package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingsItemModel

@Composable
internal fun SingleChoiceSettingRow(
    item: SingleChoiceSettingsItemModel,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by rememberSaveable(item.key.value) { mutableStateOf(false) }
    val selectedLabel =
        item.options.firstOrNull { it.id == item.selectedOptionId }?.label
            ?: item.selectedOptionId

    ListItem(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
        headlineContent = { Text(item.title) },
        supportingContent = {
            Text(
                text =
                    buildString {
                        item.description?.let { append("$it\n") }
                        append(selectedLabel)
                    },
            )
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(item.title) },
            text = {
                LazyColumn {
                    items(item.options, key = { it.id }) { option ->
                        ListItem(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelected(option.id)
                                        showDialog = false
                                    },
                            headlineContent = { Text(option.label) },
                            leadingContent = {
                                RadioButton(
                                    selected = option.id == item.selectedOptionId,
                                    onClick = {
                                        onOptionSelected(option.id)
                                        showDialog = false
                                    },
                                )
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            },
        )
    }
}
