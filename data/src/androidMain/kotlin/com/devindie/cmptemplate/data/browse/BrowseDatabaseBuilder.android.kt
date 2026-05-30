package com.devindie.cmptemplate.data.browse

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getBrowseDatabaseBuilder(context: Context): RoomDatabase.Builder<BrowseDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(BROWSE_DATABASE_NAME)
    return Room.databaseBuilder(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
