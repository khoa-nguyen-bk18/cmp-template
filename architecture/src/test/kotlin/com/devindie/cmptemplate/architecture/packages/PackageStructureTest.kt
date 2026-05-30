package com.devindie.cmptemplate.architecture.packages

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue as junitAssertTrue

/** Konsist: module package roots and repository interface/impl naming conventions. */
class PackageStructureTest {

    @Test
    fun `domain production code lives under com devindie cmp templatedomain`() {
        val packages = Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..").packages
        junitAssertTrue(packages.isNotEmpty(), "Expected domain packages in project scope")
    }

    @Test
    fun `repository contracts in domain repository are interfaces`() {
        junitAssertTrue(
            Konsist.scopeFromPackage("com.devindie.cmptemplate.domain.repository..")
                .interfaces()
                .any { it.name.endsWith("Repository") },
            "Expected repository interfaces in domain.repository",
        )
        val repositoryClasses =
            Konsist.scopeFromPackage("com.devindie.cmptemplate.domain.repository..")
                .classes()
                .filter { it.name.endsWith("Repository") }
        junitAssertTrue(
            repositoryClasses.isEmpty(),
            "Repository types in domain.repository must be interfaces, not classes: " +
                repositoryClasses.joinToString { it.name },
        )
    }

    @Test
    fun `RepositoryImpl classes live in data package`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.name.endsWith("RepositoryImpl") }
            .assertTrue { it.resideInPackage("com.devindie.cmptemplate.data..") }
    }

    @Test
    fun `use case classes end with UseCase`() {
        val baseUseCaseContracts = setOf("UseCase", "UseCaseNoParams")
        Konsist.scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("com.devindie.cmptemplate.domain.usecase..") }
            .filter { it.name !in baseUseCaseContracts }
            .filter { !it.name.endsWith("Exception") }
            .assertTrue { it.name.endsWith("UseCase") }
    }

    @Test
    fun `no production code under com jetbrains package`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.path.contains("/src/") && !it.path.contains("/test/") }
            .assertFalse { file ->
                file.packagee?.name?.startsWith("com.jetbrains.") == true
            }
    }
}
