@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "com.devindie.cmptemplate"

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
    )
}

private val firebaseSpmProducts =
    listOf(
        "FirebaseCore",
        "FirebaseAnalytics",
        "FirebaseCrashlytics",
        "FirebaseInstallations",
    )

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    android {
        namespace = "com.devindie.cmptemplate.analytics"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    iosArm64()
    iosSimulatorArm64()

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "15.0"
        swiftPackage(
            url = "https://github.com/firebase/firebase-ios-sdk.git",
            version = "12.15.0",
            products =
                listOf(
                    "FirebaseCore",
                    "FirebaseAnalytics",
                    "FirebaseCrashlytics",
                ),
        )
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.all {
            linkerOpts("-ObjC")
            val builtProductsDir = System.getenv("BUILT_PRODUCTS_DIR")
            if (builtProductsDir != null) {
                firebaseSpmProducts.forEach { product ->
                    linkerOpts("-F", "$builtProductsDir/$product")
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.gitlive.firebase.app)
            implementation(libs.gitlive.firebase.analytics)
            implementation(libs.gitlive.firebase.crashlytics)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.matching { task ->
    task.name.contains("ios", ignoreCase = true) && task.name.contains("Test", ignoreCase = true)
}.configureEach {
    enabled = false
}
