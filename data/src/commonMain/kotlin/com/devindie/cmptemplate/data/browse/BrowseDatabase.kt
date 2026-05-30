package com.devindie.cmptemplate.data.browse

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [BrowseCardEntity::class],
    version = 1,
)
@ConstructedBy(BrowseDatabaseConstructor::class)
abstract class BrowseDatabase : RoomDatabase() {
    abstract fun browseCardDao(): BrowseCardDao
}

@Suppress("KotlinNoActualForExpect")
expect object BrowseDatabaseConstructor : RoomDatabaseConstructor<BrowseDatabase> {
    override fun initialize(): BrowseDatabase
}
