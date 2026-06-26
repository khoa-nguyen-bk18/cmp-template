package com.devindie.cmptemplate.domain.usecase.user

import com.devindie.cmptemplate.domain.fake.FakeUserRepository
import com.devindie.cmptemplate.domain.model.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClearUserSessionUseCaseTest {
    @Test
    fun invoke_clearsSessionViaRepository() = runTest {
        val repository =
            FakeUserRepository().apply {
                session = UserSession(accessToken = "access", refreshToken = "refresh")
            }
        val useCase = ClearUserSessionUseCase(repository)

        useCase()

        assertEquals(1, repository.clearSessionCallCount)
        assertNull(repository.session)
    }
}
