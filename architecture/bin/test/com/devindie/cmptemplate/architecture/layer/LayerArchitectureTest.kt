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
                val presentation = Layer("Presentation", "com.devindie.cmptemplate.feature..")

                domain.doesNotDependOn(data)
                domain.doesNotDependOn(presentation)
                data.dependsOn(domain)
                presentation.dependsOn(domain)
                presentation.doesNotDependOn(data)
            }
    }

    @Test
    fun `shared di does not import data`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.core.di..")
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
                "com.devindie.cmptemplate.feature..",
                "com.devindie.cmptemplate.core.ui..",
                "com.devindie.cmptemplate.core.navigation..",
                "com.devindie.cmptemplate.core.constants..",
                "com.devindie.cmptemplate.core.di..",
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

    @Test
    fun `core navigation ui and constants do not import feature packages`() {
        val featureImportPrefix = "com.devindie.cmptemplate.feature."
        val productionCoreFiles =
            Konsist.scopeFromProduction()
                .files
                .filter { file ->
                    val packageName = file.packagee?.name ?: return@filter false
                    packageName.startsWith("com.devindie.cmptemplate.core.navigation") ||
                        packageName.startsWith("com.devindie.cmptemplate.core.ui") ||
                        packageName.startsWith("com.devindie.cmptemplate.core.constants")
                }

        productionCoreFiles.assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith(featureImportPrefix)
            }
        }
    }
}
