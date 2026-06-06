package com.devindie.cmptemplate.navigation

import com.devindie.cmptemplate.screens.main.MainDestination
import kotlin.test.Test
import kotlin.test.assertEquals

class MainRouteTest {
    @Test
    fun mainDestinationRoutesAreUnique() {
        val routes = MainDestination.entries.map { it.route }

        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun startDestination_isBrowse() {
        assertEquals(MainRoute.Browse, MainDestination.Start.route)
    }
}
