package com.devindie.cmptemplate.data.browse

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowseCardDao {
    @Query("SELECT COUNT(*) FROM browse_card")
    suspend fun count(): Int

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
        ORDER BY name ASC
        """,
    )
    fun observeFiltered(
        query: String,
        category: String,
    ): Flow<List<BrowseCardEntity>>
}
