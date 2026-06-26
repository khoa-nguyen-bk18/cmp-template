package com.devindie.cmptemplate.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override fun observeValue(key: SettingKey, kind: SettingValue): Flow<SettingValue?> =
        dataStore.data.map { preferences -> readValue(preferences, key, kind) }

    override suspend fun getValue(key: SettingKey, kind: SettingValue): SettingValue? =
        readValue(dataStore.data.first(), key, kind)

    override suspend fun setValue(key: SettingKey, value: SettingValue) {
        dataStore.edit { preferences ->
            when (value) {
                is SettingValue.BooleanValue -> preferences[booleanKey(key)] = value.value
                is SettingValue.TextValue -> preferences[stringKey(key)] = value.value
                is SettingValue.SingleChoiceValue -> preferences[stringKey(key)] = value.optionId
                is SettingValue.MultiChoiceValue -> preferences[stringSetKey(key)] = value.optionIds
                is SettingValue.IntValue -> preferences[intKey(key)] = value.value
                is SettingValue.LongValue -> preferences[longKey(key)] = value.value
                is SettingValue.DoubleValue -> preferences[doubleKey(key)] = value.value
            }
        }
    }
}
