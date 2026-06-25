package com.devindie.cmptemplate.domain.model.settings

data class SettingsScreenModel(val sections: List<SettingsSectionModel>)

data class SettingsSectionModel(
    val id: String,
    val title: String,
    val items: List<SettingsItemModel>,
)

sealed interface SettingsItemModel {
    val key: SettingKey
    val title: String
    val description: String?
}

data class BooleanSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Boolean,
) : SettingsItemModel

data class TextSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: String,
    val maxLength: Int?,
) : SettingsItemModel

data class IntSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Int,
    val min: Int?,
    val max: Int?,
) : SettingsItemModel

data class LongSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Long,
    val min: Long?,
    val max: Long?,
) : SettingsItemModel

data class DoubleSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Double,
    val min: Double?,
    val max: Double?,
) : SettingsItemModel

data class SingleChoiceSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val selectedOptionId: String,
) : SettingsItemModel

data class MultiChoiceSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val selectedOptionIds: Set<String>,
) : SettingsItemModel
