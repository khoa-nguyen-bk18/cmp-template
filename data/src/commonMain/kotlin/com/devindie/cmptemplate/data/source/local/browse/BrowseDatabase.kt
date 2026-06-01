package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [BrowseCardEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@ConstructedBy(BrowseDatabaseConstructor::class)
abstract class BrowseDatabase : RoomDatabase() {
    abstract fun browseCardDao(): BrowseCardDao
}

@Suppress("KotlinNoActualForExpect")
expect object BrowseDatabaseConstructor : RoomDatabaseConstructor<BrowseDatabase> {
    override fun initialize(): BrowseDatabase
}
