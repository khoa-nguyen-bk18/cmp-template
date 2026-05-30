package com.devindie.vaulty.data.vault.index

/** Maximum files indexed per run; larger trees are partially indexed. */
const val VAULT_INDEX_FILE_LIMIT = 10_000

/** Maximum bytes read per file for content indexing. */
const val VAULT_INDEX_MAX_CONTENT_BYTES = 512 * 1024

const val VAULT_DATABASE_SCHEMA_VERSION = 1
