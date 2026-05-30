package com.devindie.vaulty.data.vault.index.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Full text and preview for FTS; 1:1 with [VaultFileEntity]. */
@Entity(
    tableName = "vault_file_content",
    foreignKeys = [
        ForeignKey(
            entity = VaultFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["fileId"], unique = true)],
)
data class VaultFileContentEntity(
    @PrimaryKey val fileId: Long,
    val contentText: String,
    val contentPreview: String,
    val headings: String,
)
