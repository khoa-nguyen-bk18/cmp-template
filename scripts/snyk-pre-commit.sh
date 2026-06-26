#!/usr/bin/env bash
# Pre-commit Snyk scan: report high+ dependency issues; never block the commit.
set -uo pipefail

warn() { printf 'warning: %s\n' "$*" >&2; }

# Opt out: SNYK_PRECOMMIT=0 git commit ...
if [[ "${SNYK_PRECOMMIT:-1}" == "0" ]]; then
  warn "Snyk pre-commit scan disabled (SNYK_PRECOMMIT=0)"
  exit 0
fi

if ! command -v snyk >/dev/null 2>&1; then
  warn "snyk not on PATH — skipping dependency scan (install: brew install snyk-cli)"
  exit 0
fi

if ! snyk whoami >/dev/null 2>&1; then
  warn "Snyk not authenticated — skipping scan (run: snyk auth)"
  exit 0
fi

warn "Running Snyk Open Source scan (high+); findings are warnings only — commit will not be blocked."

set +e
snyk test --all-projects --severity-threshold=high
code=$?
set +e

if [[ $code -ne 0 ]]; then
  printf '\n' >&2
  warn "Snyk reported vulnerabilities or scan issues (commit NOT blocked)."
  warn "Review output above; fix before push/PR via gradle/libs.versions.toml or Snyk guidance."
fi

# Always succeed so pre-commit does not block the commit.
exit 0
