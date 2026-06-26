package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingOption
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingDefinition

object AppSettings {
    val ShowCardImages = SettingKey("appearance.show_card_images")
    val Theme = SettingKey("appearance.theme")

    fun appearanceDefinitions(): List<SettingDefinition> =
        listOf(
            BooleanSettingDefinition(
                key = ShowCardImages,
                title = "Show card images",
                description = "Display images on card rows",
                default = true,
            ),
            SingleChoiceSettingDefinition(
                key = Theme,
                title = "Theme",
                description = "App color theme",
                options =
                    listOf(
                        SettingOption("system", "System"),
                        SettingOption("light", "Light"),
                        SettingOption("dark", "Dark"),
                    ),
                defaultOptionId = "system",
            ),
        )
}
