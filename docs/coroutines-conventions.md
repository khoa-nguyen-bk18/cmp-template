# Coroutines conventions (Vaulty KMP)

Project rules for coroutines aligned with [Android coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices) and Clean Architecture module boundaries.

## Layer responsibilities

| Layer | Scopes | Dispatchers | API shape |
|-------|--------|-------------|-----------|
| **domain** | None | None — pure Kotlin | `suspend` one-shots, cold `Flow` from repositories |
| **data** | Caller-scoped only (no `GlobalScope`) | Injected via [DispatcherProvider](../data/src/commonMain/kotlin/com/devindie/cmptemplate/data/coroutines/DispatcherProvider.kt) | Main-safe `suspend`; `withContext(provider.io)` for blocking I/O |
| **shared** (ViewModels) | `viewModelScope` | Do not inject; use `runViewModelTest` in tests | `StateFlow` UI state; `viewModelScope.launch` for work |

## Inject dispatchers (data layer only)

- **Do** bind [AndroidDispatcherProvider](../data/src/androidMain/kotlin/com/devindie/cmptemplate/data/coroutines/AndroidDispatcherProvider.kt) / [IosDispatcherProvider](../data/src/iosMain/kotlin/com/devindie/cmptemplate/data/coroutines/IosDispatcherProvider.kt) in [platformDataModule](../data/src/commonMain/kotlin/com/devindie/cmptemplate/data/di/PlatformDataModule.kt) actuals.
- **Do** pass `dispatchers.io` into repository implementations and platform data sources.
- **Do not** hardcode `Dispatchers.IO` / `Dispatchers.Default` in DataSource or repository implementations (only in platform `*DispatcherProvider` types).

## Main-safe suspend functions

Classes that perform blocking or native I/O must move work off the main thread inside the data layer:

```kotlin
suspend fun loadCards(): Result<List<Card>> =
    withContext(dispatchers.io) { /* Room / disk */ }
```

Use cases and ViewModels call these suspend functions without choosing a dispatcher.

## ViewModels

- Start business work with `viewModelScope.launch` (survives configuration changes on Android).
- Expose `val uiState: StateFlow<...>` backed by `MutableStateFlow` or `stateIn`.
- Map errors via `Result.onFailure` — avoid `catch (Exception)` that swallows `CancellationException`.
- When work starts in `init` (e.g. catalog seeding), prefer `SharingStarted.Eagerly` on related `stateIn` flows so UI state updates before the first collector attaches.

## Cancellation

Long-running suspend loops call `yield()` each iteration so `viewModelScope` cancellation stops promptly. iOS directory walks inside platform callbacks remain synchronous; cancellation applies at `withContext` boundaries.

## Testing

### Data (`commonTest`)

```kotlin
runDataTest { provider ->
    val repository = BrowseCardRepositoryImpl(localDataSource, provider)
    repository.ensureCatalogSeeded()
    advanceUntilIdle()
}
```

See [RunDataTest.kt](../data/src/commonTest/kotlin/com/devindie/cmptemplate/data/coroutines/RunDataTest.kt) and [TestDispatcherProvider.kt](../data/src/commonTest/kotlin/com/devindie/cmptemplate/data/coroutines/TestDispatcherProvider.kt).

### ViewModels (`shared/commonTest`)

```kotlin
runViewModelTest {
    val viewModel = BrowseViewModel(...)
    advanceMainUntilIdle()
}
```

See [RunViewModelTest.kt](../shared/src/commonTest/kotlin/com/devindie/cmptemplate/test/RunViewModelTest.kt). Use `advanceTimeBy` for debounced flows (e.g. browse search). Prefer [Turbine](https://github.com/cashapp/turbine) when asserting multiple `StateFlow` / event emissions.

Full module map and Gradle commands: [testing.md](testing.md).

## Related docs

- [testing.md](testing.md) — unit test strategy and commands
- [kmp-feature-playbook.md](kmp-feature-playbook.md) — module placement and DI wiring
- [AGENTS.md](../AGENTS.md) §3 — async summary for agents
