#!/usr/bin/env bash
# Verifies local tools for the store publishing pipeline.
# See store/README.md
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

missing_required=0
missing_optional=0

check_cmd() {
  local name="$1"
  local required="$2"
  if command -v "$name" >/dev/null 2>&1; then
    local version
    version="$("$name" --version 2>/dev/null | head -1 || echo "installed")"
    echo -e "${GREEN}✓${NC} $name — $version"
    return 0
  fi
  if [[ "$required" == "required" ]]; then
    echo -e "${RED}✗${NC} $name — missing (required)"
    missing_required=$((missing_required + 1))
  else
    echo -e "${YELLOW}○${NC} $name — missing (optional)"
    missing_optional=$((missing_optional + 1))
  fi
  return 1
}

check_python_module() {
  local module="$1"
  local python="${ROOT_DIR}/scripts/store/.venv/bin/python3"
  if [[ -x "$python" ]] && "$python" -c "import $module" >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} venv:$module"
    return
  fi
  if python3 -c "import $module" >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} python3:$module"
    return
  fi
  echo -e "${RED}✗${NC} python3:$module — run: ./scripts/store/setup-python.sh"
  missing_required=$((missing_required + 1))
}

echo "Store pipeline prerequisites"
echo "============================"
echo ""

echo "Required (validate + frame):"
check_cmd python3 required
check_cmd jq required
check_python_module yaml
check_python_module PIL

echo ""
echo "Required for Android capture/release:"
check_cmd maestro required || true
check_cmd java required || true

if [[ -x "$ROOT_DIR/gradlew" ]]; then
  echo -e "${GREEN}✓${NC} gradlew"
else
  echo -e "${RED}✗${NC} gradlew — missing"
  missing_required=$((missing_required + 1))
fi

echo ""
echo "Optional (Play CLI validation / upload):"
check_cmd gplay optional || true

echo ""
echo "Optional (Fastlane upload — Phase 3):"
check_cmd ruby optional || true
check_cmd bundle optional || true

echo ""
echo "iOS (macOS only):"
if [[ "$(uname -s)" == "Darwin" ]]; then
  check_cmd xcodebuild optional || true
  check_cmd xcrun optional || true
else
  echo -e "${YELLOW}○${NC} Xcode — skipped (not on macOS)"
fi

echo ""
if [[ $missing_required -gt 0 ]]; then
  echo -e "${RED}Missing $missing_required required tool(s).${NC} See store/README.md"
  exit 1
fi

echo -e "${GREEN}All required tools are available.${NC}"
if [[ $missing_optional -gt 0 ]]; then
  echo -e "${YELLOW}$missing_optional optional tool(s) missing — upload lanes may be unavailable.${NC}"
fi
