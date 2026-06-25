package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.domain.model.settings.DoubleSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.IntSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.LongSettingsItemModel

@Composable
internal fun NumberSettingRow(
    item: IntSettingsItemModel,
    onValueCommit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NumberSettingRowContent(
        title = item.title,
        description = item.description,
        value = item.value.toString(),
        keyboardType = KeyboardType.Number,
        onValueCommit = { text ->
            text.toIntOrNull()?.let(onValueCommit)
        },
        modifier = modifier,
        valueKey = item.key.value,
    )
}

@Composable
internal fun NumberSettingRow(
    item: LongSettingsItemModel,
    onValueCommit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    NumberSettingRowContent(
        title = item.title,
        description = item.description,
        value = item.value.toString(),
        keyboardType = KeyboardType.Number,
        onValueCommit = { text ->
            text.toLongOrNull()?.let(onValueCommit)
        },
        modifier = modifier,
        valueKey = item.key.value,
    )
}

@Composable
internal fun NumberSettingRow(
    item: DoubleSettingsItemModel,
    onValueCommit: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    NumberSettingRowContent(
        title = item.title,
        description = item.description,
        value = item.value.toString(),
        keyboardType = KeyboardType.Decimal,
        onValueCommit = { text ->
            text.toDoubleOrNull()?.let(onValueCommit)
        },
        modifier = modifier,
        valueKey = item.key.value,
    )
}

@Composable
private fun NumberSettingRowContent(
    title: String,
    description: String?,
    value: String,
    keyboardType: KeyboardType,
    onValueCommit: (String) -> Unit,
    valueKey: String,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable(valueKey) { mutableStateOf(value) }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onValueCommit(text) },
                ),
        )
    }
}
