# Coroutines conventions (Vaulty KMP)

Project rules for coroutines aligned with [Android coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices) and Clean Architecture module boundaries.

## Layer responsibilities

| Layer | Scopes | Dispatchers | API shape |
|-------|--------|-------------|-----------|
| **domain** | None | None — pure Kotlin | `suspend` one-shots, cold `Flow` from repositories |
| **data** | Caller-scoped only (no `GlobalScope`) | Injected via [DispatcherProvider](../data/src/commonMain/kotlin/com/devindie/vaulty/data/coroutines/DispatcherProvider.kt) | Main-safe `suspend`; `withContext(provider.io)` for blocking I/O |
| **shared** (ViewModels) | `viewModelScope` | Do not inject; use `Dispatchers.setMain` in tests | `StateFlow` UI state; `viewModelScope.launch` for work |

## Inject dispatchers (data layer only)

- **Do** bind [DefaultDispatcherProvider](../data/src/commonMain/kotlin/com/devindie/vaulty/data/coroutines/DefaultDispatcherProvider.kt) in [dispatcherModule](../data/src/commonMain/kotlin/com/devindie/vaulty/data/di/DispatcherModule.kt) (included from `platformDataModule()`).
- **Do** pass `dispatchers.io` into platform DataSources and `getVaultDatabase(..., ioDispatcher)`.
- **Do not** hardcode `Dispatchers.IO` / `Dispatchers.Default` in DataSource or repository implementations (only in `DefaultDispatcherProvider`).

## Main-safe suspend functions

Classes that perform blocking or native I/O must move work off the main thread inside the data layer:

```kotlin
suspend fun scan(...): Result<Summary> =
    withContext(dispatchers.io) { /* blocking walk */ }
```

Use cases and ViewModels call these suspend functions without choosing a dispatcher.

## ViewModels

- Start business work with `viewModelScope.launch` (survives configuration changes on Android).
- Expose `val uiState: StateFlow<...>` backed by `private val _uiState = MutableStateFlow(...)`.
- Map errors via `Result.onFailure` — avoid `catch (Exception)` that swallows `CancellationException`.

## Cancellation

Long-running suspend loops (vault scan on Android, index pass, link resolution) call `yield()` each iteration so `viewModelScope` cancellation stops promptly. iOS directory walks inside `NSFileCoordinator` callbacks remain synchronous; cancellation applies at `withContext` boundaries.

## Testing

### Data (`commonTest`)

```kotlin
runDataTest {
    val provider = testDispatcherProvider(testScheduler)
    // construct class under test with provider
    advanceUntilIdle()
}
```

See [RunDataTest.kt](../data/src/commonTest/kotlin/com/devindie/vaulty/data/coroutines/RunDataTest.kt) and [TestDispatcherProvider.kt](../data/src/commonTest/kotlin/com/devindie/vaulty/data/coroutines/TestDispatcherProvider.kt).

### ViewModels (`shared/commonTest`)

```kotlin
runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try { /* test */ } finally { Dispatchers.resetMain() }
}
```

Use `advanceTimeBy` for debounced flows (e.g. search). Prefer [Turbine](https://github.com/cashapp/turbine) when asserting multiple `StateFlow` emissions.

## Related docs

- [kmp-feature-playbook.md](kmp-feature-playbook.md) — module placement and DI wiring
- [AGENTS.md](../AGENTS.md) §3 — async summary for agents
