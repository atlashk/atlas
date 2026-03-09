#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# CONFIGURATION
# =============================================================================

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly BACKEND_DIR="$SCRIPT_DIR"
readonly CONFIG_DIR="$BACKEND_DIR/app-stack/config"
readonly GENERATOR_DIR="$BACKEND_DIR/app-stack/deployment/generator"
readonly TEMPLATES_DIR="$BACKEND_DIR/app-stack/deployment/templates"
readonly DIST_DIR="$BACKEND_DIR/dist"

APP_STACK="local.compose"
SKIP_BUILD=false
DEBUG_TEMPLATE=false

# =============================================================================
# UTILITIES
# =============================================================================

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*" >&2; }
error()  { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

command_exists() { command -v "$1" >/dev/null 2>&1; }

# =============================================================================
# USAGE
# =============================================================================

show_usage() {
  cat <<EOF
Usage: $0 [OPTIONS]

Atlas installation orchestrator - generates and executes installation scripts

Options:
  --app-stack=<name>    Pick config/app-stack.<name>.yml (default: local.compose)
  --skip-build          Skip backend and Docker image builds
  --debug-template      Generate templates only, skip install execution
  -h, --help            Show this help message

Examples:
  $0                                # Deploy with local.compose config
  $0 --app-stack=local.k8s.native   # Deploy with local.k8s.native config
  $0 --skip-build                   # Deploy without rebuilding
  $0 --debug-template               # Generate templates only
EOF
  exit "${1:-0}"
}

# =============================================================================
# PREREQUISITES
# =============================================================================

ensure_generator_deps() {
  command_exists node || error "Node.js is required to render templates. Please install Node.js."

  # Check if handlebars is available
  if (cd "$GENERATOR_DIR" && node -e "require('handlebars')" &>/dev/null); then
    return 0
  fi

  warn "Missing Node package 'handlebars' required by generator.mjs."
  command_exists npm || error "npm is required to install dependencies. Please install npm."

  info "Installing 'handlebars' in generator directory..."
  (
    cd "$GENERATOR_DIR" || exit 1
    [[ -f package.json ]] || { info "Initializing npm in $GENERATOR_DIR"; npm init -y &>/dev/null || true; }
    npm install handlebars --save || error "Failed to install 'handlebars'"
  )
}

# =============================================================================
# FILE OPERATIONS
# =============================================================================

reset_dist_dir() {
  rm -rf "$DIST_DIR"
  mkdir -p "$DIST_DIR"
}

normalize_line_endings() {
  local dir="$1"
  [[ -d "$dir" ]] || return 0

  if command_exists dos2unix; then
    find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -exec dos2unix -q {} \;
  else
    while IFS= read -r -d '' f; do
      awk '{ sub(/\r$/, ""); print }' "$f" > "$f.tmp" && mv "$f.tmp" "$f"
    done < <(find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -print0)
  fi

  find "$dir" -type f -name "*.sh" -exec chmod +x {} \;
}

# =============================================================================
# TEMPLATE GENERATION
# =============================================================================

resolve_template_path() {
  case "$APP_STACK" in
    local.dev)        echo "local/compose" ;;
    local.compose)    echo "local/compose" ;;
    local.k8s.native) echo "local/k8s/native" ;;
    local.k8s.helm)   echo "local/k8s/helm" ;;
    *)                error "Could not resolve template path for app stack: $APP_STACK" ;;
  esac
}

generate_templates() {
  ensure_generator_deps

  local template_rel_path
  template_rel_path=$(resolve_template_path)
  local template_dir="$TEMPLATES_DIR/$template_rel_path"

  [[ -d "$template_dir" ]] || error "Templates directory not found: $template_dir"

  info "Generating dist files from templates..."
  (
    cd "$GENERATOR_DIR" || exit 1
    node generator.mjs \
      --dir "../templates/$template_rel_path" \
      --out-dir "../../../dist" \
      --app-stack "$APP_STACK"
  )
}

# =============================================================================
# Installation
# =============================================================================

execute_install_script() {
  local install_script="$DIST_DIR/install.sh"
  [[ -f "$install_script" ]] || install_script="$DIST_DIR/native/install.sh"

  if [[ -f "$install_script" ]]; then
    info "Executing install script: $install_script"
    chmod +x "$install_script"
    "$install_script" "$@"
  else
    warn "install.sh not found in $DIST_DIR, skipping installation step"
  fi
}

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

parse_arguments() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        show_usage 0
        ;;
      --app-stack=*)
        APP_STACK="${1#--app-stack=}"
        [[ -n "$APP_STACK" ]] || error "Missing value for --app-stack"
        shift
        ;;
      --app-stack)
        error "Invalid usage: use --app-stack=<stack-name>"
        ;;
      --skip-build)
        SKIP_BUILD=true
        shift
        ;;
      --debug-template)
        DEBUG_TEMPLATE=true
        shift
        ;;
      -*)
        error "Unsupported option: $1"
        ;;
      *)
        error "Unsupported argument: $1"
        ;;
    esac
  done
}

list_available_configs() {
  if [[ -d "$CONFIG_DIR" ]]; then
    info "Available configurations:"
    find "$CONFIG_DIR" -name "app-stack.*.yml" -exec basename {} \; | sed 's/app-stack\.\(.*\)\.yml/  - \1/'
  fi
}

# =============================================================================
# MAIN
# =============================================================================

main() {
  parse_arguments "$@"

  local config_file="$CONFIG_DIR/app-stack.${APP_STACK}.yml"

  # Validate config file
  if [[ ! -f "$config_file" ]]; then
    error "Configuration file not found: $config_file$(echo; list_available_configs)"
  fi

  info "Starting Atlas installation for app-stack: $APP_STACK"

  info "Reading configuration from: $config_file"

  # Generate templates
  reset_dist_dir
  generate_templates
  info "Generated files are available in: $DIST_DIR"

  # Normalize line endings
  normalize_line_endings "$DIST_DIR"

  # Execute install script
  if [[ "$DEBUG_TEMPLATE" == true ]]; then
    info "Debug template mode enabled. Skipping install script execution."
  else
    local install_args=()
    [[ "$SKIP_BUILD" == true ]] && install_args+=("--skip-build")

    execute_install_script "${install_args[@]}"
    info "Installation completed successfully!"
  fi
}

main "$@"
