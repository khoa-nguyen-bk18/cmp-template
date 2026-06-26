# Splash Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a branded splash screen with a 1-second minimum display and an extensible app-init gate (`InitializeAppUseCase`), wired as `feature/splash` in `:shared` with native launch screens on Android and iOS.

**Architecture:** Domain `AppStartupRepository` + `InitializeAppUseCase`; data `AppStartupRepositoryImpl` probes `BrowseCardDao.count()`; presentation `SplashViewModel` runs init and minimum delay in parallel; root `NavHost` in `App.kt` navigates `SplashRoute` → `MainShellRoute`.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose Multiplatform, Navigation Compose (type-safe routes), Koin, Room, Android SplashScreen API (`androidx.core:core-splashscreen`), iOS LaunchScreen storyboard.

**Spec:** [`docs/superpowers/specs/2026-06-24-splash-screen-design.md`](../specs/2026-06-24-splash-screen-design.md)

---

## File map

| File | Responsibility |
|------|----------------|
| `domain/.../repository/AppStartupRepository.kt` | Startup readiness contract |
| `domain/.../usecase/startup/InitializeAppUseCase.kt` | Delegates to repository |
| `domain/.../fake/FakeAppStartupRepository.kt` | Test double |
| `data/.../source/startup/AppStartupRepositoryImpl.kt` | DB probe via DAO |
| `shared/.../core/navigation/MainShellRoute.kt` | Root nav destination wrapping main shell |
| `shared/.../feature/splash/api/SplashNavigation.kt` | `SplashRoute` + nav graph extension |
| `shared/.../feature/splash/api/SplashScreen.kt` | State-holder + stateless preview entry |
| `shared/.../feature/splash/api/SplashFeatureModule.kt` | Koin ViewModel binding |
| `shared/.../feature/splash/impl/SplashScreenUiState.kt` | UI state + `SplashPhase` |
| `shared/.../feature/splash/impl/SplashViewModel.kt` | Init orchestration |
| `shared/.../feature/splash/impl/SplashContent.kt` | Branded layout composable |
| `shared/.../App.kt` | Root `NavHost` |
| `androidApp/.../themes.xml` | Splash + post-splash themes |
| `iosApp/.../LaunchScreen.storyboard` | iOS native launch screen |

---

### Task 1: Domain — `AppStartupRepository` + fake + use case tests

**Files:**
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/AppStartupRepository.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeAppStartupRepository.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCase.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCaseTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.startup

import com.devindie.cmptemplate.domain.fake.FakeAppStartupRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class InitializeAppUseCaseTest {
    @Test
    fun invoke_returnsSuccessFromRepository() = runTest {
        val repository = FakeAppStartupRepository(result = Result.success(Unit))
        val useCase = InitializeAppUseCase(repository)

        assertTrue(useCase().isSuccess)
    }

    @Test
    fun invoke_returnsFailureFromRepository() = runTest {
        val repository = FakeAppStartupRepository(result = Result.failure(IllegalStateException("db down")))
        val useCase = InitializeAppUseCase(repository)

        assertTrue(useCase().isFailure)
    }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeAppStartupRepository.kt
package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.repository.AppStartupRepository

class FakeAppStartupRepository(
    private var result: Result<Unit> = Result.success(Unit),
    var ensureReadyCallCount: Int = 0,
) : AppStartupRepository {
    override suspend fun ensureReady(): Result<Unit> {
        ensureReadyCallCount++
        return result
    }

    fun setResult(result: Result<Unit>) {
        this.result = result
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCaseTest"`

Expected: FAIL — `InitializeAppUseCase` / `AppStartupRepository` not found

- [ ] **Step 3: Write minimal implementation**

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/AppStartupRepository.kt
package com.devindie.cmptemplate.domain.repository

interface AppStartupRepository {
    suspend fun ensureReady(): Result<Unit>
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCase.kt
package com.devindie.cmptemplate.domain.usecase.startup

import com.devindie.cmptemplate.domain.repository.AppStartupRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class InitializeAppUseCase(
    private val repository: AppStartupRepository,
) : UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.ensureReady()
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCaseTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Register use case in DI**

Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

Add import and registration:

```kotlin
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase

// inside module { ... }
factoryOf(::InitializeAppUseCase)
```

- [ ] **Step 6: Commit**

```bash
git add domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/AppStartupRepository.kt \
  domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCase.kt \
  domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeAppStartupRepository.kt \
  domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/startup/InitializeAppUseCaseTest.kt \
  shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(splash): add AppStartupRepository and InitializeAppUseCase"
```

---

### Task 2: Data — `AppStartupRepositoryImpl` + DI + tests

**Files:**
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImpl.kt`
- Create: `data/src/commonTest/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImplTest.kt`
- Modify: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt`
- Modify: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// data/src/commonTest/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImplTest.kt
package com.devindie.cmptemplate.data.source.startup

import com.devindie.cmptemplate.data.local.browse.fake.FakeBrowseCardDao
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AppStartupRepositoryImplTest {
    @Test
    fun ensureReady_succeedsWhenDaoProbeSucceeds() = runTest {
        val repository = AppStartupRepositoryImpl(browseCardDao = FakeBrowseCardDao())

        assertTrue(repository.ensureReady().isSuccess)
    }

    @Test
    fun ensureReady_returnsFailureWhenDaoThrows() = runTest {
        val dao = FakeBrowseCardDao()
        dao.countThrows = IllegalStateException("db unavailable")
        val repository = AppStartupRepositoryImpl(browseCardDao = dao)

        val result = repository.ensureReady()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
```

Extend `FakeBrowseCardDao` if needed:

```kotlin
// Add to data/src/commonTest/kotlin/com/devindie/cmptemplate/data/local/browse/fake/FakeBrowseCardDao.kt
var countThrows: Throwable? = null

// In count() suspend fun:
countThrows?.let { throw it }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.source.startup.AppStartupRepositoryImplTest"`

Expected: FAIL — `AppStartupRepositoryImpl` not found

- [ ] **Step 3: Write minimal implementation**

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImpl.kt
package com.devindie.cmptemplate.data.source.startup

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardDao
import com.devindie.cmptemplate.domain.repository.AppStartupRepository

class AppStartupRepositoryImpl(
    private val browseCardDao: BrowseCardDao,
) : AppStartupRepository {
    override suspend fun ensureReady(): Result<Unit> =
        runCatching {
            browseCardDao.count()
        }
}
```

- [ ] **Step 4: Bind in platform DI (both platforms)**

Add to **both** `PlatformDataModule.android.kt` and `PlatformDataModule.ios.kt`:

```kotlin
import com.devindie.cmptemplate.data.source.startup.AppStartupRepositoryImpl
import com.devindie.cmptemplate.domain.repository.AppStartupRepository

single<AppStartupRepository> {
    AppStartupRepositoryImpl(browseCardDao = get())
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.source.startup.AppStartupRepositoryImplTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add data/src/commonMain/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImpl.kt \
  data/src/commonTest/kotlin/com/devindie/cmptemplate/data/source/startup/AppStartupRepositoryImplTest.kt \
  data/src/commonTest/kotlin/com/devindie/cmptemplate/data/local/browse/fake/FakeBrowseCardDao.kt \
  data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt \
  data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt
git commit -m "feat(splash): add AppStartupRepositoryImpl with DB probe"
```

---

### Task 3: Presentation — `SplashViewModel` + tests

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashScreenUiState.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModel.kt`
- Create: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModelTest.kt`

- [ ] **Step 1: Write UI state**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashScreenUiState.kt
package com.devindie.cmptemplate.feature.splash.impl

data class SplashScreenUiState(
    val phase: SplashPhase = SplashPhase.Loading,
    val errorMessage: String? = null,
    val isStartupComplete: Boolean = false,
)

enum class SplashPhase {
    Loading,
    Error,
}
```

`isStartupComplete` signals the composable to call `onNavigateToMain()` — not navigation state itself.

- [ ] **Step 2: Write the failing tests**

```kotlin
// shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModelTest.kt
package com.devindie.cmptemplate.feature.splash.impl

import com.devindie.cmptemplate.domain.fake.FakeAppStartupRepository
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @Test
    fun startup_marksCompleteAfterOneSecondWhenInitIsFast() = runViewModelTest {
        val repository = FakeAppStartupRepository(result = Result.success(Unit))
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)

        assertTrue(viewModel.uiState.value.isStartupComplete)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }

    @Test
    fun startup_waitsForSlowInit() = runViewModelTest {
        val repository = FakeAppStartupRepository(
            result = Result.success(Unit),
            initDelayMs = 2_000L,
        )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        assertFalse(viewModel.uiState.value.isStartupComplete)

        advanceTimeBy(1_000L)
        assertTrue(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun startup_showsErrorAfterMinimumDisplayWhenInitFails() = runViewModelTest {
        val repository = FakeAppStartupRepository(
            result = Result.failure(IllegalStateException("db down")),
        )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)

        assertEquals(SplashPhase.Error, viewModel.uiState.value.phase)
        assertEquals("db down", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun retry_recoversAfterFailure() = runViewModelTest {
        val repository = FakeAppStartupRepository(
            result = Result.failure(IllegalStateException("db down")),
        )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)

        repository.setResult(Result.success(Unit))
        viewModel.onRetryClick()
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)

        assertTrue(viewModel.uiState.value.isStartupComplete)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }
}
```

Update fake to support init delay:

```kotlin
// Add to FakeAppStartupRepository
var initDelayMs: Long = 0L

override suspend fun ensureReady(): Result<Unit> {
    ensureReadyCallCount++
    if (initDelayMs > 0) kotlinx.coroutines.delay(initDelayMs)
    return result
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.splash.impl.SplashViewModelTest"`

Expected: FAIL — `SplashViewModel` not found

- [ ] **Step 4: Write minimal implementation**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashViewModel.kt
package com.devindie.cmptemplate.feature.splash.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val initializeApp: InitializeAppUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashScreenUiState())
    val uiState: StateFlow<SplashScreenUiState> = _uiState.asStateFlow()

    init {
        runStartup()
    }

    fun onRetryClick() {
        runStartup()
    }

    private fun runStartup() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = SplashPhase.Loading,
                    errorMessage = null,
                    isStartupComplete = false,
                )
            }
            val result =
                coroutineScope {
                    val initDeferred = async { initializeApp() }
                    val minDisplayDeferred = async { delay(MIN_DISPLAY_MS) }
                    minDisplayDeferred.await()
                    initDeferred.await()
                }
            result
                .onSuccess {
                    _uiState.update { it.copy(isStartupComplete = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = SplashPhase.Error,
                            errorMessage = error.message ?: "Unable to start the app",
                        )
                    }
                }
        }
    }

    companion object {
        const val MIN_DISPLAY_MS: Long = 1_000L
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :shared:cleanJvmTest :shared:jvmTest --tests "com.devindie.cmptemplate.feature.splash.impl.SplashViewModelTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/ \
  shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/splash/impl/ \
  domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeAppStartupRepository.kt
git commit -m "feat(splash): add SplashViewModel with min display and retry"
```

---

### Task 4: Presentation — Splash UI + navigation + feature module

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashContent.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashNavigation.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashFeatureModule.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/navigation/MainShellRoute.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Create `MainShellRoute`**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/navigation/MainShellRoute.kt
package com.devindie.cmptemplate.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object MainShellRoute
```

- [ ] **Step 2: Create splash navigation**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashNavigation.kt
package com.devindie.cmptemplate.feature.splash.api

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute
```

- [ ] **Step 3: Create `SplashContent` (branded layout)**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/impl/SplashContent.kt
package com.devindie.cmptemplate.feature.splash.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding

@Composable
internal fun SplashContent(
    state: SplashScreenUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .appStatusBarsPadding()
                .appNavigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "CM",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = "CMPTemplate",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            when (state.phase) {
                SplashPhase.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
                SplashPhase.Error -> {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create `SplashScreen` entry points**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashScreen.kt
package com.devindie.cmptemplate.feature.splash.api

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
import com.devindie.cmptemplate.feature.splash.impl.SplashContent
import com.devindie.cmptemplate.feature.splash.impl.SplashPhase
import com.devindie.cmptemplate.feature.splash.impl.SplashScreenUiState
import com.devindie.cmptemplate.feature.splash.impl.SplashViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isStartupComplete) {
        if (state.isStartupComplete) {
            onNavigateToMain()
        }
    }

    SplashScreen(
        state = state,
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Composable
fun SplashScreen(
    state: SplashScreenUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SplashContent(
        state = state,
        onRetryClick = onRetryClick,
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    )
}

@Preview
@Composable
private fun SplashScreenLoadingPreview() {
    AppTheme {
        SplashScreen(state = SplashScreenUiState(), onRetryClick = {})
    }
}

@Preview
@Composable
private fun SplashScreenErrorPreview() {
    AppTheme {
        SplashScreen(
            state =
                SplashScreenUiState(
                    phase = SplashPhase.Error,
                    errorMessage = "Unable to start the app",
                ),
            onRetryClick = {},
        )
    }
}
```

- [ ] **Step 5: Create feature module and register**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/api/SplashFeatureModule.kt
package com.devindie.cmptemplate.feature.splash.api

import com.devindie.cmptemplate.feature.splash.impl.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val splashFeatureModule =
    module {
        viewModelOf(::SplashViewModel)
    }
```

In `AppDomainModule.kt`:

```kotlin
import com.devindie.cmptemplate.feature.splash.api.splashFeatureModule

includes(
    // existing modules...
    splashFeatureModule,
)
```

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/splash/ \
  shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/navigation/MainShellRoute.kt \
  shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(splash): add SplashScreen UI and feature module"
```

---

### Task 5: App entry — root `NavHost` in `App.kt`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt`

- [ ] **Step 1: Replace direct `MainScreen()` with root navigation**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt
package com.devindie.cmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devindie.cmptemplate.core.navigation.MainShellRoute
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.main.api.MainScreen
import com.devindie.cmptemplate.feature.splash.api.SplashRoute
import com.devindie.cmptemplate.feature.splash.api.SplashScreen

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = modifier,
        ) {
            composable<SplashRoute> {
                SplashScreen(
                    onNavigateToMain = {
                        navController.navigate(MainShellRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<MainShellRoute> {
                MainScreen()
            }
        }
    }
}
```

- [ ] **Step 2: Build shared module**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/App.kt
git commit -m "feat(splash): wire root NavHost SplashRoute to MainShellRoute"
```

---

### Task 6: Android — native splash theme + SplashScreen API

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `androidApp/build.gradle.kts`
- Create: `androidApp/src/main/res/values/themes.xml`
- Create: `androidApp/src/main/res/values-night/themes.xml`
- Modify: `androidApp/src/main/AndroidManifest.xml`
- Modify: `androidApp/src/main/kotlin/com/devindie/cmptemplate/MainActivity.kt`

- [ ] **Step 1: Add dependency to version catalog**

In `gradle/libs.versions.toml`:

```toml
[versions]
androidx-core-splashscreen = "1.0.1"

[libraries]
androidx-core-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "androidx-core-splashscreen" }
```

- [ ] **Step 2: Add dependency to androidApp**

In `androidApp/build.gradle.kts` `dependencies { }`:

```kotlin
implementation(libs.androidx.core.splashscreen)
```

- [ ] **Step 3: Create themes**

```xml
<!-- androidApp/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.CmpTemplate" parent="android:Theme.Material.Light.NoActionBar" />

    <style name="Theme.CmpTemplate.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="postSplashScreenTheme">@style/Theme.CmpTemplate</item>
    </style>

    <color name="splash_background">#FFF9F9FF</color>
</resources>
```

```xml
<!-- androidApp/src/main/res/values-night/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="splash_background">#FF273143</color>
</resources>
```

- [ ] **Step 4: Update manifest**

In `AndroidManifest.xml`, change application theme and activity theme:

```xml
<application
    android:theme="@style/Theme.CmpTemplate"
    ...>
    <activity
        android:name=".MainActivity"
        android:theme="@style/Theme.CmpTemplate.Splash"
        ...>
```

- [ ] **Step 5: Install splash screen in MainActivity**

```kotlin
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // ...
    }
}
```

- [ ] **Step 6: Build androidApp**

Run: `./gradlew :androidApp:assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml androidApp/
git commit -m "feat(splash): add Android SplashScreen API and matching themes"
```

---

### Task 7: iOS — Launch Screen

**Files:**
- Create: `iosApp/iosApp/LaunchScreen.storyboard`
- Modify: `iosApp/iosApp/Info.plist`
- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj`

- [ ] **Step 1: Create LaunchScreen.storyboard**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document type="com.apple.InterfaceBuilder3.CocoaTouch.Storyboard.XIB" version="3.0" toolsVersion="22505" targetRuntime="iOS.CocoaTouch" propertyAccessControl="none" useAutolayout="YES" launchScreen="YES" useTraitCollections="YES" useSafeAreas="YES" colorMatched="YES" initialViewController="01J-lp-oVM">
    <scenes>
        <scene sceneID="EHf-IW-A2E">
            <objects>
                <viewController id="01J-lp-oVM" sceneMemberID="viewController">
                    <view key="view" contentMode="scaleToFill" id="Ze5-6b-2t3">
                        <rect key="frame" x="0.0" y="0.0" width="393" height="852"/>
                        <autoresizingMask key="autoresizingMask" widthSizable="YES" heightSizable="YES"/>
                        <color key="backgroundColor" red="0.976" green="0.976" blue="1.0" alpha="1" colorSpace="custom" customColorSpace="sRGB"/>
                    </view>
                </viewController>
                <placeholder placeholderIdentifier="IBFirstResponder" id="iYj-Kq-Ea1" userLabel="First Responder" sceneMemberID="firstResponder"/>
            </objects>
            <point key="canvasLocation" x="53" y="375"/>
        </scene>
    </scenes>
</document>
```

Background `#F9F9FF` = RGB(0.976, 0.976, 1.0).

- [ ] **Step 2: Register in Info.plist**

Add inside `<dict>`:

```xml
<key>UILaunchStoryboardName</key>
<string>LaunchScreen</string>
```

- [ ] **Step 3: Add storyboard to Xcode project**

In `iosApp/iosApp.xcodeproj/project.pbxproj`:
- Add `LaunchScreen.storyboard` to `PBXFileReference` and `PBXBuildFile`
- Include in the `iosApp` target's `PBXResourcesBuildPhase`

- [ ] **Step 4: Commit**

```bash
git add iosApp/
git commit -m "feat(splash): add iOS LaunchScreen storyboard"
```

---

### Task 8: Final verification

- [ ] **Step 1: Run all unit tests**

```bash
./gradlew :domain:allTests :data:allTests :shared:allTests
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run architecture + quality gate**

```bash
./gradlew :architecture:test qualityCheck
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Manual smoke (Android)**

Install and cold-start the app. Verify: system splash → Compose splash (~1s) → main tabs, no background flash.

- [ ] **Step 4: Manual smoke (iOS)**

Cold-start on simulator. Verify: launch screen → Compose splash → main.

- [ ] **Step 5: Commit any fixups**

```bash
git add -A
git commit -m "chore(splash): verification fixups"
```

---

## Spec coverage checklist

| Spec requirement | Task |
|------------------|------|
| `feature/splash` api/impl | Tasks 3–4 |
| `InitializeAppUseCase` + `AppStartupRepository` | Tasks 1–2 |
| DB probe v1 | Task 2 |
| 1s minimum display | Task 3 |
| Error + retry | Tasks 3–4 |
| Root `SplashRoute` → `MainShellRoute` | Tasks 4–5 |
| Android SplashScreen API | Task 6 |
| iOS LaunchScreen | Task 7 |
| Unit tests (domain, data, shared) | Tasks 1–3, 8 |
| `qualityCheck` / architecture | Task 8 |
