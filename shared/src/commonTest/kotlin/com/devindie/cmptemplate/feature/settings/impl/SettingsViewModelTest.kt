package com.devindie.cmptemplate.feature.settings.impl

import com.devindie.cmptemplate.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SettingsViewModelTest {
    @Test
    fun emitsScreenItemsFromCatalog() = runViewModelTest {
        val viewModel =
            SettingsViewModel(
                observeSettingsScreen = ObserveSettingsScreenUseCase(FakeSettingsRepository(), FakeSettingsCatalog()),
                updateSetting = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog()),
            )
        advanceMainUntilIdle()

        val item = viewModel.uiState.value.sections.single().items.first()
        assertIs<BooleanSettingsItemModel>(item)
        assertEquals(true, item.value)
    }

    @Test
    fun onSettingChanged_updatesValue() = runViewModelTest {
        val repository = FakeSettingsRepository()
        val catalog = FakeSettingsCatalog()
        val viewModel =
            SettingsViewModel(
                observeSettingsScreen = ObserveSettingsScreenUseCase(repository, catalog),
                updateSetting = UpdateSettingUseCase(repository, catalog),
            )
        advanceMainUntilIdle()

        viewModel.onSettingChanged(
            SettingKey("general.notifications"),
            SettingValue.BooleanValue(false),
        )
        advanceMainUntilIdle()

        val item = viewModel.uiState.value.sections.single().items.first() as BooleanSettingsItemModel
        assertEquals(false, item.value)
    }
}
