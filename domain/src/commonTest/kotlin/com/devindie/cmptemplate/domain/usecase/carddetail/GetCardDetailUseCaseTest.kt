package com.devindie.cmptemplate.domain.usecase.carddetail

import com.devindie.cmptemplate.domain.fake.FakeCardDetailRepository
import com.devindie.cmptemplate.domain.model.carddetail.sampleCardDetail
import com.devindie.cmptemplate.domain.model.carddetail.stubFailure
import com.devindie.cmptemplate.domain.model.carddetail.stubSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCardDetailUseCaseTest {
    @Test
    fun invoke_returnsDetailFromRepository() = runTest {
        val repository = FakeCardDetailRepository()
        val detail = sampleCardDetail(id = 42L)
        repository.stubSuccess(cardId = 42L, detail = detail)
        val useCase = GetCardDetailUseCase(repository)

        val result = useCase(42L)

        assertTrue(result.isSuccess)
        assertEquals(detail, result.getOrNull())
        assertEquals(42L, repository.lastRequestedCardId)
    }

    @Test
    fun invoke_propagatesRepositoryFailure() = runTest {
        val repository = FakeCardDetailRepository()
        repository.stubFailure(message = "Card not found")
        val useCase = GetCardDetailUseCase(repository)

        val result = useCase(99L)

        assertTrue(result.isFailure)
        assertEquals("Card not found", result.exceptionOrNull()?.message)
    }
}
