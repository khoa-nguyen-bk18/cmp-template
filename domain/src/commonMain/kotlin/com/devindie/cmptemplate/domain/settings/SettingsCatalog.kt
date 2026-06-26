package com.devindie.cmptemplate.domain.settings

import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingsSection

interface SettingsCatalog {
    val sections: List<SettingsSection>

    fun definition(key: SettingKey): SettingDefinition? =
        sections
            .asSequence()
            .flatMap { it.definitions }
            .firstOrNull { it.key == key }

    fun allDefinitions(): List<SettingDefinition> = sections.flatMap { it.definitions }
}
