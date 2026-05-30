package com.devindie.cmptemplate.data.browse

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
)
