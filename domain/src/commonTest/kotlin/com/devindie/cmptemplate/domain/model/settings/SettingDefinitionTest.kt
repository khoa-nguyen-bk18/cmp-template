package com.devindie.cmptemplate.domain.model.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingDefinitionTest {
    @Test
    fun defaultValue_mapsBooleanDefinition() {
        val definition =
            BooleanSettingDefinition(
                key = SettingKey("test.flag"),
                title = "Flag",
                description = null,
                default = true,
            )

        assertEquals(SettingValue.BooleanValue(true), definition.defaultValue())
    }

    @Test
    fun defaultValue_mapsSingleChoiceDefinition() {
        val definition =
            SingleChoiceSettingDefinition(
                key = SettingKey("test.theme"),
                title = "Theme",
                description = null,
                options = listOf(SettingOption("light", "Light"), SettingOption("dark", "Dark")),
                defaultOptionId = "dark",
            )

        assertEquals(SettingValue.SingleChoiceValue("dark"), definition.defaultValue())
    }
}
