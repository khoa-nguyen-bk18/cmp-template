# AGENTS Guidelines for This Repository

This repository contains an Android application project. When working on the project interactively with an AI coding agent, please follow the guidelines below to ensure architectural consistency, maximum performance, and a smooth development experience.

## 1. Project Specifications
- **Minimum SDK:** 24 (or defined by project)
- **Target SDK:** 34
- **Language:** Kotlin (1.9+)
- **Build System:** Gradle (Kotlin DSL preferred)

## 2. Architecture & Design Patterns
We follow the official Android Architecture Guidelines:
- **Presentation Layer:** StateFlow/SharedFlow in ViewModels. UI defined in Jetpack Compose (or XML if legacy). Unidirectional Data Flow (UDF).
- **Domain Layer:** Optional UseCases for complex business logic.
- **Data Layer:** Repository pattern to abstract data sources (Room for local, Retrofit for remote).
- **Dependency Injection:** Hilt/Dagger (preferred) or Koin.

## 3. Asynchronous Programming
- **Concurrency:** Kotlin Coroutines exclusively. Avoid RxJava for new code (migrate if possible).
- **Dispatchers:** Inject dispatchers in the **data** layer via `DispatcherProvider` (don't hardcode `Dispatchers.IO` in DataSources/repos). ViewModels use `viewModelScope`; domain stays dispatcher-free.
- **Conventions:** [docs/coroutines-conventions.md](docs/coroutines-conventions.md) — main-safe suspend, cancellation, `runTest` / Turbine testing.

## 4. UI Framework
- **Jetpack Compose:** Default choice for all new features. Follow Compose best practices (state hoisting, modifiers, no side-effects in composables).
- **Navigation:** Jetpack Navigation Compose.

## 5. Testing Philosophy
- **Unit Tests:** JUnit4/JUnit5, MockK for mocking, Turbine for Flow testing. 
- **UI Tests:** Compose Test Rule for UI components, Espresso for legacy XML.
- Prefer testing ViewModel state emission over testing implementation details.

### Architecture tests (Konsist)

The [`architecture`](architecture/build.gradle.kts) module uses [Konsist](https://docs.konsist.lemonappdev.com/) to enforce Clean Architecture boundaries at the package/import level.

- **Run:** `./gradlew :architecture:test` or `./gradlew qualityCheck` (includes architecture tests).
- **Enabled:** Domain purity (no Android/Compose/Koin/Ktor), `data` isolated from UI, ViewModels not in domain, repository interfaces in `domain.repository`, `*RepositoryImpl` in `data`, presentation/`di` must not import `data` (wired at `androidApp` / iOS `doInitKoin`), `androidApp` may import `data.di` only.

When adding features: put contracts in `domain`, implementations in `data`, use cases in `domain.usecase`, ViewModels in `shared` under `screens` — depend on use cases, not `*RepositoryImpl` or data-layer types. Register `platformDataModule()` in app entry points, not in `:shared` `commonMain`.

**Full step-by-step playbook** (layers, `expect`/`actual` vs DataSource, checklist): [docs/kmp-feature-playbook.md](docs/kmp-feature-playbook.md). Cursor applies this automatically via `.cursor/rules/kmp-feature-implementation.mdc`.

**Inline KDoc conventions** (what to document, connection `@see` pattern, vault flow map): [docs/code-documentation.md](docs/code-documentation.md).

## 6. External Documentation
- When asked to implement a functionality that you are not sure of, refer to the official [Android Developer Documentation](https://developer.android.com) or [Kotlin Documentation](https://kotlinlang.org) for additional context and best practices.

## 7. Useful Agent Skills Recap

| Skill Folder          | Purpose                                            |
| --------------------- | -------------------------------------------------- |
| `architecture/`       | Clean architecture, ViewModels, and Data Layer.    |
| `ui/`                 | Jetpack Compose best practices, Coil, Accessibility. |
| `performance/`        | Auditing Compose and Gradle build performance.     |
| `migration/`          | XML to Compose, RxJava to Coroutines.              |
| `testing_and_automation/` | Unit/UI Testing setup, Emulator automation scripts. |
| `concurrency_and_networking/` | Coroutines fixes, Retrofit networking.             |

---

Following these practices ensures that the agent-assisted development workflow stays reliable and consistent. When in doubt, always refer to the specific agent skills provided in `.github/skills/` for deeper task-specific context!

*Note to developers: Update this file whenever the project makes architectural shifts to ensure AI agents stay aligned with your conventions.*

<!-- CODEGRAPH_START -->
## CodeGraph

This project has a CodeGraph MCP server (`codegraph_*` tools) configured. CodeGraph is a tree-sitter-parsed knowledge graph of every symbol, edge, and file. Reads are sub-millisecond and return structural information grep cannot.

### When to prefer codegraph over native search

Use codegraph for **structural** questions — what calls what, what would break, where is X defined, what is X's signature. Use native grep/read only for **literal text** queries (string contents, comments, log messages) or after you already have a specific file open.

| Question | Tool |
|---|---|
| "Where is X defined?" / "Find symbol named X" | `codegraph_search` |
| "What calls function Y?" | `codegraph_callers` |
| "What does Y call?" | `codegraph_callees` |
| "What would break if I changed Z?" | `codegraph_impact` |
| "Show me Y's signature / source / docstring" | `codegraph_node` |
| "Give me focused context for a task/area" | `codegraph_context` |
| "See several related symbols' source at once" | `codegraph_explore` |
| "What files exist under path/" | `codegraph_files` |
| "Is the index healthy?" | `codegraph_status` |

### Rules of thumb

- **Answer directly — don't delegate exploration.** For "how does X work" / architecture / trace questions, answer with 2-3 codegraph calls: `codegraph_context` first, then ONE `codegraph_explore` for the source of the symbols it surfaces. Codegraph IS the pre-built index, so spawning a separate file-reading sub-task/agent — or running a grep + read loop — repeats work codegraph already did and costs more for the same answer.
- **Trust codegraph results.** They come from a full AST parse. Do NOT re-verify them with grep — that's slower, less accurate, and wastes context.
- **Don't grep first** when looking up a symbol by name. `codegraph_search` is faster and returns kind + location + signature in one call.
- **Don't chain `codegraph_search` + `codegraph_node`** when you just want context — `codegraph_context` is one call.
- **Don't loop `codegraph_node` over many symbols** — one `codegraph_explore` call returns several symbols' source grouped in a single capped call, while each separate node/Read call re-reads the whole context and costs far more.
- **Index lag**: the file watcher debounces ~500ms behind writes; don't re-query immediately after editing a file in the same turn.

### If `.codegraph/` doesn't exist

The MCP server returns "not initialized." Ask the user: *"I notice this project doesn't have CodeGraph initialized. Want me to run `codegraph init -i` to build the index?"*
<!-- CODEGRAPH_END -->
