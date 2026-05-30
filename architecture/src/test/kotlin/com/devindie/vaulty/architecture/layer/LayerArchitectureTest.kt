package com.devindie.vaulty.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** Konsist: production layers obey Clean Architecture dependency graph (domain ← data ← UI). */
class LayerArchitectureTest {

    @Test
    fun `layers follow clean architecture dependency rules`() {
        Konsist.scopeFromProduction()
            .assertArchitecture {
                val domain = Layer("Domain", "com.devindie.vaulty.domain..")
                val data = Layer("Data", "com.devindie.vaulty.data..")
                val presentation = Layer("Presentation", "com.devindie.vaulty.screens..")

                domain.doesNotDependOn(data)
                domain.doesNotDependOn(presentation)
                data.dependsOn(domain)
                presentation.dependsOn(domain)
                presentation.doesNotDependOn(data)
            }
    }

    @Test
    fun `shared di does not import data`() {
        Konsist.scopeFromPackage("com.devindie.vaulty.di..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.devindie.vaulty.data.")
                }
            }
    }

    @Test
    fun `presentation does not depend on data packages`() {
        val presentationPackages =
            listOf(
                "com.devindie.vaulty.screens..",
                "com.devindie.vaulty.ui..",
                "com.devindie.vaulty.navigation..",
                "com.devindie.vaulty.di..",
            )
        val dataImportPrefix = "com.devindie.vaulty.data."

        presentationPackages.forEach { packageName ->
            Konsist.scopeFromPackage(packageName)
                .files
                .assertFalse { file ->
                    file.imports.any { import ->
                        import.name.startsWith(dataImportPrefix)
                    }
                }
        }
    }
}
