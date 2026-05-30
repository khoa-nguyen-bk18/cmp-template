package com.devindie.vaulty.domain.model.index

/** Parsed link target kind (wiki `[[...]]`, markdown `[](...)`, embed). */
enum class VaultLinkKind {
    Wiki,
    Markdown,
    Embed,
}

/**
 * Directed link edge from a source file to a target path.
 *
 * @property resolvedTargetFileId Set when the target path matches an indexed file; else link may be broken.
 * @property isBroken `true` when [resolvedTargetFileId] is null for a required target.
 */
data class VaultLink(
    val id: Long,
    val sourceFileId: Long,
    val targetPath: String,
    val resolvedTargetFileId: Long?,
    val linkKind: VaultLinkKind,
    val anchor: String?,
    val label: String?,
    val isBroken: Boolean,
)
