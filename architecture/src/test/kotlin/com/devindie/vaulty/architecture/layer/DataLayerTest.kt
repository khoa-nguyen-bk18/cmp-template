package com.devindie.vaulty.architecture.layer

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/** Konsist: `data` commonMain stays free of Android except allowed Room/SQLite prefixes. */
class DataLayerTest {

    @Test
    fun `vaultdata commonMain has no Android imports`() {
        val allowedAndroidxPrefixes =
            listOf(
                "androidx.datastore.",
                "androidx.room.",
                "androidx.sqlite.",
            )
        Konsist.scopeFromPackage("com.devindie.vaulty.data..")
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
    fun `vaultdata commonMain has no Compose imports`() {
        Konsist.scopeFromPackage("com.devindie.vaulty.data..")
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
    fun `VaultFolderRepositoryImpl implements VaultFolderRepository`() {
        Konsist.scopeFromPackage("com.devindie.vaulty.data..")
            .classes()
            .filter { it.name == "VaultFolderRepositoryImpl" }
            .assertTrue {
                it.hasParentWithName(
                    "VaultFolderRepository",
                    indirectParents = true,
                )
            }
    }
}
