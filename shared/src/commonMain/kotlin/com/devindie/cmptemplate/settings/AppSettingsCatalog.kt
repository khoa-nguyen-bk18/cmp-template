package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.model.settings.SettingsSection
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.feature.browse.api.BrowseSettings

class AppSettingsCatalog : SettingsCatalog {
    override val sections =
        listOf(
            SettingsSection(
                id = "appearance",
                title = "Appearance",
                definitions = AppSettings.appearanceDefinitions(),
            ),
            SettingsSection(
                id = "browse",
                title = "Browse",
                definitions = BrowseSettings.definitions(),
            ),
        )
}
