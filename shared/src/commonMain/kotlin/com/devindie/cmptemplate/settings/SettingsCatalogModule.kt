package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import org.koin.dsl.module

fun settingsCatalogModule() =
    module {
        single<SettingsCatalog> { AppSettingsCatalog() }
    }
