package com.devindie.vaulty.data.vault.index

/**
 * Stage-level timings for a single indexing run (debug / performance baseline).
 * Log via [report] when [enabled] is true.
 */
internal class IndexingStageMetrics(private val enabled: Boolean = true) {
    private val stageMillis = mutableMapOf<String, Long>()

    suspend fun <T> measure(stage: String, block: suspend () -> T): T {
        if (!enabled) return block()
        val start = currentEpochMillis()
        return try {
            block()
        } finally {
            stageMillis[stage] = (stageMillis[stage] ?: 0L) + (currentEpochMillis() - start)
        }
    }

    fun report() {
        if (!enabled || stageMillis.isEmpty()) return
        val summary =
            stageMillis.entries
                .sortedByDescending { it.value }
                .joinToString { "${it.key}=${it.value}ms" }
        println("VaultIndex stages: $summary")
    }
}
