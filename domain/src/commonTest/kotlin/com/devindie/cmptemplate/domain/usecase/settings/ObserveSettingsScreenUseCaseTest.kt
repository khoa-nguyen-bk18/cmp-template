package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveSettingsScreenUseCaseTest {
    @Test
    fun invoke_emitsSectionsWithResolvedValues() = runTest {
        val repository = FakeSettingsRepository()
        repository.setValue(
            SettingKey("general.notifications"),
            SettingValue.BooleanValue(false),
        )
        val useCase = ObserveSettingsScreenUseCase(repository, FakeSettingsCatalog())

        val model = useCase().first()
        val item = model.sections.single().items.first()
        assertIs<BooleanSettingsItemModel>(item)
        assertEquals(false, item.value)
    }
}
