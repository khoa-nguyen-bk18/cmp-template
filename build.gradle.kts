import java.net.HttpURLConnection
import java.net.URI
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
    alias(libs.plugins.stability.analyzer) apply false
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
    description =
        "Runs formatting, static analysis, Android security lint, unit tests, and architecture tests"
    dependsOn(
        "spotlessCheck",
        "detektAll",
        ":androidApp:lint",
        ":domain:allTests",
        ":data:allTests",
        ":shared:allTests",
        ":architecture:test",
    )
}

val dockerComposeFile = rootProject.layout.projectDirectory.file("docker-compose.yml")

fun registerDockerComposeTask(name: String, description: String, vararg args: String) {
    tasks.register<Exec>(name) {
        group = "sonarqube"
        this.description = description
        workingDir = rootProject.layout.projectDirectory.asFile
        commandLine(
            listOf("docker", "compose", "-f", dockerComposeFile.asFile.absolutePath) + args,
        )
    }
}

registerDockerComposeTask(
    "sonarUp",
    "Starts local SonarQube and PostgreSQL via Docker Compose",
    "up",
    "-d",
)
registerDockerComposeTask(
    "sonarDown",
    "Stops the local SonarQube Docker Compose stack (keeps volumes/data)",
    "down",
)

tasks.register("sonarWait") {
    group = "sonarqube"
    description = "Waits until SonarQube at http://localhost:9000 responds with status UP"
    notCompatibleWithConfigurationCache("Polls SonarQube HTTP API at execution time")
    doLast {
        val props = Properties()
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
        val hostUrl = props.getProperty("SONAR_HOST_URL")?.trim()?.takeIf { it.isNotEmpty() }
            ?: "http://localhost:9000"
        val statusUrl = URI("$hostUrl/api/system/status").toURL()
        val deadlineMs = System.currentTimeMillis() + 5 * 60 * 1000
        while (System.currentTimeMillis() < deadlineMs) {
            val status =
                runCatching {
                    val connection = statusUrl.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5_000
                    connection.readTimeout = 5_000
                    connection.requestMethod = "GET"
                    try {
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            null
                        } else {
                            Regex(""""status"\s*:\s*"([^"]+)"""")
                                .find(connection.inputStream.bufferedReader().readText())
                                ?.groupValues
                                ?.get(1)
                        }
                    } finally {
                        connection.disconnect()
                    }
                }.getOrNull()
            if (status == "UP") {
                logger.lifecycle("SonarQube is UP at $hostUrl")
                return@doLast
            }
            if (status != null) {
                logger.lifecycle("SonarQube status: $status (waiting for UP)...")
            } else {
                logger.lifecycle("SonarQube not reachable yet at $hostUrl...")
            }
            Thread.sleep(3_000)
        }
        error(
            """
            SonarQube did not become ready at $hostUrl within 5 minutes.
            Check logs: ./gradlew sonarLogs
            """.trimIndent(),
        )
    }
}

registerDockerComposeTask(
    "sonarLogs",
    "Tails SonarQube Docker Compose logs",
    "logs",
    "-f",
    "sonarqube",
)

val defaultSonarProjectKey = rootProject.name

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

            Add to local.properties (see local.properties.example):
              SONAR_TOKEN=<token-from-local-sonarqube>
              SONAR_PROJECT_KEY=<project-key>   # optional, defaults to "$defaultSonarProjectKey"
              SONAR_HOST_URL=http://localhost:9000  # optional, default shown

            First-time setup:
              1. ./gradlew sonarUp
              2. Open $hostUrl (default login admin / admin — change password when prompted)
              3. My Account → Security → Generate Tokens → paste into SONAR_TOKEN
              4. ./gradlew sonarAnalysis
            Or run everything: ./gradlew sonarLocalAnalysis
            """.trimIndent()
        }
    }
}

tasks.register("sonarLocalAnalysis") {
    group = "verification"
    description = "Starts SonarQube (Docker), waits until ready, then runs sonarAnalysis"
    dependsOn("sonarUp", "sonarWait", "sonarAnalysis")
}

tasks.named("sonarWait") {
    mustRunAfter("sonarUp")
}

tasks.named("sonarAnalysis") {
    mustRunAfter("sonarWait")
}
