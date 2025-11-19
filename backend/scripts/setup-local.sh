#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------------
# Atlas Local Setup Script
# -------------------------------------------------------------
# Goal:
# - Read 'backend/app-stack.local.cfg' and generate 'backend/app-stack.cfg'
# - Render on-premise Docker Compose deployment files (same as onprem-compose)
# - No interactive selections; uses values from dev config
#
# Notes:
# - Requires Node.js; will install 'ejs' if missing (via npm).
# - On Windows, recommended to run in Git Bash or WSL.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

APP_STACK_DEV_FILE="$BACKEND_DIR/app-stack.local.cfg"
APP_STACK_FILE="$BACKEND_DIR/app-stack.cfg"
TEMPLATE_GENERATOR="$BACKEND_DIR/scripts/template-generator.mjs"
COMPOSE_TEMPLATES_DIR="$BACKEND_DIR/scripts/deployment/templates/onprem/compose"
DEPLOYMENT_GENERATED_DIR="$BACKEND_DIR/scripts/deployment/generated"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'
BOLD='\033[1m'

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*"; }
err()  { printf "[ERROR] %s\n" "$*"; }

ensure_template_deps() {
  # Ensure Node.js and 'ejs' exist for EJS template rendering
  if ! command -v node >/dev/null 2>&1; then
    err "Node.js is required to render templates. Please install Node.js."
    exit 1
  fi
  if node -e "try{require('ejs');}catch(e){process.exit(1)}" >/dev/null 2>&1; then
    return 0
  fi
  warn "Missing Node package 'ejs' required by template-generator.mjs."
  if ! command -v npm >/dev/null 2>&1; then
    err "npm is required to install dependencies. Please install npm (Node.js)."
    exit 1
  fi
  printf "Install 'ejs' in backend directory now? [Y/n] "
  read -r answer
  answer=${answer:-Y}
  case "$answer" in
    Y|y|Yes|yes)
      (
        cd "$BACKEND_DIR" || exit 1
        if [[ ! -f package.json ]]; then
          info "Initializing npm in $BACKEND_DIR"
          npm init -y >/dev/null 2>&1 || true
        fi
        info "Installing ejs in $BACKEND_DIR"
        npm install ejs --save || {
          err "Failed to install 'ejs'."
          exit 1
        }
      )
      ;;
    *)
      err "Cannot proceed without 'ejs'. Aborting."
      exit 1
      ;;
  esac
}

reset_deployment_generated_dir() {
  # Reset generated deployment output directory
  if [[ -d "$DEPLOYMENT_GENERATED_DIR" ]]; then
    rm -rf "$DEPLOYMENT_GENERATED_DIR"
  fi
  mkdir -p "$DEPLOYMENT_GENERATED_DIR"
}

normalize_line_endings_to_lf() {
  # Normalize line endings to LF in generated scripts for better cross-platform compatibility
  local dir="$1"
  if [[ -d "$dir" ]]; then
    if command -v dos2unix >/dev/null 2>&1; then
      find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -exec dos2unix -q {} \;
    else
      while IFS= read -r -d '' f; do
        awk '{ sub(/\r$/, ""); print }' "$f" > "$f.tmp" && mv "$f.tmp" "$f"
      done < <(find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -print0)
    fi
    find "$dir" -type f -name "*.sh" -exec chmod +x {} \;
  fi
}

generate_app_stack_from_dev() {
  # Generate app-stack.cfg from app-stack.local.cfg, forcing platform=onprem-compose
  if [[ ! -f "$APP_STACK_DEV_FILE" ]]; then
    err "Dev config not found: $APP_STACK_DEV_FILE"
    exit 1
  fi
  info "Reading dev config: $APP_STACK_DEV_FILE"
  if grep -q '^platform=' "$APP_STACK_DEV_FILE"; then
    sed -E 's/^platform=.*/platform=onprem-compose/' "$APP_STACK_DEV_FILE" > "$APP_STACK_FILE"
  else
    {
      printf "platform=onprem-compose\n"
      cat "$APP_STACK_DEV_FILE"
    } > "$APP_STACK_FILE"
  fi
  if [[ ! -f "$APP_STACK_FILE" ]]; then
    err "Failed to create $APP_STACK_FILE"
    exit 1
  fi
  info "app-stack.cfg generated at: $APP_STACK_FILE"
}

render_templates_compose() {
  # Render Docker Compose templates using template generator
  ensure_template_deps
  if [[ ! -d "$COMPOSE_TEMPLATES_DIR" ]]; then
    err "Compose templates directory not found: $COMPOSE_TEMPLATES_DIR"
    exit 1
  fi
  info "Rendering templates from $COMPOSE_TEMPLATES_DIR to $DEPLOYMENT_GENERATED_DIR"
  node "$TEMPLATE_GENERATOR" --dir "$COMPOSE_TEMPLATES_DIR" --out-dir "$DEPLOYMENT_GENERATED_DIR" --cfg "$APP_STACK_FILE"
}

main() {
  info "Atlas local setup starting..."
  generate_app_stack_from_dev
  reset_deployment_generated_dir
  render_templates_compose
  normalize_line_endings_to_lf "$DEPLOYMENT_GENERATED_DIR"

  info "Setup completed."
  info "Next steps:"
  info "  - Review generated files in: $DEPLOYMENT_GENERATED_DIR"
  info "  - Run: $DEPLOYMENT_GENERATED_DIR/deploy.sh to start services"
  info "  - Run: $DEPLOYMENT_GENERATED_DIR/clean.sh to stop and cleanup"
}

main "$@"
