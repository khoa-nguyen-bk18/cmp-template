package com.devindie.cmptemplate.data.local.browse.fake

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardDao
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBrowseCardDao : BrowseCardDao {
    private val cards = MutableStateFlow<List<BrowseCardEntity>>(emptyList())

    fun setCards(value: List<BrowseCardEntity>) {
        cards.value = value
    }

    override suspend fun count(): Int = cards.value.size

    override suspend fun deleteAll() {
        cards.value = emptyList()
    }

    override suspend fun insertAll(cards: List<BrowseCardEntity>) {
        this.cards.update { existing ->
            val byId = existing.associateBy { it.id }.toMutableMap()
            cards.forEach { card ->
                if (card.id == 0L) {
                    val nextId = (byId.keys.maxOrNull() ?: 0L) + 1L
                    byId[nextId] = card.copy(id = nextId)
                } else {
                    byId[card.id] = card
                }
            }
            byId.values.sortedBy { it.name }
        }
    }

    override fun observeFiltered(query: String, category: String): Flow<List<BrowseCardEntity>> =
        cards.map { entities ->
            entities.filter { entity ->
                val categoryMatches = category == "All" || entity.category == category
                val normalizedQuery = query.trim()
                val queryMatches =
                    normalizedQuery.isEmpty() ||
                        entity.name.contains(normalizedQuery, ignoreCase = true) ||
                        entity.setName.contains(normalizedQuery, ignoreCase = true)
                categoryMatches && queryMatches
            }.sortedBy { it.name }
        }

    override suspend fun getById(cardId: Long): BrowseCardEntity? = cards.value.firstOrNull { it.id == cardId }
}
