#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# CONFIGURATION
# =============================================================================

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly BACKEND_DIR="$SCRIPT_DIR"
readonly CONFIG_DIR="$BACKEND_DIR/app-stack/config"
readonly DIST_DIR="$BACKEND_DIR/dist"
readonly GENERATE_TEMPLATES_SCRIPT="$BACKEND_DIR/generate-templates.sh"

APP_STACK="local.dev"
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
  --app-stack=<name>    Pick config/app-stack.<name>.yml (default: local.dev)
  --skip-build          Skip backend and Docker image builds
  --debug-template      Generate templates only, skip install execution
  -h, --help            Show this help message

Examples:
  $0                                # Deploy with local.compose config
  $0 --app-stack=local.k8s          # Deploy with local.k8s config
  $0 --skip-build                   # Deploy without rebuilding
  $0 --debug-template               # Generate templates only
EOF
  exit "${1:-0}"
}

# =============================================================================
# TEMPLATE GENERATION
# =============================================================================

generate_templates() {
  [[ -f "$GENERATE_TEMPLATES_SCRIPT" ]] || error "Template generator script not found: $GENERATE_TEMPLATES_SCRIPT"
  chmod +x "$GENERATE_TEMPLATES_SCRIPT" 2>/dev/null || true
  "$GENERATE_TEMPLATES_SCRIPT" --app-stack="$APP_STACK" --out-dir="$DIST_DIR"
}

# =============================================================================
# INSTALLATION
# =============================================================================

execute_install_script() {
  local install_script="$DIST_DIR/install.sh"
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
    find "$CONFIG_DIR" -name "app-stack.*.yml" -exec basename {} \; | \
      sed 's/app-stack\.\(.*\)\.yml/  - \1/' | sort
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
  generate_templates
  info "Generated files are available in: $DIST_DIR"

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
