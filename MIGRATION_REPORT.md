# CocoaPods → SwiftPM Migration Report

**Date:** 2026-06-25  
**Module:** `:analytics`  
**Reason:** Fix iOS build failures from mixing Firebase CocoaPods (`Pods.xcodeproj`) with Firebase SPM in `iosApp` ([KT-66278](https://youtrack.jetbrains.com/issue/KT-66278)).

## Pre-Migration State

| Item | Value |
|------|-------|
| Kotlin | 2.3.21 |
| CocoaPods module | `:analytics` only |
| Pods | `FirebaseCore`, `FirebaseAnalytics`, `FirebaseCrashlytics` (no pinned versions) |
| iOS app Firebase | SPM `firebase-ios-sdk` ≥ 12.15.0 |
| Kotlin imports | None (`dev.gitlive:firebase-*` only — no `import cocoapods.*`) |
| Podfile | None (Gradle-generated CocoaPods only) |
| Framework | `:shared` static `Shared.framework` (`isStatic = true`) |

## Migration Steps

### Phase 2 — Toolchain

- Kotlin **2.3.21 → 2.4.0** (`gradle/libs.versions.toml`)
- KSP **2.3.5 → 2.3.9** (Kotlin 2.4 compatibility)
- Compose Stability Analyzer **0.8.0 → 0.10.0** (Kotlin 2.4 compiler plugin)

### Phase 3 — `swiftPMDependencies` in `:analytics`

- Added `group = "com.devindie.cmptemplate"`
- Replaced `cocoapods { }` with:

```kotlin
swiftPMDependencies {
    iosMinimumDeploymentTarget = "15.0"
    swiftPackage(
        url = "https://github.com/firebase/firebase-ios-sdk.git",
        version = "12.15.0",
        products = listOf("FirebaseCore", "FirebaseAnalytics", "FirebaseCrashlytics"),
    )
}
```

- Added `BUILT_PRODUCTS_DIR` framework search paths for `dev.gitlive` linker flags

### Phase 4 — Kotlin imports

No changes (no `cocoapods.*` imports in source).

### Phase 5 — Xcode integration

- Ran `:analytics:integrateLinkagePackage` → added `iosApp/KotlinMultiplatformLinkedPackage/` and linked it to `iosApp` target
- Ran `:shared:integrateEmbedAndSign` (already configured)
- Added `FRAMEWORK_SEARCH_PATHS` for Firebase SPM product directories
- Removed duplicate `GoogleService-Info.plist` from Copy Bundle Resources (synced folder already includes it)

### Phase 6 — Remove CocoaPods

- Removed `kotlinNativeCocoapods` plugin from `:analytics` and root `build.gradle.kts`
- Deleted `analytics/analytics.podspec`
- Removed `kotlinNativeCocoapods` from version catalog

## Verification

| Command | Result |
|---------|--------|
| `./gradlew :analytics:compileKotlinIosSimulatorArm64` | ✅ |
| `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` | ✅ |

## Files Changed

**Gradle:** `gradle/libs.versions.toml`, `build.gradle.kts`, `analytics/build.gradle.kts`

**Deleted:** `analytics/analytics.podspec`

**Xcode:** `iosApp/iosApp.xcodeproj/project.pbxproj`, `iosApp/KotlinMultiplatformLinkedPackage/` (created)

**Docs:** `analytics/README.md`, `MIGRATION_REPORT.md`

## Notes

- `iosApp` still links Firebase SPM directly for `FirebaseApp.configure()` in Swift — intentional.
- `KotlinMultiplatformLinkedPackage` mirrors `:analytics` SPM products for KMP linking.
- Re-run `integrateLinkagePackage` after changing `swiftPMDependencies` products or versions.
