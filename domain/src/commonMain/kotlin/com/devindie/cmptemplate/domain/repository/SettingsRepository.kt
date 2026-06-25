package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeValue(key: SettingKey, kind: SettingValue): Flow<SettingValue?>

    suspend fun getValue(key: SettingKey, kind: SettingValue): SettingValue?

    suspend fun setValue(key: SettingKey, value: SettingValue)
}
