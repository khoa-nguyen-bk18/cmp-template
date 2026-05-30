# Compose Performance Audit — 2026-05-25 — Search (`SearchScreen`)

## Environment

| Item | Value |
|------|--------|
| Compose Multiplatform | 1.11.0 |
| Compose Compiler | Kotlin plugin 2.3.21 (strong skipping default ON) |
| Kotlin | 2.3.21 |
| AGP | 9.0.1 |
| Module | `:shared` (`SearchScreen.kt` + `SearchResultItem`, `SearchMatchLine`, `SearchQuickActions`) |
| Release build | `:androidApp:assembleRelease` — **SUCCESS** (R8 + shrink resources enabled) |
| Macrobenchmark module | **Not present** |
| Baseline Profile | **Not present** |
| Compose compiler reports | **Not configured** (`composeReports` destination not in Gradle) |
| Device measurements (this session) | **Not captured** — requires physical device + benchmark harness |

---

## Baseline (Phase 1)

| Metric | Status |
|--------|--------|
| Cold startup median (`StartupTimingMetric`) | **Not measured** — no Macrobenchmark target for Search |
| Search scroll `FrameTimingMetric` (P50 / P90 / P99) | **Not measured** — no scroll benchmark; user-reported jank on result scroll |
| Baseline Profile present | **No** |
| Release + R8 representative build | **Yes** (`assembleRelease` OK) |

**Phase 1 gap:** Vaulty is KMP-first; perf gates from the Android audit playbook (Macrobenchmark + Baseline Profile + compiler reports in CI) are not wired yet. **Do not treat debug builds or emulator as baseline.**

**Recommended Phase 1 follow-up (before claiming scroll improvements):**

1. Add `:benchmark` module with a `searchResultsScroll` test: `FrameTimingMetric`, `CompilationMode.Partial(BaselineProfileMode.Require)`, ≥5 iterations on a physical device.
2. Enable Compose Compiler reports on `:shared` release compile:
   ```kotlin
   composeCompiler {
       reportsDestination = layout.buildDirectory.dir("compose_compiler")
   }
   ```
   Run: `./gradlew :shared:compileReleaseKotlinAndroid -PcomposeReports=true` (after wiring property).
3. Record P50/P90/P99 in this document **before** further Search changes.

---

## Diagnosis (Phase 2)

Analysis combines: static review of current code (post `fix-screen-recomposition-performance`), prior root-cause investigation (scroll jank), and Compose performance axes. **No `composables.txt` / Layout Inspector counts** in this run.

### Architecture (current — good)

| Pattern | Status |
|---------|--------|
| State-holder / UI split (`SearchScreen(viewModel)` → `SearchScreen(state,…)`) | ✅ Preview-friendly; VM wiring isolated |
| Match-line extraction in `PrepareSearchResultDisplayUseCase` + `Dispatchers.Default` in VM | ✅ **Removes 8 KB excerpt scan from scroll composition** (primary jank fix) |
| `LazyColumn` `key` + `contentType` on results (`file.id`, `"result"`) | ✅ Item identity + recycling |
| Query / quick actions / filter as separate lazy items (no `query` on chip items) | ✅ Typing should not restart header items when params stable |
| Stable `remember` chip callbacks (`SearchQuickActions`, `SearchMarkdownFilter`) | ✅ |
| `HighlightedText` `remember` keyed on theme colors (not fresh `SpanStyle`) | ✅ Fixed |

### Top hotspots (ranked by frequency × cost)

| Rank | Hotspot | Axis | Evidence |
|------|---------|------|----------|
| **1** | **`SearchResultItem` row cost** | Composition + layout | Per visible row: `VaultySurfaceCard` + up to **3× `HighlightedText`** (annotation build) + **`FlowRow` + 2× `FilterChip`** + optional `SearchMatchLineSection` (`Surface` + 4th `HighlightedText`). Dominates **first compose** when flinging through results. |
| **2** | **`highlightTerms` updates while typing** | Stability / recomposition | `SearchUiState.highlightTerms` updates every keystroke; result rows receive `List<String>` — **unstable interface**. When terms change, visible rows **must** rebuild annotations (correct UX, still costly). |
| **3** | **`SearchUiState` as single blob** | Invalidation scope | `SearchScreen(state: SearchUiState)` — any field change re-enters the screen composable. Children with stable params can skip (strong skipping), but lazy item lambdas still run. |
| **4** | **Unstable list / model parameters** | Stability | `List<SearchResultDisplay>`, `List<String>`, `List<SearchQueryTemplate>`; `SearchResultDisplay` / `VaultSearchResult` plain `data class` (no `@Immutable`). Compiler likely compares by `===` for lists → skipping fragile. |
| **5** | **Feedback ↔ results slot swap** | Structure | `if (feedbackKind != None) { item feedback }` vs `if (feedbackKind == None) { items results }` — toggling searching/no-results **replaces** result items with feedback (full list remeasure, not scroll-only). |
| **6** | **Header items in same `LazyColumn`** | Measurement | Query + quick actions + filter scroll away with results; extra lazy slots vs sticky header pattern (minor vs row cost). |
| **7** | **`AnimatedVisibility` in `SearchQuickActions`** | Layout | Expand/collapse animates chip grid; low frequency unless user toggles section. |

### Restartable-not-skippable composables (estimate)

| Count | Notes |
|-------|--------|
| **Not run** | Requires `shared-composables.txt` from compiler reports |
| **Likely candidates** | `SearchResultItem`, `SearchScreen(state,…)`, `HighlightedText`, `VaultyTagChip` / `FilterChip` when parent passes unstable `List` or new lambdas |

### Unstable classes (estimate)

| Count | Notes |
|-------|--------|
| **Not run** | Requires `shared-classes.txt` |
| **Known** | `SearchUiState`, `SearchResultDisplay`, `VaultSearchResult`, `VaultFileRef`, `SearchQueryTemplate` — all plain `data class` with `List` fields |

### Phase-misplaced reads

| Count | Notes |
|-------|--------|
| **0 identified** | No scroll/animation `State` read in `SearchScreen` composition; `LinearProgressIndicator` N/A here |

### Back-writing

| Count | Notes |
|-------|--------|
| **0** | No composition-time `mutableState` map/list mutation; no cross-row `height(state)` |

---

## Fixes applied (Phase 3) — already landed in `fix-screen-recomposition-performance`

| Skill / area | Change | Files | Macrobench delta |
|--------------|--------|-------|------------------|
| Recomposition boundaries | Split state-holder vs UI; isolated lazy items for query / quick actions / filter | `SearchScreen.kt` | **Not measured** |
| Domain offload | `PrepareSearchResultDisplayUseCase` + VM `withContext(Default)` | domain + `SearchViewModel.kt` | **Not measured** (expected: large scroll improvement) |
| Stability (partial) | `remember` stable chip lambdas; `HighlightedText` remember keys | `SearchQuickActions.kt`, `HighlightedText.kt` | **Not measured** |
| Lazy list | `key` + `contentType` on results | `SearchScreen.kt` | **Not measured** |

---

## Verification (Phase 4)

| Item | Status |
|------|--------|
| Scroll P50/P90/P99 vs Phase 1 | **N/A** — no Phase 1 numbers |
| Cold startup | **N/A** |
| Baseline Profile regenerated | **No module** |
| CI stability gate (`stabilityCheck`) | **Not configured** |
| Manual smoke | **Recommended:** search with 20+ hits, fling scroll, type in query field |

---

## Open items / follow-ups (prioritized)

### P0 — Measurement infrastructure (audit playbook)

1. Add Macrobenchmark `searchResultsScroll` on release + physical device; store baseline in this doc.
2. Enable Compose Compiler reports for `:shared`; commit `compose_compiler/` to CI artifact or review locally.
3. Optional: Baseline Profile module for cold start + scroll JIT.

### P1 — Remaining Search scroll cost (after measurement)

| Fix | Skill | Rationale |
|-----|--------|-----------|
| `ImmutableList` for `highlightTerms` / templates at VM boundary; `@Immutable` on `SearchResultDisplay` where possible | `stabilizing-compose-types` | Improve skip when only unrelated `SearchUiState` fields change |
| Replace disabled `FlowRow` + `FilterChip` with lightweight `Text` chips | `optimizing-lazy-layouts` / layout simplification | Cut per-row measure cost |
| Batch `AnnotatedString` for title/path/match in `PrepareSearchResultDisplayUseCase` (domain strings only) | Custom + stability | Move annotation CPU off scroll path entirely |
| Sticky search chrome: `Column { fixed header; LazyColumn(results only) }` | `optimizing-lazy-layouts` | Shorter lazy list, less work while scrolling results |

### P2 — Diagnostics

- Layout Inspector: recomposition counts on `SearchResultItem` while flinging (debug build OK for **relative** comparison, confirm on release with `@TraceRecomposition` if needed).
- `@TraceRecomposition` on `SearchResultItem` + `HighlightedText` for release Systrace.

### P3 — Do not chase

- 100% skippability on compiler report — goal is **frame time**, not report KPI.
- `remember(index)` for static row metadata with no unstable inputs — no gain.

---

## Summary

**Search scroll jank** was primarily **composition-time CPU** on each result row (snippet extraction + highlighting), not missing lazy `key`s. **`PrepareSearchResultDisplayUseCase`** addresses the worst offender. **Remaining cost** is concentrated in **`SearchResultItem` layout and `HighlightedText` work** when rows enter the viewport or when `highlightTerms` change.

**This audit cannot claim numeric improvement** until Phase 1 Macrobenchmark + compiler reports are run on a release build and device. The codebase is ready for that measurement pass; structural fixes from the OpenSpec change are in place.
