package com.devindie.cmptemplate.data.local.browse

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devindie.cmptemplate.data.source.local.browse.BROWSE_DATABASE_NAME
import com.devindie.cmptemplate.data.source.local.browse.BrowseDatabase

fun getBrowseDatabaseBuilder(context: Context): RoomDatabase.Builder<BrowseDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(BROWSE_DATABASE_NAME)
    return Room.databaseBuilder(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
