package com.devindie.cmptemplate.data.local.browse

import androidx.room.Room
import androidx.room.RoomDatabase
import com.devindie.cmptemplate.data.source.local.browse.BROWSE_DATABASE_NAME
import com.devindie.cmptemplate.data.source.local.browse.BrowseDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun getBrowseDatabaseBuilder(): RoomDatabase.Builder<BrowseDatabase> {
    val dbFilePath = "${browseDocumentDirectory()}/$BROWSE_DATABASE_NAME"
    return Room.databaseBuilder(name = dbFilePath)
}

@OptIn(ExperimentalForeignApi::class)
private fun browseDocumentDirectory(): String {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(documentDirectory?.path)
}
