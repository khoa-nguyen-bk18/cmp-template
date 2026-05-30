package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.entity.VaultFileContentEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFilePropertyEntity
import com.devindie.vaulty.domain.model.VaultFolderSelection

/** Parsed file content ready for a single transactional Room upsert. */
internal data class IndexedFilePayload(
    val file: VaultFileEntity,
    val content: VaultFileContentEntity,
    val properties: List<VaultFilePropertyEntity>,
    val parsedLinks: List<ExtractedLink>,
    val existingFileId: Long?,
)

/** Link extracted during indexing; resolved to [VaultLinkEntity] after all files are indexed. */
internal data class PendingVaultLink(val sourceFileId: Long, val sourceRelativePath: String, val link: ExtractedLink)

internal fun buildIndexedFilePayload(
    selection: VaultFolderSelection,
    entry: VaultFileEntry,
    normalizedPath: String,
    existingFileId: Long?,
    rawContent: String,
): IndexedFilePayload {
    val isMarkdown = isMarkdownExtension(entry.extension)
    val isText = isTextExtension(entry.extension)
    val parsed = MarkdownLinkExtractor.parse(rawContent, isMarkdown)
    val hash =
        contentHashFor(
            relativePath = entry.relativePath,
            sizeBytes = entry.sizeBytes,
            modifiedAtEpochMs = entry.modifiedAtEpochMs,
            contentSample = rawContent.take(512),
        )
    val propertiesText =
        parsed.properties.joinToString(" ") { "${it.key}:${it.value}" }
    val indexedAt = currentEpochMillis()
    val file =
        VaultFileEntity(
            id = existingFileId ?: 0L,
            vaultStorageKey = selection.storageKey,
            relativePath = normalizedPath,
            name = entry.name,
            extension = entry.extension,
            mimeCategory = mimeCategoryFor(entry.extension),
            sizeBytes = entry.sizeBytes,
            modifiedAtEpochMs = entry.modifiedAtEpochMs,
            contentHash = hash,
            indexedAtEpochMs = indexedAt,
            isMarkdown = isMarkdown,
            isText = isText,
            propertiesText = propertiesText,
            contentBody = parsed.body,
            headings = parsed.headings,
        )
    val preview = parsed.body.take(500)
    val content =
        VaultFileContentEntity(
            fileId = existingFileId ?: 0L,
            contentText = parsed.body,
            contentPreview = preview,
            headings = parsed.headings,
        )
    val properties =
        parsed.properties.map { prop ->
            VaultFilePropertyEntity(
                fileId = existingFileId ?: 0L,
                namespace = prop.namespace,
                key = prop.key,
                value = prop.value,
            )
        }
    return IndexedFilePayload(
        file = file,
        content = content,
        properties = properties,
        parsedLinks = parsed.links,
        existingFileId = existingFileId,
    )
}
