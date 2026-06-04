package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BrowseRemoteKeyDao {
    @Query("SELECT * FROM remote_keys WHERE `key` = :key LIMIT 1")
    suspend fun getRemoteKey(key: String): BrowseRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: BrowseRemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAll()
}
