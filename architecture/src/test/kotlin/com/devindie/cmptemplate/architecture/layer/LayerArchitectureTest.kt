package com.devindie.cmptemplate.architecture.layer

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
                val domain = Layer("Domain", "com.devindie.cmptemplate.domain..")
                val data = Layer("Data", "com.devindie.cmptemplate.data..")
                val presentation = Layer("Presentation", "com.devindie.cmptemplate.screens..")

                domain.doesNotDependOn(data)
                domain.doesNotDependOn(presentation)
                data.dependsOn(domain)
                presentation.dependsOn(domain)
                presentation.doesNotDependOn(data)
            }
    }

    @Test
    fun `shared di does not import data`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.di..")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.devindie.cmptemplate.data.")
                }
            }
    }

    @Test
    fun `presentation does not depend on data packages`() {
        val presentationPackages =
            listOf(
                "com.devindie.cmptemplate.screens..",
                "com.devindie.cmptemplate.ui..",
                "com.devindie.cmptemplate.navigation..",
                "com.devindie.cmptemplate.di..",
            )
        val dataImportPrefix = "com.devindie.cmptemplate.data."

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
