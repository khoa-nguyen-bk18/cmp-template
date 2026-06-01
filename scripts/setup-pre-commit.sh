#!/usr/bin/env bash
# Automate pre-commit setup: Gitleaks + Snyk Open Source hooks for this repo.
# Idempotent — safe to re-run after pulling hook config changes.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

INSTALL_DEPS=false
RUN_BASELINE=false
SKIP_SNYK_AUTH_CHECK=false

usage() {
  cat <<'EOF'
Usage: ./scripts/setup-pre-commit.sh [options]

Installs git pre-commit hooks (Gitleaks secret scan + Snyk Gradle dependency scan).

Options:
  --install-deps       Install pre-commit and snyk via Homebrew when missing (macOS)
  --baseline           Run `pre-commit run --all-files` after setup (may take minutes)
  --skip-snyk-check    Skip Snyk authentication check (hooks will still fail until authed)
  -h, --help           Show this help

Examples:
  ./scripts/setup-pre-commit.sh --install-deps
  ./scripts/setup-pre-commit.sh --install-deps --baseline
  snyk auth && ./scripts/setup-pre-commit.sh --baseline

Manual installs (Linux / no Homebrew):
  pip install pre-commit && npm install -g snyk
  pre-commit install && pre-commit run --all-files
EOF
}

log() { printf '%s\n' "$*"; }
warn() { printf 'warning: %s\n' "$*" >&2; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

while (($# > 0)); do
  case "$1" in
    --install-deps) INSTALL_DEPS=true ;;
    --baseline) RUN_BASELINE=true ;;
    --skip-snyk-check) SKIP_SNYK_AUTH_CHECK=true ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      die "unknown option: $1 (try --help)"
      ;;
  esac
  shift
done

[[ -d "$ROOT/.git" ]] || die "not a git repository: $ROOT"

ensure_brew_pkg() {
  local pkg="$1"
  if command -v brew >/dev/null 2>&1; then
    if ! brew list --formula "$pkg" >/dev/null 2>&1; then
      log "Installing $pkg via Homebrew..."
      brew install "$pkg"
    fi
  else
    die "Homebrew not found; install $pkg manually or use pip/npm (see --help)"
  fi
}

ensure_tool() {
  local cmd="$1"
  local brew_pkg="${2:-}"
  local hint="${3:-}"

  if command -v "$cmd" >/dev/null 2>&1; then
    return 0
  fi

  if $INSTALL_DEPS && [[ -n "$brew_pkg" ]]; then
    ensure_brew_pkg "$brew_pkg"
    command -v "$cmd" >/dev/null 2>&1 || die "still missing $cmd after brew install $brew_pkg"
    return 0
  fi

  die "missing $cmd — $hint"
}

log "==> Checking prerequisites"
ensure_tool pre-commit pre-commit "brew install pre-commit  OR  pip install pre-commit  (or rerun with --install-deps)"
ensure_tool snyk snyk-cli "brew install snyk-cli  OR  npm install -g snyk  (or rerun with --install-deps)"

log "==> Installing git hooks"
pre-commit install

log "==> Downloading hook environments (Gitleaks via pre-commit)"
pre-commit install-hooks

if ! $SKIP_SNYK_AUTH_CHECK; then
  log "==> Checking Snyk authentication"
  if snyk whoami >/dev/null 2>&1; then
    log "Snyk authenticated as: $(snyk whoami 2>/dev/null | head -1)"
  else
    warn "Snyk is not authenticated — the snyk hook will skip until you run:"
    warn "  snyk auth"
    warn "  or: export SNYK_TOKEN=<token>"
    warn "Re-run this script with --baseline after authenticating."
  fi
fi

if $RUN_BASELINE; then
  log "==> Running baseline pre-commit scan (Gitleaks + Snyk, all files)"
  if ! $SKIP_SNYK_AUTH_CHECK && ! snyk whoami >/dev/null 2>&1; then
    die "Snyk not authenticated — run 'snyk auth' first, or use --skip-snyk-check without --baseline"
  fi
  pre-commit run --all-files
fi

log ""
log "Done. Pre-commit hooks are active for this clone."
log "  On commit: Gitleaks blocks on secrets → Snyk warns on vulns (high+, does not block)"
log "  Config:    .pre-commit-config.yaml  .gitleaks.toml"
log "  Manual:    pre-commit run  |  pre-commit run gitleaks --all-files"
if ! $RUN_BASELINE; then
  log "  Baseline:  ./scripts/setup-pre-commit.sh --baseline  (after snyk auth)"
fi
