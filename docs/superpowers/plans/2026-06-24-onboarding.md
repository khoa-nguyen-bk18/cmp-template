# Onboarding Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add first-run-only 3-slide onboarding carousel (`feature/onboarding` in `:shared`) with KMP DataStore persistence, wired between splash and main.

**Architecture:** Domain `OnboardingRepository` + `HasCompletedOnboardingUseCase` / `CompleteOnboardingUseCase`; data `OnboardingRepositoryImpl` backed by Jetpack DataStore Preferences (platform file factories in `:data`); splash routes to onboarding or main via `SplashPostStartupDestination`; onboarding completes with **Get Started** on slide 3.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose Multiplatform (`HorizontalPager`), Navigation Compose (type-safe routes), Koin, androidx DataStore Preferences 1.2.1 (KMP).

**Spec:** [`docs/superpowers/specs/2026-06-24-onboarding-design.md`](../specs/2026-06-24-onboarding-design.md)

---

## File map

| File | Responsibility |
|------|----------------|
| `domain/.../repository/OnboardingRepository.kt` | Onboarding completion contract |
| `domain/.../usecase/onboarding/HasCompletedOnboardingUseCase.kt` | Read completion flag |
| `domain/.../usecase/onboarding/CompleteOnboardingUseCase.kt` | Persist completion |
| `domain/.../fake/FakeOnboardingRepository.kt` | Domain test double |
| `data/.../onboarding/OnboardingDataStore.kt` | Shared `DataStoreFactory` helper + file name constant |
| `data/.../onboarding/createOnboardingDataStore.android.kt` | Android `FileStorage` factory |
| `data/.../onboarding/createOnboardingDataStore.ios.kt` | iOS `OkioStorage` factory |
| `data/.../onboarding/OnboardingRepositoryImpl.kt` | DataStore read/write |
| `shared/.../feature/onboarding/api/OnboardingNavigation.kt` | `OnboardingRoute` |
| `shared/.../feature/onboarding/api/OnboardingScreen.kt` | State-holder + stateless preview entry |
| `shared/.../feature/onboarding/api/OnboardingFeatureModule.kt` | Koin ViewModel binding |
| `shared/.../feature/onboarding/impl/OnboardingScreenUiState.kt` | UI state + page model |
| `shared/.../feature/onboarding/impl/OnboardingPages.kt` | Static 3-slide content |
| `shared/.../feature/onboarding/impl/OnboardingViewModel.kt` | Pager index + completion |
| `shared/.../feature/onboarding/impl/OnboardingContent.kt` | Carousel layout |
| `shared/.../fake/FakeOnboardingRepository.kt` | Shared-layer test double |
| `shared/.../App.kt` | Root `NavHost` with onboarding destination |
| `shared/.../feature/splash/impl/SplashScreenUiState.kt` | `SplashPostStartupDestination` |
| `shared/.../feature/splash/impl/SplashViewModel.kt` | Post-startup routing |

---

### Task 1: Domain — `OnboardingRepository` + use cases

**Files:**
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/OnboardingRepository.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/HasCompletedOnboardingUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/CompleteOnboardingUseCase.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeOnboardingRepository.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/HasCompletedOnboardingUseCaseTest.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/CompleteOnboardingUseCaseTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeOnboardingRepository.kt
package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.repository.OnboardingRepository

class FakeOnboardingRepository(
    private var completed: Boolean = false,
) : OnboardingRepository {
    var markCompletedCallCount: Int = 0

    override suspend fun hasCompleted(): Boolean = completed

    override suspend fun markCompleted() {
        markCompletedCallCount++
        completed = true
    }

    fun setCompleted(value: Boolean) {
        completed = value
    }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/HasCompletedOnboardingUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasCompletedOnboardingUseCaseTest {
    @Test
    fun invoke_returnsFalseWhenNotCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = false)
        val useCase = HasCompletedOnboardingUseCase(repository)

        assertFalse(useCase())
    }

    @Test
    fun invoke_returnsTrueWhenCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = true)
        val useCase = HasCompletedOnboardingUseCase(repository)

        assertTrue(useCase())
    }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/CompleteOnboardingUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompleteOnboardingUseCaseTest {
    @Test
    fun invoke_marksOnboardingCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = false)
        val useCase = CompleteOnboardingUseCase(repository)

        useCase()

        assertEquals(1, repository.markCompletedCallCount)
        assertTrue(repository.hasCompleted())
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.onboarding.*"`

Expected: FAIL — `OnboardingRepository` / use cases not found

- [ ] **Step 3: Write minimal implementation**

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/OnboardingRepository.kt
package com.devindie.cmptemplate.domain.repository

interface OnboardingRepository {
    suspend fun hasCompleted(): Boolean

    suspend fun markCompleted()
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/HasCompletedOnboardingUseCase.kt
package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class HasCompletedOnboardingUseCase(
    private val repository: OnboardingRepository,
) : UseCaseNoParams<Boolean> {
    override suspend fun invoke(): Boolean = repository.hasCompleted()
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/CompleteOnboardingUseCase.kt
package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository,
) : UseCaseNoParams<Unit> {
    override suspend fun invoke() {
        repository.markCompleted()
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.onboarding.*"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Register use cases in DI**

Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

Add imports and registrations:

```kotlin
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.onboarding.HasCompletedOnboardingUseCase

// inside module { ... }
factoryOf(::HasCompletedOnboardingUseCase)
factoryOf(::CompleteOnboardingUseCase)
```

- [ ] **Step 6: Commit**

```bash
git add domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/OnboardingRepository.kt \
  domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/ \
  domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeOnboardingRepository.kt \
  domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/onboarding/ \
  shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(domain): add onboarding repository and use cases"
```

---

### Task 2: Data — DataStore dependencies + platform factories + repository

**Files:**
- Modify: `data/build.gradle.kts`
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingDataStore.kt`
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingRepositoryImpl.kt`
- Create: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/onboarding/createOnboardingDataStore.android.kt`
- Create: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/onboarding/createOnboardingDataStore.ios.kt`
- Create: `data/src/commonTest/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingRepositoryImplTest.kt`
- Modify: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt`
- Modify: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt`

- [ ] **Step 1: Add DataStore dependencies**

Modify: `data/build.gradle.kts` — inside `commonMain.dependencies { ... }`:

```kotlin
implementation(libs.androidx.datastore)
implementation(libs.androidx.datastore.preferences)
```

- [ ] **Step 2: Write the failing repository test**

```kotlin
// data/src/commonTest/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingRepositoryImplTest.kt
package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingRepositoryImplTest {
    @Test
    fun hasCompleted_returnsFalseByDefault() = runTest {
        val dataStore = createTestOnboardingDataStore(backgroundScope)
        val repository = OnboardingRepositoryImpl(dataStore)

        assertFalse(repository.hasCompleted())
    }

    @Test
    fun markCompleted_persistsTrue() = runTest {
        val dataStore = createTestOnboardingDataStore(backgroundScope)
        val repository = OnboardingRepositoryImpl(dataStore)

        repository.markCompleted()

        assertTrue(repository.hasCompleted())
    }

    private fun createTestOnboardingDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                "${System.getProperty("java.io.tmpdir")}/onboarding_test_${System.nanoTime()}.preferences_pb".toPath()
            },
        )
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.onboarding.OnboardingRepositoryImplTest"`

Expected: FAIL — `OnboardingRepositoryImpl` not found

- [ ] **Step 4: Implement DataStore helpers and repository**

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingDataStore.kt
package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

internal const val ONBOARDING_DATASTORE_FILE = "onboarding.preferences_pb"

internal fun buildOnboardingDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    DataStoreFactory.create(storage = storage)
```

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/onboarding/OnboardingRepositoryImpl.kt
package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.first

private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

class OnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : OnboardingRepository {
    override suspend fun hasCompleted(): Boolean =
        dataStore.data.first()[ONBOARDING_COMPLETED_KEY] ?: false

    override suspend fun markCompleted() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }
}
```

```kotlin
// data/src/androidMain/kotlin/com/devindie/cmptemplate/data/onboarding/createOnboardingDataStore.android.kt
package com.devindie.cmptemplate.data.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.storage.file.FileStorage

fun createOnboardingDataStore(context: Context): DataStore<Preferences> =
    buildOnboardingDataStore(
        storage =
            FileStorage(
                serializer = PreferencesSerializer,
                produceFile = { context.filesDir.resolve(ONBOARDING_DATASTORE_FILE) },
            ),
    )
```

```kotlin
// data/src/iosMain/kotlin/com/devindie/cmptemplate/data/onboarding/createOnboardingDataStore.ios.kt
package com.devindie.cmptemplate.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.storage.okio.OkioStorage
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createOnboardingDataStore(): DataStore<Preferences> =
    buildOnboardingDataStore(
        storage =
            OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = {
                    val documentDirectory: NSURL? =
                        NSFileManager.defaultManager.URLForDirectory(
                            directory = NSDocumentDirectory,
                            inDomain = NSUserDomainMask,
                            appropriateForURL = null,
                            create = false,
                            error = null,
                        )
                    (requireNotNull(documentDirectory).path + "/$ONBOARDING_DATASTORE_FILE").toPath()
                },
            ),
    )
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.onboarding.OnboardingRepositoryImplTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Wire platform DI**

Modify: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt`

```kotlin
import com.devindie.cmptemplate.data.onboarding.OnboardingRepositoryImpl
import com.devindie.cmptemplate.data.onboarding.createOnboardingDataStore
import com.devindie.cmptemplate.domain.repository.OnboardingRepository

// inside module { ... } after AppStartupRepository binding:
single { createOnboardingDataStore(get<Context>()) }
single<OnboardingRepository> { OnboardingRepositoryImpl(dataStore = get()) }
```

Modify: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt`

```kotlin
import com.devindie.cmptemplate.data.onboarding.OnboardingRepositoryImpl
import com.devindie.cmptemplate.data.onboarding.createOnboardingDataStore
import com.devindie.cmptemplate.domain.repository.OnboardingRepository

// inside module { ... } after AppStartupRepository binding:
single { createOnboardingDataStore() }
single<OnboardingRepository> { OnboardingRepositoryImpl(dataStore = get()) }
```

- [ ] **Step 7: Commit**

```bash
git add data/build.gradle.kts \
  data/src/commonMain/kotlin/com/devindie/cmptemplate/data/onboarding/ \
  data/src/androidMain/kotlin/com/devindie/cmptemplate/data/onboarding/ \
  data/src/iosMain/kotlin/com/devindie/cmptemplate/data/onboarding/ \
  data/src/commonTest/kotlin/com/devindie/cmptemplate/data/onboarding/ \
  data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt \
  data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt
git commit -m "feat(data): persist onboarding completion with KMP DataStore"
```

---

### Task 3: Shared — `OnboardingViewModel` + tests

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingScreenUiState.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingPages.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingViewModel.kt`
- Create: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/fake/FakeOnboardingRepository.kt`
- Create: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingViewModelTest.kt`

- [ ] **Step 1: Write UI state and static pages**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingScreenUiState.kt
package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingScreenUiState(
    val pages: List<OnboardingPage> = OnboardingPages.default,
    val currentPageIndex: Int = 0,
    val isStartupComplete: Boolean = false,
)

data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
)
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingPages.kt
package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.RocketLaunch

internal object OnboardingPages {
    val default =
        listOf(
            OnboardingPage(
                title = "Welcome",
                body = "Discover and collect your favorite cards.",
                icon = Icons.Outlined.Explore,
            ),
            OnboardingPage(
                title = "Browse",
                body = "Swipe through curated collections anytime.",
                icon = Icons.Outlined.Collections,
            ),
            OnboardingPage(
                title = "Get started",
                body = "Your collection is ready — dive in.",
                icon = Icons.Outlined.RocketLaunch,
            ),
        )
}
```

- [ ] **Step 2: Write the failing ViewModel tests**

```kotlin
// shared/src/commonTest/kotlin/com/devindie/cmptemplate/fake/FakeOnboardingRepository.kt
package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.repository.OnboardingRepository

class FakeOnboardingRepository(
    private var completed: Boolean = false,
) : OnboardingRepository {
    var markCompletedCallCount: Int = 0

    override suspend fun hasCompleted(): Boolean = completed

    override suspend fun markCompleted() {
        markCompletedCallCount++
        completed = true
    }
}
```

```kotlin
// shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingViewModelTest.kt
package com.devindie.cmptemplate.feature.onboarding.impl

import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.fake.FakeOnboardingRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {
    @Test
    fun onNextClick_advancesPageIndex() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))

        viewModel.onNextClick()
        assertEquals(1, viewModel.uiState.value.currentPageIndex)

        viewModel.onNextClick()
        assertEquals(2, viewModel.uiState.value.currentPageIndex)
    }

    @Test
    fun onNextClick_doesNotAdvancePastLastPage() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))
        viewModel.onPageChanged(2)

        viewModel.onNextClick()

        assertEquals(2, viewModel.uiState.value.currentPageIndex)
        assertFalse(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun onGetStartedClick_marksCompleteAndNavigates() = runViewModelTest {
        val repository = FakeOnboardingRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        viewModel.onPageChanged(2)

        viewModel.onGetStartedClick()
        advanceMainUntilIdle()

        assertEquals(1, repository.markCompletedCallCount)
        assertTrue(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun onPageChanged_updatesCurrentPageIndex() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))

        viewModel.onPageChanged(1)

        assertEquals(1, viewModel.uiState.value.currentPageIndex)
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.onboarding.impl.OnboardingViewModelTest"`

Expected: FAIL — `OnboardingViewModel` not found

- [ ] **Step 4: Implement ViewModel**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingViewModel.kt
package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingScreenUiState())
    val uiState: StateFlow<OnboardingScreenUiState> = _uiState.asStateFlow()

    fun onPageChanged(pageIndex: Int) {
        _uiState.update { it.copy(currentPageIndex = pageIndex) }
    }

    fun onNextClick() {
        val lastIndex = _uiState.value.pages.lastIndex
        if (_uiState.value.currentPageIndex < lastIndex) {
            _uiState.update { it.copy(currentPageIndex = it.currentPageIndex + 1) }
        }
    }

    fun onGetStartedClick() {
        if (_uiState.value.currentPageIndex != _uiState.value.pages.lastIndex) return
        viewModelScope.launch {
            completeOnboarding()
            _uiState.update { it.copy(isStartupComplete = true) }
        }
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.onboarding.impl.OnboardingViewModelTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/ \
  shared/src/commonTest/kotlin/com/devindie/cmptemplate/fake/FakeOnboardingRepository.kt \
  shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingViewModelTest.kt
git commit -m "feat(onboarding): add ViewModel with pager state and completion"
```

---

### Task 4: Shared — onboarding UI + feature module + navigation route

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingContent.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingNavigation.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingFeatureModule.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Implement carousel content**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/impl/OnboardingContent.kt
package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding

@Composable
internal fun OnboardingContent(
    state: OnboardingScreenUiState,
    onPageChanged: (Int) -> Unit,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPageIndex) { state.pages.size }

    LaunchedEffect(state.currentPageIndex) {
        if (pagerState.currentPage != state.currentPageIndex) {
            pagerState.animateScrollToPage(state.currentPageIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentPageIndex) {
            onPageChanged(pagerState.currentPage)
        }
    }

    val isLastPage = state.currentPageIndex == state.pages.lastIndex

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .appStatusBarsPadding()
                .appNavigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { pageIndex ->
            val page = state.pages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            state.pages.indices.forEach { index ->
                val selected = index == state.currentPageIndex
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                            ),
                )
            }
        }

        Button(
            onClick = if (isLastPage) onGetStartedClick else onNextClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
        ) {
            Text(if (isLastPage) "Get Started" else "Next")
        }
    }
}
```

- [ ] **Step 2: Implement screen entry + route + Koin module**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingNavigation.kt
package com.devindie.cmptemplate.feature.onboarding.api

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingScreen.kt
package com.devindie.cmptemplate.feature.onboarding.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingContent
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingScreenUiState
import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isStartupComplete) {
        if (state.isStartupComplete) {
            onNavigateToMain()
        }
    }

    OnboardingScreen(
        state = state,
        onPageChanged = viewModel::onPageChanged,
        onNextClick = viewModel::onNextClick,
        onGetStartedClick = viewModel::onGetStartedClick,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingScreenUiState,
    onPageChanged: (Int) -> Unit,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingContent(
        state = state,
        onPageChanged = onPageChanged,
        onNextClick = onNextClick,
        onGetStartedClick = onGetStartedClick,
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    )
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            state = OnboardingScreenUiState(),
            onPageChanged = {},
            onNextClick = {},
            onGetStartedClick = {},
        )
    }
}
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/api/OnboardingFeatureModule.kt
package com.devindie.cmptemplate.feature.onboarding.api

import com.devindie.cmptemplate.feature.onboarding.impl.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingFeatureModule =
    module {
        viewModelOf(::OnboardingViewModel)
    }
```

- [ ] **Step 3: Register feature module in DI**

Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

```kotlin
import com.devindie.cmptemplate.feature.onboarding.api.onboardingFeatureModule

// inside includes(...)
onboardingFeatureModule,
```

- [ ] **Step 4: Compile shared module**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`

Expected: BUILD SUCCESSFUL (catches missing pager imports / API issues)

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/onboarding/ \
  shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(onboarding): add carousel UI, route, and feature module"
```

---

### Task 5: Splash — route to onboarding or main

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashScreenUiState.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashScreen.kt`
- Modify: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModelTest.kt`

- [ ] **Step 1: Update failing splash tests**

Add to `SplashViewModelTest.kt`:

```kotlin
import com.devindie.cmptemplate.domain.usecase.onboarding.HasCompletedOnboardingUseCase
import com.devindie.cmptemplate.fake.FakeOnboardingRepository

@Test
fun startup_navigatesToOnboardingWhenNotCompleted() = runViewModelTest {
    val viewModel =
        SplashViewModel(
            initializeApp = InitializeAppUseCase(FakeAppStartupRepository(result = Result.success(Unit))),
            hasCompletedOnboarding = HasCompletedOnboardingUseCase(FakeOnboardingRepository(completed = false)),
        )

    advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
    advanceMainUntilIdle()

    assertEquals(SplashPostStartupDestination.Onboarding, viewModel.uiState.value.postStartupDestination)
}

@Test
fun startup_navigatesToMainWhenOnboardingCompleted() = runViewModelTest {
    val viewModel =
        SplashViewModel(
            initializeApp = InitializeAppUseCase(FakeAppStartupRepository(result = Result.success(Unit))),
            hasCompletedOnboarding = HasCompletedOnboardingUseCase(FakeOnboardingRepository(completed = true)),
        )

    advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
    advanceMainUntilIdle()

    assertEquals(SplashPostStartupDestination.Main, viewModel.uiState.value.postStartupDestination)
}
```

Update existing tests to pass `HasCompletedOnboardingUseCase(FakeOnboardingRepository(completed = true))` so they still expect main navigation. Replace assertions on `isStartupComplete` with `postStartupDestination == SplashPostStartupDestination.Main` where applicable.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.splash.impl.SplashViewModelTest"`

Expected: FAIL — constructor / state shape mismatch

- [ ] **Step 3: Update splash state, ViewModel, and screen**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashScreenUiState.kt
package com.devindie.cmptemplate.feature.splash.impl

data class SplashScreenUiState(
    val phase: SplashPhase = SplashPhase.Loading,
    val errorMessage: String? = null,
    val postStartupDestination: SplashPostStartupDestination? = null,
)

enum class SplashPhase {
    Loading,
    Error,
}

sealed interface SplashPostStartupDestination {
    data object Main : SplashPostStartupDestination

    data object Onboarding : SplashPostStartupDestination
}
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModel.kt
// Add constructor param:
class SplashViewModel(
    private val initializeApp: InitializeAppUseCase,
    private val hasCompletedOnboarding: HasCompletedOnboardingUseCase,
) : ViewModel() {

// In runStartup(), replace isStartupComplete success branch:
.onSuccess {
    val destination =
        if (hasCompletedOnboarding()) {
            SplashPostStartupDestination.Main
        } else {
            SplashPostStartupDestination.Onboarding
        }
    _uiState.update {
        it.copy(postStartupDestination = destination)
    }
}

// In runStartup() loading branch, clear destination:
_uiState.update {
    it.copy(
        phase = SplashPhase.Loading,
        errorMessage = null,
        postStartupDestination = null,
    )
}
```

Add import: `com.devindie.cmptemplate.domain.usecase.onboarding.HasCompletedOnboardingUseCase`

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashScreen.kt
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.postStartupDestination) {
        when (state.postStartupDestination) {
            SplashPostStartupDestination.Main -> onNavigateToMain()
            SplashPostStartupDestination.Onboarding -> onNavigateToOnboarding()
            null -> Unit
        }
    }
    // ... rest unchanged
}
```

Add imports for `SplashPostStartupDestination` from impl package.

- [ ] **Step 4: Run splash tests**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.splash.impl.SplashViewModelTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/ \
  shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModelTest.kt
git commit -m "feat(splash): route to onboarding or main after startup"
```

---

### Task 6: App entry — wire root navigation

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt`

- [ ] **Step 1: Add onboarding destination to root NavHost**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt
import com.devindie.cmptemplate.feature.onboarding.api.OnboardingRoute
import com.devindie.cmptemplate.feature.onboarding.api.OnboardingScreen

// Inside NavHost { ... }
composable<SplashRoute> {
    SplashScreen(
        onNavigateToMain = {
            navController.navigate(MainShellRoute) {
                popUpTo<SplashRoute> { inclusive = true }
            }
        },
        onNavigateToOnboarding = {
            navController.navigate(OnboardingRoute) {
                popUpTo<SplashRoute> { inclusive = true }
            }
        },
    )
}
composable<OnboardingRoute> {
    OnboardingScreen(
        onNavigateToMain = {
            navController.navigate(MainShellRoute) {
                popUpTo<OnboardingRoute> { inclusive = true }
            }
        },
    )
}
```

- [ ] **Step 2: Compile and run unit tests**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest :domain:cleanJvmTest :domain:jvmTest :data:cleanJvmTest :data:jvmTest`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt
git commit -m "feat(app): wire onboarding into root navigation"
```

---

### Task 7: Architecture verification

**Files:** (none — verification only)

- [ ] **Step 1: Run architecture tests**

Run: `./gradlew :architecture:test`

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run full quality check**

Run: `./gradlew qualityCheck`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit (only if hooks auto-formatted files)**

```bash
git status
# If clean, no commit needed. If spotless/detekt changed files:
git add -A && git commit -m "chore: qualityCheck formatting for onboarding feature"
```

---

## Manual verification checklist

| Platform | Check |
|----------|-------|
| Android | Fresh install / clear app data: splash → onboarding → main |
| Android | Relaunch: splash → main (skip onboarding) |
| Android | Swipe between slides; **Next** advances; **Get Started** only on slide 3 |
| iOS | Same flows as Android |

To reset onboarding on Android during dev: **Settings → Apps → CMPTemplate → Clear storage**, or uninstall/reinstall.
