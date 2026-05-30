package com.devindie.vaulty.data.vault.index.entity

/**
 * Room entities for the vault index. Graph: [VaultFileEntity] 1—1 [VaultFileContentEntity],
 * 1—N [VaultFilePropertyEntity], outbound [VaultLinkEntity] edges; [VaultIndexEntity] per vault;
 * [VaultIndexRunEntity] audit rows; [VaultFileFtsEntity] FTS shadow.
 */

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vault_file",
    indices = [
        Index(value = ["vaultStorageKey"]),
        Index(value = ["vaultStorageKey", "relativePath"], unique = true),
        Index(value = ["contentHash"]),
    ],
)
data class VaultFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vaultStorageKey: String,
    val relativePath: String,
    val name: String,
    val extension: String,
    val mimeCategory: String,
    val sizeBytes: Long,
    val modifiedAtEpochMs: Long,
    val contentHash: String,
    val indexedAtEpochMs: Long,
    val isMarkdown: Boolean,
    val isText: Boolean,
    val propertiesText: String = "",
    val contentBody: String = "",
    val headings: String = "",
)
