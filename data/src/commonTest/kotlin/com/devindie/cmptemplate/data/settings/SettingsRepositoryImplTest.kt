package com.devindie.cmptemplate.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsRepositoryImplTest {
    @Test
    fun roundTrip_booleanValue() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))
        val key = SettingKey("general.flag")
        val kind = SettingValue.BooleanValue(false)

        repository.setValue(key, SettingValue.BooleanValue(true))

        assertEquals(SettingValue.BooleanValue(true), repository.getValue(key, kind))
        assertEquals(SettingValue.BooleanValue(true), repository.observeValue(key, kind).first())
    }

    @Test
    fun getValue_returnsNullWhenUnset() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))
        val kind = SettingValue.BooleanValue(false)

        assertNull(repository.getValue(SettingKey("missing"), kind))
    }

    @Test
    fun roundTrip_multiChoiceValue() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))
        val key = SettingKey("general.tags")
        val value = SettingValue.MultiChoiceValue(setOf("a", "b"))
        val kind = SettingValue.MultiChoiceValue(emptySet())

        repository.setValue(key, value)

        assertEquals(value, repository.getValue(key, kind))
    }

    private fun createTestDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                FileSystem.SYSTEM_TEMPORARY_DIRECTORY /
                    "settings_test_${Random.nextLong()}.preferences_pb"
            },
        )
}
