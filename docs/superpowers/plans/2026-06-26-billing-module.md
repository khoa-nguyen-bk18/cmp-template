# Billing Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add optional `:billing` KMP Gradle module with `BillingClient` facade, RevenueCat default provider on Android/iOS, NoOp when disabled, and a README mirroring `:analytics` (including iOS configure + compile verification).

**Architecture:** Thin `BillingClient` delegates to swappable `BillingProvider`. `NoOpBillingProvider` when `BillingConfig.enabled = false`. `RevenueCatBillingProvider` in `androidMain` + `iosMain` (same KMP Purchases API). `expect`/`actual` for platform default provider — `jvmMain` stubs for JVM unit tests. No `:domain` / `:data` changes. **Do not** add `implementation(projects.billing)` to `:shared` or wire Koin in app entry by default.

**Tech Stack:** Kotlin Multiplatform, RevenueCat `purchases-kmp-core` 3.x, Koin, Kotlin Coroutines + Flow, kotlin-test (commonTest on JVM).

**Spec:** [`docs/superpowers/specs/2026-06-26-billing-module-design.md`](../specs/2026-06-26-billing-module-design.md)

---

## File map

| File | Responsibility |
|------|----------------|
| `billing/build.gradle.kts` | KMP module; RC dep on androidMain/iosMain only; jvm for tests |
| `billing/README.md` | Integration guide — Android, **iOS**, optional wiring, verify checklist |
| `billing/src/commonMain/.../api/BillingModels.kt` | Public result + domain models |
| `billing/src/commonMain/.../api/BillingClient.kt` | Facade interface |
| `billing/src/commonMain/.../api/BillingConfig.kt` | Init config |
| `billing/src/commonMain/.../api/provider/BillingProvider.kt` | Swappable contract |
| `billing/src/commonMain/.../api/BillingFeatureModule.kt` | Public Koin entry |
| `billing/src/commonMain/.../impl/BillingClientImpl.kt` | Facade impl + entitlement cache |
| `billing/src/commonMain/.../impl/BillingModule.kt` | Koin module + provider selection |
| `billing/src/commonMain/.../impl/provider/NoOpBillingProvider.kt` | Disabled / JVM stub |
| `billing/src/androidMain/.../impl/provider/RevenueCatBillingProvider.kt` | RC backend (Android) |
| `billing/src/iosMain/.../impl/provider/RevenueCatBillingProvider.kt` | RC backend (iOS) — same source as Android |
| `billing/src/jvmMain/.../impl/BillingModule.jvm.kt` | `actual` → NoOp for JVM tests |
| `billing/src/androidMain/.../impl/BillingModule.android.kt` | `actual` → RevenueCat |
| `billing/src/iosMain/.../impl/BillingModule.ios.kt` | `actual` → RevenueCat |
| `billing/src/commonTest/.../impl/BillingClientImplTest.kt` | Facade tests with fake provider |
| `billing/src/commonTest/.../impl/provider/NoOpBillingProviderTest.kt` | NoOp behavior |
| `settings.gradle.kts` | `include(":billing")` |
| `gradle/libs.versions.toml` | `purchases-kmp` version + coordinate |
| `build.gradle.kts` | Add `:billing:allTests` to `qualityCheck` |
| `architecture/.../LayerDependencyTest.kt` | `domain` / `data` must not import `:billing` |

**Not modified (optional integration):** `shared/build.gradle.kts`, `CmpTemplateApplication.kt`, `KoinIos.kt`, `iOSApp.swift` — documented in README only.

---

### Task 1: Gradle module scaffold

**Files:**
- Create: `billing/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (`qualityCheck`)

- [ ] **Step 1: Add version catalog entry**

In `gradle/libs.versions.toml`:

```toml
[versions]
purchases-kmp = "3.0.5"

[libraries]
purchases-kmp-core = { module = "com.revenuecat.purchases:purchases-kmp-core", version.ref = "purchases-kmp" }
```

Use latest stable `3.x` from [Maven Central](https://central.sonatype.com/artifact/com.revenuecat.purchases/purchases-kmp-core) if `3.0.5` is stale.

- [ ] **Step 2: Register module**

In `settings.gradle.kts` after `include(":analytics")`:

```kotlin
include(":billing")
```

- [ ] **Step 3: Create `billing/build.gradle.kts`**

Mirror `analytics/build.gradle.kts` structure:

```kotlin
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "com.devindie.cmptemplate"

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
        "src/jvmMain/kotlin",
    )
}

kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    android {
        namespace = "com.devindie.cmptemplate.billing"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.purchases.kmp.core)
        }
        iosMain.dependencies {
            implementation(libs.purchases.kmp.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.matching { task ->
    task.name.contains("ios", ignoreCase = true) && task.name.contains("Test", ignoreCase = true)
}.configureEach {
    enabled = false
}
```

- [ ] **Step 4: Add to qualityCheck**

In root `build.gradle.kts`, add `":billing:allTests"` to `qualityCheck` `dependsOn` list (next to `:analytics:allTests`).

- [ ] **Step 5: Verify scaffold compiles**

Run: `./gradlew :billing:compileKotlinJvm`

Expected: `BUILD SUCCESSFUL` (empty module compiles once a dummy file exists — add `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/.gitkeep` or skip until Task 2).

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts gradle/libs.versions.toml build.gradle.kts billing/
git commit -m "chore: scaffold :billing KMP module"
```

---

### Task 2: Public models + BillingProvider contract

**Files:**
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingModels.kt`
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/provider/BillingProvider.kt`
- Create: `billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/api/BillingModelsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/api/BillingModelsTest.kt
package com.devindie.cmptemplate.billing.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingModelsTest {
    @Test
    fun billingCustomerInfo_emptyHasNoEntitlements() {
        val info = BillingCustomerInfo(emptySet(), emptyMap())
        assertTrue(info.activeEntitlements.isEmpty())
    }

    @Test
    fun billingResult_successWrapsValue() {
        val result = BillingResult.Success("ok")
        assertEquals("ok", (result as BillingResult.Success).value)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :billing:cleanJvmTest :billing:jvmTest --tests "com.devindie.cmptemplate.billing.api.BillingModelsTest"`

Expected: FAIL — types not found

- [ ] **Step 3: Implement models**

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingModels.kt
package com.devindie.cmptemplate.billing.api

import kotlin.time.Instant

data class BillingOfferings(
    val current: BillingOffering?,
    val all: Map<String, BillingOffering>,
)

data class BillingOffering(
    val identifier: String,
    val packages: List<BillingPackage>,
)

data class BillingPackage(
    val identifier: String,
    val productId: String,
    val title: String,
    val description: String,
    val priceFormatted: String,
    val packageType: BillingPackageType,
)

enum class BillingPackageType {
    UNKNOWN,
    CUSTOM,
    LIFETIME,
    ANNUAL,
    SIX_MONTH,
    THREE_MONTH,
    TWO_MONTH,
    MONTHLY,
    WEEKLY,
}

data class BillingPurchase(
    val productId: String,
    val transactionId: String?,
    val customerInfo: BillingCustomerInfo,
)

data class BillingCustomerInfo(
    val activeEntitlements: Set<String>,
    val expirationByEntitlement: Map<String, Instant?>,
) {
    companion object {
        val Empty = BillingCustomerInfo(emptySet(), emptyMap())
    }
}

sealed interface BillingResult<out T> {
    data class Success<T>(val value: T) : BillingResult<T>
    data class Failure(val error: BillingError) : BillingResult<Nothing>
}

sealed class BillingError {
    data object UserCancelled : BillingError()
    data object NotConfigured : BillingError()
    data class StoreError(val message: String, val code: Int? = null) : BillingError()
    data class Unknown(val message: String) : BillingError()
}
```

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/provider/BillingProvider.kt
package com.devindie.cmptemplate.billing.api.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import kotlinx.coroutines.flow.Flow

interface BillingProvider {
    suspend fun initialize(): BillingResult<Unit>
    suspend fun getOfferings(): BillingResult<BillingOfferings>
    suspend fun purchase(packageId: String): BillingResult<BillingPurchase>
    suspend fun restorePurchases(): BillingResult<BillingCustomerInfo>
    fun observeCustomerInfo(): Flow<BillingCustomerInfo>
    suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo>
    suspend fun logOut(): BillingResult<BillingCustomerInfo>
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :billing:cleanJvmTest :billing:jvmTest --tests "com.devindie.cmptemplate.billing.api.BillingModelsTest"`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/ \
        billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/api/
git commit -m "feat(billing): add public models and BillingProvider contract"
```

---

### Task 3: BillingConfig + BillingClient interface

**Files:**
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingConfig.kt`
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingClient.kt`

- [ ] **Step 1: Implement config and client interface**

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingConfig.kt
package com.devindie.cmptemplate.billing.api

import com.devindie.cmptemplate.billing.api.provider.BillingProvider

data class BillingConfig(
    val enabled: Boolean = false,
    val revenueCatApiKeyAndroid: String = "",
    val revenueCatApiKeyIos: String = "",
    val provider: BillingProvider? = null,
)
```

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingClient.kt
package com.devindie.cmptemplate.billing.api

import kotlinx.coroutines.flow.Flow

interface BillingClient {
    suspend fun initialize(): BillingResult<Unit>
    suspend fun getOfferings(): BillingResult<BillingOfferings>
    suspend fun purchase(packageId: String): BillingResult<BillingPurchase>
    suspend fun restorePurchases(): BillingResult<BillingCustomerInfo>
    fun isEntitled(entitlementId: String): Boolean
    fun observeCustomerInfo(): Flow<BillingCustomerInfo>
    suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo>
    suspend fun logOut(): BillingResult<BillingCustomerInfo>
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :billing:compileKotlinJvm`

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingConfig.kt \
        billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingClient.kt
git commit -m "feat(billing): add BillingConfig and BillingClient interface"
```

---

### Task 4: NoOpBillingProvider

**Files:**
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/provider/NoOpBillingProvider.kt`
- Create: `billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/impl/provider/NoOpBillingProviderTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/impl/provider/NoOpBillingProviderTest.kt
package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoOpBillingProviderTest {
  private val provider = NoOpBillingProvider()

  @Test
  fun initialize_returnsSuccess() = runTest {
    assertTrue(provider.initialize() is BillingResult.Success)
  }

  @Test
  fun getOfferings_returnsEmpty() = runTest {
    val result = provider.getOfferings() as BillingResult.Success
    assertEquals(null, result.value.current)
    assertTrue(result.value.all.isEmpty())
  }

  @Test
  fun purchase_returnsNotConfigured() = runTest {
    val result = provider.purchase("monthly") as BillingResult.Failure
    assertEquals(BillingError.NotConfigured, result.error)
  }

  @Test
  fun observeCustomerInfo_emitsEmpty() = runTest {
    assertEquals(BillingCustomerInfo.Empty, provider.observeCustomerInfo().first())
  }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `./gradlew :billing:cleanJvmTest :billing:jvmTest --tests "com.devindie.cmptemplate.billing.impl.provider.NoOpBillingProviderTest"`

- [ ] **Step 3: Implement NoOp**

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/provider/NoOpBillingProvider.kt
package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoOpBillingProvider : BillingProvider {
    override suspend fun initialize(): BillingResult<Unit> = BillingResult.Success(Unit)

    override suspend fun getOfferings(): BillingResult<BillingOfferings> =
        BillingResult.Success(BillingOfferings(current = null, all = emptyMap()))

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> =
        BillingResult.Failure(BillingError.NotConfigured)

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> = flowOf(BillingCustomerInfo.Empty)

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)
}
```

- [ ] **Step 4: Run tests — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(billing): add NoOpBillingProvider"
```

---

### Task 5: BillingClientImpl

**Files:**
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingClientImpl.kt`
- Create: `billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/impl/BillingClientImplTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
// billing/src/commonTest/kotlin/com/devindie/cmptemplate/billing/impl/BillingClientImplTest.kt
package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillingClientImplTest {
  @Test
  fun isEntitled_reflectsCachedCustomerInfo() = runTest {
    val provider = FakeBillingProvider(
      customerInfo = BillingCustomerInfo(setOf("premium"), emptyMap()),
    )
    val client = BillingClientImpl(provider)
    client.initialize()
    assertTrue(client.isEntitled("premium"))
    assertFalse(client.isEntitled("pro"))
  }

  @Test
  fun purchase_delegatesToProvider() = runTest {
    val provider = FakeBillingProvider()
    val client = BillingClientImpl(provider)
    client.purchase("monthly")
    assertEquals("monthly", provider.lastPurchasePackageId)
  }

  @Test
  fun providerThrows_returnsUnknownFailure() = runTest {
    val provider =
      object : BillingProvider by FakeBillingProvider() {
        override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
          error("network down")
      }
    val client = BillingClientImpl(provider)
    val result = client.restorePurchases() as BillingResult.Failure
    assertTrue(result.error is BillingError.Unknown)
  }
}

private class FakeBillingProvider(
  private val customerInfo: BillingCustomerInfo = BillingCustomerInfo.Empty,
) : BillingProvider {
  var lastPurchasePackageId: String? = null
  private val state = MutableStateFlow(customerInfo)

  override suspend fun initialize(): BillingResult<Unit> {
    state.value = customerInfo
    return BillingResult.Success(Unit)
  }

  override suspend fun getOfferings(): BillingResult<BillingOfferings> =
    BillingResult.Success(BillingOfferings(null, emptyMap()))

  override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> {
    lastPurchasePackageId = packageId
    return BillingResult.Success(
      BillingPurchase("prod", "tx", customerInfo),
    )
  }

  override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
    BillingResult.Success(customerInfo)

  override fun observeCustomerInfo(): Flow<BillingCustomerInfo> = state

  override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
    BillingResult.Success(customerInfo)

  override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
    BillingResult.Success(BillingCustomerInfo.Empty)
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement BillingClientImpl**

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingClientImpl.kt
package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

internal class BillingClientImpl(
    private val provider: BillingProvider,
) : BillingClient {
    @Volatile
    private var cachedCustomerInfo: BillingCustomerInfo = BillingCustomerInfo.Empty

    override suspend fun initialize(): BillingResult<Unit> = runSafely { provider.initialize() }

    override suspend fun getOfferings() = runSafely { provider.getOfferings() }

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> =
        runSafely {
            provider.purchase(packageId).also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value.customerInfo
                }
            }
        }

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.restorePurchases().also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value
                }
            }
        }

    override fun isEntitled(entitlementId: String): Boolean =
        entitlementId in cachedCustomerInfo.activeEntitlements

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> =
        provider.observeCustomerInfo().onEach { cachedCustomerInfo = it }

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.logIn(appUserId).also { result ->
                if (result is BillingResult.Success) cachedCustomerInfo = result.value
            }
        }

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.logOut().also { result ->
                if (result is BillingResult.Success) cachedCustomerInfo = result.value
            }
        }

    private suspend inline fun <T> runSafely(
        crossinline block: suspend () -> BillingResult<T>,
    ): BillingResult<T> =
        try {
            block()
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            BillingResult.Failure(BillingError.Unknown(error.message ?: "billing_error"))
        }
}
```

- [ ] **Step 4: Run tests — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(billing): add BillingClientImpl with entitlement cache"
```

---

### Task 6: Koin module + public entry

**Files:**
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.kt`
- Create: `billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingFeatureModule.kt`
- Create: `billing/src/jvmMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.jvm.kt`
- Create: `billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.android.kt`
- Create: `billing/src/iosMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.ios.kt`

- [ ] **Step 1: Common Koin module**

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.kt
package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.NoOpBillingProvider
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

internal fun createBillingModule(config: BillingConfig): Module =
    module {
        single<BillingClient> {
            val provider: BillingProvider =
                if (!config.enabled) {
                    NoOpBillingProvider()
                } else {
                    config.provider ?: defaultBillingProvider(config)
                }
            BillingClientImpl(provider = provider)
        }
    }

internal expect fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider
```

```kotlin
// billing/src/commonMain/kotlin/com/devindie/cmptemplate/billing/api/BillingFeatureModule.kt
package com.devindie.cmptemplate.billing.api

import com.devindie.cmptemplate.billing.impl.createBillingModule
import org.koin.core.module.Module

fun billingFeatureModule(config: BillingConfig): Module = createBillingModule(config)
```

- [ ] **Step 2: JVM actual (tests / no RC on JVM)**

```kotlin
// billing/src/jvmMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.jvm.kt
package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.NoOpBillingProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider =
    NoOpBillingProvider()
```

- [ ] **Step 3: Android + iOS actuals (stubs until Task 7)**

```kotlin
// billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/BillingModule.android.kt
package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.RevenueCatBillingProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider =
    RevenueCatBillingProvider()
```

Duplicate for `iosMain` with same body.

- [ ] **Step 4: Compile JVM + iOS**

Run:

```bash
./gradlew :billing:compileKotlinJvm :billing:compileKotlinIosSimulatorArm64
```

Expected: iOS compile may FAIL until Task 7 implements `RevenueCatBillingProvider` — proceed to Task 7 if so.

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(billing): add Koin module and platform provider selection"
```

---

### Task 7: RevenueCatBillingProvider + mappers

**Files:**
- Create: `billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/mapper/RevenueCatMappers.kt`
- Create: `billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/provider/RevenueCatBillingProvider.kt`
- Create: `billing/src/iosMain/kotlin/com/devindie/cmptemplate/billing/impl/mapper/RevenueCatMappers.kt`
- Create: `billing/src/iosMain/kotlin/com/devindie/cmptemplate/billing/impl/provider/RevenueCatBillingProvider.kt`

**Note:** `androidMain` and `iosMain` files are **identical** — copy verbatim. RevenueCat KMP API is shared; Purchases must be configured in app shell before `initialize()`.

- [ ] **Step 1: Implement mappers**

Map RevenueCat `CustomerInfo`, `Offerings`, `Package`, `PackageType` → `Billing*` types. Use `kotlin.time.Instant` for expiration (RC 3.x). Handle null expiration for lifetime entitlements.

```kotlin
// billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/mapper/RevenueCatMappers.kt
package com.devindie.cmptemplate.billing.impl.mapper

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingOffering
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPackage
import com.devindie.cmptemplate.billing.api.BillingPackageType
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlin.time.Instant

internal fun CustomerInfo.toBillingCustomerInfo(): BillingCustomerInfo {
    val active = entitlements.active.keys.toSet()
    val expirations =
        entitlements.active.mapValues { (_, info) ->
            info.expirationDate?.let { millis ->
                Instant.fromEpochMilliseconds(millis)
            }
        }
    return BillingCustomerInfo(activeEntitlements = active, expirationByEntitlement = expirations)
}

internal fun Offerings.toBillingOfferings(): BillingOfferings =
    BillingOfferings(
        current = current?.toBillingOffering(),
        all = all.mapValues { (_, offering) -> offering.toBillingOffering() },
    )

private fun Offering.toBillingOffering(): BillingOffering =
    BillingOffering(
        identifier = identifier,
        packages = availablePackages.map { it.toBillingPackage() },
    )

private fun Package.toBillingPackage(): BillingPackage =
    BillingPackage(
        identifier = identifier,
        productId = storeProduct.id,
        title = storeProduct.title,
        description = storeProduct.localizedDescription,
        priceFormatted = storeProduct.price.formatted,
        packageType = packageType.toBillingPackageType(),
    )

private fun PackageType.toBillingPackageType(): BillingPackageType =
    when (this) {
        PackageType.UNKNOWN -> BillingPackageType.UNKNOWN
        PackageType.CUSTOM -> BillingPackageType.CUSTOM
        PackageType.LIFETIME -> BillingPackageType.LIFETIME
        PackageType.ANNUAL -> BillingPackageType.ANNUAL
        PackageType.SIX_MONTH -> BillingPackageType.SIX_MONTH
        PackageType.THREE_MONTH -> BillingPackageType.THREE_MONTH
        PackageType.TWO_MONTH -> BillingPackageType.TWO_MONTH
        PackageType.MONTHLY -> BillingPackageType.MONTHLY
        PackageType.WEEKLY -> BillingPackageType.WEEKLY
    }

internal fun StoreTransaction.toBillingPurchase(customerInfo: CustomerInfo): BillingPurchase =
    BillingPurchase(
        productId = productIds.firstOrNull().orEmpty(),
        transactionId = transactionIdentifier,
        customerInfo = customerInfo.toBillingCustomerInfo(),
    )
```

Adjust property names to match the installed `purchases-kmp-core` version if the compiler reports mismatches (RC renames are common across 2.x → 3.x).

- [ ] **Step 2: Implement RevenueCatBillingProvider**

Use `Purchases.sharedInstance` from `com.revenuecat.purchases.kmp`. Wrap callback APIs in `suspendCancellableCoroutine` where suspend helpers are unavailable.

```kotlin
// billing/src/androidMain/kotlin/com/devindie/cmptemplate/billing/impl/provider/RevenueCatBillingProvider.kt
package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.mapper.toBillingCustomerInfo
import com.devindie.cmptemplate.billing.impl.mapper.toBillingOfferings
import com.devindie.cmptemplate.billing.impl.mapper.toBillingPurchase
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesException
import com.revenuecat.purchases.kmp.models.CustomerInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class RevenueCatBillingProvider : BillingProvider {
    private val purchases: Purchases
        get() = Purchases.sharedInstance

    override suspend fun initialize(): BillingResult<Unit> =
        getCustomerInfo().map { }

    override suspend fun getOfferings() =
        suspendCancellableCoroutine { cont ->
            purchases.getOfferings(
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { cont.resume(BillingResult.Success(it.toBillingOfferings())) },
            )
        }

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> {
        val offeringsResult = getOfferings()
        if (offeringsResult is BillingResult.Failure) return offeringsResult
        val offerings = (offeringsResult as BillingResult.Success).value
        val pkg =
            offerings.current?.packages?.find { it.identifier == packageId }
                ?: offerings.all.values
                    .flatMap { it.packages }
                    .find { it.identifier == packageId }
                ?: return BillingResult.Failure(
                    BillingError.StoreError("Package not found: $packageId"),
                )
        // Resolve native Package from offerings — re-fetch and find by identifier
        return suspendCancellableCoroutine { cont ->
            purchases.getOfferings(
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { rcOfferings ->
                    val rcPackage =
                        rcOfferings.all.values
                            .flatMap { it.availablePackages }
                            .find { it.identifier == packageId }
                    if (rcPackage == null) {
                        cont.resume(
                            BillingResult.Failure(
                                BillingError.StoreError("Package not found: $packageId"),
                            ),
                        )
                        return@getOfferings
                    }
                    purchases.purchase(
                        packageToPurchase = rcPackage,
                        onError = { error, userCancelled ->
                            cont.resume(
                                if (userCancelled) {
                                    BillingResult.Failure(BillingError.UserCancelled)
                                } else {
                                    BillingResult.Failure(error.toBillingError())
                                },
                            )
                        },
                        onSuccess = { transaction, customerInfo ->
                            cont.resume(
                                BillingResult.Success(transaction.toBillingPurchase(customerInfo)),
                            )
                        },
                    )
                },
            )
        }
    }

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { cont ->
            purchases.restorePurchases(
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { cont.resume(BillingResult.Success(it.toBillingCustomerInfo())) },
            )
        }

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> =
        callbackFlow {
            val listener: (CustomerInfo) -> Unit = { info ->
                trySend(info.toBillingCustomerInfo())
            }
            purchases.addCustomerInfoUpdateListener(listener)
            awaitClose { purchases.removeCustomerInfoUpdateListener(listener) }
        }

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { cont ->
            purchases.logIn(
                newAppUserID = appUserId,
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { _, info -> cont.resume(BillingResult.Success(info.toBillingCustomerInfo())) },
            )
        }

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { cont ->
            purchases.logOut(
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { cont.resume(BillingResult.Success(it.toBillingCustomerInfo())) },
            )
        }

    private suspend fun getCustomerInfo(): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            purchases.getCustomerInfo(
                onError = { cont.resume(BillingResult.Failure(it.toBillingError())) },
                onSuccess = { cont.resume(BillingResult.Success(it)) },
            )
        }

    private fun PurchasesException.toBillingError(): BillingError =
        BillingError.StoreError(message ?: "purchases_error", code?.code)
}

private inline fun <T, R> BillingResult<T>.map(transform: (T) -> R): BillingResult<R> =
    when (this) {
        is BillingResult.Success -> BillingResult.Success(transform(value))
        is BillingResult.Failure -> this
    }
```

Copy both files to `iosMain` with the same package paths.

- [ ] **Step 3: Compile Android + iOS**

```bash
./gradlew :billing:compileDebugKotlinAndroid :billing:compileKotlinIosSimulatorArm64
```

Fix any RC API renames until both pass.

- [ ] **Step 4: Run JVM tests still pass**

```bash
./gradlew :billing:allTests
```

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(billing): add RevenueCatBillingProvider for Android and iOS"
```

---

### Task 8: Architecture test

**Files:**
- Modify: `architecture/src/test/kotlin/com/devindie/cmptemplate/architecture/layer/LayerDependencyTest.kt`

- [ ] **Step 1: Add domain/data billing import guards**

```kotlin
@Test
fun `domain layer does not import billing`() {
    Konsist.scopeFromPackage("com.devindie.cmptemplate.domain..")
        .files
        .assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.billing.")
            }
        }
}

@Test
fun `data layer does not import billing`() {
    Konsist.scopeFromPackage("com.devindie.cmptemplate.data..")
        .files
        .assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.devindie.cmptemplate.billing.")
            }
        }
}
```

- [ ] **Step 2: Run architecture tests**

```bash
./gradlew :architecture:test
```

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git commit -m "test(architecture): enforce domain and data stay billing-free"
```

---

### Task 9: README.md (Android + iOS integration guide)

**Files:**
- Create: `billing/README.md`

- [ ] **Step 1: Write README** — mirror `analytics/README.md` sections:

Required sections:

1. **What you get** — table mapping capabilities → `BillingClient` API  
2. **Prerequisites** — RevenueCat account, Play Console + App Store Connect products, entitlements/offerings  
3. **Step 1 — Gradle** — `include(":billing")`, `implementation(projects.billing)` on `:shared` + `:androidApp` when needed, version catalog  
4. **Step 2 — Android setup**
   - `Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())` in `Application.onCreate()` **before** `startKoinApp`
   - API key from `local.properties` → `BuildConfig` (document pattern; do not commit secrets)
   - `billingFeatureModule(BillingConfig(enabled = true, revenueCatApiKeyAndroid = …))`
   - `androidContext(...)` required for Koin if other modules need it  
5. **Step 3 — iOS setup** (explicit)
   - Register iOS app in RevenueCat; use **iOS public API key** (different from Android)
   - **SDK 3.x:** `purchases-kmp-core` in Gradle handles native iOS linking — no manual CocoaPods/SPM for `PurchasesHybridCommon` (per [RC KMP 3.0 blog](https://www.revenuecat.com/blog/engineering/kmp-sdk-3/))
   - Configure in Kotlin from `iosMain` app entry **or** Swift before Koin:

```kotlin
// shared/src/iosMain/kotlin/.../BillingIosInit.kt (host adds when enabling billing)
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

fun configureBilling(apiKey: String) {
    Purchases.configure(PurchasesConfiguration(apiKey))
}
```

Call from `iOSApp.init()` via `BillingIosKt.configureBilling(apiKey: "...")` **before** `doInitKoin()`.

   - `billingFeatureModule(BillingConfig(enabled = true, revenueCatApiKeyIos = …))` in `KoinIos.kt`
   - If iOS link errors after dependency changes, run:

```bash
XCODEPROJ_PATH="$(realpath iosApp/iosApp.xcodeproj)" \
GRADLE_PROJECT_PATH=":billing" \
./gradlew :billing:integrateLinkagePackage
```

   - Sandbox testing: StoreKit sandbox Apple ID; verify entitlement in RevenueCat customer viewer  
6. **Step 4 — Use in app code** — ViewModel injection example (from spec)  
7. **Step 5 — Verify setup**

```bash
./gradlew :billing:allTests
./gradlew :billing:compileKotlinIosSimulatorArm64
./gradlew :androidApp:assembleDebug   # after wiring deps
```

Manual: sandbox purchase Android (license tester) + iOS (sandbox account); restore; `enabled = false` builds without keys.

8. **Setup checklist** — checkbox list for Android + iOS  
9. **Module layout** — `api/` vs `impl/` tree  
10. **Troubleshooting** — table (not configured, package not found, iOS link errors, user cancelled)

- [ ] **Step 2: Commit**

```bash
git commit -m "docs(billing): add integration README with iOS setup"
```

---

### Task 10: Final verification

- [ ] **Step 1: Full quality gate**

```bash
./gradlew :billing:allTests
./gradlew :billing:compileKotlinIosSimulatorArm64
./gradlew :architecture:test
./gradlew qualityCheck
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Spotless / detekt on billing**

```bash
./gradlew :billing:detekt
```

Fix any findings.

- [ ] **Step 3: Confirm optional default**

Verify **unchanged**:
- `shared/build.gradle.kts` has **no** `projects.billing` dependency
- `CmpTemplateApplication.kt` / `KoinIos.kt` have **no** `billingFeatureModule`
- App still builds: `./gradlew :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`

- [ ] **Step 4: Commit if fixes needed**

```bash
git commit -m "chore(billing): verification fixes"
```

---

## Plan self-review (spec coverage)

| Spec requirement | Task |
|------------------|------|
| `:billing` Gradle module | Task 1 |
| `api` / `impl` layout | Tasks 2–7, 9 |
| `BillingClient` facade | Tasks 3, 5 |
| `BillingProvider` + NoOp | Tasks 4, 6 |
| RevenueCat default Android/iOS | Task 7 |
| `BillingConfig(enabled = false)` | Task 6 |
| Optional — no shared dep by default | Tasks 1, 10 |
| iOS compile + configure in README | Tasks 7, 9 |
| Domain/data isolation | Task 8 |
| Unit tests | Tasks 2, 4, 5 |
| README mirroring analytics | Task 9 |

No TBD placeholders. RC property names may need adjustment at compile time (Task 7 Step 3) — documented explicitly.

---

## Execution handoff

**Plan saved to `docs/superpowers/plans/2026-06-26-billing-module.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
2. **Inline Execution** — implement tasks in this session with checkpoints

Which approach do you want?
