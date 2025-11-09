#!/usr/bin/env bash
set -euo pipefail

# Atlas On-Premise Setup Script
# Purpose:
#  1) Step 1: If app-stack.default.cfg exists, show its content and confirm with user.
#             If not found, prompt user to input information.
#             Then generate backend/app-stack.cfg file.
#  2) Generate deployment files from EJS templates into backend/scripts/deployment/onprem

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$BACKEND_DIR/.." && pwd)"

APP_STACK_FILE="$BACKEND_DIR/app-stack.cfg"
DEFAULT_APP_STACK_FILE="$BACKEND_DIR/app-stack.default.cfg"
WIZARD_SCRIPT="$BACKEND_DIR/scripts/app-stack-config.sh"
TEMPLATE_GENERATOR="$BACKEND_DIR/scripts/template-generator.mjs"
COMPOSE_TEMPLATES_DIR="$BACKEND_DIR/scripts/deployment/onprem/compose/_templates"
OUTPUT_DIR="$BACKEND_DIR/scripts/deployment/onprem/compose"
K8S_TEMPLATES_DIR="$BACKEND_DIR/scripts/deployment/onprem/k8s/_templates"
K8S_OUTPUT_DIR="$BACKEND_DIR/scripts/deployment/onprem/k8s"

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*"; }
err()  { printf "[ERROR] %s\n" "$*"; }

confirm_default_stack() {
  printf "Use app-stack.default.cfg to generate app-stack.cfg? [Y/n] "
  read -r answer
  answer=${answer:-Y}
  case "$answer" in
    Y|y|Yes|yes)
      return 0 ;;
    *)
      return 1 ;;
  esac
}

show_default_stack() {
  if [[ -f "$DEFAULT_APP_STACK_FILE" ]]; then
    info "Found default configuration: $DEFAULT_APP_STACK_FILE"
    cat "$DEFAULT_APP_STACK_FILE"
    echo "---------------------------------"
  else
    warn "Default configuration not found: $DEFAULT_APP_STACK_FILE"
  fi
}

# Wizard to prompt user inputs and generate app-stack.cfg
run_custom_stack_wizard() {
  info "Starting interactive wizard to create app-stack.cfg via app-stack-config.sh"
  if [[ -f "$WIZARD_SCRIPT" ]]; then
    chmod +x "$WIZARD_SCRIPT" || true
    # Pass the target file name (not path); the wizard writes to backend/<file>
    local target_file_name
    target_file_name="$(basename "$APP_STACK_FILE")"
    bash "$WIZARD_SCRIPT" --app-stack-file="$target_file_name"
    if [[ -f "$APP_STACK_FILE" ]]; then
      info "Generated app-stack.cfg at: $APP_STACK_FILE"
      echo "----- app-stack.cfg -----"
      cat "$APP_STACK_FILE"
      echo "-------------------------"
    else
      err "Wizard completed but configuration file not found: $APP_STACK_FILE"
      exit 1
    fi
  else
    err "Wizard script not found: $WIZARD_SCRIPT"
    exit 1
  fi
}

ensure_node() {
  if ! command -v node >/dev/null 2>&1; then
    err "Node.js is required to render templates. Please install Node.js."
    exit 1
  fi
}

# Ensure the 'ejs' package is available for template rendering; prompt to install if missing
ensure_template_deps() {
  ensure_node
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

read_platform() {
  if [[ -f "$APP_STACK_FILE" ]]; then
    grep '^platform=' "$APP_STACK_FILE" | cut -d'=' -f2 | tr -d '[:space:]'
  else
    echo "onprem-compose"
  fi
}

render_onprem_compose_files() {
  ensure_template_deps
  if [[ ! -d "$COMPOSE_TEMPLATES_DIR" ]]; then
    err "Compose templates directory not found: $COMPOSE_TEMPLATES_DIR"
    exit 1
  fi
  info "Rendering on-premise compose files from $COMPOSE_TEMPLATES_DIR to $OUTPUT_DIR"
  node "$TEMPLATE_GENERATOR" \
    --dir "$COMPOSE_TEMPLATES_DIR" \
    --out-dir "$OUTPUT_DIR" \
    --cfg "$APP_STACK_FILE"
}

render_onprem_k8s_files() {
  ensure_template_deps
  if [[ ! -d "$K8S_TEMPLATES_DIR" ]]; then
    err "Kubernetes templates directory not found: $K8S_TEMPLATES_DIR"
    exit 1
  fi
  info "Rendering on-premise Kubernetes files from $K8S_TEMPLATES_DIR to $K8S_OUTPUT_DIR"
  node "$TEMPLATE_GENERATOR" \
    --dir "$K8S_TEMPLATES_DIR" \
    --out-dir "$K8S_OUTPUT_DIR" \
    --cfg "$APP_STACK_FILE"
}

main() {
  info "Atlas on-premise setup starting..."

  # Step 1: Initialize app-stack.cfg
  if [[ -f "$DEFAULT_APP_STACK_FILE" ]]; then
    show_default_stack
    if confirm_default_stack; then
      cp "$DEFAULT_APP_STACK_FILE" "$APP_STACK_FILE"
      info "app-stack.cfg generated from default config."
    else
      run_custom_stack_wizard
    fi
  else
    warn "Default config not found; collecting information from user."
    run_custom_stack_wizard
  fi

  local platform
  platform="$(read_platform)"
  info "Detected platform: $platform"

  # Step 2: Render templates
  case "$platform" in
    onprem-compose)
      render_onprem_compose_files
      next_dir="$OUTPUT_DIR"
      next_deploy_script="deploy.sh"
      next_cleanup_script="clean.sh"
      ;;
    onprem-k8s)
      render_onprem_k8s_files
      next_dir="$K8S_OUTPUT_DIR"
      next_deploy_script="deploy.sh"
      next_cleanup_script="clean.sh"
      ;;
    *)
      warn "Platform '$platform' is not yet supported by this simple setup."
      exit 1
      ;;
  esac

  info "Setup completed."
  info "Next steps:"
  info "  - Review generated files in: ${next_dir:-$OUTPUT_DIR}"
  info "  - Run: ${next_dir:-$OUTPUT_DIR}/${next_deploy_script:-deploy.sh} to start services"
  info "  - Run: ${next_dir:-$OUTPUT_DIR}/${next_cleanup_script:-cleanup.sh} to stop and cleanup"
}

main "$@"
