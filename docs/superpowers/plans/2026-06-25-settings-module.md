# Settings Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a definition-driven Settings feature with typed KMP DataStore persistence, externally configured `SettingsCatalog`, and Profile-tab navigation.

**Architecture:** Domain `SettingsRepository` + four use cases; `SettingsCatalog` aggregates `SettingDefinition` lists from features; `SettingsRepositoryImpl` maps `SettingValue` to DataStore Preferences; `shared/feature/settings` renders rows from `ObserveSettingsScreenUseCase`.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose Multiplatform, Navigation Compose (type-safe routes), Koin, androidx DataStore Preferences (KMP).

**Spec:** [`docs/superpowers/specs/2026-06-25-settings-module-design.md`](../specs/2026-06-25-settings-module-design.md)

---

## File map

| File | Responsibility |
|------|----------------|
| `domain/.../model/settings/*.kt` | `SettingKey`, `SettingValue`, definitions, screen models, errors |
| `domain/.../settings/SettingsCatalog.kt` | External catalog contract |
| `domain/.../repository/SettingsRepository.kt` | Persistence contract |
| `domain/.../usecase/settings/*.kt` | Get, observe, update, screen merge |
| `domain/.../fake/FakeSettings*.kt` | Test doubles |
| `data/.../settings/*.kt` | DataStore factories + `SettingsRepositoryImpl` |
| `shared/.../settings/AppSettingsCatalog.kt` | App-level section aggregation |
| `shared/.../feature/browse/api/BrowseSettings.kt` | Example feature keys/definitions |
| `shared/.../feature/settings/api/*` | Route, navigation, screen entry, Koin |
| `shared/.../feature/settings/impl/*` | ViewModel + type-specific rows |
| `shared/.../feature/main/api/MainNavigation.kt` | Profile nested nav → Settings |
| `shared/.../core/di/AppDomainModule.kt` | Use case + feature module registration |
| `androidApp/.../CmpTemplateApplication.kt` | `settingsCatalogModule()` |
| `shared/.../KoinIos.kt` | `settingsCatalogModule()` |

---

### Task 1: Domain models + catalog + repository

**Files:**
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingKey.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingValue.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingOption.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingDefinition.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsSection.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsScreenModel.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsError.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/settings/SettingsCatalog.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/SettingsRepository.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingDefinitionTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingDefinitionTest.kt
package com.devindie.cmptemplate.domain.model.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingDefinitionTest {
    @Test
    fun defaultValue_mapsBooleanDefinition() {
        val definition =
            BooleanSettingDefinition(
                key = SettingKey("test.flag"),
                title = "Flag",
                description = null,
                default = true,
            )

        assertEquals(SettingValue.BooleanValue(true), definition.defaultValue())
    }

    @Test
    fun defaultValue_mapsSingleChoiceDefinition() {
        val definition =
            SingleChoiceSettingDefinition(
                key = SettingKey("test.theme"),
                title = "Theme",
                description = null,
                options = listOf(SettingOption("light", "Light"), SettingOption("dark", "Dark")),
                defaultOptionId = "dark",
            )

        assertEquals(SettingValue.SingleChoiceValue("dark"), definition.defaultValue())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.model.settings.SettingDefinitionTest"`

Expected: FAIL — types / `defaultValue()` not found

- [ ] **Step 3: Write minimal implementation**

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingKey.kt
package com.devindie.cmptemplate.domain.model.settings

@JvmInline
value class SettingKey(val value: String)
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingValue.kt
package com.devindie.cmptemplate.domain.model.settings

sealed interface SettingValue {
    data class BooleanValue(val value: Boolean) : SettingValue

    data class TextValue(val value: String) : SettingValue

    data class IntValue(val value: Int) : SettingValue

    data class LongValue(val value: Long) : SettingValue

    data class DoubleValue(val value: Double) : SettingValue

    data class SingleChoiceValue(val optionId: String) : SettingValue

    data class MultiChoiceValue(val optionIds: Set<String>) : SettingValue
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingOption.kt
package com.devindie.cmptemplate.domain.model.settings

data class SettingOption(val id: String, val label: String)
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingDefinition.kt
package com.devindie.cmptemplate.domain.model.settings

sealed interface SettingDefinition {
    val key: SettingKey
    val title: String
    val description: String?
}

data class BooleanSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Boolean,
) : SettingDefinition

data class TextSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: String,
    val maxLength: Int? = null,
) : SettingDefinition

data class IntSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Int,
    val min: Int? = null,
    val max: Int? = null,
) : SettingDefinition

data class LongSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Long,
    val min: Long? = null,
    val max: Long? = null,
) : SettingDefinition

data class DoubleSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val default: Double,
    val min: Double? = null,
    val max: Double? = null,
) : SettingDefinition

data class SingleChoiceSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val defaultOptionId: String,
) : SettingDefinition

data class MultiChoiceSettingDefinition(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val defaultOptionIds: Set<String>,
) : SettingDefinition

fun SettingDefinition.defaultValue(): SettingValue =
    when (this) {
        is BooleanSettingDefinition -> SettingValue.BooleanValue(default)
        is TextSettingDefinition -> SettingValue.TextValue(default)
        is IntSettingDefinition -> SettingValue.IntValue(default)
        is LongSettingDefinition -> SettingValue.LongValue(default)
        is DoubleSettingDefinition -> SettingValue.DoubleValue(default)
        is SingleChoiceSettingDefinition -> SettingValue.SingleChoiceValue(defaultOptionId)
        is MultiChoiceSettingDefinition -> SettingValue.MultiChoiceValue(defaultOptionIds)
    }
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsSection.kt
package com.devindie.cmptemplate.domain.model.settings

data class SettingsSection(
    val id: String,
    val title: String,
    val definitions: List<SettingDefinition>,
)
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsScreenModel.kt
package com.devindie.cmptemplate.domain.model.settings

data class SettingsScreenModel(val sections: List<SettingsSectionModel>)

data class SettingsSectionModel(
    val id: String,
    val title: String,
    val items: List<SettingsItemModel>,
)

sealed interface SettingsItemModel {
    val key: SettingKey
    val title: String
    val description: String?
}

data class BooleanSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Boolean,
) : SettingsItemModel

data class TextSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: String,
    val maxLength: Int?,
) : SettingsItemModel

data class IntSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Int,
    val min: Int?,
    val max: Int?,
) : SettingsItemModel

data class LongSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Long,
    val min: Long?,
    val max: Long?,
) : SettingsItemModel

data class DoubleSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val value: Double,
    val min: Double?,
    val max: Double?,
) : SettingsItemModel

data class SingleChoiceSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val selectedOptionId: String,
) : SettingsItemModel

data class MultiChoiceSettingsItemModel(
    override val key: SettingKey,
    override val title: String,
    override val description: String?,
    val options: List<SettingOption>,
    val selectedOptionIds: Set<String>,
) : SettingsItemModel
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/SettingsError.kt
package com.devindie.cmptemplate.domain.model.settings

sealed interface SettingsError {
    data object UnknownSettingKey : SettingsError

    data object TypeMismatch : SettingsError

    data class OutOfRange(val message: String) : SettingsError

    data class InvalidChoice(val message: String) : SettingsError

    data class TextTooLong(val maxLength: Int) : SettingsError
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/settings/SettingsCatalog.kt
package com.devindie.cmptemplate.domain.settings

import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingsSection

interface SettingsCatalog {
    val sections: List<SettingsSection>

    fun definition(key: SettingKey): SettingDefinition? =
        sections
            .asSequence()
            .flatMap { it.definitions }
            .firstOrNull { it.key == key }

    fun allDefinitions(): List<SettingDefinition> = sections.flatMap { it.definitions }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/SettingsRepository.kt
package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeValue(key: SettingKey): Flow<SettingValue?>

    suspend fun getValue(key: SettingKey): SettingValue?

    suspend fun setValue(key: SettingKey, value: SettingValue)
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.model.settings.SettingDefinitionTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/model/settings/ \
        domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/settings/ \
        domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/repository/SettingsRepository.kt \
        domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/model/settings/
git commit -m "feat(settings): add domain models and repository contract"
```

---

### Task 2: Domain use cases + fakes + tests

**Files:**
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/GetSettingUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/UpdateSettingUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingsScreenUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/SettingsMapping.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeSettingsRepository.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeSettingsCatalog.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/GetSettingUseCaseTest.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/UpdateSettingUseCaseTest.kt`
- Create: `domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingsScreenUseCaseTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Write fakes + failing tests**

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeSettingsRepository.kt
package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSettingsRepository : SettingsRepository {
    private val values = MutableStateFlow<Map<SettingKey, SettingValue>>(emptyMap())

    override fun observeValue(key: SettingKey): Flow<SettingValue?> =
        values.map { it[key] }

    override suspend fun getValue(key: SettingKey): SettingValue? = values.value[key]

    override suspend fun setValue(key: SettingKey, value: SettingValue) {
        values.update { current -> current + (key to value) }
    }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeSettingsCatalog.kt
package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingsSection
import com.devindie.cmptemplate.domain.model.settings.TextSettingDefinition
import com.devindie.cmptemplate.domain.settings.SettingsCatalog

class FakeSettingsCatalog : SettingsCatalog {
    override val sections =
        listOf(
            SettingsSection(
                id = "general",
                title = "General",
                definitions =
                    listOf(
                        BooleanSettingDefinition(
                            key = SettingKey("general.notifications"),
                            title = "Notifications",
                            description = null,
                            default = true,
                        ),
                        TextSettingDefinition(
                            key = SettingKey("general.nickname"),
                            title = "Nickname",
                            description = null,
                            default = "Player",
                            maxLength = 20,
                        ),
                    ),
            ),
        )
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/GetSettingUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetSettingUseCaseTest {
  @Test
  fun invoke_returnsStoredValueWhenPresent() = runTest {
    val key = SettingKey("general.notifications")
    val repository = FakeSettingsRepository()
    repository.setValue(key, SettingValue.BooleanValue(false))
    val useCase = GetSettingUseCase(repository, FakeSettingsCatalog())

    assertEquals(SettingValue.BooleanValue(false), useCase(key))
  }

  @Test
  fun invoke_returnsCatalogDefaultWhenMissing() = runTest {
    val key = SettingKey("general.notifications")
    val useCase = GetSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

    assertEquals(SettingValue.BooleanValue(true), useCase(key))
  }

  @Test
  fun invoke_returnsNullForUnknownKey() = runTest {
    val useCase = GetSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

    assertNull(useCase(SettingKey("unknown.key")))
  }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/UpdateSettingUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateSettingUseCaseTest {
  @Test
  fun invoke_persistsValidBoolean() = runTest {
    val key = SettingKey("general.notifications")
    val repository = FakeSettingsRepository()
    val useCase = UpdateSettingUseCase(repository, FakeSettingsCatalog())

    val result = useCase(key, SettingValue.BooleanValue(false))

    assertTrue(result.isSuccess)
    assertEquals(SettingValue.BooleanValue(false), repository.getValue(key))
  }

  @Test
  fun invoke_rejectsUnknownKey() = runTest {
    val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

    val result = useCase(SettingKey("missing"), SettingValue.BooleanValue(true))

    assertTrue(result.isFailure)
    assertIs<SettingsError.UnknownSettingKey>(result.exceptionOrNull())
  }

  @Test
  fun invoke_rejectsTypeMismatch() = runTest {
    val key = SettingKey("general.notifications")
    val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

    val result = useCase(key, SettingValue.TextValue("nope"))

    assertTrue(result.isFailure)
    assertIs<SettingsError.TypeMismatch>(result.exceptionOrNull())
  }

  @Test
  fun invoke_rejectsTextTooLong() = runTest {
    val key = SettingKey("general.nickname")
    val useCase = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog())

    val result = useCase(key, SettingValue.TextValue("a".repeat(21)))

    assertTrue(result.isFailure)
    assertIs<SettingsError.TextTooLong>(result.exceptionOrNull())
  }
}
```

```kotlin
// domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingsScreenUseCaseTest.kt
package com.devindie.cmptemplate.domain.usecase.settings

import app.cash.turbine.test
import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.test.runDataTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveSettingsScreenUseCaseTest {
  @Test
  fun invoke_emitsSectionsWithResolvedValues() = runDataTest {
    val repository = FakeSettingsRepository()
    repository.setValue(
      SettingKey("general.notifications"),
      SettingValue.BooleanValue(false),
    )
    val useCase = ObserveSettingsScreenUseCase(repository, FakeSettingsCatalog())

    useCase().test {
      val model = awaitItem()
      val item = model.sections.single().items.first()
      assertIs<BooleanSettingsItemModel>(item)
      assertEquals(false, item.value)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.settings.*"`

Expected: FAIL — use cases not found

- [ ] **Step 3: Write implementation**

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/GetSettingUseCase.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.domain.usecase.UseCase

class GetSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) : UseCase<SettingKey, SettingValue?> {
    override suspend fun invoke(parameters: SettingKey): SettingValue? {
        val definition = catalog.definition(parameters) ?: return null
        return repository.getValue(parameters) ?: definition.defaultValue()
    }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingUseCase.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) : UseCase<SettingKey, Flow<SettingValue?>> {
    override fun invoke(parameters: SettingKey): Flow<SettingValue?> {
        val definition = catalog.definition(parameters) ?: return repository.observeValue(parameters)
        return repository.observeValue(parameters).map { stored ->
            stored ?: definition.defaultValue()
        }
    }
}
```

Note: `UseCase` is `suspend` in this project — for `ObserveSettingUseCase`, use a regular class with `operator fun invoke(key: SettingKey): Flow<SettingValue?>` instead of implementing `UseCase` (Flow return + suspend mismatch). Final shape:

```kotlin
class ObserveSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) {
    operator fun invoke(key: SettingKey): Flow<SettingValue?> { /* as above */ }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/UpdateSettingUseCase.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.*
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog

class UpdateSettingUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) {
    suspend operator fun invoke(key: SettingKey, value: SettingValue): Result<Unit> = runCatching {
        val definition =
            catalog.definition(key)
                ?: throw SettingsError.UnknownSettingKey
        validate(definition, value)
        repository.setValue(key, value)
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error -> Result.failure(error) },
    )

    private fun validate(definition: SettingDefinition, value: SettingValue) {
        when (definition) {
            is BooleanSettingDefinition -> {
                if (value !is SettingValue.BooleanValue) throw SettingsError.TypeMismatch
            }
            is TextSettingDefinition -> {
                if (value !is SettingValue.TextValue) throw SettingsError.TypeMismatch
                definition.maxLength?.let { max ->
                    if (value.value.length > max) throw SettingsError.TextTooLong(max)
                }
            }
            is IntSettingDefinition -> {
                if (value !is SettingValue.IntValue) throw SettingsError.TypeMismatch
                definition.min?.let { min -> if (value.value < min) throw SettingsError.OutOfRange("below min $min") }
                definition.max?.let { max -> if (value.value > max) throw SettingsError.OutOfRange("above max $max") }
            }
            is LongSettingDefinition -> {
                if (value !is SettingValue.LongValue) throw SettingsError.TypeMismatch
                definition.min?.let { min -> if (value.value < min) throw SettingsError.OutOfRange("below min $min") }
                definition.max?.let { max -> if (value.value > max) throw SettingsError.OutOfRange("above max $max") }
            }
            is DoubleSettingDefinition -> {
                if (value !is SettingValue.DoubleValue) throw SettingsError.TypeMismatch
                definition.min?.let { min -> if (value.value < min) throw SettingsError.OutOfRange("below min $min") }
                definition.max?.let { max -> if (value.value > max) throw SettingsError.OutOfRange("above max $max") }
            }
            is SingleChoiceSettingDefinition -> {
                if (value !is SettingValue.SingleChoiceValue) throw SettingsError.TypeMismatch
                if (definition.options.none { it.id == value.optionId }) {
                    throw SettingsError.InvalidChoice(value.optionId)
                }
            }
            is MultiChoiceSettingDefinition -> {
                if (value !is SettingValue.MultiChoiceValue) throw SettingsError.TypeMismatch
                val validIds = definition.options.map { it.id }.toSet()
                if (!validIds.containsAll(value.optionIds)) {
                    throw SettingsError.InvalidChoice(value.optionIds.joinToString())
                }
            }
        }
    }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/SettingsMapping.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.*

internal fun SettingDefinition.toItemModel(value: SettingValue): SettingsItemModel =
    when (this) {
        is BooleanSettingDefinition -> {
            val resolved = (value as SettingValue.BooleanValue).value
            BooleanSettingsItemModel(key, title, description, resolved)
        }
        is TextSettingDefinition -> {
            val resolved = (value as SettingValue.TextValue).value
            TextSettingsItemModel(key, title, description, resolved, maxLength)
        }
        is IntSettingDefinition -> {
            val resolved = (value as SettingValue.IntValue).value
            IntSettingsItemModel(key, title, description, resolved, min, max)
        }
        is LongSettingDefinition -> {
            val resolved = (value as SettingValue.LongValue).value
            LongSettingsItemModel(key, title, description, resolved, min, max)
        }
        is DoubleSettingDefinition -> {
            val resolved = (value as SettingValue.DoubleValue).value
            DoubleSettingsItemModel(key, title, description, resolved, min, max)
        }
        is SingleChoiceSettingDefinition -> {
            val resolved = (value as SettingValue.SingleChoiceValue).optionId
            SingleChoiceSettingsItemModel(key, title, description, options, resolved)
        }
        is MultiChoiceSettingDefinition -> {
            val resolved = (value as SettingValue.MultiChoiceValue).optionIds
            MultiChoiceSettingsItemModel(key, title, description, options, resolved)
        }
    }
```

```kotlin
// domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ObserveSettingsScreenUseCase.kt
package com.devindie.cmptemplate.domain.usecase.settings

import com.devindie.cmptemplate.domain.model.settings.SettingsScreenModel
import com.devindie.cmptemplate.domain.model.settings.SettingsSectionModel
import com.devindie.cmptemplate.domain.model.settings.defaultValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ObserveSettingsScreenUseCase(
    private val repository: SettingsRepository,
    private val catalog: SettingsCatalog,
) : UseCaseNoParams<Flow<SettingsScreenModel>> {
    override fun invoke(): Flow<SettingsScreenModel> {
        val definitions = catalog.allDefinitions()
        if (definitions.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(SettingsScreenModel(sections = emptyList()))
        }
        val flows =
            definitions.map { definition ->
                repository.observeValue(definition.key).map { stored ->
                    definition.key to (stored ?: definition.defaultValue())
                }
            }
        return combine(flows) { entries ->
            val values = entries.toMap()
            SettingsScreenModel(
                sections =
                    catalog.sections.map { section ->
                        SettingsSectionModel(
                            id = section.id,
                            title = section.title,
                            items =
                                section.definitions.map { definition ->
                                    definition.toItemModel(values.getValue(definition.key))
                                },
                        )
                    },
            )
        }
    }
}
```

Note: `UseCaseNoParams` is `suspend fun invoke()` — same issue. Use:

```kotlin
class ObserveSettingsScreenUseCase(...) {
    operator fun invoke(): Flow<SettingsScreenModel> { /* as above */ }
}
```

- [ ] **Step 4: Register in DI**

Add to `AppDomainModule.kt`:

```kotlin
import com.devindie.cmptemplate.domain.usecase.settings.GetSettingUseCase
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingUseCase
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase

factoryOf(::GetSettingUseCase)
factory { ObserveSettingUseCase(get(), get()) }
factory { ObserveSettingsScreenUseCase(get(), get()) }
factory { UpdateSettingUseCase(get(), get()) }
```

- [ ] **Step 5: Run tests**

Run: `./gradlew :domain:cleanJvmTest :domain:jvmTest --tests "com.devindie.cmptemplate.domain.usecase.settings.*"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add domain/src/commonMain/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ \
        domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/fake/FakeSettings*.kt \
        domain/src/commonTest/kotlin/com/devindie/cmptemplate/domain/usecase/settings/ \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt
git commit -m "feat(settings): add domain use cases with validation"
```

---

### Task 3: Data layer — DataStore + repository

**Files:**
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsDataStore.kt`
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsPreferenceKeys.kt`
- Create: `data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsRepositoryImpl.kt`
- Create: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/settings/createSettingsDataStore.android.kt`
- Create: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/settings/createSettingsDataStore.ios.kt`
- Create: `data/src/commonTest/kotlin/com/devindie/cmptemplate/data/settings/SettingsRepositoryImplTest.kt`
- Modify: `data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt`
- Modify: `data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt`

- [ ] **Step 1: Write failing repository test**

```kotlin
// data/src/commonTest/kotlin/com/devindie/cmptemplate/data/settings/SettingsRepositoryImplTest.kt
package com.devindie.cmptemplate.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsRepositoryImplTest {
    @Test
    fun roundTrip_booleanValue() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))
        val key = SettingKey("general.flag")

        repository.setValue(key, SettingValue.BooleanValue(true))

        assertEquals(SettingValue.BooleanValue(true), repository.getValue(key))
        assertEquals(SettingValue.BooleanValue(true), repository.observeValue(key).first())
    }

    @Test
    fun getValue_returnsNullWhenUnset() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))

        assertNull(repository.getValue(SettingKey("missing")))
    }

    @Test
    fun roundTrip_multiChoiceValue() = runTest {
        val repository = SettingsRepositoryImpl(createTestDataStore(backgroundScope))
        val key = SettingKey("general.tags")
        val value = SettingValue.MultiChoiceValue(setOf("a", "b"))

        repository.setValue(key, value)

        assertEquals(value, repository.getValue(key))
    }

    private fun createTestDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                FileSystem.SYSTEM_TEMPORARY_DIRECTORY /
                    "settings_test_${Random.nextLong()}.preferences_pb"
            },
        )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.settings.SettingsRepositoryImplTest"`

Expected: FAIL

- [ ] **Step 3: Implement data layer**

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsDataStore.kt
package com.devindie.cmptemplate.data.settings

internal const val SETTINGS_DATASTORE_FILE = "settings.preferences_pb"
```

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsPreferenceKeys.kt
package com.devindie.cmptemplate.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue

internal fun booleanKey(key: SettingKey) = booleanPreferencesKey(prefName(key))

internal fun stringKey(key: SettingKey) = stringPreferencesKey(prefName(key))

internal fun stringSetKey(key: SettingKey) = stringSetPreferencesKey(prefName(key))

internal fun intKey(key: SettingKey) = intPreferencesKey(prefName(key))

internal fun longKey(key: SettingKey) = longPreferencesKey(prefName(key))

internal fun doubleKey(key: SettingKey) = doublePreferencesKey(prefName(key))

private fun prefName(key: SettingKey): String = "setting_${key.value}"

internal fun readValue(preferences: androidx.datastore.preferences.core.Preferences, key: SettingKey, kind: SettingValue): SettingValue? =
    when (kind) {
        is SettingValue.BooleanValue -> preferences[booleanKey(key)]?.let(SettingValue::BooleanValue)
        is SettingValue.TextValue -> preferences[stringKey(key)]?.let(SettingValue::TextValue)
        is SettingValue.SingleChoiceValue -> preferences[stringKey(key)]?.let(SettingValue::SingleChoiceValue)
        is SettingValue.MultiChoiceValue -> preferences[stringSetKey(key)]?.let(SettingValue::MultiChoiceValue)
        is SettingValue.IntValue -> preferences[intKey(key)]?.let(SettingValue::IntValue)
        is SettingValue.LongValue -> preferences[longKey(key)]?.let(SettingValue::LongValue)
        is SettingValue.DoubleValue -> preferences[doubleKey(key)]?.let(SettingValue::DoubleValue)
    }
```

Repository needs type hint on read — pass `SettingValue` kind from caller OR store type discriminator. Simpler approach: repository API accepts kind on read:

Update domain repository to:

```kotlin
suspend fun getValue(key: SettingKey, kind: SettingValue): SettingValue?
fun observeValue(key: SettingKey, kind: SettingValue): Flow<SettingValue?>
```

Use cases pass `definition.defaultValue()` as kind template. Update Task 1 repository contract accordingly when implementing.

```kotlin
// data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/SettingsRepositoryImpl.kt
package com.devindie.cmptemplate.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override fun observeValue(key: SettingKey, kind: SettingValue): Flow<SettingValue?> =
        dataStore.data.map { preferences -> readValue(preferences, key, kind) }

    override suspend fun getValue(key: SettingKey, kind: SettingValue): SettingValue? =
        readValue(dataStore.data kotlinx.coroutines.flow.first(), key, kind)

    override suspend fun setValue(key: SettingKey, value: SettingValue) {
        dataStore.edit { preferences ->
            when (value) {
                is SettingValue.BooleanValue -> preferences[booleanKey(key)] = value.value
                is SettingValue.TextValue -> preferences[stringKey(key)] = value.value
                is SettingValue.SingleChoiceValue -> preferences[stringKey(key)] = value.optionId
                is SettingValue.MultiChoiceValue -> preferences[stringSetKey(key)] = value.optionIds
                is SettingValue.IntValue -> preferences[intKey(key)] = value.value
                is SettingValue.LongValue -> preferences[longKey(key)] = value.value
                is SettingValue.DoubleValue -> preferences[doubleKey(key)] = value.value
            }
        }
    }
}
```

Use `import kotlinx.coroutines.flow.first` for getValue.

Platform factories mirror onboarding:

```kotlin
// data/src/androidMain/kotlin/com/devindie/cmptemplate/data/settings/createSettingsDataStore.android.kt
fun createSettingsDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        produceFile = { context.filesDir.resolve(SETTINGS_DATASTORE_FILE) },
    )
```

```kotlin
// data/src/iosMain/kotlin/com/devindie/cmptemplate/data/settings/createSettingsDataStore.ios.kt
// Copy path pattern from createOnboardingDataStore.ios.kt using SETTINGS_DATASTORE_FILE
```

Wire DI:

```kotlin
single { createSettingsDataStore(get<Context>()) } // android
single { createSettingsDataStore() } // ios
single<SettingsRepository> { SettingsRepositoryImpl(dataStore = get()) }
```

- [ ] **Step 4: Update domain use cases to pass kind template**

In `GetSettingUseCase`, `ObserveSettingUseCase`, `ObserveSettingsScreenUseCase` — call repository with `definition.defaultValue()` as kind.

- [ ] **Step 5: Run tests**

Run: `./gradlew :data:cleanJvmTest :data:jvmTest --tests "com.devindie.cmptemplate.data.settings.SettingsRepositoryImplTest"`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add data/src/commonMain/kotlin/com/devindie/cmptemplate/data/settings/ \
        data/src/androidMain/kotlin/com/devindie/cmptemplate/data/settings/ \
        data/src/iosMain/kotlin/com/devindie/cmptemplate/data/settings/ \
        data/src/commonTest/kotlin/com/devindie/cmptemplate/data/settings/ \
        data/src/androidMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.android.kt \
        data/src/iosMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.ios.kt \
        domain/
git commit -m "feat(settings): add DataStore-backed repository"
```

---

### Task 4: App catalog + feature definition example

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/browse/api/BrowseSettings.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/AppSettings.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/AppSettingsCatalog.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/SettingsCatalogModule.kt`
- Modify: `androidApp/src/main/kotlin/com/devindie/cmptemplate/CmpTemplateApplication.kt`
- Modify: `shared/src/iosMain/kotlin/com/devindie/cmptemplate/KoinIos.kt`

- [ ] **Step 1: Add catalog files**

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/browse/api/BrowseSettings.kt
package com.devindie.cmptemplate.feature.browse.api

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey

object BrowseSettings {
    val ShowPrices = SettingKey("browse.show_prices")

    fun definitions(): List<SettingDefinition> =
        listOf(
            BooleanSettingDefinition(
                key = ShowPrices,
                title = "Show prices",
                description = "Display card prices in browse lists",
                default = true,
            ),
        )
}
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/AppSettings.kt
package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SingleChoiceSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingOption

object AppSettings {
    val ShowCardImages = SettingKey("appearance.show_card_images")
    val Theme = SettingKey("appearance.theme")

    fun appearanceDefinitions(): List<SettingDefinition> =
        listOf(
            BooleanSettingDefinition(
                key = ShowCardImages,
                title = "Show card images",
                description = "Display images on card rows",
                default = true,
            ),
            SingleChoiceSettingDefinition(
                key = Theme,
                title = "Theme",
                description = "App color theme",
                options =
                    listOf(
                        SettingOption("system", "System"),
                        SettingOption("light", "Light"),
                        SettingOption("dark", "Dark"),
                    ),
                defaultOptionId = "system",
            ),
        )
}
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/AppSettingsCatalog.kt
package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingsSection
import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import com.devindie.cmptemplate.feature.browse.api.BrowseSettings

class AppSettingsCatalog : SettingsCatalog {
    override val sections =
        listOf(
            SettingsSection(
                id = "appearance",
                title = "Appearance",
                definitions = AppSettings.appearanceDefinitions(),
            ),
            SettingsSection(
                id = "browse",
                title = "Browse",
                definitions = BrowseSettings.definitions(),
            ),
        )
}
```

```kotlin
// shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/SettingsCatalogModule.kt
package com.devindie.cmptemplate.settings

import com.devindie.cmptemplate.domain.settings.SettingsCatalog
import org.koin.dsl.module

fun settingsCatalogModule() =
    module {
        single<SettingsCatalog> { AppSettingsCatalog() }
    }
```

- [ ] **Step 2: Wire Koin in app entry points**

```kotlin
// CmpTemplateApplication.kt — add to appModules list:
import com.devindie.cmptemplate.settings.settingsCatalogModule

settingsCatalogModule(),
```

Same in `KoinIos.kt`.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/devindie/cmptemplate/settings/ \
        shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/browse/api/BrowseSettings.kt \
        androidApp/src/main/kotlin/com/devindie/cmptemplate/CmpTemplateApplication.kt \
        shared/src/iosMain/kotlin/com/devindie/cmptemplate/KoinIos.kt
git commit -m "feat(settings): add app catalog and Koin wiring"
```

---

### Task 5: Settings presentation — ViewModel + API

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsRoute.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsNavigation.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsFeatureModule.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsScreenUiState.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsViewModel.kt`
- Create: `shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsViewModelTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/core/di/AppDomainModule.kt`

- [ ] **Step 1: Write failing ViewModel test**

```kotlin
// shared/src/commonTest/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsViewModelTest.kt
package com.devindie.cmptemplate.feature.settings.impl

import com.devindie.cmptemplate.domain.fake.FakeSettingsCatalog
import com.devindie.cmptemplate.domain.fake.FakeSettingsRepository
import com.devindie.cmptemplate.domain.model.settings.BooleanSettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SettingsViewModelTest {
    @Test
    fun emitsScreenItemsFromCatalog() = runViewModelTest {
        val viewModel =
            SettingsViewModel(
                observeSettingsScreen = ObserveSettingsScreenUseCase(FakeSettingsRepository(), FakeSettingsCatalog()),
                updateSetting = UpdateSettingUseCase(FakeSettingsRepository(), FakeSettingsCatalog()),
            )
        advanceMainUntilIdle()

        val item = viewModel.uiState.value.sections.single().items.first()
        assertIs<BooleanSettingsItemModel>(item)
        assertEquals(true, item.value)
    }

    @Test
    fun onSettingChanged_updatesValue() = runViewModelTest {
        val repository = FakeSettingsRepository()
        val catalog = FakeSettingsCatalog()
        val viewModel =
            SettingsViewModel(
                observeSettingsScreen = ObserveSettingsScreenUseCase(repository, catalog),
                updateSetting = UpdateSettingUseCase(repository, catalog),
            )
        advanceMainUntilIdle()

        viewModel.onSettingChanged(
            SettingKey("general.notifications"),
            SettingValue.BooleanValue(false),
        )
        advanceMainUntilIdle()

        val item = viewModel.uiState.value.sections.single().items.first() as BooleanSettingsItemModel
        assertEquals(false, item.value)
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `./gradlew :shared:cleanTestDebugUnitTest :shared:testDebugUnitTest --tests "com.devindie.cmptemplate.feature.settings.impl.SettingsViewModelTest"`

- [ ] **Step 3: Implement ViewModel + API**

```kotlin
// SettingsScreenUiState.kt
package com.devindie.cmptemplate.feature.settings.impl

import com.devindie.cmptemplate.domain.model.settings.SettingsScreenModel

typealias SettingsScreenUiState = SettingsScreenModel
```

```kotlin
// SettingsViewModel.kt
package com.devindie.cmptemplate.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsScreenModel
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeSettingsScreen: ObserveSettingsScreenUseCase,
    private val updateSetting: UpdateSettingUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingsScreenUiState> =
        observeSettingsScreen()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsScreenModel(sections = emptyList()),
            )

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun onSettingChanged(key: SettingKey, value: SettingValue) {
        viewModelScope.launch {
            updateSetting(key, value)
                .onFailure { error -> _messages.emit(error.message ?: "Could not save setting") }
        }
    }
}
```

```kotlin
// SettingsRoute.kt
@Serializable data object SettingsRoute
```

```kotlin
// SettingsFeatureModule.kt
val settingsFeatureModule = module { viewModelOf(::SettingsViewModel) }
```

Register `settingsFeatureModule` in `AppDomainModule.includes(...)`.

- [ ] **Step 4: Run ViewModel tests — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(settings): add SettingsViewModel and feature module"
```

---

### Task 6: Settings UI rows + screen content

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsContent.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SettingsItemRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/BooleanSettingRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/TextSettingRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/NumberSettingRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/SingleChoiceSettingRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/impl/MultiChoiceSettingRow.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsScreen.kt`

- [ ] **Step 1: Implement `SettingsContent`**

Scaffold with `TopAppBar` title "Settings", `LazyColumn` grouped by section headers (`Text` with `titleMedium`), snackbar host collecting `viewModel.messages`.

- [ ] **Step 2: Implement `SettingsItemRow`**

`when (item)` dispatch to row composables; each row calls `onValueChange(SettingValue.*)`.

- [ ] **Step 3: Row composables**

| Composable | Control |
|------------|---------|
| `BooleanSettingRow` | `ListItem` + `Switch` |
| `TextSettingRow` | `OutlinedTextField`; commit on `ImeAction.Done` |
| `NumberSettingRow` | shared helper for Int/Long/Double fields |
| `SingleChoiceSettingRow` | `ExposedDropdownMenuBox` or `AlertDialog` with `RadioButton` list |
| `MultiChoiceSettingRow` | `AlertDialog` with `Checkbox` list |

- [ ] **Step 4: Public `SettingsScreen` entry**

```kotlin
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // collect messages → snackbar
    SettingsContent(state = state, onBack = onBack, onSettingChanged = viewModel::onSettingChanged)
}
```

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(settings): add definition-driven settings UI"
```

---

### Task 7: Profile navigation integration

**Files:**
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/main/api/ProfileHomeRoute.kt`
- Create: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/main/impl/ProfileScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/main/api/MainNavigation.kt`
- Modify: `shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/settings/api/SettingsNavigation.kt`

- [ ] **Step 1: Add nested Profile nav**

```kotlin
// ProfileHomeRoute.kt
@Serializable internal data object ProfileHomeRoute
```

Replace `profileDestination()` body:

```kotlin
fun NavGraphBuilder.profileDestination() {
    composable<MainRoute.Profile> {
        val profileNavController = rememberNavController()
        NavHost(
            navController = profileNavController,
            startDestination = ProfileHomeRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<ProfileHomeRoute> {
                ProfileScreen(onNavigateToSettings = { profileNavController.navigate(SettingsRoute) })
            }
            settingsDestination(onBack = { profileNavController.popBackStack() })
        }
    }
}
```

```kotlin
// ProfileScreen.kt — simple list with "Settings" row
@Composable
fun ProfileScreen(onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ListItem(
            headlineContent = { Text("Settings") },
            modifier = Modifier.clickable(onClick = onNavigateToSettings),
        )
    }
}
```

```kotlin
// SettingsNavigation.kt
fun NavGraphBuilder.settingsDestination(onBack: () -> Unit) {
    composable<SettingsRoute> { SettingsScreen(onBack = onBack) }
}
```

- [ ] **Step 2: Manual smoke test**

Run Android app → Profile tab → Settings → toggle a value → back → re-enter Settings → value persisted.

- [ ] **Step 3: Commit**

```bash
git commit -m "feat(settings): wire Profile tab navigation to Settings screen"
```

---

### Task 8: Verification

- [ ] **Step 1: Run full test suite**

```bash
./gradlew :domain:cleanJvmTest :domain:jvmTest
./gradlew :data:cleanJvmTest :data:jvmTest
./gradlew :shared:cleanTestDebugUnitTest :shared:testDebugUnitTest
./gradlew :architecture:test
```

Expected: all BUILD SUCCESSFUL

- [ ] **Step 2: Run quality gate**

```bash
./gradlew qualityCheck
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit if needed**

```bash
git commit -m "test(settings): complete settings module verification"
```

---

## Plan self-review

| Spec requirement | Task |
|------------------|------|
| All setting types | Task 1 models + Task 6 rows |
| External catalog | Task 4 |
| DataStore persistence | Task 3 |
| Use cases for cross-feature access | Task 2 |
| Settings screen | Tasks 5–6 |
| Profile navigation | Task 7 |
| Architecture boundaries | Task 8 `:architecture:test` |
| Demo catalog (appearance + browse) | Task 4 |

**Repository kind-parameter note:** DataStore does not store type metadata. Task 3 introduces a `kind: SettingValue` template parameter on read paths; use cases derive it from `definition.defaultValue()`. Update the domain `SettingsRepository` interface in Task 1/3 consistently.

**UseCase interface note:** `ObserveSettingUseCase` and `ObserveSettingsScreenUseCase` use plain `operator fun invoke()` (not `UseCase` / `UseCaseNoParams`) because they return `Flow` without `suspend`.
