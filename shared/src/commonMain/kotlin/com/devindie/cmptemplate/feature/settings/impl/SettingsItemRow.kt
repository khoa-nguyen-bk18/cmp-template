package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.runtime.Composable
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.DoubleSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.IntSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.LongSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.MultiChoiceSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.TextSettingsItemModel

@Composable
internal fun SettingsItemRow(
    item: SettingsItemModel,
    onValueChange: (SettingKey, SettingValue) -> Unit,
) {
    when (item) {
        is BooleanSettingsItemModel ->
            BooleanSettingRow(
                item = item,
                onCheckedChange = { checked ->
                    onValueChange(item.key, SettingValue.BooleanValue(checked))
                },
            )
        is TextSettingsItemModel ->
            TextSettingRow(
                item = item,
                onValueCommit = { text ->
                    onValueChange(item.key, SettingValue.TextValue(text))
                },
            )
        is IntSettingsItemModel ->
            NumberSettingRow(
                item = item,
                onValueCommit = { value ->
                    onValueChange(item.key, SettingValue.IntValue(value))
                },
            )
        is LongSettingsItemModel ->
            NumberSettingRow(
                item = item,
                onValueCommit = { value ->
                    onValueChange(item.key, SettingValue.LongValue(value))
                },
            )
        is DoubleSettingsItemModel ->
            NumberSettingRow(
                item = item,
                onValueCommit = { value ->
                    onValueChange(item.key, SettingValue.DoubleValue(value))
                },
            )
        is SingleChoiceSettingsItemModel ->
            SingleChoiceSettingRow(
                item = item,
                onOptionSelected = { optionId ->
                    onValueChange(item.key, SettingValue.SingleChoiceValue(optionId))
                },
            )
        is MultiChoiceSettingsItemModel ->
            MultiChoiceSettingRow(
                item = item,
                onSelectionChanged = { optionIds ->
                    onValueChange(item.key, SettingValue.MultiChoiceValue(optionIds))
                },
            )
    }
}
