#!/usr/bin/env bash
# Pre-commit Snyk scan: report high+ dependency issues but never block the commit.
set -uo pipefail

warn() { printf 'warning: %s\n' "$*" >&2; }

if ! command -v snyk >/dev/null 2>&1; then
  warn "snyk not on PATH — skipping dependency scan (install: brew install snyk-cli)"
  exit 0
fi

if ! snyk whoami >/dev/null 2>&1; then
  warn "Snyk not authenticated — skipping scan (run: snyk auth)"
  exit 0
fi

set +e
snyk test --all-projects --severity-threshold=high
code=$?
set -e

if [[ $code -ne 0 ]]; then
  printf '\n' >&2
  warn "Snyk reported vulnerabilities or scan issues (commit not blocked)."
  warn "Review output above; fix before push/PR via gradle/libs.versions.toml or Snyk guidance."
fi

exit 0
