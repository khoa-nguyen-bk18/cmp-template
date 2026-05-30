package com.devindie.vaulty.data.vault.index

/** Tunable parallelism for vault indexing (read/parse vs DB writes). */
internal object IndexingConcurrency {
    const val FILE_PARALLELISM = 4
    const val WRITE_PARALLELISM = 2
    const val STALE_DELETE_CHUNK_SIZE = 100
}
