package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsError
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog

class UpdateSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) {
    suspend operator fun invoke(key: SettingKey, value: SettingValue): Result<Unit> =
        runCatching {
            val definition =
                catalog.definition(key)
                    ?: throw SettingsError.UnknownSettingKey
            SettingsValidators.validate(definition, value)
            repository.setValue(key, value)
        }
}
