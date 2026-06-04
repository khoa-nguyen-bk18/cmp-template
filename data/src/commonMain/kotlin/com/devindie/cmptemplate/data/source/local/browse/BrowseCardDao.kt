package com.devindie.cmptemplate.data.source.local.browse

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowseCardDao {
    @Query("SELECT COUNT(*) FROM browse_card")
    suspend fun count(): Int

    @Query("DELETE FROM browse_card")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<BrowseCardEntity>)

    @Query(
        """
        SELECT * FROM browse_card
        WHERE (:category = 'All' OR category = :category)
        AND (
            :query = ''
            OR LOWER(name) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(setName) LIKE '%' || LOWER(:query) || '%'
        )
        """,
    )
    fun observeFiltered(query: String, category: String): Flow<List<BrowseCardEntity>>

    @Query(
        """
        SELECT * FROM browse_card
        WHERE (:category = 'All' OR category = :category)
        AND (
            :query = ''
            OR LOWER(name) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(setName) LIKE '%' || LOWER(:query) || '%'
        )
        """,
    )
    fun pagingSource(query: String, category: String): PagingSource<Int, BrowseCardEntity>

    @Query("SELECT * FROM browse_card WHERE id = :cardId LIMIT 1")
    suspend fun getById(cardId: Long): BrowseCardEntity?
}
