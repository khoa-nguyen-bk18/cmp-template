package com.devindie.vaulty.data.vault.index.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/** FTS4 shadow of searchable columns on [VaultFileEntity]. */
@Fts4(contentEntity = VaultFileEntity::class)
@Entity(tableName = "vault_file_fts")
data class VaultFileFtsEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "relativePath") val relativePath: String,
    @ColumnInfo(name = "extension") val extension: String,
    @ColumnInfo(name = "propertiesText") val propertiesText: String,
    @ColumnInfo(name = "contentBody") val contentBody: String,
    @ColumnInfo(name = "headings") val headings: String,
)
