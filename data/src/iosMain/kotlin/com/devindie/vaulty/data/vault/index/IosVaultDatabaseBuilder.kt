package com.devindie.vaulty.data.vault.index

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** iOS Room builder using Documents directory `vault_index.db`. */
fun getVaultDatabaseBuilder(): RoomDatabase.Builder<VaultDatabase> {
    val dbFilePath = documentDirectory() + "/$VAULT_DATABASE_NAME"
    return Room.databaseBuilder<VaultDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(documentDirectory?.path)
}

private const val VAULT_DATABASE_NAME = "vault_index.db"
