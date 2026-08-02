# Phase 1 Checklist

Use this while in `PHASE_1_WORK`. Full rules live in project `AGENTS.md`.

## Before coding

- [ ] Active feature identified
- [ ] Existing project structure and conventions reviewed
- [ ] Scope limited to approved behavior — no speculative extras

## What to create

- [ ] Interfaces and function signatures
- [ ] Input/output models, state and result types, error types
- [ ] Control flow and validation logic
- [ ] Fake/simulated implementations (deterministic, test-configurable)
- [ ] Behavior tests (externally observable, not private internals)
- [ ] Test case matrix with stable IDs and coverage status
- [ ] Phase 1 report

## Fake implementation quality

- [ ] Represents every documented outcome (success and failure)
- [ ] Independent from external systems
- [ ] Minimal — models observable behavior, not internal complexity of future real code
- [ ] Configurable by tests
- [ ] Fake implementation **class exists in production source** (not only in tests)
- [ ] DI / module bindings point to fake types only — no `Room*`, `Retrofit*`, or network client in production wiring

## Behavior and errors to define

Document and test where applicable:

- [ ] Successful results
- [ ] Validation failures
- [ ] Missing or empty input
- [ ] External operation failures (via fake)
- [ ] Invalid states
- [ ] Duplicate actions, cancellation, timeout, retry (if relevant)
- [ ] Boundary values
- [ ] Unexpected failure behavior

Every handled case appears in the edge-case matrix. No silent failures. No broad catch-all hiding undefined behavior.

## Technology restrictions (production code)

Must **not**:

- Add external dependencies or modify dependency config
- Import framework-specific or platform-specific types/APIs
- Make real network, database, filesystem, or device calls
- Call third-party SDKs or modify app initialization

Existing language features and project testing tools are allowed.

## Tests

- [ ] Main and alternative success flows
- [ ] Validation and error flows
- [ ] Boundary conditions and state transitions
- [ ] Repeated actions, ordering, cancellation, recovery (if relevant)
- [ ] Each documented behavior maps to a test or explicit "cannot execute yet" note

## Phase 2 technology detection

For each capability needed later (network, persistence, navigation, etc.), document in the Phase 1 report:

1. Behavior requiring it
2. Whether it exists in the project
3. Existing technology that could support it
4. Proposed library/SDK/API
5. Alternatives, config, permissions, compatibility
6. Lifecycle, error-handling, performance, security/privacy
7. Testing approach
8. Whether a technical experiment is recommended
9. **Decision required from human**

Do not install or select technology — propose only.

## Phase 1 report must include

1. Feature summary
2. Files created or changed
3. Code organization
4. Public interfaces and signatures
5. Input/output types
6. Data-flow diagram (Mermaid: triggers, validation, decisions, fakes, outputs, failures, edge cases)
7. State transitions
8. Successful and failure flows
9. Edge-case matrix
10. Test-to-behavior mapping
11. Required Phase 2 technologies (full detection per capability)
12. Proposed libraries/frameworks/SDKs/APIs and alternatives
13. New code justification
14. Intentionally unsupported behavior
15. Test execution results
16. Questions requiring human decisions
17. **Function relationship diagram** — caller → callee with full signatures and file paths

## Test case matrix file

- [ ] Stable case IDs
- [ ] Coverage status (covered / pending / N/A)
- [ ] Links to test functions and report sections
- [ ] Gaps between defined behavior and tests called out

## Completion

- [ ] Permitted tests run; results in report
- [ ] Contract file list recorded
- [ ] Unresolved decisions listed
- [ ] Output: `AWAITING_PHASE_1_APPROVAL <feature-id>`
- [ ] Stop generating code
