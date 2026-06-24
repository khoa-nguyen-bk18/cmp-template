package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingRepositoryImplTest {
    @Test
    fun hasCompleted_returnsFalseByDefault() = runTest {
        val dataStore = createTestOnboardingDataStore(backgroundScope)
        val repository = OnboardingRepositoryImpl(dataStore)

        assertFalse(repository.hasCompleted())
    }

    @Test
    fun markCompleted_persistsTrue() = runTest {
        val dataStore = createTestOnboardingDataStore(backgroundScope)
        val repository = OnboardingRepositoryImpl(dataStore)

        repository.markCompleted()

        assertTrue(repository.hasCompleted())
    }

    private fun createTestOnboardingDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                FileSystem.SYSTEM_TEMPORARY_DIRECTORY /
                    "onboarding_test_${Random.nextLong()}.preferences_pb"
            },
        )
}
