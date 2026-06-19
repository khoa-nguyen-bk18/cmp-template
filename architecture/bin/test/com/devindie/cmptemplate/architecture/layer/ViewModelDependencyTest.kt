package com.devindie.cmptemplate.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** Konsist: screen ViewModels in `shared` must not import `data` or repository implementations. */
class ViewModelDependencyTest {

    @Test
    fun `screen ViewModels do not import data`() {
        screenViewModels().assertFalse { viewModel ->
            viewModel.containingFile.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.data.")
            }
        }
    }

    @Test
    fun `screen ViewModels do not import DataSource`() {
        screenViewModels().assertFalse { viewModel ->
            viewModel.containingFile.imports.any { import ->
                import.name.contains("DataSource")
            }
        }
    }

    private fun screenViewModels() =
        Konsist.scopeFromPackage("com.devindie.cmptemplate.feature..impl..")
        .classes()
        .filter { it.name.endsWith("ViewModel") }
}
