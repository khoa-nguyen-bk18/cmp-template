package com.devindie.vaulty.domain.model.index

/** Registry of search query templates (quick actions). */
object SearchQueryTemplates {
    fun all(): List<SearchQueryTemplate> = catalog

    fun quickSearchMvp(): List<SearchQueryTemplate> = catalog.filter { it.supported }.sortedBy { it.priority }

    fun byId(id: String): SearchQueryTemplate? = catalog.firstOrNull { it.id == id }

    private val catalog =
        listOf(
            SearchQueryTemplate(
                id = "yesterday-activity",
                labelKey = "search_template_yesterday",
                category = SearchQueryTemplateCategory.Timeline,
                dsl = "modified:yesterday sort:modified_desc",
                supported = true,
                priority = 1,
            ),
            SearchQueryTemplate(
                id = "unfinished-tasks",
                labelKey = "search_template_unfinished_tasks",
                category = SearchQueryTemplateCategory.Task,
                dsl = "task:open",
                supported = false,
                priority = 2,
            ),
            SearchQueryTemplate(
                id = "recent-meetings",
                labelKey = "search_template_recent_meetings",
                category = SearchQueryTemplateCategory.Meeting,
                dsl = "tag:meeting sort:modified_desc",
                supported = true,
                priority = 3,
            ),
            SearchQueryTemplate(
                id = "recently-modified",
                labelKey = "search_template_recently_modified",
                category = SearchQueryTemplateCategory.Timeline,
                dsl = "sort:modified_desc",
                supported = true,
                priority = 4,
            ),
            SearchQueryTemplate(
                id = "notes-with-attachments",
                labelKey = "search_template_notes_with_attachments",
                category = SearchQueryTemplateCategory.Media,
                dsl = "has:attachment sort:modified_desc",
                supported = true,
                priority = 5,
            ),
            SearchQueryTemplate(
                id = "most-active",
                labelKey = "search_template_most_active",
                category = SearchQueryTemplateCategory.Productivity,
                dsl = "sort:modified_desc",
                supported = true,
                priority = 6,
            ),
            SearchQueryTemplate(
                id = "orphan-notes",
                labelKey = "search_template_orphan_notes",
                category = SearchQueryTemplateCategory.Organization,
                dsl = "backlinks:0",
                supported = true,
                priority = 7,
            ),
            SearchQueryTemplate(
                id = "todays-daily",
                labelKey = "search_template_todays_daily",
                category = SearchQueryTemplateCategory.Daily,
                dsl = "daily:today",
                supported = false,
                priority = 8,
            ),
            SearchQueryTemplate(
                id = "what-changed-this-week",
                labelKey = "search_template_changed_this_week",
                category = SearchQueryTemplateCategory.Timeline,
                dsl = "modified:this_week sort:modified_desc",
                supported = true,
                priority = 20,
            ),
            SearchQueryTemplate(
                id = "notes-with-images",
                labelKey = "search_template_notes_with_images",
                category = SearchQueryTemplateCategory.Media,
                dsl = "has:image sort:modified_desc",
                supported = true,
                priority = 21,
            ),
        )
}
