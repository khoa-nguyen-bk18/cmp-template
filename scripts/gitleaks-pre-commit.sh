#!/usr/bin/env bash
# Scan staged files for secrets. The upstream gitleaks pre-commit hook uses
# `gitleaks git --pre-commit --staged`, which often misses new staged files.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

resolve_gitleaks() {
  if [[ -n "${GITLEAKS_BIN:-}" && -x "${GITLEAKS_BIN}" ]]; then
    printf '%s' "${GITLEAKS_BIN}"
    return 0
  fi
  if command -v gitleaks >/dev/null 2>&1; then
    command -v gitleaks
    return 0
  fi
  local cached
  cached="$(find "${HOME}/.cache/pre-commit" -path '*/bin/gitleaks' -type f 2>/dev/null | head -1 || true)"
  if [[ -n "$cached" && -x "$cached" ]]; then
    printf '%s' "$cached"
    return 0
  fi
  return 1
}

die() { printf 'error: %s\n' "$*" >&2; exit 1; }

GITLEAKS="$(resolve_gitleaks)" || die "gitleaks not found — run: pre-commit install-hooks  OR  brew install gitleaks"

mapfile -t STAGED < <(git diff --cached --name-only --diff-filter=ACM 2>/dev/null || true)

if ((${#STAGED[@]} == 0)); then
  printf 'gitleaks: no staged files to scan\n'
  exit 0
fi

CONFIG_ARGS=()
[[ -f "$ROOT/.gitleaks.toml" ]] && CONFIG_ARGS=(-c "$ROOT/.gitleaks.toml")

found=0
for path in "${STAGED[@]}"; do
  [[ -f "$path" ]] || continue
  # Skip likely binary assets
  case "$path" in
    *.png | *.jpg | *.jpeg | *.gif | *.webp | *.ico | *.jar | *.apk | *.aab | *.dex | *.class)
      continue
      ;;
  esac
  set +e
  "$GITLEAKS" detect --source "$path" --no-git "${CONFIG_ARGS[@]}" -v
  code=$?
  set -e
  if [[ $code -ne 0 ]]; then
    found=1
  fi
done

if [[ $found -ne 0 ]]; then
  printf '\nerror: Gitleaks found secrets in staged files — commit blocked.\n' >&2
  printf 'Remove secrets, use env vars / gitignored local.properties, or rotate credentials.\n' >&2
  exit 1
fi

exit 0
