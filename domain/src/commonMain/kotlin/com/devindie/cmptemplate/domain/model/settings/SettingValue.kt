package com.devindie.cmptemplate.domain.model.settings

sealed interface SettingValue {
    data class BooleanValue(val value: Boolean) : SettingValue

    data class TextValue(val value: String) : SettingValue

    data class IntValue(val value: Int) : SettingValue

    data class LongValue(val value: Long) : SettingValue

    data class DoubleValue(val value: Double) : SettingValue

    data class SingleChoiceValue(val optionId: String) : SettingValue

    data class MultiChoiceValue(val optionIds: Set<String>) : SettingValue
}
