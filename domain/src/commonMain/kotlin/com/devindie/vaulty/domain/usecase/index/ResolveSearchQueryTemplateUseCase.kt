package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.ResolveSearchQueryTemplateResult
import com.devindie.vaulty.domain.model.index.SearchQueryTemplates
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Resolves a template id to DSL and a parsed [VaultSearchDslParseResult].
 */
class ResolveSearchQueryTemplateUseCase(private val parseDsl: ParseVaultSearchDslUseCase) :
    UseCase<String, ResolveSearchQueryTemplateResult?> {
    override suspend fun invoke(parameters: String): ResolveSearchQueryTemplateResult? {
        val template = SearchQueryTemplates.byId(parameters) ?: return null
        val parseResult = parseDsl(template.dsl)
        return ResolveSearchQueryTemplateResult(
            templateId = template.id,
            dsl = template.dsl,
            parseResult = parseResult,
        )
    }
}
