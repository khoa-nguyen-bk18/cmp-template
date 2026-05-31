package com.devindie.cmptemplate.screens.main

/** One-shot events consumed by [MainScreenEffects] — navigation stays in the composable layer. */
sealed class MainEvent {
    data class NavigateToTab(val destination: MainDestination) : MainEvent()
}
