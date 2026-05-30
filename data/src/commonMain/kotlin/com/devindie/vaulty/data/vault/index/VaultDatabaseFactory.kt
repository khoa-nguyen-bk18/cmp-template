package com.devindie.vaulty.data.vault.index

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher

/** Builds [VaultDatabase] with bundled SQLite and injected IO dispatcher for queries. */
fun getVaultDatabase(builder: RoomDatabase.Builder<VaultDatabase>, ioDispatcher: CoroutineDispatcher): VaultDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioDispatcher)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
