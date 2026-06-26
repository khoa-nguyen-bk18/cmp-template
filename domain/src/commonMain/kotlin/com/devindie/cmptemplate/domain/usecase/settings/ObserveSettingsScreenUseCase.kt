package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingsScreenModel
import com.devindie.cmptemplate.domain.model.settings.SettingsSectionModel
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveSettingsScreenUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) {
    operator fun invoke(): Flow<SettingsScreenModel> {
        val definitions = catalog.allDefinitions()
        if (definitions.isEmpty()) {
            return flowOf(SettingsScreenModel(sections = emptyList()))
        }
        val flows =
            definitions.map { definition ->
                val kind = definition.defaultValue()
                repository.observeValue(definition.key, kind).map { stored ->
                    definition.key to (stored ?: kind)
                }
            }
        return combine(flows) { entries ->
            val values = entries.toMap()
            SettingsScreenModel(
                sections =
                    catalog.sections.map { section ->
                        SettingsSectionModel(
                            id = section.id,
                            title = section.title,
                            items =
                                section.definitions.map { definition ->
                                    definition.toItemModel(values.getValue(definition.key))
                                },
                        )
                    },
            )
        }
    }
}
