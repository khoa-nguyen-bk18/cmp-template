plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

detekt {
    source.setFrom("src/test/kotlin")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
    workingDir = rootProject.rootDir
}
