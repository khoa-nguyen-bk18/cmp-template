package com.devindie.cmptemplate.feature.settings.api

import com.devindie.cmptemplate.feature.settings.impl.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule =
    module {
        viewModelOf(::SettingsViewModel)
    }
