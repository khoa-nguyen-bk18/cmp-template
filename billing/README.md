# `:billing` module

Standalone KMP module for in-app purchases and subscriptions via [RevenueCat](https://www.revenuecat.com/). Google Play Billing (Android) and StoreKit (iOS) are handled by the RevenueCat KMP SDK. Features inject `BillingClient` from the public `api` package when monetization is enabled.

**Public API:** `com.devindie.cmptemplate.billing.api`  
**Internal impl:** `com.devindie.cmptemplate.billing.impl` (do not import from app code)

**Design spec:** [`docs/superpowers/specs/2026-06-26-billing-module-design.md`](../docs/superpowers/specs/2026-06-26-billing-module-design.md)

---

## What you get

| Capability | API |
|------------|-----|
| Fetch offerings | `BillingClient.getOfferings()` |
| Purchase (subscription or one-time) | `BillingClient.purchase(packageId)` |
| Restore purchases | `BillingClient.restorePurchases()` |
| Entitlement check | `BillingClient.isEntitled("premium")` |
| Reactive entitlements | `BillingClient.observeCustomerInfo()` |
| Link auth user | `BillingClient.logIn(userId)` / `logOut()` |
| Disable all billing | `BillingConfig(enabled = false)` at Koin init |
| Swap backend | Custom `BillingProvider` in config (tests / future backends) |

---

## Prerequisites

1. A [RevenueCat account](https://www.revenuecat.com/) (free tier available).
2. Register **Android** and **iOS** apps in the RevenueCat dashboard.
3. Create products in **Google Play Console** and **App Store Connect**.
4. Configure **Entitlements** and **Offerings** in RevenueCat.
5. Separate **public API keys** for Android and iOS from RevenueCat project settings.
6. **Do not commit** API keys to a public repo. Use `local.properties`, CI secrets, or Xcode build settings.

---

## Step 1 — Add the Gradle module

### 1.1 Register the module

In `settings.gradle.kts`:

```kotlin
include(":billing")
```

### 1.2 Depend on it when you need billing

In `shared/build.gradle.kts` (only when a feature uses `BillingClient`):

```kotlin
commonMain.dependencies {
    implementation(projects.billing)
}
```

In `androidApp/build.gradle.kts`:

```kotlin
implementation(projects.billing)
```

### 1.3 Version catalog (`gradle/libs.versions.toml`)

```toml
[versions]
purchases-kmp = "3.1.0"

[libraries]
purchases-kmp-core = { module = "com.revenuecat.purchases:purchases-kmp-core", version.ref = "purchases-kmp" }
```

---

## Step 2 — Android setup

### 2.1 Configure RevenueCat before Koin

In your `Application` class, call `Purchases.configure` **before** `startKoinApp`:

```kotlin
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.billingFeatureModule

class CmpTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.BILLING_ENABLED) {
            Purchases.configure(
                PurchasesConfiguration.Builder(
                    context = this,
                    apiKey = BuildConfig.REVENUECAT_API_KEY_ANDROID,
                ).build(),
            )
        }

        startKoinApp(
            appModules = listOf(
                // … other modules
                billingFeatureModule(
                    BillingConfig(
                        enabled = BuildConfig.BILLING_ENABLED,
                        revenueCatApiKeyAndroid = BuildConfig.REVENUECAT_API_KEY_ANDROID,
                    ),
                ),
            ),
        ) {
            androidContext(this@CmpTemplateApplication)
        }
    }
}
```

Store `REVENUECAT_API_KEY_ANDROID` in `local.properties` and expose via `BuildConfig` — never commit real keys.

### 2.2 Google Play products

- Product IDs in Play Console must match RevenueCat dashboard configuration.
- Use **license testers** for sandbox purchases on Android.

---

## Step 3 — iOS setup

### 3.1 Register iOS app in RevenueCat

Use the **iOS public API key** (different from Android).

### 3.2 RevenueCat KMP 3.x linking

`purchases-kmp-core` 3.x handles native iOS dependencies through Gradle — you do **not** need to manually add `PurchasesHybridCommon` via CocoaPods or SPM for the `:billing` module.

If you see iOS link errors after adding billing:

```bash
XCODEPROJ_PATH="$(realpath iosApp/iosApp.xcodeproj)" \
GRADLE_PROJECT_PATH=":billing" \
./gradlew :billing:integrateLinkagePackage
```

### 3.3 Configure RevenueCat before Koin

Call `Purchases.configure` **before** `doInitKoin()`:

```kotlin
// shared/src/iosMain/kotlin/.../BillingIosInit.kt
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

fun configureBilling(apiKey: String) {
    Purchases.configure(PurchasesConfiguration(apiKey))
}
```

From `iOSApp.swift` (before shared UI / Koin):

```swift
import shared

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        BillingIosInitKt.configureBilling(apiKey: "your_ios_public_api_key")
        KoinIosKt.doInitKoin()
    }
    // …
}
```

Or configure entirely from Kotlin in `doInitKoin()` if you load the key from a secure source.

### 3.4 Wire Koin on iOS

In `KoinIos.kt`:

```kotlin
import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.billingFeatureModule

fun doInitKoin() {
    startKoinApp(
        appModules = listOf(
            // …
            billingFeatureModule(
                BillingConfig(
                    enabled = true,
                    revenueCatApiKeyIos = "your_ios_public_api_key",
                ),
            ),
        ),
    )
}
```

### 3.5 Sandbox testing (iOS)

- Create a **Sandbox Apple ID** in App Store Connect.
- Sign in on the simulator/device: Settings → App Store → Sandbox Account.
- Run a test purchase; verify entitlement in RevenueCat **Customer** view and via `observeCustomerInfo()`.

---

## Step 4 — Use in app code

### Inject in a ViewModel

```kotlin
import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingResult

class PremiumViewModel(
    private val billing: BillingClient,
) : ViewModel() {
    val isPremium =
        billing
            .observeCustomerInfo()
            .map { "premium" in it.activeEntitlements }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun purchaseMonthly() {
        viewModelScope.launch {
            when (val result = billing.purchase("\$rc_monthly")) {
                is BillingResult.Success -> Unit
                is BillingResult.Failure -> {
                    if (result.error !is BillingError.UserCancelled) {
                        // show error
                    }
                }
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            billing.restorePurchases()
        }
    }
}
```

Use the **package identifier** from your RevenueCat offering (e.g. `$rc_monthly`), not the raw store product ID.

### Initialize on startup

```kotlin
viewModelScope.launch {
    billing.initialize()
}
```

---

## Step 5 — Verify setup

### 5.1 Automated

```bash
./gradlew :billing:allTests
./gradlew :billing:compileKotlinIosSimulatorArm64
./gradlew :architecture:test
```

**Expected:** `BUILD SUCCESSFUL` for each command.

### 5.2 Android sandbox

1. Set `BillingConfig(enabled = true)` and a real Android API key.
2. Add your account as a **license tester** in Play Console.
3. Call `billing.purchase("<package_id>")` from the app.
4. Verify entitlement in `observeCustomerInfo()` and RevenueCat dashboard.

### 5.3 iOS sandbox

1. Set `BillingConfig(enabled = true)` and a real iOS API key.
2. Configure `Purchases` before Koin (see Step 3).
3. Purchase with a sandbox Apple ID.
4. Verify entitlement updates in the app and RevenueCat dashboard.

### 5.4 Disabled mode

With `BillingConfig(enabled = false)`:

- Omit `Purchases.configure`.
- No `:shared` dependency on `:billing` if no feature needs it.
- All `BillingClient` calls no-op via `NoOpBillingProvider`.

---

## Setup checklist

- [ ] `:billing` included in `settings.gradle.kts`
- [ ] `implementation(projects.billing)` in `:shared` + `:androidApp` (when using billing)
- [ ] `purchases-kmp` version in `libs.versions.toml`
- [ ] RevenueCat dashboard: products, entitlements, offerings
- [ ] Android: `Purchases.configure` + `billingFeatureModule` in `Application`
- [ ] iOS: `Purchases.configure` before Koin + `billingFeatureModule` in `KoinIos.kt`
- [ ] `./gradlew :billing:allTests` passes
- [ ] `./gradlew :billing:compileKotlinIosSimulatorArm64` passes
- [ ] Sandbox purchase works on Android and/or iOS
- [ ] Restore purchases entry in UI (store policy)

---

## Module layout

```
billing/
├── api/                          # Import from here
│   ├── BillingClient.kt
│   ├── BillingConfig.kt
│   ├── BillingModels.kt
│   ├── BillingFeatureModule.kt
│   └── provider/BillingProvider.kt
└── impl/                         # Internal — RevenueCat, NoOp, Koin
    ├── BillingClientImpl.kt
    ├── BillingModule.kt
    ├── mapper/RevenueCatMappers.kt      # androidMain / iosMain
    └── provider/
        ├── NoOpBillingProvider.kt
        └── RevenueCatBillingProvider.kt   # androidMain / iosMain
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Package not found` | Wrong package id | Use RevenueCat **package** identifier from offering, not store SKU |
| Purchase returns `NotConfigured` | `enabled = false` or missing Koin module | Set `enabled = true`; register `billingFeatureModule` |
| Android crash on purchase | `Purchases.configure` not called | Configure in `Application.onCreate()` before Koin |
| iOS link errors | SPM linkage out of date | Run `:billing:integrateLinkagePackage` |
| Entitlement not updating | No customer info sync | Call `billing.initialize()`; observe `observeCustomerInfo()` |
| `BillingClient` not in Koin | Module not registered | Add `billingFeatureModule(...)` to `startKoinApp` |

---

## Further reading

- [RevenueCat KMP installation](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform)
- [RevenueCat offerings guide](https://www.revenuecat.com/docs/tools/offerings)
- [KMP SDK 3.0 — simplified iOS setup](https://www.revenuecat.com/blog/engineering/kmp-sdk-3/)
