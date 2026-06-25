package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSettingsRepository : SettingsRepository {
    private val values = MutableStateFlow<Map<SettingKey, SettingValue>>(emptyMap())

    override fun observeValue(key: SettingKey, kind: SettingValue): Flow<SettingValue?> =
        values.map { it[key] }

    override suspend fun getValue(key: SettingKey, kind: SettingValue): SettingValue? = values.value[key]

    override suspend fun setValue(key: SettingKey, value: SettingValue) {
        values.update { current -> current + (key to value) }
    }
}
