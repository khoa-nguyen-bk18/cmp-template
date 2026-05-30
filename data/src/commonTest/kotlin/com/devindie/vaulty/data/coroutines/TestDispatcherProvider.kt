package com.devindie.vaulty.data.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/** [DispatcherProvider] backed by a shared [TestCoroutineScheduler] for deterministic tests. */
fun testDispatcherProvider(scheduler: TestCoroutineScheduler): DispatcherProvider {
    val dispatcher = StandardTestDispatcher(scheduler)
    return object : DispatcherProvider {
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }
}
