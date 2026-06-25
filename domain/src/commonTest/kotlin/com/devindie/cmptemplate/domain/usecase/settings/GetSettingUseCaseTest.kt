package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetSettingUseCaseTest {
    @Test
    fun invoke_returnsStoredValueWhenPresent() = runTest {
        val key = SettingKey("general.notifications")
        val repository = FakeSettingsRepository()
        repository.setValue(key, SettingValue.BooleanValue(false))
        val useCase = GetSettingUseCase(repository, FakeSettingsCatalog())

        assertEquals(SettingValue.BooleanValue(false), useCase(key))
    }

    @Test
    fun invoke_returnsCatalogDefaultWhenMissing() = runTest {
        val key = SettingKey("general.notifications")
        val useCase = GetSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

        assertEquals(SettingValue.BooleanValue(true), useCase(key))
    }

    @Test
    fun invoke_returnsNullForUnknownKey() = runTest {
        val useCase = GetSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

        assertNull(useCase(SettingKey("unknown.key")))
    }
}
