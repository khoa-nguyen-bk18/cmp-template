# Testing strategy (CMPTemplate / Vaulty KMP)

This project uses **kotlin-test** in KMP `commonTest`, **JUnit 5** in the JVM `:architecture` module, and **manual fakes** (no MockK) for repository and use-case doubles.

## Module map

| Module | Source set | What is tested | What is excluded |
|--------|------------|----------------|------------------|
| `:domain` | `commonTest` | Use cases, `CardCondition` logic | Repository interfaces (contracts only) |
| `:data` | `commonTest` | Repository impls, local data source, mappers, `runIoResult`, price helpers | Room schema, Koin modules, platform dispatchers |
| `:shared` | `commonTest` (+ host smoke in `androidHostTest` / `iosTest`) | `BrowseViewModel`, `CardDetailViewModel`, `MainViewModel` | Compose screens, theme, navigation UI |
| `:architecture` | JVM `test` | Konsist clean-architecture boundaries | — |
| `:androidApp` | — | — | Activity / Application shell (no business logic) |
| `:benchmark` | instrumented | — | Macrobenchmark journeys (perf, not unit) |

## Commands

```bash
# Full verification gate (formatting, detekt, all unit tests, architecture)
./gradlew qualityCheck

# Per-module KMP unit tests (JVM host + iOS simulator where configured)
./gradlew :domain:allTests
./gradlew :data:allTests
./gradlew :shared:allTests

# Architecture boundary tests only
./gradlew :architecture:test

# Coverage (Kover — domain, data, shared, architecture)
./gradlew koverXmlReport
```

## Conventions

### Fakes over mocks

- **Domain:** `FakeBrowseCardRepository`, `FakeCardDetailRepository` in `domain/src/commonTest/.../fake/`
- **Data:** `FakeBrowseCardDao` in `data/src/commonTest/.../fake/`
- **Shared:** duplicate lightweight repository fakes in `shared/src/commonTest/.../fake/` (shared must not depend on `:data`)

ViewModel tests use **real use cases** wired to fakes so presentation stays aligned with architecture rules.

### Coroutine test helpers

| Helper | Module | Purpose |
|--------|--------|---------|
| `runDataTest { provider -> }` | `data/commonTest` | `runTest` + `TestDispatcherProvider` for IO-bound repos |
| `runViewModelTest { }` | `shared/commonTest` | Sets `Dispatchers.Main` to a `StandardTestDispatcher` sharing `testScheduler` |
| `advanceMainUntilIdle()` | `shared/commonTest` | Drains the shared test scheduler (Main + test scope) |

See [coroutines-conventions.md](coroutines-conventions.md) for layer-specific async rules.

### Flow / StateFlow assertions

- Use **Turbine** (`app.cash.turbine:turbine`) when asserting multiple emissions (e.g. `MainViewModel` events).
- `BrowseViewModel` uses `SharingStarted.Eagerly` on `stateIn` because catalog seeding runs in `init` before UI subscription — reading `uiState.value` in tests (and showing correct state on first frame) requires eager sharing.

## Test layout (reference)

```
domain/src/commonTest/     → use case + model tests
data/src/commonTest/       → repo, mapper, coroutine helper tests
shared/src/commonTest/     → ViewModel tests
architecture/src/test/     → Konsist layer/package tests
```

## Intentionally out of scope

- **Compose UI / screenshot / Roborazzi** — follow [testing-setup skill](../.github/skills/testing_and_automation/) Steps 8–9 when needed
- **Room instrumented SQLite** — fake DAO covers browse logic; add in-memory Room `androidHostTest` if SQL regressions become a concern
- **End-to-end / UI Automator** — not configured in this template

## Related docs

- [coroutines-conventions.md](coroutines-conventions.md) — dispatcher injection, `runTest`, Turbine
- [kmp-feature-playbook.md](kmp-feature-playbook.md) — where to place testable logic
- [AGENTS.md](../AGENTS.md) — architecture and testing summary for agents
