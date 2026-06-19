package com.devindie.cmptemplate.domain.model.browse

enum class BrowseCategory(val label: String) {
    All(label = "All"),
    Pokemon(label = "Pokémon"),
    Magic(label = "Magic"),
    Sports(label = "Sports"),
    ;

    companion object {
        val filters: List<BrowseCategory> = entries
    }
}
