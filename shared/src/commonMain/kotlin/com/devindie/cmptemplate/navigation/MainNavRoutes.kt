package com.devindie.cmptemplate.navigation

import androidx.navigation.NavBackStackEntry

object MainNavRoutes {
    const val CardIdArg = "cardId"
    const val CardDetailPattern = "card_detail/{$CardIdArg}"

    fun cardDetailRoute(cardId: Long): String = "card_detail/$cardId"

    fun isCardDetailRoute(route: String?): Boolean = route?.startsWith("card_detail/") == true
}

fun NavBackStackEntry.cardIdArg(): Long =
    requireNotNull(savedStateHandle.get<Long>(MainNavRoutes.CardIdArg)) {
        "Missing ${MainNavRoutes.CardIdArg} navigation argument"
    }
