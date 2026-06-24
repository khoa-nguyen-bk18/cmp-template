package com.devindie.cmptemplate.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/**
 * Layer boundary rules enforced on the current codebase.
 * Stricter graph checks live in [LayerArchitectureTest].
 */
class LayerDependencyTest {

    @Test
    fun `domain layer has no Android imports`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.")
                }
            }
    }

    @Test
    fun `domain layer has no Compose imports`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("org.jetbrains.compose.") ||
                        import.name.contains(".compose.")
                }
            }
    }

    @Test
    fun `domain layer has no framework imports`() {
        val forbiddenPrefixes =
            listOf(
                "io.ktor.",
                "org.koin.",
                "io.coil.",
            )
        Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    forbiddenPrefixes.any { prefix -> import.name.startsWith(prefix) }
                }
            }
    }

    @Test
    fun `domain layer does not import data`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.devindie.cmptemplate.data.")
                }
            }
    }

    @Test
    fun `data layer does not import presentation packages`() {
        val presentationImportPrefixes =
            listOf(
                "com.devindie.cmptemplate.feature.",
                "com.devindie.cmptemplate.core.ui.",
                "com.devindie.cmptemplate.core.navigation.",
            )
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    presentationImportPrefixes.any { prefix -> import.name.startsWith(prefix) }
                }
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
    fun `androidApp does not import feature packages`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.feature.")
            }
        }
    }

    @Test
    fun `androidApp does not import RepositoryImpl`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { import ->
                import.name.contains("RepositoryImpl")
            }
        }
    }

    @Test
    fun `ViewModels live in shared not domain`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.name.endsWith("ViewModel") }
            .assertFalse { it.resideInPackage("com.devindie.cmptemplate.domain..") }
    }

    @Test
    fun `screen ViewModels do not import data`() {
        screenViewModels().assertFalse { viewModel ->
            viewModel.containingFile.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.data.")
            }
        }
    }

    @Test
    fun `screen ViewModels do not import RepositoryImpl`() {
        screenViewModels().assertFalse { viewModel ->
            viewModel.containingFile.imports.any { import ->
                import.name.contains("RepositoryImpl")
            }
        }
    }

    private fun androidAppFiles() = Konsist.scopeFromPackage("com.devindie.cmptemplate..")
        .files
        .filter { it.path.contains("androidApp") }

    private fun screenViewModels() = Konsist.scopeFromPackage("com.devindie.cmptemplate.feature..impl..")
        .classes()
        .filter { it.name.endsWith("ViewModel") }
}
