package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.SearchResultRow
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity
import com.devindie.vaulty.data.vault.index.entity.VaultLinkEntity
import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.model.index.VaultLink
import com.devindie.vaulty.domain.model.index.VaultLinkKind
import com.devindie.vaulty.domain.model.index.VaultSearchResult

internal fun VaultFileEntity.toRef(): VaultFileRef = VaultFileRef(
    id = id,
    relativePath = relativePath,
    name = name,
    extension = extension,
    isMarkdown = isMarkdown,
)

internal fun VaultLinkEntity.toDomain(): VaultLink = VaultLink(
    id = id,
    sourceFileId = sourceFileId,
    targetPath = targetPath,
    resolvedTargetFileId = resolvedTargetFileId,
    linkKind = linkKind.toLinkKind(),
    anchor = anchor,
    label = label,
    isBroken = resolvedTargetFileId == null,
)

internal fun SearchResultRow.toDomain(): VaultSearchResult = VaultSearchResult(
    file =
    VaultFileRef(
        id = id,
        relativePath = relativePath,
        name = name,
        extension = extension,
        isMarkdown = extension == "md" || extension == "markdown",
    ),
    snippet = snippet,
    contentBodyExcerpt = contentBodyExcerpt,
    rank = rankScore,
    modifiedAtEpochMs = modifiedAtEpochMs,
    mimeCategory = mimeCategory,
    sizeBytes = sizeBytes,
)

internal fun String.toIndexStatus(): VaultIndexStatus = when (this) {
    "indexing" -> VaultIndexStatus.Indexing
    "ready" -> VaultIndexStatus.Ready
    "failed" -> VaultIndexStatus.Failed
    else -> VaultIndexStatus.NotIndexed
}

private fun String.toLinkKind(): VaultLinkKind = when (this) {
    "wiki" -> VaultLinkKind.Wiki
    "markdown" -> VaultLinkKind.Markdown
    "embed" -> VaultLinkKind.Embed
    else -> VaultLinkKind.Markdown
}
