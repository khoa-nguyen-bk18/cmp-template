package com.devindie.cmptemplate.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/** Konsist: `data` commonMain stays free of Android except allowed Room/SQLite prefixes. */
class DataLayerTest {

    @Test
    fun `data commonMain has no Android imports`() {
        val allowedAndroidxPrefixes =
            listOf(
                "androidx.datastore.",
                "androidx.room.",
                "androidx.sqlite.",
            )
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .files
            .filter { it.path.contains("commonMain") }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("android.") ||
                        (
                            import.name.startsWith("androidx.") &&
                                allowedAndroidxPrefixes.none { prefix -> import.name.startsWith(prefix) }
                            )
                }
            }
    }

    @Test
    fun `data commonMain has no Compose imports`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .files
            .filter { it.path.contains("commonMain") }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("org.jetbrains.compose.") ||
                        import.name.contains(".compose.")
                }
            }
    }

    @Test
    fun `BrowseCardRepositoryImpl implements BrowseCardRepository`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .classes()
            .filter { it.name == "BrowseCardRepositoryImpl" }
            .assertTrue {
                it.hasParentWithName(
                    "BrowseCardRepository",
                    indirectParents = true,
                )
            }
    }

    @Test
    fun `CardDetailRepositoryImpl implements CardDetailRepository`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .classes()
            .filter { it.name == "CardDetailRepositoryImpl" }
            .assertTrue {
                it.hasParentWithName(
                    "CardDetailRepository",
                    indirectParents = true,
                )
            }
    }
}
