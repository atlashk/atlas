#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# CONFIGURATION
# =============================================================================

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly BACKEND_DIR="$SCRIPT_DIR"
readonly CONFIG_DIR="$BACKEND_DIR/app-stack/config"
readonly GENERATOR_DIR="$BACKEND_DIR/app-stack/generator"
readonly TEMPLATES_DIR="$BACKEND_DIR/app-stack/templates"
readonly DIST_DIR="$BACKEND_DIR/dist"

# =============================================================================
# UTILITIES
# =============================================================================

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*" >&2; }
die()  { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

command_exists() { command -v "$1" >/dev/null 2>&1; }

# =============================================================================
# USAGE
# =============================================================================

show_usage() {
  cat <<EOF
Usage: $0 [OPTIONS]

Atlas deployment orchestrator - generates and executes deployment scripts

Options:
  --app-stack=<name>    Pick config/app-stack.<name>.yml (default: local.compose)
  --skip-build          Skip backend and Docker image builds
  --infra-only          Deploy only infrastructure (implies --skip-build)
  --debug-template      Generate templates only, skip install execution
  -h, --help            Show this help message

Examples:
  $0                              # Deploy with local.compose config
  $0 --app-stack=local.k8s        # Deploy with local.k8s config
  $0 --skip-build                 # Deploy without rebuilding
  $0 --debug-template             # Generate templates only
EOF
  exit "${1:-0}"
}

# =============================================================================
# PREREQUISITES
# =============================================================================

ensure_generator_deps() {
  command_exists node || die "Node.js is required to render templates. Please install Node.js."

  # Check if handlebars is available
  if (cd "$GENERATOR_DIR" && node -e "require('handlebars')" &>/dev/null); then
    return 0
  fi

  warn "Missing Node package 'handlebars' required by generator.mjs."
  command_exists npm || die "npm is required to install dependencies. Please install npm."

  printf "Install 'handlebars' in generator directory now? [Y/n] "
  read -r answer
  case "${answer:-Y}" in
    [Yy]|[Yy]es)
      (
        cd "$GENERATOR_DIR" || exit 1
        [[ -f package.json ]] || { info "Initializing npm in $GENERATOR_DIR"; npm init -y &>/dev/null || true; }
        info "Installing handlebars..."
        npm install handlebars --save || die "Failed to install 'handlebars'"
      )
      ;;
    *)
      die "Cannot proceed without 'handlebars'. Aborting."
      ;;
  esac
}

# =============================================================================
# CONFIG PARSING
# =============================================================================

read_deployment_from_config() {
  local config_file="$1"
  [[ -f "$config_file" ]] || die "Config file not found: $config_file"

  local deployment
  deployment=$(grep '^deployment:' "$config_file" | sed 's/deployment:[[:space:]]*//' | tr -d '[:space:]')

  if [[ -n "$deployment" ]]; then
    echo "$deployment"
  else
    warn "No deployment field found in $config_file, defaulting to local.compose"
    echo "local.compose"
  fi
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
  local deployment="$1"

  case "$deployment" in
    local-compose)    echo "local/compose" ;;
    local-k8s-native) echo "local/k8s/native" ;;
    *)                die "Unsupported deployment type: $deployment" ;;
  esac
}

generate_templates() {
  local deployment="$1"
  local infra_only="$2"
  local app_stack="$3"

  ensure_generator_deps

  local template_rel_path
  template_rel_path=$(resolve_template_path "$deployment")
  local template_dir="$TEMPLATES_DIR/$template_rel_path"

  [[ -d "$template_dir" ]] || die "Templates directory not found: $template_dir"

  info "Generating dist files from templates..."
  (
    cd "$GENERATOR_DIR" || exit 1
    node generator.mjs \
      --dir "../templates/$template_rel_path" \
      --out-dir "../../dist" \
      --app-stack "$app_stack" \
      --infra-only "$infra_only"
  )
}

# =============================================================================
# DEPLOYMENT
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
  APP_STACK="local.compose"
  SKIP_BUILD=false
  INFRA_ONLY=false
  DEBUG_TEMPLATE=false

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        show_usage 0
        ;;
      --app-stack=*)
        APP_STACK="${1#--app-stack=}"
        [[ -n "$APP_STACK" ]] || die "Missing value for --app-stack"
        shift
        ;;
      --app-stack)
        die "Invalid usage: use --app-stack=<stack-name>"
        ;;
      --infra-only)
        INFRA_ONLY=true
        SKIP_BUILD=true
        shift
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
        die "Unsupported option: $1"
        ;;
      *)
        die "Unsupported argument: $1"
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

  info "Starting Atlas deployment for app-stack: $APP_STACK"

  # Validate config file
  if [[ ! -f "$config_file" ]]; then
    die "Configuration file not found: $config_file$(echo; list_available_configs)"
  fi

  info "Reading configuration from: $config_file"

  # Read deployment type
  local deployment
  deployment=$(read_deployment_from_config "$config_file")
  info "Deployment type: $deployment"

  # Generate templates
  reset_dist_dir
  generate_templates "$deployment" "$INFRA_ONLY" "$APP_STACK"
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
