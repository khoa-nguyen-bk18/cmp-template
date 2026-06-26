package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.domain.usecase.UseCase

class GetSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) : UseCase<SettingKey, SettingValue?> {
    override suspend fun invoke(parameters: SettingKey): SettingValue? {
        val definition = catalog.definition(parameters) ?: return null
        val kind = definition.defaultValue()
        return repository.getValue(parameters, kind) ?: kind
    }
}
