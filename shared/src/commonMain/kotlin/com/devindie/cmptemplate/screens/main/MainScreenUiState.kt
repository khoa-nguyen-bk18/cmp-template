package com.devindie.cmptemplate.screens.main

/** UI contract for [MainScreen] — Stitch "Empty Nav Screen". */
data class MainScreenUiState(
    val storeName: String = DEFAULT_STORE_NAME,
    val selectedDestination: MainDestination = MainDestination.Start,
    val detailCardId: Long? = null,
) {
    /** Card detail sheet is browse-scoped and only visible on the browse tab. */
    val visibleDetailCardId: Long?
        get() = detailCardId?.takeIf { selectedDestination == MainDestination.Browse }
}

const val DEFAULT_STORE_NAME = "Good Games Belconnen"
