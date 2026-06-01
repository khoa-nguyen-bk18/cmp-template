package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browse_card")
data class BrowseCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val setName: String,
    val condition: String,
    val priceCents: Long,
    val quantity: Int,
    val category: String,
    @ColumnInfo(defaultValue = "''")
    val gameName: String = "",
    @ColumnInfo(defaultValue = "''")
    val rarityLabel: String = "",
    @ColumnInfo(defaultValue = "''")
    val editionLabel: String = "",
    val imageUrl: String? = null,
    @ColumnInfo(defaultValue = "''")
    val abilitiesText: String = "",
    @ColumnInfo(defaultValue = "''")
    val flavorText: String = "",
    @ColumnInfo(defaultValue = "0")
    val marketPriceCents: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val buylistPriceCents: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val lpPriceCents: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val mpPriceCents: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val hpPriceCents: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val dPriceCents: Long = 0,
)
