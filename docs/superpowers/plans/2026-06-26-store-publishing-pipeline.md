# Store Publishing Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship an in-repo App Factory store pipeline: metadata, Maestro capture, framed screenshots, validation, Fastlane internal upload, and CI workflows.

**Architecture:** `store/app.factory.yaml` + locale metadata; Maestro → `store/.raw/` → `frame_assets.py` → `store/output/`; `sync_to_fastlane.sh` + Fastlane `beta_internal` / `beta_testflight`; GitHub Actions validate on PR and optional internal release.

**Tech Stack:** Maestro, Python (Pillow, PyYAML), Fastlane, gplay CLI (optional), GitHub Actions

**Design spec:** [docs/superpowers/specs/2026-06-26-store-publishing-pipeline-design.md](../specs/2026-06-26-store-publishing-pipeline-design.md)

---

## Implementation summary (completed)

| Task | Files |
|------|-------|
| Python venv + validate | `scripts/store/setup-python.sh`, `validate_store.py`, `validate_store.sh` |
| Prerequisites check | `scripts/store/check-prerequisites.sh` |
| Capture + frame | `scripts/store/capture.sh`, `frame_assets.py`, `store/capture/flows/*.yaml` |
| Fastlane sync + lanes | `scripts/store/sync_to_fastlane.sh`, `androidApp/fastlane/`, `iosApp/fastlane/`, `Gemfile` |
| CI | `.github/workflows/store-validate.yml`, `store-release-internal.yml` |
| Docs | `store/README.md`, design spec |

**Verify locally:**

```bash
./scripts/store/setup-python.sh
./scripts/store/validate_store.sh --locale en-US --metadata-only --skip-gplay
```
