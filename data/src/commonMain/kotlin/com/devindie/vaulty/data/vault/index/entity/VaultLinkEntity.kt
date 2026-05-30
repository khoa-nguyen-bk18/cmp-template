package com.devindie.vaulty.data.vault.index.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Directed link edge; [resolvedTargetFileId] null when target file is missing (broken link). */
@Entity(
    tableName = "vault_link",
    foreignKeys = [
        ForeignKey(
            entity = VaultFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceFileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["sourceFileId"]),
        Index(value = ["resolvedTargetFileId"]),
        Index(value = ["targetPath"]),
    ],
)
data class VaultLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceFileId: Long,
    val targetPath: String,
    val resolvedTargetFileId: Long?,
    val linkKind: String,
    val anchor: String?,
    val label: String?,
)
