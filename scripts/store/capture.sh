#!/usr/bin/env bash
# Run Maestro store capture flows and copy screenshots to store/.raw/
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

PLATFORM=""
DEVICE=""

usage() {
  echo "Usage: $0 --platform android|ios [--device DEVICE_ID]"
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --platform)
      PLATFORM="$2"
      shift 2
      ;;
    --device)
      DEVICE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      ;;
  esac
done

if [[ -z "$PLATFORM" ]]; then
  echo "ERROR: --platform is required" >&2
  usage
fi

if ! command -v maestro >/dev/null 2>&1; then
  echo "ERROR: maestro CLI not found. See store/README.md" >&2
  exit 1
fi

FLOW="store/capture/flows/${PLATFORM}.yaml"
RAW_DIR="store/.raw/${PLATFORM}"
STAGING_DIR="${RAW_DIR}/.maestro_staging"

if [[ ! -f "$FLOW" ]]; then
  echo "ERROR: flow not found: $FLOW" >&2
  exit 1
fi

rm -rf "$STAGING_DIR" "$RAW_DIR"/*.png
mkdir -p "$STAGING_DIR" "$RAW_DIR"

MAESTRO_ARGS=(test "$FLOW" --test-output-dir "$STAGING_DIR" --flatten-debug-output)
if [[ -n "$DEVICE" ]]; then
  MAESTRO_ARGS+=(--device "$DEVICE")
fi
if [[ "$PLATFORM" == "ios" ]]; then
  MAESTRO_ARGS+=(--platform ios)
fi

echo "Running Maestro capture: $FLOW"
maestro "${MAESTRO_ARGS[@]}"

shopt -s nullglob
screenshots=("$STAGING_DIR"/*.png "$STAGING_DIR"/**/*.png)
if [[ ${#screenshots[@]} -eq 0 ]]; then
  echo "ERROR: Maestro did not produce screenshots in $STAGING_DIR" >&2
  exit 1
fi

index=1
for file in $(printf '%s\n' "${screenshots[@]}" | sort); do
  base="$(basename "$file")"
  if [[ "$base" =~ ^[0-9]{2}_ ]]; then
    cp "$file" "$RAW_DIR/$base"
  else
    printf -v dest "%02d_%s" "$index" "$base"
    cp "$file" "$RAW_DIR/$dest"
    index=$((index + 1))
  fi
done

rm -rf "$STAGING_DIR"
count=$(find "$RAW_DIR" -maxdepth 1 -name '*.png' | wc -l | tr -d ' ')
echo "Captured $count screenshot(s) → $RAW_DIR"
echo "Next: ./scripts/store/setup-python.sh && ./scripts/store/.venv/bin/python3 scripts/store/frame_assets.py --locale en-US"
