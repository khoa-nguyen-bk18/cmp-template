#!/usr/bin/env bash
# Bootstrap a new CMP App Factory project with a fresh git history and renamed identity.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PACKAGE=""
DISPLAY_NAME=""
OUTPUT_DIR=""
DRY_RUN=false
FORCE=false
NO_GIT=false
VERIFY=false

usage() {
  cat <<'EOF'
Usage: ./scripts/bootstrap-app.sh --package <id> --display-name <name> --output-dir <path> [options]

Copy this template into a new directory, rename packages / app identifiers, and
initialize a fresh git repository (Approach A — no link to template history).

Required:
  --package <id>           Reverse-DNS id (e.g. com.acme.myvault)
  --display-name <name>    Human-readable app name (e.g. "My Vault")
  --output-dir <path>      Destination directory for the new project

Options:
  --dry-run                Preview transforms without writing files
  --force                  Replace an existing output directory
  --no-git                 Skip git init and initial commit
  --verify                 Run ./gradlew qualityCheck in the output directory
  -h, --help               Show this help

Examples:
  ./scripts/bootstrap-app.sh \
    --package com.acme.myvault \
    --display-name "My Vault" \
    --output-dir ../my-vault

  ./scripts/bootstrap-app.sh --dry-run \
    --package com.acme.myvault \
    --display-name "My Vault" \
    --output-dir /tmp/my-vault-preview

After bootstrap:
  cd <output-dir>
  cp local.properties.example local.properties   # set sdk.dir
  ./scripts/setup-pre-commit.sh --baseline      # optional
  ./gradlew qualityCheck
EOF
}

log() { printf '%s\n' "$*"; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

while (($# > 0)); do
  case "$1" in
    --package)
      PACKAGE="${2:-}"
      shift 2
      ;;
    --display-name)
      DISPLAY_NAME="${2:-}"
      shift 2
      ;;
    --output-dir)
      OUTPUT_DIR="${2:-}"
      shift 2
      ;;
    --dry-run) DRY_RUN=true; shift ;;
    --force) FORCE=true; shift ;;
    --no-git) NO_GIT=true; shift ;;
    --verify) VERIFY=true; shift ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      die "unknown option: $1 (try --help)"
      ;;
  esac
done

[[ -n "$PACKAGE" ]] || die "--package is required (try --help)"
[[ -n "$DISPLAY_NAME" ]] || die "--display-name is required (try --help)"
[[ -n "$OUTPUT_DIR" ]] || die "--output-dir is required (try --help)"

PY_ARGS=(
  --package "$PACKAGE"
  --display-name "$DISPLAY_NAME"
  --output-dir "$OUTPUT_DIR"
)
$DRY_RUN && PY_ARGS+=(--dry-run)
$FORCE && PY_ARGS+=(--force)
$NO_GIT && PY_ARGS+=(--no-git)

python3 "$ROOT/scripts/bootstrap_app.py" "${PY_ARGS[@]}"

if $VERIFY && ! $DRY_RUN; then
  OUTPUT_ABS="$(cd "$OUTPUT_DIR" && pwd)"
  log ""
  log "Running qualityCheck in $OUTPUT_ABS ..."
  (cd "$OUTPUT_ABS" && ./gradlew qualityCheck)
fi
