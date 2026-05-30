package com.devindie.vaulty.architecture.layer

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
        Konsist.scopeFromPackage("com.devindie.vaulty.domain..")
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
        Konsist.scopeFromPackage("com.devindie.vaulty.domain..")
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
        Konsist.scopeFromPackage("com.devindie.vaulty.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    forbiddenPrefixes.any { prefix -> import.name.startsWith(prefix) }
                }
            }
    }

    @Test
    fun `domain layer does not import data`() {
        Konsist.scopeFromPackage("com.devindie.vaulty.domain..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.devindie.vaulty.data.")
                }
            }
    }

    @Test
    fun `data layer does not import presentation packages`() {
        val presentationImportPrefixes =
            listOf(
                "com.devindie.vaulty.screens.",
                "com.devindie.vaulty.ui.",
                "com.devindie.vaulty.navigation.",
            )
        Konsist.scopeFromPackage("com.devindie.vaulty.data..")
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
                import.name.startsWith("com.devindie.vaulty.data.") &&
                    !import.name.startsWith("com.devindie.vaulty.data.di.")
            }
        }
    }

    @Test
    fun `androidApp does not import screens`() {
        androidAppFiles().assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.vaulty.screens.")
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
            .assertFalse { it.resideInPackage("com.devindie.vaulty.domain..") }
    }

    @Test
    fun `screen ViewModels do not import data`() {
        screenViewModels().assertFalse { viewModel ->
            viewModel.containingFile.imports.any { import ->
                import.name.startsWith("com.devindie.vaulty.data.")
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

    private fun androidAppFiles() = Konsist.scopeFromPackage("com.devindie.vaulty..")
        .files
        .filter { it.path.contains("androidApp") }

    private fun screenViewModels() = Konsist.scopeFromPackage("com.devindie.vaulty.screens..")
        .classes()
        .filter { it.name.endsWith("ViewModel") }
}
