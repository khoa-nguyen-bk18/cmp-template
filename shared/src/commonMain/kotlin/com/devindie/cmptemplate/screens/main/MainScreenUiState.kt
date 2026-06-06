package com.devindie.cmptemplate.screens.main

/** UI contract for [MainScreen] — Stitch "Empty Nav Screen". */
data class MainScreenUiState(
    val storeName: String = DEFAULT_STORE_NAME,
)

const val DEFAULT_STORE_NAME = "Good Games Belconnen"
