package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.first

private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

class OnboardingRepositoryImpl(private val dataStore: DataStore<Preferences>) : OnboardingRepository {
    override suspend fun hasCompleted(): Boolean = dataStore.data.first()[ONBOARDING_COMPLETED_KEY] ?: false

    override suspend fun markCompleted() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }
}
