package com.devindie.vaulty.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [UseCase] and [UseCaseNoParams] invoke contracts. */
class UseCaseTest {
    private class EchoUseCase : UseCase<String, String> {
        override suspend fun invoke(parameters: String): String = parameters
    }

    private class GreetingUseCase : UseCaseNoParams<String> {
        override suspend fun invoke(): String = "hello"
    }

    @Test
    fun useCase_withParameters_returnsResult() = runTest {
        val useCase = EchoUseCase()
        assertEquals("vaulty", useCase("vaulty"))
    }

    @Test
    fun useCaseNoParams_returnsResult() = runTest {
        val useCase = GreetingUseCase()
        assertEquals("hello", useCase())
    }
}
