package com.devindie.cmptemplate.data.vault.index

import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher

internal fun createInMemoryVaultDatabase(ioDispatcher: CoroutineDispatcher): VaultDatabase = getVaultDatabase(
    builder = Room.inMemoryDatabaseBuilder<VaultDatabase>(),
    ioDispatcher = ioDispatcher,
)
