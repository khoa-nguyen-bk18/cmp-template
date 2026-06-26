#!/usr/bin/env bash
# Wrapper for store metadata and asset validation.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -x "$ROOT_DIR/scripts/store/.venv/bin/python3" ]]; then
  "$ROOT_DIR/scripts/store/setup-python.sh"
fi
PYTHON="$ROOT_DIR/scripts/store/.venv/bin/python3"

LOCALE="en-US"
SKIP_GPLAY=false
METADATA_ONLY=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --locale)
      LOCALE="$2"
      shift 2
      ;;
    --skip-gplay)
      SKIP_GPLAY=true
      shift
      ;;
    --metadata-only)
      METADATA_ONLY=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [--locale en-US] [--skip-gplay] [--metadata-only]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

ARGS=(--locale "$LOCALE")
if [[ "$SKIP_GPLAY" == true ]]; then
  ARGS+=(--skip-gplay)
fi
if [[ "$METADATA_ONLY" == true ]]; then
  ARGS+=(--metadata-only)
fi

"$PYTHON" scripts/store/validate_store.py "${ARGS[@]}"
