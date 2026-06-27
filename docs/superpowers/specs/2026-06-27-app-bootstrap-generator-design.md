# App bootstrap generator — design

**Date:** 2026-06-27  
**Status:** Approved for implementation  
**Roadmap:** README item #3 (template generator script)

## Goal

Provide a single command that copies the CMP App Factory template into a **new directory** with a **fresh git history**, renaming package names, Kotlin source trees, app identifiers, store/Maestro config, and Konsist architecture test roots to a new app identity.

## User flow (Approach A — fresh history)

```bash
./scripts/bootstrap-app.sh \
  --package com.acme.myvault \
  --display-name "My Vault" \
  --output-dir ../my-vault
```

Output: `../my-vault/` — full project copy, identifiers rewritten, `git init` + initial commit. Template repo unchanged.

## CLI

| Flag | Required | Description |
|------|----------|-------------|
| `--package` | yes | Reverse-DNS id, e.g. `com.acme.myvault` |
| `--display-name` | yes | Human app name, e.g. `My Vault` |
| `--output-dir` | yes | Destination path (must not exist unless `--force`) |
| `--dry-run` | no | Print planned changes only |
| `--force` | no | Remove existing output dir before copy |
| `--no-git` | no | Skip `git init` / initial commit |
| `--verify` | no | Run `./gradlew qualityCheck` in output (slow) |

Derived values (no extra flags):

| Concept | Source | Example |
|---------|--------|---------|
| Pascal name | Last package segment, title-cased | `MyVault` |
| Slug | Last package segment, lowercased | `myvault` |
| iOS bundle ID | `{package}.{PascalName}` | `com.acme.myvault.MyVault` |
| Gradle root name | Pascal name | `MyVault` |

## Template identity (source)

| Key | Value |
|-----|-------|
| Package | `com.devindie.cmptemplate` |
| Pascal / product | `CMPTemplate` / `CmpTemplate` |
| Slug | `cmptemplate` |
| iOS bundle | `com.devindie.cmptemplate.CMPTemplate` |

## Replacement order

Longest-first text replace in text files:

1. `com.devindie.cmptemplate` → new package
2. `CMPTemplate` → Pascal name
3. `CmpTemplate` → Pascal name
4. `cmp-template` → `{slug}-template` (Firebase placeholder project id)
5. `cmptemplate` → slug

## File operations

1. **Copy** template via `rsync` with excludes: `.git`, `build/`, `.gradle/`, `.kotlin/`, `architecture/bin/`, `.codegraph/`, `local.properties`, `store/.raw`, `store/output`, `scripts/store/.venv`, `.DS_Store`
2. **Text replace** in non-binary extensions (kotlin, kts, xml, yaml, md, sh, conf, plist, pbxproj, xcconfig, properties, json, txt, yml)
3. **Move** Kotlin trees: `**/com/devindie/cmptemplate` → `**/{package/path}`
4. **Rename** files whose names contain template tokens (e.g. `CmpTemplateApplication.kt`)
5. **Git** `init`, `add .`, commit `Initial commit from CMP App Factory template`

## Out of scope (documented warnings)

- Real Firebase configs (`google-services.json`, `GoogleService-Info.plist`) — package updated; user replaces with real project files
- Play/App Store signing keys and service accounts
- `local.properties` — user copies from `local.properties.example`

## Success criteria

- `./scripts/bootstrap-app.sh --dry-run` exits 0
- Bootstrap to temp dir + `./gradlew :architecture:test` passes (package roots match Konsist)
- Pytest for name derivation and replace ordering

## Architecture

- `scripts/bootstrap-app.sh` — bash entry (arg parsing, `rsync`, delegates to Python)
- `scripts/bootstrap_app.py` — identity model, walk/replace/rename, git init
- `scripts/test/test_bootstrap_app.py` — unit tests
