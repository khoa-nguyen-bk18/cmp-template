package com.devindie.vaulty.data.vault.index.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Front-matter and parsed properties for filtering and tag counts. */
@Entity(
    tableName = "vault_file_property",
    foreignKeys = [
        ForeignKey(
            entity = VaultFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["fileId"]),
        Index(value = ["namespace", "key", "value"]),
    ],
)
data class VaultFilePropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val namespace: String,
    val key: String,
    val value: String,
)
