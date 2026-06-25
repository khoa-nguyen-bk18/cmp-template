# `:analytics` module

Standalone KMP module for product analytics and crash reporting. Firebase Analytics + Crashlytics are the default backends on Android and iOS. Features inject `AnalyticsClient` from the public `api` package.

**Public API:** `com.devindie.cmptemplate.analytics.api`  
**Internal impl:** `com.devindie.cmptemplate.analytics.impl` (do not import from app code)

---

## What you get

| Capability | API |
|------------|-----|
| Custom events | `AnalyticsClient.logEvent(...)` |
| Screen views | `AnalyticsClient.logScreen(...)` or `TrackScreen` composable |
| User properties | `AnalyticsClient.setUserProperty(...)` |
| User ID | `AnalyticsClient.setUserId(...)` |
| Handled crashes | `AnalyticsClient.recordException(...)` |
| Breadcrumbs | `AnalyticsClient.log(...)` |
| Disable all tracking | `AnalyticsConfig(enabled = false)` at Koin init |
| Swap backend | Custom `EventAnalyticsProvider` / `CrashReportingProvider` in config |

---

## Prerequisites

1. A [Firebase project](https://console.firebase.google.com/) with **Google Analytics** enabled.
2. Register your apps:
   - **Android** — package name must match `applicationId` (e.g. `com.devindie.cmptemplate`).
   - **iOS** — bundle ID must match the Xcode target (e.g. `com.devindie.cmptemplate`).
3. Download config files from Firebase Console:
   - Android → `google-services.json`
   - iOS → `GoogleService-Info.plist`
4. **Do not commit** production Firebase secrets to a public repo. Use placeholders locally or CI secrets.

---

## Step 1 — Add the Gradle module

### 1.1 Register the module

In `settings.gradle.kts`:

```kotlin
include(":analytics")
```

### 1.2 Depend on it from app layers

In `shared/build.gradle.kts` (features inject `AnalyticsClient`):

```kotlin
commonMain.dependencies {
    implementation(projects.analytics)
}
```

In `androidApp/build.gradle.kts`:

```kotlin
implementation(projects.analytics)
```

### 1.3 Firebase version catalog (root `gradle/libs.versions.toml`)

This template already declares:

- `firebase-analytics`, `firebase-crashlytics` (Android)
- `gitlive-firebase-app`, `gitlive-firebase-analytics`, `gitlive-firebase-crashlytics` (iOS via GitLive)
- `google-services` and `firebase-crashlytics` Gradle plugins

### 1.4 Root `build.gradle.kts` plugins (apply false)

```kotlin
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
```

---

## Step 2 — Android setup

### 2.1 App plugins

In `androidApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}
```

### 2.2 Firebase config file

Place your real `google-services.json` at:

```
androidApp/google-services.json
```

The repo ships a **placeholder** file for local builds. Replace it with the file from Firebase Console before testing real data.

### 2.3 Wire Koin at app startup

In your `Application` class:

```kotlin
import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.analyticsFeatureModule

startKoinApp(
    appModules = listOf(
        // … other modules
        analyticsFeatureModule(
            AnalyticsConfig(
                enabled = !BuildConfig.DEBUG, // example: off in debug
            ),
        ),
    ),
) {
    androidContext(this@YourApplication)
}
```

`androidContext(...)` is required — the default Firebase event provider needs `Context`.

---

## Step 3 — iOS setup

### 3.1 Firebase SPM in Xcode

The `iosApp` target must link (via Swift Package Manager):

- `FirebaseCore`
- `FirebaseAnalytics`
- `FirebaseCrashlytics`

This template already has them wired. For a fresh project, add the [firebase-ios-sdk](https://github.com/firebase/firebase-ios-sdk) package and link those three products to the app target. Ensure **Other Linker Flags** includes `-ObjC`.

### 3.2 Firebase config file

Place your real `GoogleService-Info.plist` at:

```
iosApp/iosApp/GoogleService-Info.plist
```

It must be included in the app target **Copy Bundle Resources** build phase.

### 3.3 Initialize Firebase before Koin / UI

In `iOSApp.swift`:

```swift
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
    }
    // …
}
```

Call `doInitKoin()` (or your Koin bootstrap) **after** `FirebaseApp.configure()`.

### 3.4 Crashlytics dSYM upload (release)

The Xcode project should include a **Run Script** build phase:

```bash
"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
```

Without this, crash reports may lack symbolicated stack traces in release builds.

### 3.5 Wire Koin

In `KoinIos.kt` (or equivalent):

```kotlin
import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.analyticsFeatureModule

startKoinApp(
    appModules = listOf(
        // … other modules
        analyticsFeatureModule(AnalyticsConfig(enabled = true)),
    ),
)
```

### 3.6 Swift Package Manager (Kotlin/Native linking)

`:analytics` declares Firebase via `swiftPMDependencies` in `analytics/build.gradle.kts` (Firebase iOS SDK **12.15.0**). Gradle provisions SPM artifacts when compiling iOS targets — **no CocoaPods** required.

After changing SPM dependencies, re-run Xcode integration:

```bash
XCODEPROJ_PATH="$(realpath iosApp/iosApp.xcodeproj)" \
GRADLE_PROJECT_PATH=":analytics" \
./gradlew :analytics:integrateLinkagePackage
```

The `iosApp` project also links `KotlinMultiplatformLinkedPackage` (local package under `iosApp/KotlinMultiplatformLinkedPackage/`) so KMP and the app share the same Firebase SPM products.

---

## Step 4 — Use in app code

### Inject in a ViewModel

```kotlin
import com.devindie.cmptemplate.analytics.api.AnalyticsClient

class MyViewModel(
    private val analytics: AnalyticsClient,
) : ViewModel() {
    fun onItemTapped(id: String) {
        analytics.logEvent("item_tapped", mapOf("item_id" to id))
    }
}
```

Register the ViewModel in your feature Koin module as usual (`factory { MyViewModel(get()) }`).

### Track a screen (Compose)

```kotlin
import com.devindie.cmptemplate.analytics.api.compose.TrackScreen

@Composable
fun MyScreen() {
    TrackScreen(screenName = "my_screen")
    // …
}
```

### Swap providers (optional)

```kotlin
analyticsFeatureModule(
    AnalyticsConfig(
        enabled = true,
        eventProvider = MyCustomEventProvider(),
        crashProvider = MyCustomCrashProvider(),
    ),
)
```

---

## Step 5 — Verify setup

Run these checks in order. All should pass before you consider setup complete.

### 5.1 Automated (CI / local)

```bash
# Module unit tests (JVM)
./gradlew :analytics:allTests

# iOS Kotlin compile (no device required)
./gradlew :analytics:compileKotlinIosSimulatorArm64

# Full app Android build
./gradlew :androidApp:assembleDebug

# Architecture boundaries
./gradlew :architecture:test
```

**Expected:** `BUILD SUCCESSFUL` for each command.

> **Note:** iOS native unit tests for `:analytics` are disabled (Firebase + KMP test linking). Logic is covered by JVM tests in `commonTest`.

### 5.2 Android — Firebase DebugView

1. Replace `androidApp/google-services.json` with your real Firebase Android config.
2. Set `AnalyticsConfig(enabled = true)` temporarily (or run a **release** build if you use `enabled = !BuildConfig.DEBUG`).
3. Enable debug mode on the device/emulator:

   ```bash
   adb shell setprop debug.firebase.analytics.app com.devindie.cmptemplate
   ```

4. Run the app and trigger an event (e.g. add a test button that calls `analytics.logEvent("setup_test", emptyMap())`).
5. Open Firebase Console → **Analytics** → **DebugView**.

**Expected:** `setup_test` (or your event) appears within ~30 seconds.

6. Clear debug mode when done:

   ```bash
   adb shell setprop debug.firebase.analytics.app .none.
   ```

### 5.3 Android — Crashlytics

1. With `enabled = true` and a real `google-services.json`, trigger a **handled** error:

   ```kotlin
   analytics.recordException(
       throwable = IllegalStateException("setup_test_crash"),
       message = "analytics_readme_verification",
   )
   ```

2. Open Firebase Console → **Crashlytics**.

**Expected:** Non-fatal issue appears (may take a few minutes).

### 5.4 iOS — Analytics

1. Replace `iosApp/iosApp/GoogleService-Info.plist` with your real iOS config.
2. Build and run `iosApp` from Xcode on a simulator or device.
3. Confirm `FirebaseApp.configure()` runs (no crash on launch).
4. Log a test event from Kotlin and check Firebase Console → **Analytics** → **DebugView** (enable debug mode for iOS per [Firebase docs](https://firebase.google.com/docs/analytics/debugview)).

**Expected:** App launches; test event visible in DebugView.

### 5.5 iOS — Crashlytics

Same as Android: call `recordException` with a test throwable, then check Crashlytics dashboard after a few minutes.

### 5.6 Verify disabled mode (no Firebase calls)

With `AnalyticsConfig(enabled = false)`:

- App should build and run without Firebase config files (placeholders are fine).
- No events should appear in DebugView.
- All `AnalyticsClient` calls are no-ops.

---

## Setup checklist

Use this checklist to confirm integration:

- [ ] `:analytics` included in `settings.gradle.kts`
- [ ] `implementation(projects.analytics)` in `:shared` and `:androidApp`
- [ ] `google-services` + `firebase-crashlytics` plugins on `:androidApp`
- [ ] Real `google-services.json` in `androidApp/` (for production analytics)
- [ ] `analyticsFeatureModule(...)` in Android `Application` + `androidContext(...)`
- [ ] Firebase SPM linked in `iosApp` (Core, Analytics, Crashlytics)
- [ ] Real `GoogleService-Info.plist` in `iosApp/iosApp/`
- [ ] `FirebaseApp.configure()` in `iOSApp.swift`
- [ ] Crashlytics run script in Xcode build phases
- [ ] `analyticsFeatureModule(...)` in iOS Koin bootstrap
- [ ] `./gradlew :analytics:allTests` passes
- [ ] `./gradlew :androidApp:assembleDebug` passes
- [ ] Test event visible in Firebase DebugView (Android and/or iOS)
- [ ] Test non-fatal visible in Crashlytics (optional but recommended)

---

## Module layout

```
analytics/
├── api/                          # Import from here
│   ├── AnalyticsClient.kt
│   ├── AnalyticsConfig.kt
│   ├── AnalyticsEvent.kt
│   ├── AnalyticsFeatureModule.kt
│   ├── compose/TrackScreen.kt
│   └── provider/                 # Swappable contracts
└── impl/                         # Internal — Firebase, NoOp, Koin wiring
    ├── AnalyticsClientImpl.kt
    ├── AnalyticsModule.kt
    ├── provider/NoOp*.kt
    └── firebase/                 # androidMain / iosMain
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Android build fails on `google-services.json` | Missing or invalid config | Add valid `google-services.json`; package name must match `applicationId` |
| No events in DebugView (debug build) | `enabled = false` or debug mode off | Set `enabled = true` or use release; run `adb shell setprop debug.firebase.analytics.app <package>` |
| iOS crash on launch after adding Firebase | Missing `FirebaseApp.configure()` | Call `configure()` in `iOSApp.init()` before Koin/UI |
| iOS link errors when building `:shared` / `:analytics` | SPM linkage package out of date or Firebase not resolved in Xcode | Run `:analytics:integrateLinkagePackage`; resolve Firebase SPM in Xcode |
| Crashlytics empty symbolicated traces (iOS release) | dSYM not uploaded | Confirm Crashlytics run script build phase |
| `AnalyticsClient` not found in Koin | Module not registered | Add `analyticsFeatureModule(...)` to `startKoinApp` modules list |

---

## Further reading

- Design spec: [`docs/superpowers/specs/2026-06-25-analytics-design.md`](../docs/superpowers/specs/2026-06-25-analytics-design.md)
- [Firebase Android setup](https://firebase.google.com/docs/android/setup)
- [Firebase iOS setup](https://firebase.google.com/docs/ios/setup)
- [Analytics DebugView](https://firebase.google.com/docs/analytics/debugview)
