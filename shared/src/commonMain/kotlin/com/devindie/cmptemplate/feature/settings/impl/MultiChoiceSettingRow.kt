package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devindie.cmptemplate.domain.model.settings.MultiChoiceSettingsItemModel

@Composable
internal fun MultiChoiceSettingRow(
    item: MultiChoiceSettingsItemModel,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by rememberSaveable(item.key.value) { mutableStateOf(false) }
    var draftSelection by rememberSaveable(item.key.value) { mutableStateOf(item.selectedOptionIds) }

    val summary =
        item.options
            .filter { it.id in item.selectedOptionIds }
            .joinToString { it.label }
            .ifEmpty { "None selected" }

    ListItem(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    draftSelection = item.selectedOptionIds
                    showDialog = true
                },
        headlineContent = { Text(item.title) },
        supportingContent = {
            Text(
                text =
                    buildString {
                        item.description?.let { append("$it\n") }
                        append(summary)
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
                        val checked = option.id in draftSelection
                        ListItem(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        draftSelection =
                                            if (checked) {
                                                draftSelection - option.id
                                            } else {
                                                draftSelection + option.id
                                            }
                                    },
                            headlineContent = { Text(option.label) },
                            leadingContent = {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        draftSelection =
                                            if (isChecked) {
                                                draftSelection + option.id
                                            } else {
                                                draftSelection - option.id
                                            }
                                    },
                                )
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSelectionChanged(draftSelection)
                        showDialog = false
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
