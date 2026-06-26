package com.devindie.cmptemplate.architecture.feature

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** Konsist: `feature.*.impl` is private to its feature; other packages import `feature.*.api` only. */
class FeatureApiImplBoundaryTest {

    @Test
    fun `production code imports feature impl only from the same feature`() {
        Konsist.scopeFromProduction()
            .files
            .assertFalse { file ->
                val filePackage = file.packagee?.name
                file.imports.any { import ->
                    violatesFeatureImplBoundary(import.name, filePackage)
                }
            }
    }

    private fun violatesFeatureImplBoundary(importName: String, filePackage: String?): Boolean {
        val match = FEATURE_IMPL_IMPORT.find(importName) ?: return false
        val featureName = match.groupValues[1]
        val ownerPrefix = "com.devindie.cmptemplate.feature.$featureName."
        return filePackage?.startsWith(ownerPrefix) != true
    }

    private companion object {
        private val FEATURE_IMPL_IMPORT =
            Regex("""com\.devindie\.cmptemplate\.feature\.([^.]+)\.impl(\..+)?""")
    }
}
