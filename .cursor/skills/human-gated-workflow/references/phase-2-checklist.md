# Phase 2 Checklist

Use this while in `PHASE_2_WORK`. Full rules live in project `AGENTS.md`.

## Entry gate (all required)

**Do not conflate artifacts with approval.** Phase 1 code, tests, and reports in the repo do not authorize Phase 2. Conversational encouragement does not either.

- [ ] Phase 1 approval recorded by human workflow mechanism (not chat, not artifact presence)
- [ ] Active feature matches approved feature
- [ ] Approved code version known
- [ ] Phase 1 contract files unchanged since approval
- [ ] Technical implementation plan approved
- [ ] Dependencies and platform decisions approved

Search for approval records and Phase 1 reports across project paths (`.ai-workflow/`, `reports/<feature>/`, etc.). Name each condition and its evidence in your response.

If any fail → `PHASE_2_BLOCKED_NOT_APPROVED <feature-id>` and stop. Do not proceed because Room or the API library is already in the project.

## Allowed work

- [ ] Add **approved** dependencies only
- [ ] Implement real function bodies using approved libraries/SDKs/platform APIs
- [ ] Configuration, permissions, initialization as approved
- [ ] Input/output conversion and technical error conversion
- [ ] Integration tests
- [ ] Connect to real external systems per approved plan

Follow existing project structure. Fake implementations may remain for tests.

## Contract integrity

Do **not** change without returning to Phase 1:

- Public interfaces, names, argument/return types
- Input/output models
- Approved behavior tests and edge cases
- State transitions, success/failure behavior

If real implementation cannot meet the contract → `PHASE_1_CHANGE_REQUIRED <feature-id>` and stop.

## Error handling

- [ ] Technical failures converted to Phase 1-approved outcomes
- [ ] Technology-specific errors do not escape feature boundaries
- [ ] New observable errors require Phase 1 return
- [ ] Document: failure source, detection, conversion, retry, cleanup, user recovery

## Performance review (significant work)

Consider: main-thread work, blocking, concurrency, cancellation, memory/CPU/network, conversion cost, storage, caching, repeated work, cleanup, startup/rendering impact.

No speculative optimization without requirement, measurement, or clear justification.

## Phase 2 report must include

1. Files created or changed
2. Approved contract integrity result
3. Real implementation summary
4. Function-by-function mapping to Phase 1 behavior
5. Libraries, frameworks, SDKs, platform APIs, external APIs used
6. Configuration and permission changes
7. Input/output and error conversion
8. Concurrency and cancellation
9. Performance, security, privacy considerations
10. Integration tests
11. Full test results and static-analysis results
12. Known limitations
13. Questions requiring human decisions

Per significant class/function: purpose, Phase 1 behavior implemented, technology used, conversions, concurrency, performance, related tests.

## Completion

- [ ] Phase 1 contract files verified unchanged
- [ ] Phase 1 behavior tests run unchanged
- [ ] Integration tests, build, lint run
- [ ] Phase 2 report written
- [ ] Unresolved issues listed
- [ ] Output: `AWAITING_PHASE_2_APPROVAL <feature-id>`
- [ ] Stop generating code — do not self-approve
