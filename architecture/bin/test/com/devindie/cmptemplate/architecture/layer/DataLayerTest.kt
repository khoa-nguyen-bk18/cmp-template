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
                "androidx.room.",
                "androidx.sqlite.",
                "androidx.paging.",
                "androidx.datastore.",
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
    fun `BrowseCardPagerFactoryImpl lives in data layer`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .classes()
            .filter { it.name == "BrowseCardPagerFactoryImpl" }
            .assertTrue { it.resideInPackage("com.devindie.cmptemplate.data..") }
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

    @Test
    fun `UserRepositoryImpl implements UserRepository`() {
        Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
            .classes()
            .filter { it.name == "UserRepositoryImpl" }
            .assertTrue {
                it.hasParentWithName(
                    "UserRepository",
                    indirectParents = true,
                )
            }
    }
}
