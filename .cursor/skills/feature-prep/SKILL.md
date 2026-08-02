---
name: feature-prep
description: >-
  Orchestrates pre-implementation design: brainstorming (steps 1–5), grill-with-docs,
  then brainstorming (steps 6–9) ending in writing-plans. Use when preparing a new
  feature or plan before coding, or when the user says feature-prep, design and grill,
  or wants to stress-test a design before implementation.
disable-model-invocation: true
---

# Feature Prep

End-to-end pre-implementation pipeline. Do **not** write application code, scaffold, or invoke implementation skills until Phase C finishes and `writing-plans` has produced the plan.

## Pipeline (verbatim)

```
brainstorming (steps 1–5)
  explore context → questions → approaches → present design → get approval
        ↓
grill-with-docs
  stress-test the approved design; resolve ambiguities; update CONTEXT.md / ADRs
        ↓
brainstorming (steps 6–9)
  write spec doc → self-review → your review → writing-plans
```

## Progress tracking

Create these todos at start and update as you go:

```
- [ ] Phase A: Brainstorming steps 1–5 (design approved)
- [ ] Phase B: grill-with-docs (shared understanding + docs)
- [ ] Phase C: Brainstorming steps 6–9 (spec reviewed)
- [ ] Terminal: writing-plans (implementation plan written)
```

Announce at start: "I'm running feature-prep: brainstorm → grill-with-docs → spec → writing-plans."

## Skill loading

Before each phase, **Read** that phase's skill file and follow it. Resolve paths from available skills / agent skill folders by name:

| Phase | Skill `name` | Also load |
|-------|--------------|-----------|
| A, C | `brainstorming` | — |
| B | `grill-with-docs` | Then `grilling` + `domain-modeling` as that skill requires |
| Terminal | `writing-plans` | — |

If a skill file is missing, stop and tell the user which skill to install (brainstorming / writing-plans via Superpowers; grill-with-docs via Matt Pocock agent skills).

## Phase A — Brainstorming steps 1–5

Read and follow `brainstorming`, **but stop after design approval**. Do **not** write the spec doc, commit, or call `writing-plans` in this phase.

Run in order:

1. Explore project context
2. Offer visual companion if needed (own message only, per brainstorming rules)
3. Ask clarifying questions (one at a time)
4. Propose 2–3 approaches with trade-offs; recommend one
5. Present design in sections; get user approval after each section as required

**Gate:** Wait for explicit user approval of the design. Only then mark Phase A complete and enter Phase B.

If the idea is multi-subsystem and too large for one spec, decompose first (per brainstorming) and run feature-prep on the first sub-project only.

## Phase B — grill-with-docs

Read and follow `grill-with-docs` (grilling + domain-modeling) against the **approved design** from Phase A.

- Interview relentlessly, one question at a time, with a recommended answer each time
- Prefer codebase exploration over asking when the code can answer
- Sharpen domain terms; invent edge-case scenarios; cross-check claims against code
- Update `CONTEXT.md` inline when terms resolve; offer ADRs only when domain-modeling's three ADR criteria are met
- Keep grilling until shared understanding — do not rush to the next phase

**Gate:** Confirm with the user that grilling is done and design/docs are settled. Then Phase C.

## Phase C — Brainstorming steps 6–9

Return to `brainstorming` for documentation and handoff only. Incorporate grilling outcomes (resolved ambiguities, glossary, ADRs) into the written spec.

6. Write design doc → `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md` (user path preference overrides); commit if brainstorming requires it
7. Spec self-review — placeholders, consistency, scope, ambiguity; fix inline
8. User reviews written spec — wait for approval or requested changes
9. Transition → invoke `writing-plans` (next section)

**Gate:** Do not start `writing-plans` until the user approves the written spec.

## Terminal — writing-plans

Read and follow `writing-plans` to produce the implementation plan from the approved spec.

After the plan exists, feature-prep is **complete**. Stop. Do not implement unless the user explicitly asks to proceed.

## Hard rules

- No application code, scaffolding, or feature implementation during any phase
- Do not skip Phase B unless the user explicitly opts out ("skip grill")
- Do not run `writing-plans` before Phases A–C gates pass
- One clarifying/grilling question per message
- Stay YAGNI; keep design units small and testable (per brainstorming)

## Skip / resume

| User says | Action |
|-----------|--------|
| Skip grill | After Phase A approval, jump to Phase C; note that docs/ADRs were not grilled |
| Already have an approved design | Start at Phase B |
| Already have a grilled design / CONTEXT updates | Start at Phase C |
| Spec already approved | Run Terminal (`writing-plans`) only |

## Optional lenses (only if user asks)

After Phase B, before Phase C, user may request a short pass using existing skills such as `mobile-security`, `accessibility-patterns`, or `test-scenarios`. Do not run these by default.
