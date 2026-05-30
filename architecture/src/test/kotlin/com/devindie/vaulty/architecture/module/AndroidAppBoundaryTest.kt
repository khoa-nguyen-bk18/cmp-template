package com.devindie.vaulty.architecture.module

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** Konsist: `androidApp` may import `data.di` only, not screens or other `data` packages. */
class AndroidAppBoundaryTest {

    @Test
    fun `androidApp does not import screens`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { it.name.startsWith("com.devindie.vaulty.screens.") }
        }
    }

    @Test
    fun `androidApp imports data only for DI wiring`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.vaulty.data.") &&
                    !import.name.startsWith("com.devindie.vaulty.data.di.")
            }
        }
    }

    @Test
    fun `androidApp does not import RepositoryImpl`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { it.name.contains("RepositoryImpl") }
        }
    }

    private fun androidAppFiles() = Konsist.scopeFromPackage("com.devindie.vaulty..")
        .files
        .filter { it.path.contains("androidApp") }
}
