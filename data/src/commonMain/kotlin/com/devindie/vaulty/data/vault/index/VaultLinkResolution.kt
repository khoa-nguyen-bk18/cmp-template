package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.entity.VaultLinkEntity

internal fun linkEntitiesFromPending(
    pending: List<PendingVaultLink>,
    pathToId: Map<String, Long>,
): List<VaultLinkEntity> = pending.map { item ->
    val resolvedPath =
        VaultPathResolver.resolveLinkTarget(item.sourceRelativePath, item.link.targetPath)
    val targetId =
        VaultPathResolver.withMarkdownExtension(resolvedPath)
            .firstNotNullOfOrNull { candidate ->
                pathToId[VaultPathResolver.normalizeRelativePath(candidate)]
            }
    VaultLinkEntity(
        sourceFileId = item.sourceFileId,
        targetPath = resolvedPath,
        resolvedTargetFileId = targetId,
        linkKind = item.link.linkKind,
        anchor = item.link.anchor,
        label = item.link.label,
    )
}

internal fun linkEntitiesForSourceRow(
    file: com.devindie.vaulty.data.vault.index.dao.VaultFileLinkSourceRow,
    pathToId: Map<String, Long>,
): List<VaultLinkEntity> {
    if (!file.isMarkdown && file.contentBody.isBlank()) return emptyList()
    val parsed = MarkdownLinkExtractor.parse(file.contentBody, file.isMarkdown)
    return parsed.links.map { link ->
        val resolvedPath =
            VaultPathResolver.resolveLinkTarget(file.relativePath, link.targetPath)
        val targetId =
            VaultPathResolver.withMarkdownExtension(resolvedPath)
                .firstNotNullOfOrNull { candidate ->
                    pathToId[VaultPathResolver.normalizeRelativePath(candidate)]
                }
        VaultLinkEntity(
            sourceFileId = file.id,
            targetPath = resolvedPath,
            resolvedTargetFileId = targetId,
            linkKind = link.linkKind,
            anchor = link.anchor,
            label = link.label,
        )
    }
}
