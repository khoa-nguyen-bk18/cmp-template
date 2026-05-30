package com.devindie.vaulty.data.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DispatcherProviderTest {
    @Test
    fun testDispatcherProvider_runsWorkOnInjectedScheduler() = runDataTest {
        val provider = testDispatcherProvider(testScheduler)
        var executed = false
        withContext(provider.io) {
            executed = true
        }
        advanceUntilIdle()
        assertTrue(executed)
    }
}
