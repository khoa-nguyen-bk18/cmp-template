package com.devindie.vaulty.data.vault.index.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Audit row for one indexing run (`full` or `incremental` trigger). */
@Entity(
    tableName = "vault_index_run",
    foreignKeys = [
        ForeignKey(
            entity = VaultIndexEntity::class,
            parentColumns = ["storageKey"],
            childColumns = ["vaultStorageKey"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["vaultStorageKey"])],
)
data class VaultIndexRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vaultStorageKey: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long?,
    val filesProcessed: Int,
    val errorsCount: Int,
    val trigger: String,
)
