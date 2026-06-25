package com.devindie.cmptemplate.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue

internal fun booleanKey(key: SettingKey) = booleanPreferencesKey(prefName(key))

internal fun stringKey(key: SettingKey) = stringPreferencesKey(prefName(key))

internal fun stringSetKey(key: SettingKey) = stringSetPreferencesKey(prefName(key))

internal fun intKey(key: SettingKey) = intPreferencesKey(prefName(key))

internal fun longKey(key: SettingKey) = longPreferencesKey(prefName(key))

internal fun doubleKey(key: SettingKey) = doublePreferencesKey(prefName(key))

private fun prefName(key: SettingKey): String = "setting_${key.value}"

internal fun readValue(
    preferences: Preferences,
    key: SettingKey,
    kind: SettingValue,
): SettingValue? =
    when (kind) {
        is SettingValue.BooleanValue -> preferences[booleanKey(key)]?.let(SettingValue::BooleanValue)
        is SettingValue.TextValue -> preferences[stringKey(key)]?.let(SettingValue::TextValue)
        is SettingValue.SingleChoiceValue -> preferences[stringKey(key)]?.let(SettingValue::SingleChoiceValue)
        is SettingValue.MultiChoiceValue -> preferences[stringSetKey(key)]?.let(SettingValue::MultiChoiceValue)
        is SettingValue.IntValue -> preferences[intKey(key)]?.let(SettingValue::IntValue)
        is SettingValue.LongValue -> preferences[longKey(key)]?.let(SettingValue::LongValue)
        is SettingValue.DoubleValue -> preferences[doubleKey(key)]?.let(SettingValue::DoubleValue)
    }
