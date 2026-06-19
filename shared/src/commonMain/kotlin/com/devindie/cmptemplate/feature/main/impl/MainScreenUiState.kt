package com.devindie.cmptemplate.feature.main

import com.devindie.cmptemplate.core.constants.DEFAULT_STORE_NAME

/** UI contract for [MainScreen] — Stitch "Empty Nav Screen". */
data class MainScreenUiState(val storeName: String = DEFAULT_STORE_NAME)
