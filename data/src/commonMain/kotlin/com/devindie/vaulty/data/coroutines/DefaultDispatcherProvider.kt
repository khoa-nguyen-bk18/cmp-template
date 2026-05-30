package com.devindie.vaulty.data.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** Production [DispatcherProvider] using kotlinx default dispatchers. */
class DefaultDispatcherProvider(
    override val io: CoroutineDispatcher = Dispatchers.IO,
    override val default: CoroutineDispatcher = Dispatchers.Default,
    override val main: CoroutineDispatcher = Dispatchers.Main,
) : DispatcherProvider
