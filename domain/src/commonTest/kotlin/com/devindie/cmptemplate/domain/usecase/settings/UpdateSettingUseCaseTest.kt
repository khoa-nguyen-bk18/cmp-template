package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateSettingUseCaseTest {
    @Test
    fun invoke_persistsValidBoolean() = runTest {
        val key = SettingKey("general.notifications")
        val repository = FakeSettingsRepository()
        val useCase = UpdateSettingUseCase(repository, FakeSettingsCatalog())

        val result = useCase(key, SettingValue.BooleanValue(false))

        assertTrue(result.isSuccess)
        assertEquals(SettingValue.BooleanValue(false), repository.getValue(key, SettingValue.BooleanValue(false)))
    }

    @Test
    fun invoke_rejectsUnknownKey() = runTest {
        val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

        val result = useCase(SettingKey("missing"), SettingValue.BooleanValue(true))

        assertTrue(result.isFailure)
        assertIs<SettingsError.UnknownSettingKey>(result.exceptionOrNull())
    }

    @Test
    fun invoke_rejectsTypeMismatch() = runTest {
        val key = SettingKey("general.notifications")
        val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

        val result = useCase(key, SettingValue.TextValue("nope"))

        assertTrue(result.isFailure)
        assertIs<SettingsError.TypeMismatch>(result.exceptionOrNull())
    }

    @Test
    fun invoke_rejectsTextTooLong() = runTest {
        val key = SettingKey("general.nickname")
        val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

        val result = useCase(key, SettingValue.TextValue("a".repeat(21)))

        assertTrue(result.isFailure)
        assertIs<SettingsError.TextTooLong>(result.exceptionOrNull())
    }
}
