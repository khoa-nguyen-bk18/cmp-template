package com.devindie.vaulty.data.vault.index

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.devindie.vaulty.data.vault.index.dao.VaultFileGraphDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileQueryDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileSearchDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileStatsDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileWriteDao
import com.devindie.vaulty.data.vault.index.dao.VaultIndexDao
import com.devindie.vaulty.data.vault.index.dao.VaultIndexedFileTransactionDao
import com.devindie.vaulty.data.vault.index.entity.VaultFileContentEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFileFtsEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFilePropertyEntity
import com.devindie.vaulty.data.vault.index.entity.VaultIndexEntity
import com.devindie.vaulty.data.vault.index.entity.VaultIndexRunEntity
import com.devindie.vaulty.data.vault.index.entity.VaultLinkEntity

@Database(
    entities = [
        VaultIndexEntity::class,
        VaultFileEntity::class,
        VaultFileContentEntity::class,
        VaultFilePropertyEntity::class,
        VaultLinkEntity::class,
        VaultIndexRunEntity::class,
        VaultFileFtsEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
/**
 * Room database for vault index metadata, files, FTS, properties, and link graph.
 *
 * Tables: `vault_index`, `vault_file`, `vault_file_content`, `vault_file_property`,
 * `vault_link`, `vault_index_run`, FTS virtual table.
 *
 * @see com.devindie.vaulty.data.vault.index.dao.VaultFileQueryDao
 * @see com.devindie.vaulty.data.vault.index.dao.VaultIndexDao
 */
@ConstructedBy(VaultDatabaseConstructor::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultIndexDao(): VaultIndexDao

    abstract fun vaultFileQueryDao(): VaultFileQueryDao

    abstract fun vaultFileWriteDao(): VaultFileWriteDao

    abstract fun vaultIndexedFileTransactionDao(): VaultIndexedFileTransactionDao

    abstract fun vaultFileGraphDao(): VaultFileGraphDao

    abstract fun vaultFileStatsDao(): VaultFileStatsDao

    abstract fun vaultFileSearchDao(): VaultFileSearchDao
}

@Suppress("KotlinNoActualForExpect")
expect object VaultDatabaseConstructor : RoomDatabaseConstructor<VaultDatabase> {
    override fun initialize(): VaultDatabase
}
