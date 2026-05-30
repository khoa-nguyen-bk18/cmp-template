package com.devindie.vaulty.domain.model.index

/** User-facing quick search preset mapped to vault search DSL. */
data class SearchQueryTemplate(
    val id: String,
    val labelKey: String,
    val category: SearchQueryTemplateCategory,
    val dsl: String,
    val supported: Boolean,
    val priority: Int,
)

enum class SearchQueryTemplateCategory {
    Timeline,
    Task,
    Meeting,
    Relationship,
    Daily,
    Media,
    Organization,
    Productivity,
}
