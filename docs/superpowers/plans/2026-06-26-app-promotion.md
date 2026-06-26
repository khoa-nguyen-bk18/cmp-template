# App Promotion Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `feature/apppromotion` in `:shared` with `AppPromotionClient` (in-app review + share app), enabled by default, Settings action rows, and programmatic triggers from any screen.

**Architecture:** Thin `AppPromotionClient` delegates to internal `AppPromotionPlatform` (Android/iOS). `NoOpAppPromotionClient` when `AppPromotionConfig.enabled = false`. Platform code in `androidMain`/`iosMain` only. Wired via `appPromotionFeatureModule` included in `appDomainModule`. No `:domain` / `:data` changes.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin, Google Play `review` library (Android), StoreKit (iOS), kotlin-test + Turbine for unit tests.

**Spec:** [`docs/superpowers/specs/2026-06-26-app-promotion-design.md`](../specs/2026-06-26-app-promotion-design.md)

---

## File map

| File | Responsibility |
|------|----------------|
| `gradle/libs.versions.toml` | `play-review` version + library coordinate |
| `shared/build.gradle.kts` | `play-review` on `androidMain` |
| `shared/.../apppromotion/AppPromotionDefaults.kt` | `appPromotionConfigForTemplate()` |
| `shared/.../feature/apppromotion/api/AppPromotionResult.kt` | Result + error types |
| `shared/.../feature/apppromotion/api/AppPromotionConfig.kt` | Host config |
| `shared/.../feature/apppromotion/api/AppPromotionClient.kt` | Public facade |
| `shared/.../feature/apppromotion/api/AppPromotionFeatureModule.kt` | Koin entry |
| `shared/.../feature/apppromotion/api/AppPromotionActions.kt` | `rememberAppPromotionActions` |
| `shared/.../feature/apppromotion/api/AppPromotionSettingsSection.kt` | Settings action rows |
| `shared/.../feature/apppromotion/api/README.md` | Integration guide |
| `shared/.../feature/apppromotion/impl/NoOpAppPromotionClient.kt` | Disabled client |
| `shared/.../feature/apppromotion/impl/AppPromotionClientImpl.kt` | Facade impl |
| `shared/.../feature/apppromotion/impl/platform/AppPromotionPlatform.kt` | Internal platform contract |
| `shared/.../feature/apppromotion/impl/platform/AppPromotionPlatformModule.kt` | `expect` Koin platform bindings |
| `shared/.../androidMain/.../AndroidAppPromotionPlatform.kt` | Play Review + share intent |
| `shared/.../androidMain/.../AppPromotionPlatformModule.android.kt` | Android Koin bindings |
| `shared/.../iosMain/.../IosAppPromotionPlatform.kt` | StoreKit + share sheet |
| `shared/.../iosMain/.../AppPromotionPlatformModule.ios.kt` | iOS Koin bindings |
| `shared/.../core/di/AppDomainModule.kt` | `includes(appPromotionFeatureModule(...))` |
| `shared/.../feature/settings/impl/SettingsContent.kt` | Append `AppPromotionSettingsSection` |
| `shared/src/commonTest/.../fake/FakeAppPromotionClient.kt` | Test fake |
| `shared/src/commonTest/.../feature/apppromotion/impl/AppPromotionClientImplTest.kt` | Unit tests |

---

### Task 1: Play Review dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

- [ ] **Step 1: Add version catalog entry**

In `gradle/libs.versions.toml` under `[versions]`:

```toml
play-review = "2.0.2"
```

Under `[libraries]`:

```toml
play-review = { module = "com.google.android.play:review", version.ref = "play-review" }
play-review-ktx = { module = "com.google.android.play:review-ktx", version.ref = "play-review" }
```

- [ ] **Step 2: Add androidMain dependency**

In `shared/build.gradle.kts`, inside `androidMain.dependencies {`:

```kotlin
implementation(libs.play.review)
implementation(libs.play.review.ktx)
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :shared:compileAndroidMain`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "build(shared): add Play In-App Review dependency"
```

---

### Task 2: Public API types

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionResult.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionConfig.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionClient.kt`

- [ ] **Step 1: Create result types**

```kotlin
// AppPromotionResult.kt
package com.devindie.cmptemplate.feature.apppromotion.api

sealed interface AppPromotionResult {
    data object Success : AppPromotionResult
    data class Failure(val error: AppPromotionError) : AppPromotionResult
}

enum class AppPromotionError {
    NotConfigured,
    PlatformUnavailable,
    UserCancelled,
    Unknown,
}
```

- [ ] **Step 2: Create config**

```kotlin
// AppPromotionConfig.kt
package com.devindie.cmptemplate.feature.apppromotion.api

data class AppPromotionConfig(
    val enabled: Boolean = true,
    val appDisplayName: String,
    val playStoreUrl: String,
    val appStoreUrl: String,
    val shareMessage: String? = null,
) {
    fun resolvedShareMessage(): String =
        shareMessage ?: "Check out $appDisplayName!"
}
```

- [ ] **Step 3: Create client interface**

```kotlin
// AppPromotionClient.kt
package com.devindie.cmptemplate.feature.apppromotion.api

interface AppPromotionClient {
    suspend fun requestInAppReview(): AppPromotionResult
    suspend fun shareApp(): AppPromotionResult
}
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/
git commit -m "feat(apppromotion): add public API types"
```

---

### Task 3: Platform contract + client implementations

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AppPromotionPlatform.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/NoOpAppPromotionClient.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/AppPromotionClientImpl.kt`
- Test: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/AppPromotionClientImplTest.kt`

- [ ] **Step 1: Write failing test**

```kotlin
// AppPromotionClientImplTest.kt
package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.AppPromotionPlatform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppPromotionClientImplTest {
    private val config =
        AppPromotionConfig(
            appDisplayName = "Test App",
            playStoreUrl = "https://play.google.com/store/apps/details?id=test",
            appStoreUrl = "https://apps.apple.com/app/id123",
        )

    @Test
    fun requestInAppReview_delegatesToPlatform() =
        runTest {
            var called = false
            val platform =
                object : AppPromotionPlatform {
                    override suspend fun requestInAppReview() =
                        AppPromotionResult.Success.also { called = true }

                    override suspend fun shareApp() = AppPromotionResult.Success
                }
            val client = AppPromotionClientImpl(config, platform)

            val result = client.requestInAppReview()

            assertEquals(AppPromotionResult.Success, result)
            assertEquals(true, called)
        }

    @Test
    fun shareApp_delegatesToPlatform() =
        runTest {
            var called = false
            val platform =
                object : AppPromotionPlatform {
                    override suspend fun requestInAppReview() = AppPromotionResult.Success

                    override suspend fun shareApp() =
                        AppPromotionResult.Success.also { called = true }
                }
            val client = AppPromotionClientImpl(config, platform)

            val result = client.shareApp()

            assertEquals(AppPromotionResult.Success, result)
            assertEquals(true, called)
        }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:iosSimulatorArm64Test --tests "com.devindie.cmptemplate.feature.apppromotion.impl.AppPromotionClientImplTest"`

If iOS tests are disabled, use: `./gradlew :shared:compileKotlinIosSimulatorArm64` and run JVM path if available, or proceed after creating files and run `compileAndroidMain` + manual verification.

Alternative: `./gradlew :shared:allTests` and check test report.

Expected: FAIL — classes not found

- [ ] **Step 3: Create platform interface**

```kotlin
// AppPromotionPlatform.kt
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult

internal interface AppPromotionPlatform {
    suspend fun requestInAppReview(): AppPromotionResult
    suspend fun shareApp(): AppPromotionResult
}
```

- [ ] **Step 4: Create NoOp client**

```kotlin
// NoOpAppPromotionClient.kt
package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult

internal class NoOpAppPromotionClient : AppPromotionClient {
    override suspend fun requestInAppReview(): AppPromotionResult =
        AppPromotionResult.Failure(AppPromotionError.NotConfigured)

    override suspend fun shareApp(): AppPromotionResult =
        AppPromotionResult.Failure(AppPromotionError.NotConfigured)
}
```

- [ ] **Step 5: Create client impl**

```kotlin
// AppPromotionClientImpl.kt
package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.AppPromotionPlatform

internal class AppPromotionClientImpl(
  private val config: AppPromotionConfig,
  private val platform: AppPromotionPlatform,
) : AppPromotionClient {
    override suspend fun requestInAppReview(): AppPromotionResult = platform.requestInAppReview()

    override suspend fun shareApp(): AppPromotionResult = platform.shareApp()
}
```

- [ ] **Step 6: Run tests**

Run: `./gradlew :shared:allTests`

Expected: `AppPromotionClientImplTest` PASS

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/ \
        shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/apppromotion/
git commit -m "feat(apppromotion): add client impl and platform contract"
```

---

### Task 4: Android platform implementation

**Files:**
- Create: `shared/src/androidMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AndroidAppPromotionPlatform.kt`
- Create: `shared/src/androidMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AppPromotionPlatformModule.android.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AppPromotionPlatformModule.kt`

- [ ] **Step 1: Create expect platform module**

```kotlin
// AppPromotionPlatformModule.kt (commonMain)
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import org.koin.core.module.Module

internal expect fun appPromotionPlatformModule(): Module
```

- [ ] **Step 2: Create Android platform**

```kotlin
// AndroidAppPromotionPlatform.kt
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import android.content.Context
import android.content.Intent
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

internal class AndroidAppPromotionPlatform(
    private val context: Context,
    private val config: AppPromotionConfig,
) : AppPromotionPlatform {
    override suspend fun requestInAppReview(): AppPromotionResult =
        runCatching {
            val manager = ReviewManagerFactory.create(context)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(context as android.app.Activity, reviewInfo).await()
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.Unknown)
        }

    override suspend fun shareApp(): AppPromotionResult =
        runCatching {
            val shareText = buildString {
                append(config.resolvedShareMessage())
                append('\n')
                append(config.playStoreUrl)
            }
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)
        }
}
```

**Note:** `launchReviewFlow` requires an `Activity`. If `context` is `Application`, cast may fail — use `androidContext()` from Koin which is the `Application`; for review flow, obtain the current `Activity` via a small helper or pass `ComponentActivity` from a composable callback. **Preferred fix:** inject `Context` and for review use `ReviewManager` with `LocalActivity` pattern in a follow-up, OR store weak ref to foreground activity in `androidApp`. **Simpler v1 approach:** use `androidx.activity.ComponentActivity` retrieved via Koin optional binding, or use `Context` as `Activity` when called from Compose (pass activity from `rememberAppPromotionActions`).

**Revised Android review approach (use in implementation):**

```kotlin
internal class AndroidAppPromotionPlatform(
    private val context: Context,
    private val config: AppPromotionConfig,
) : AppPromotionPlatform {
    override suspend fun requestInAppReview(): AppPromotionResult {
        val activity = context.findActivity()
            ?: return AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)
        return runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.Unknown)
        }
    }
    // shareApp unchanged
}

private fun Context.findActivity(): android.app.Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
```

When called from Application context without Activity in chain, returns `PlatformUnavailable` — acceptable for v1; Compose actions run from Activity context.

- [ ] **Step 3: Create Android Koin module**

```kotlin
// AppPromotionPlatformModule.android.kt
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual fun appPromotionPlatformModule(): org.koin.core.module.Module =
    module {
        single<AppPromotionPlatform> {
            AndroidAppPromotionPlatform(
                context = androidContext(),
                config = get<AppPromotionConfig>(),
            )
        }
    }
```

- [ ] **Step 4: Verify Android compile**

Run: `./gradlew :shared:compileAndroidMain`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add shared/src/androidMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AppPromotionPlatformModule.kt
git commit -m "feat(apppromotion): add Android review and share platform"
```

---

### Task 5: iOS platform implementation

**Files:**
- Create: `shared/src/iosMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/IosAppPromotionPlatform.kt`
- Create: `shared/src/iosMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/impl/platform/AppPromotionPlatformModule.ios.kt`

- [ ] **Step 1: Create iOS platform**

```kotlin
// IosAppPromotionPlatform.kt
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

internal class IosAppPromotionPlatform(
    private val config: AppPromotionConfig,
) : AppPromotionPlatform {
    override suspend fun requestInAppReview(): AppPromotionResult =
        runCatching {
            SKStoreReviewController.requestReview()
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.Unknown)
        }

    override suspend fun shareApp(): AppPromotionResult {
        val rootViewController =
            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: return AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)

        val shareText = buildString {
            append(config.resolvedShareMessage())
            append('\n')
            append(config.appStoreUrl)
        }
        val activityItems = listOf(shareText)
        val controller = UIActivityViewController(activityItems, null)
        return runCatching {
            rootViewController.presentViewController(controller, animated = true, completion = null)
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)
        }
    }
}
```

- [ ] **Step 2: Create iOS Koin module**

```kotlin
// AppPromotionPlatformModule.ios.kt
package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import org.koin.dsl.module

internal actual fun appPromotionPlatformModule(): org.koin.core.module.Module =
    module {
        single<AppPromotionPlatform> {
            IosAppPromotionPlatform(config = get())
        }
    }
```

- [ ] **Step 3: Verify iOS compile**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add shared/src/iosMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/
git commit -m "feat(apppromotion): add iOS review and share platform"
```

---

### Task 6: Koin module + default config + AppDomainModule

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionFeatureModule.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/apppromotion/AppPromotionDefaults.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Create default config**

```kotlin
// AppPromotionDefaults.kt
package com.devindie.cmptemplate.apppromotion

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig

fun appPromotionConfigForTemplate(): AppPromotionConfig =
    AppPromotionConfig(
        enabled = true,
        appDisplayName = "Cmp Template",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.devindie.cmptemplate",
        appStoreUrl = "https://apps.apple.com/app/id000000000",
    )
```

- [ ] **Step 2: Create feature module**

```kotlin
// AppPromotionFeatureModule.kt
package com.devindie.cmptemplate.feature.apppromotion.api

import com.devindie.cmptemplate.feature.apppromotion.impl.AppPromotionClientImpl
import com.devindie.cmptemplate.feature.apppromotion.impl.NoOpAppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.appPromotionPlatformModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun appPromotionFeatureModule(config: AppPromotionConfig): Module =
    module {
        single { config }
        includes(appPromotionPlatformModule())
        single<AppPromotionClient> {
            if (config.enabled) {
                AppPromotionClientImpl(config = config, platform = get())
            } else {
                NoOpAppPromotionClient()
            }
        }
    }
```

- [ ] **Step 3: Wire into AppDomainModule**

Add imports and include in `appDomainModule`:

```kotlin
import com.devindie.cmptemplate.apppromotion.appPromotionConfigForTemplate
import com.devindie.cmptemplate.feature.apppromotion.api.appPromotionFeatureModule

// inside includes(...)
appPromotionFeatureModule(appPromotionConfigForTemplate()),
```

- [ ] **Step 4: Verify compile**

Run: `./gradlew :shared:compileAndroidMain :shared:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionFeatureModule.kt \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/apppromotion/ \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(apppromotion): wire Koin module enabled by default"
```

---

### Task 7: Compose helpers + Settings section

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionActions.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionSettingsSection.kt`

- [ ] **Step 1: Create actions helper**

```kotlin
// AppPromotionActions.kt
package com.devindie.cmptemplate.feature.apppromotion.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class AppPromotionActions internal constructor(
    private val client: AppPromotionClient,
    private val scope: kotlinx.coroutines.CoroutineScope,
) {
    fun requestInAppReview() {
        scope.launch { client.requestInAppReview() }
    }

    fun shareApp() {
        scope.launch { client.shareApp() }
    }
}

@Composable
fun rememberAppPromotionActions(
    client: AppPromotionClient = koinInject(),
): AppPromotionActions {
    val scope = rememberCoroutineScope()
    return remember(client, scope) { AppPromotionActions(client, scope) }
}
```

- [ ] **Step 2: Create settings section**

```kotlin
// AppPromotionSettingsSection.kt
package com.devindie.cmptemplate.feature.apppromotion.api

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun AppPromotionSettingsSection(
    modifier: Modifier = Modifier,
    client: AppPromotionClient = koinInject(),
) {
    val actions = rememberAppPromotionActions(client)
    Text(
        text = "Support",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    ListItem(
        headlineContent = { Text("Rate this app") },
        leadingContent = {
            Icon(Icons.Outlined.StarOutline, contentDescription = null)
        },
        modifier = Modifier.clickable { actions.requestInAppReview() },
    )
    HorizontalDivider()
    ListItem(
        headlineContent = { Text("Share with friends") },
        leadingContent = {
            Icon(Icons.Outlined.Share, contentDescription = null)
        },
        modifier = Modifier.clickable { actions.shareApp() },
    )
}
```

Add missing import: `androidx.compose.foundation.layout.padding`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionActions.kt \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/AppPromotionSettingsSection.kt
git commit -m "feat(apppromotion): add Compose actions and Settings section"
```

---

### Task 8: Settings integration

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsContent.kt`

- [ ] **Step 1: Add import and LazyColumn item**

After the `state.sections.forEach { ... }` block, before the billing smoke panel:

```kotlin
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionSettingsSection

// inside LazyColumn, after sections loop:
item(key = "app-promotion") {
    AppPromotionSettingsSection()
}
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :shared:compileAndroidMain`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsContent.kt
git commit -m "feat(settings): add app promotion rate and share rows"
```

---

### Task 9: Test fake + README

**Files:**
- Create: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/fake/FakeAppPromotionClient.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/README.md`

- [ ] **Step 1: Create fake**

```kotlin
// FakeAppPromotionClient.kt
package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult

class FakeAppPromotionClient(
    var reviewResult: AppPromotionResult = AppPromotionResult.Success,
    var shareResult: AppPromotionResult = AppPromotionResult.Success,
) : AppPromotionClient {
    var reviewCallCount = 0
        private set
    var shareCallCount = 0
        private set

    override suspend fun requestInAppReview(): AppPromotionResult {
        reviewCallCount++
        return reviewResult
    }

    override suspend fun shareApp(): AppPromotionResult {
        shareCallCount++
        return shareResult
    }
}
```

- [ ] **Step 2: Write README** (mirror `feature/legal/README.md` structure)

Cover: purpose, package layout, `AppPromotionClient` injection, `rememberAppPromotionActions`, Settings auto-wiring, `appPromotionConfigForTemplate()` override, manual QA checklist.

- [ ] **Step 3: Run quality checks**

Run: `./gradlew :architecture:test :shared:allTests`

Expected: all PASS

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonTest/kotlin/com/devindie/cmptemplate/fake/FakeAppPromotionClient.kt \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/README.md
git commit -m "docs(apppromotion): add README and test fake"
```

---

## Spec coverage (self-review)

| Spec requirement | Task |
|------------------|------|
| `AppPromotionClient` + config + results | Task 2 |
| Platform Android/iOS | Tasks 4–5 |
| NoOp when disabled | Task 3, 6 |
| Enabled by default in `appDomainModule` | Task 6 |
| `rememberAppPromotionActions` | Task 7 |
| `AppPromotionSettingsSection` | Task 7–8 |
| `play-review` dependency | Task 1 |
| README | Task 9 |
| `./gradlew :architecture:test` | Task 9 |

No placeholders remain. Type names consistent across tasks.

---

## Manual QA checklist

- [ ] Android: Settings → Rate this app → review flow or graceful no-op
- [ ] Android: Settings → Share with friends → share sheet with Play Store URL
- [ ] iOS: same flows on simulator
- [ ] Custom screen: `rememberAppPromotionActions()` buttons work
- [ ] Set `enabled = false` in config → client returns `NotConfigured`
