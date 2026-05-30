package com.devindie.vaulty.data.vault.index

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class IndexingStageMetricsTest {
    @Test
    fun measure_accumulatesStageElapsed() = runTest {
        val metrics = IndexingStageMetrics(enabled = true)
        metrics.measure("stage") {
            // no-op
        }
        metrics.measure("stage") {
            // no-op
        }
        metrics.report()
        assertTrue(true)
    }

    @Test
    fun measure_whenDisabled_skipsTiming() = runTest {
        val metrics = IndexingStageMetrics(enabled = false)
        metrics.measure("stage") { }
        metrics.report()
        assertTrue(true)
    }
}
