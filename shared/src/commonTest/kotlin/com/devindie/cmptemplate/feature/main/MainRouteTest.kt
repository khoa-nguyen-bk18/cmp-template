package com.devindie.cmptemplate.feature.main

import com.devindie.cmptemplate.core.navigation.MainRoute
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
