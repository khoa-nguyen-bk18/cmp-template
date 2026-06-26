package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.DoubleSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.IntSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.LongSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.MultiChoiceSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsError
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.TextSettingDefinition

internal object SettingsValidators {
    fun validate(definition: SettingDefinition, value: SettingValue) {
        when (definition) {
            is BooleanSettingDefinition -> requireBoolean(value)
            is TextSettingDefinition -> requireText(definition, value)
            is IntSettingDefinition -> requireInt(definition, value)
            is LongSettingDefinition -> requireLong(definition, value)
            is DoubleSettingDefinition -> requireDouble(definition, value)
            is SingleChoiceSettingDefinition -> requireSingleChoice(definition, value)
            is MultiChoiceSettingDefinition -> requireMultiChoice(definition, value)
        }
    }

    private fun requireBoolean(value: SettingValue) {
        if (value !is SettingValue.BooleanValue) throw SettingsError.TypeMismatch
    }

    private fun requireText(definition: TextSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.TextValue) throw SettingsError.TypeMismatch
        definition.maxLength?.let { max ->
            if (value.value.length > max) throw SettingsError.TextTooLong(max)
        }
    }

    private fun requireInt(definition: IntSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.IntValue) throw SettingsError.TypeMismatch
        ensureInRange(value.value, definition.min, definition.max)
    }

    private fun requireLong(definition: LongSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.LongValue) throw SettingsError.TypeMismatch
        ensureInRange(value.value, definition.min, definition.max)
    }

    private fun requireDouble(definition: DoubleSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.DoubleValue) throw SettingsError.TypeMismatch
        ensureInRange(value.value, definition.min, definition.max)
    }

    private fun requireSingleChoice(definition: SingleChoiceSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.SingleChoiceValue) throw SettingsError.TypeMismatch
        if (definition.options.none { it.id == value.optionId }) {
            throw SettingsError.InvalidChoice(value.optionId)
        }
    }

    private fun requireMultiChoice(definition: MultiChoiceSettingDefinition, value: SettingValue) {
        if (value !is SettingValue.MultiChoiceValue) throw SettingsError.TypeMismatch
        val validIds = definition.options.map { it.id }.toSet()
        if (!validIds.containsAll(value.optionIds)) {
            throw SettingsError.InvalidChoice(value.optionIds.joinToString())
        }
    }

    private fun ensureInRange(value: Int, min: Int?, max: Int?) {
        val message =
            when {
                min != null && value < min -> "below min $min"
                max != null && value > max -> "above max $max"
                else -> null
            }
        if (message != null) throw SettingsError.OutOfRange(message)
    }

    private fun ensureInRange(value: Long, min: Long?, max: Long?) {
        val message =
            when {
                min != null && value < min -> "below min $min"
                max != null && value > max -> "above max $max"
                else -> null
            }
        if (message != null) throw SettingsError.OutOfRange(message)
    }

    private fun ensureInRange(value: Double, min: Double?, max: Double?) {
        val message =
            when {
                min != null && value < min -> "below min $min"
                max != null && value > max -> "above max $max"
                else -> null
            }
        if (message != null) throw SettingsError.OutOfRange(message)
    }
}
