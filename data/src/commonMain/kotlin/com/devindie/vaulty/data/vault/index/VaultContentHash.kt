package com.devindie.vaulty.data.vault.index

private const val HASH_HEX_RADIX = 16

/** Fingerprint stored on [com.devindie.vaulty.data.vault.index.entity.VaultFileEntity] for change detection. */
internal fun contentHashFor(
    relativePath: String,
    sizeBytes: Long,
    modifiedAtEpochMs: Long,
    contentSample: String,
): String {
    val payload = "$relativePath|$sizeBytes|$modifiedAtEpochMs|${contentSample.hashCode()}"
    return payload.hashCode().toUInt().toString(HASH_HEX_RADIX)
}
