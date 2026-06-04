package com.devindie.cmptemplate.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/** Konsist: Browse paging port/impl placement and ViewModel boundaries. */
class BrowsePagingArchitectureTest {

    @Test
    fun `BrowseCardPagerFactory interface lives in shared screens package`() {
        Konsist.scopeFromProject()
            .interfaces()
            .filter { it.name == "BrowseCardPagerFactory" }
            .assertTrue { it.resideInPackage("com.devindie.cmptemplate.screens.browse..") }
    }

    @Test
    fun `BrowseCardPagerFactoryImpl lives in data browse package`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.name == "BrowseCardPagerFactoryImpl" }
            .assertTrue { it.resideInPackage("com.devindie.cmptemplate.data.source.local.browse..") }
    }

    @Test
    fun `BrowseViewModel does not import data layer`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.name == "BrowseViewModel" }
            .assertTrue { viewModel ->
                viewModel.containingFile.imports.none { import ->
                    import.name.startsWith("com.devindie.cmptemplate.data.")
                }
            }
    }
}
