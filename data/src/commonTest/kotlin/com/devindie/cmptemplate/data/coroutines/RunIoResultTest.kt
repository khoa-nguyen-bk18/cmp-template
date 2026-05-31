package com.devindie.cmptemplate.data.coroutines

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RunIoResultTest {
    @Test
    fun runIoResult_returnsSuccessForValue() {
        val result = runIoResult { "ok" }

        assertEquals("ok", result.getOrNull())
    }

    @Test
    fun runIoResult_wrapsOrdinaryExceptionInFailure() {
        val result =
            runIoResult {
                error("boom")
            }

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun runIoResult_rethrowsCancellationException() {
        assertFailsWith<CancellationException> {
            runIoResult {
                throw CancellationException("cancelled")
            }
        }
    }
}
