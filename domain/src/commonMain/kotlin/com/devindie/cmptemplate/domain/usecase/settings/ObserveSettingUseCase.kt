package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) {
    operator fun invoke(key: SettingKey): Flow<SettingValue?> {
        val definition = catalog.definition(key)
        if (definition == null) {
            return repository.observeValue(key, SettingValue.BooleanValue(false))
        }
        val kind = definition.defaultValue()
        return repository.observeValue(key, kind).map { stored -> stored ?: kind }
    }
}
