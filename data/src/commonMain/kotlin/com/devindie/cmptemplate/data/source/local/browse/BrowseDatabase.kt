package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [BrowseCardEntity::class, BrowseRemoteKeyEntity::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@ConstructedBy(BrowseDatabaseConstructor::class)
abstract class BrowseDatabase : RoomDatabase() {
    abstract fun browseCardDao(): BrowseCardDao

    abstract fun browseRemoteKeyDao(): BrowseRemoteKeyDao
}

expect object BrowseDatabaseConstructor : RoomDatabaseConstructor<BrowseDatabase> {
    override fun initialize(): BrowseDatabase
}
