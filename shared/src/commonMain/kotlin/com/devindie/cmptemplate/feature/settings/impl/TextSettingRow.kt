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
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.domain.model.settings.TextSettingsItemModel

@Composable
internal fun TextSettingRow(
    item: TextSettingsItemModel,
    onValueCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable(item.key.value) { mutableStateOf(item.value) }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = item.title, style = MaterialTheme.typography.titleSmall)
        item.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (text != item.value) {
                            onValueCommit(text)
                        }
                    },
                ),
        )
    }
}
