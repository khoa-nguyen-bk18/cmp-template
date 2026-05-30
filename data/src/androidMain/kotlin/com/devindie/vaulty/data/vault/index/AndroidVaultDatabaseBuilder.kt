package com.devindie.vaulty.data.vault.index

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** Android Room builder using app-internal `vault_index.db` path. */
fun getVaultDatabaseBuilder(context: Context): RoomDatabase.Builder<VaultDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(VAULT_DATABASE_NAME)
    return Room.databaseBuilder<VaultDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

private const val VAULT_DATABASE_NAME = "vault_index.db"
