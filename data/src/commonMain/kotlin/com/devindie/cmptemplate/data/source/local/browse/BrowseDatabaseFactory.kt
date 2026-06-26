package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher

const val BROWSE_DATABASE_NAME = "browse_cards.db"

fun getBrowseDatabase(
    builder: RoomDatabase.Builder<BrowseDatabase>,
    ioDispatcher: CoroutineDispatcher,
): BrowseDatabase = builder.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(ioDispatcher).build()
