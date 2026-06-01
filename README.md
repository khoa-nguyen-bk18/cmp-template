# CMPTemplate

Kotlin Multiplatform app for Android and iOS that indexes and searches local vault folders (markdown notes, wikilinks, and related metadata).

## Modules

| Module | Role |
|--------|------|
| `:domain` | Models, repository contracts, use cases |
| `:data` | Room index, platform file access, repository implementations |
| `:shared` | Compose Multiplatform UI, ViewModels, navigation |
| `:androidApp` | Android application entry point and platform DI |
| `:architecture` | Konsist boundary tests |

Dependency injection is wired at the app layer: `androidApp` and iOS `doInitKoin` supply `platformDataModule()`; `:shared` depends on `:domain` only (iOS `iosMain` also links `:data` for Koin bootstrap).

## Technologies

- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) for UI
- [Compose Navigation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html) with typed routes
- [Room](https://developer.android.com/kotlin/multiplatform/room) (KMP) for the vault index
- [Koin](https://insert-koin.io/) for dependency injection

## Build & quality

```bash
./gradlew :androidApp:assembleDebug
./gradlew qualityCheck
```

Architecture rules: `./gradlew :architecture:test`

Feature implementation guide: [docs/kmp-feature-playbook.md](docs/kmp-feature-playbook.md) (Clean Architecture + KMP native APIs).

### SonarQube (local)

Requires Docker. Copy `local.properties.example` → `local.properties` and set `SONAR_TOKEN` after first login.

```bash
./gradlew sonarUp              # start SonarQube + PostgreSQL (http://localhost:9000)
./gradlew sonarAnalysis        # tests + Kover report + upload (server must be running)
./gradlew sonarLocalAnalysis   # sonarUp → wait → sonarAnalysis
./gradlew sonarDown            # stop containers (data kept in Docker volumes)
```
