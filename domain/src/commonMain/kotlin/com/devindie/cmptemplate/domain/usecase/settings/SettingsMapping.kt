package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.DoubleSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.DoubleSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.IntSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.IntSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.LongSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.LongSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.MultiChoiceSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.MultiChoiceSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.TextSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.TextSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.defaultValue

internal fun SettingDefinition.toItemModel(value: SettingValue): SettingsItemModel =
    when (this) {
        is BooleanSettingDefinition -> {
            val resolved = (value as SettingValue.BooleanValue).value
            BooleanSettingsItemModel(key, title, description, resolved)
        }
        is TextSettingDefinition -> {
            val resolved = (value as SettingValue.TextValue).value
            TextSettingsItemModel(key, title, description, resolved, maxLength)
        }
        is IntSettingDefinition -> {
            val resolved = (value as SettingValue.IntValue).value
            IntSettingsItemModel(key, title, description, resolved, min, max)
        }
        is LongSettingDefinition -> {
            val resolved = (value as SettingValue.LongValue).value
            LongSettingsItemModel(key, title, description, resolved, min, max)
        }
        is DoubleSettingDefinition -> {
            val resolved = (value as SettingValue.DoubleValue).value
            DoubleSettingsItemModel(key, title, description, resolved, min, max)
        }
        is SingleChoiceSettingDefinition -> {
            val resolved = (value as SettingValue.SingleChoiceValue).optionId
            SingleChoiceSettingsItemModel(key, title, description, options, resolved)
        }
        is MultiChoiceSettingDefinition -> {
            val resolved = (value as SettingValue.MultiChoiceValue).optionIds
            MultiChoiceSettingsItemModel(key, title, description, options, resolved)
        }
    }
