package com.devindie.vaulty.data.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Injectable coroutine dispatchers for the data layer.
 *
 * Production bindings use platform pools; tests substitute [kotlinx.coroutines.test.StandardTestDispatcher].
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
