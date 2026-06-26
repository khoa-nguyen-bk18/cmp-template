#!/usr/bin/env bash
# Create scripts/store/.venv with Pillow + PyYAML (PEP 668 safe).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
VENV_DIR="$ROOT_DIR/scripts/store/.venv"

if [[ ! -d "$VENV_DIR" ]]; then
  python3 -m venv "$VENV_DIR"
fi

"$VENV_DIR/bin/pip" install -q --upgrade pip
"$VENV_DIR/bin/pip" install -q -r "$ROOT_DIR/scripts/store/requirements.txt"
echo "Store Python venv ready: $VENV_DIR"
