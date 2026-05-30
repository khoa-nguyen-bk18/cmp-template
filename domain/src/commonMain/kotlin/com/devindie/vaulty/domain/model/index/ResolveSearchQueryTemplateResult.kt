package com.devindie.vaulty.domain.model.index

data class ResolveSearchQueryTemplateResult(
    val templateId: String,
    val dsl: String,
    val parseResult: VaultSearchDslParseResult,
)
