import java.util.Properties

plugins {
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kover)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}

dependencies {
    kover(projects.domain)
    kover(projects.data)
    kover(projects.shared)
    kover(projects.architecture)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

fun localProperty(name: String): String? = localProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }

val sonarHostUrl = localProperty("SONAR_HOST_URL") ?: "http://localhost:9000"

sonar {
    properties {
        property("sonar.host.url", sonarHostUrl)
        localProperty("SONAR_ORGANIZATION")?.let { property("sonar.organization", it) }
        property("sonar.projectKey", localProperty("SONAR_PROJECT_KEY") ?: rootProject.name)
        localProperty("SONAR_TOKEN")?.let { property("sonar.token", it) }
        property(
            "sonar.exclusions",
            "**/build/**,**/iosApp/**,**/.codegraph/**,**/*.generated.kt",
        )
        property("sonar.issue.ignore.multicriteria", "ignoreMonochromeLauncherIcon")
        property(
            "sonar.issue.ignore.multicriteria.ignoreMonochromeLauncherIcon.ruleKey",
            "android:MonochromeLauncherIcon",
        )
        property(
            "sonar.issue.ignore.multicriteria.ignoreMonochromeLauncherIcon.resourceKey",
            "**/ic_launcher*.xml",
        )
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/report.xml").get().asFile.path,
        )
    }
}

tasks.named("sonar") {
    dependsOn(tasks.named("koverXmlReport"))
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude(
            "**/build/**",
            "**/iosApp/**",
        )
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "max_line_length" to "120",
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "max_line_length" to "120",
            ),
        )
    }
}

subprojects {
    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            config.from(rootProject.file("detekt.yml"))
            parallel = true
        }
        tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
            reports {
                html.required.set(false)
                checkstyle.required.set(false)
                sarif.required.set(false)
                markdown.required.set(false)
            }
        }
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all subprojects"
    dependsOn(
        subprojects
            .mapNotNull { project ->
                project.tasks.findByName("detekt")?.let { project.path to "detekt" }
            }.map { (path, task) -> "$path:$task" },
    )
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs formatting, static analysis, unit tests, and architecture tests"
    dependsOn(
        "spotlessCheck",
        "detektAll",
        ":domain:allTests",
        ":data:allTests",
        ":shared:allTests",
        ":architecture:test",
    )
}

tasks.register("sonarAnalysis") {
    group = "verification"
    description =
        "Runs unit tests with Kover coverage, then uploads analysis and coverage to SonarQube (default http://localhost:9000)"
    notCompatibleWithConfigurationCache("Reads SONAR_TOKEN from local.properties at execution time")
    dependsOn(tasks.named("koverXmlReport"), tasks.named("sonar"))
    doFirst {
        val props = Properties()
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
        fun prop(name: String): String? = props.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }

        val token = prop("SONAR_TOKEN")
        val hostUrl = prop("SONAR_HOST_URL") ?: "http://localhost:9000"
        require(!token.isNullOrBlank()) {
            """
            SONAR_TOKEN is missing in local.properties.

            Add to local.properties (file is gitignored):
              SONAR_TOKEN=<token-from-local-sonarqube>
              SONAR_PROJECT_KEY=<project-key>   # optional, defaults to "${project.name}"
              SONAR_HOST_URL=http://localhost:9000  # optional, default shown

            Create a token: SonarQube at $hostUrl → My Account → Security → Generate Tokens.
            Ensure the Docker container is running before ./gradlew sonarAnalysis.
            """.trimIndent()
        }
    }
}
