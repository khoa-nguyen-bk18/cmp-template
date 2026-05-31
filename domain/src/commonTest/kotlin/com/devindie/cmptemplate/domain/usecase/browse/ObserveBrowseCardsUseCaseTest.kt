package com.devindie.cmptemplate.domain.usecase.browse

import com.devindie.cmptemplate.domain.fake.FakeBrowseCardRepository
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveBrowseCardsUseCaseTest {
    @Test
    fun invoke_delegatesQueryAndCategoryToRepository() = runTest {
        val repository = FakeBrowseCardRepository()
        val cards =
            listOf(
                CollectibleCard(
                    id = 1L,
                    name = "Charizard ex",
                    setName = "Obsidian Flames",
                    condition = "NM",
                    priceDisplay = "$189.99",
                    quantity = 2,
                    category = BrowseCategory.Pokemon,
                ),
            )
        repository.setCards(cards)
        val useCase = ObserveBrowseCardsUseCase(repository)

        val result =
            useCase(
                BrowseCardsQuery(
                    query = "char",
                    category = BrowseCategory.Pokemon,
                ),
            ).first()

        assertEquals("char", repository.lastObserveQuery)
        assertEquals(BrowseCategory.Pokemon, repository.lastObserveCategory)
        assertEquals(cards, result)
    }
}
