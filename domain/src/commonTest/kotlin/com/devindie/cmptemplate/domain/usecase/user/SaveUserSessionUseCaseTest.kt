package com.devindie.cmptemplate.domain.usecase.user

import com.devindie.cmptemplate.domain.fake.FakeUserRepository
import com.devindie.cmptemplate.domain.model.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveUserSessionUseCaseTest {
    @Test
    fun invoke_persistsSessionViaRepository() = runTest {
        val repository = FakeUserRepository()
        val useCase = SaveUserSessionUseCase(repository)
        val session = UserSession(accessToken = "access", refreshToken = "refresh")

        useCase(session)

        assertEquals(1, repository.saveSessionCallCount)
        assertEquals(session, repository.lastSavedSession)
        assertEquals(session, repository.session)
    }
}
