package com.devindie.cmptemplate.screens.browse

/** Navigation route for the card detail fullscreen bottom sheet. */
const val CARD_DETAIL_ROUTE = "card_detail/{cardId}"

fun cardDetailRoute(cardId: Long): String = "card_detail/$cardId"
