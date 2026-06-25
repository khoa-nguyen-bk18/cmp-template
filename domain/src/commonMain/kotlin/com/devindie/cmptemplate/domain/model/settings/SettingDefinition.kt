package com.devindie.cmptemplate.domain.model.settings

sealed interface SettingDefinition {
    val key: SettingKey
    val title: String
    val description: String?
}

data class BooleanSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Boolean,
) : SettingDefinition

data class TextSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: String,
    val maxLength: Int? = null,
) : SettingDefinition

data class IntSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Int,
    val min: Int? = null,
    val max: Int? = null,
) : SettingDefinition

data class LongSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Long,
    val min: Long? = null,
    val max: Long? = null,
) : SettingDefinition

data class DoubleSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Double,
    val min: Double? = null,
    val max: Double? = null,
) : SettingDefinition

data class SingleChoiceSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val defaultOptionId: String,
) : SettingDefinition

data class MultiChoiceSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val defaultOptionIds: Set<String>,
) : SettingDefinition

fun SettingDefinition.defaultValue(): SettingValue =
    when (this) {
        is BooleanSettingDefinition -> SettingValue.BooleanValue(default)
        is TextSettingDefinition -> SettingValue.TextValue(default)
        is IntSettingDefinition -> SettingValue.IntValue(default)
        is LongSettingDefinition -> SettingValue.LongValue(default)
        is DoubleSettingDefinition -> SettingValue.DoubleValue(default)
        is SingleChoiceSettingDefinition -> SettingValue.SingleChoiceValue(defaultOptionId)
        is MultiChoiceSettingDefinition -> SettingValue.MultiChoiceValue(defaultOptionIds)
    }
