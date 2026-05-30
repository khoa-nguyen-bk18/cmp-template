package com.devindie.cmptemplate.architecture.module

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** Konsist: `androidApp` may import `data.di` only, not screens or other `data` packages. */
class AndroidAppBoundaryTest {

    @Test
    fun `androidApp does not import screens`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { it.name.startsWith("com.devindie.cmptemplate.screens.") }
        }
    }

    @Test
    fun `androidApp imports data only for DI wiring`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.data.") &&
                    !import.name.startsWith("com.devindie.cmptemplate.data.di.")
            }
        }
    }

    @Test
    fun `androidApp does not import RepositoryImpl`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { it.name.contains("RepositoryImpl") }
        }
    }

    private fun androidAppFiles() = Konsist.scopeFromPackage("com.devindie.cmptemplate..")
        .files
        .filter { it.path.contains("androidApp") }
}
