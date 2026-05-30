package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultSearchDslClock
import com.devindie.vaulty.domain.model.index.VaultSearchSort
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ResolveSearchQueryTemplateUseCaseTest {
    private val fixedClock =
        object : VaultSearchDslClock {
            override fun nowEpochMs(): Long = 1_748_995_200_000L

            override fun zoneOffsetMinutes(): Int = 0
        }

    private val useCase =
        ResolveSearchQueryTemplateUseCase(ParseVaultSearchDslUseCase(fixedClock))

    @Test
    fun yesterdayTemplate_resolvesToDslAndQuery() = runTest {
        val result = useCase("yesterday-activity")
        assertNotNull(result)
        assertEquals("modified:yesterday sort:modified_desc", result.dsl)
        assertTrue(result.parseResult.isSupported)
        assertEquals(VaultSearchSort.ModifiedDesc, result.parseResult.query.sort)
    }
}
