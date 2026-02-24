#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR"

CONFIG_DIR="$BACKEND_DIR/app-stack/config"
GENERATOR_DIR="$BACKEND_DIR/app-stack/generator"
TEMPLATES_DIR="$BACKEND_DIR/app-stack/templates"
DIST_DIR="$BACKEND_DIR/dist"

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*"; }
err()  { printf "[ERROR] %s\n" "$*"; }

usage() {
  local code="${1:-1}"
  echo "Usage: $0 [--app-stack=<stack-name>] [--skip-build] [--infra-only] [--enable-observability=<true|false>] [--debug-template]"
  echo ""
  echo "Options:"
  echo "  --app-stack=<stack-name>              Pick config/app-stack.<stack-name>.yml"
  echo "  --skip-build                          Pass '--skip-build' to install.sh"
  echo "  --infra-only                          Pass '--infra-only' to install.sh"
  echo "  --enable-observability=<true|false>   Pass '--enable-observability=<true|false>' to install.sh"
  echo "  --debug-template                      Skip install.sh execution"
  echo "  -h, --help                            Show help and exit"
  echo ""
  echo "Defaults:"
  echo "  - app-stack: local.compose"
  echo "  - skip-build: No"
  echo "  - infra-only: No"
  echo "  - enable-observability: true"
  exit "$code"
}

ensure_generator_deps() {
  # Ensure Node.js is available for template rendering
  if ! command -v node >/dev/null 2>&1; then
    err "Node.js is required to render templates. Please install Node.js."
    exit 1
  fi

  # Check if we're in the generator directory and handlebars is available
  if (cd "$GENERATOR_DIR" && node -e "try{require('handlebars');}catch(e){process.exit(1)}" >/dev/null 2>&1); then
    return 0
  fi

  warn "Missing Node package 'handlebars' required by generator.mjs."
  if ! command -v npm >/dev/null 2>&1; then
    err "npm is required to install dependencies. Please install npm (Node.js)."
    exit 1
  fi

  printf "Install 'handlebars' in generator directory now? [Y/n] "
  read -r answer
  answer=${answer:-Y}
  case "$answer" in
    Y|y|Yes|yes)
      (
        cd "$GENERATOR_DIR" || exit 1
        if [[ ! -f package.json ]]; then
          info "Initializing npm in $GENERATOR_DIR"
          npm init -y >/dev/null 2>&1 || true
        fi
        info "Installing handlebars in $GENERATOR_DIR"
        npm install handlebars --save || {
          err "Failed to install 'handlebars'."
          exit 1
        }
      )
      ;;
    *)
      err "Cannot proceed without 'handlebars'. Aborting."
      exit 1
      ;;
  esac
}

read_deployment_from_config() {
  # Read deployment value from the app-stack config file
  local config_file="$1"
  if [[ -f "$config_file" ]]; then
    # Parse YAML to get deployment value
    # Simple grep approach for deployment field
    local deployment
    deployment=$(grep '^deployment:' "$config_file" | sed 's/deployment:[[:space:]]*//' | tr -d '[:space:]')
    if [[ -n "$deployment" ]]; then
      echo "$deployment"
    else
      warn "No deployment field found in $config_file, defaulting to local.compose"
      echo "local.compose"
    fi
  else
    err "Config file not found: $config_file"
    exit 1
  fi
}

reset_dist_dir() {
  # Clean and recreate dist directory
  if [[ -d "$DIST_DIR" ]]; then
    rm -rf "$DIST_DIR"
  fi
  mkdir -p "$DIST_DIR"
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

generate_templates() {
  # Generate templates using the generator script
  local deployment="$1"
  local infra_only="$2"
  local enable_observability="$3"
  local app_stack="$4"
  
  ensure_generator_deps

  info "Generating dist files from templates..."
  local template_dir
  local template_rel_path
  case "$deployment" in
    local-compose)
      template_dir="$TEMPLATES_DIR/local/compose"
      template_rel_path="../templates/local/compose"
      ;;
    local-k8s-native)
      template_dir="$TEMPLATES_DIR/local/k8s"
      template_rel_path="../templates/local/k8s"
      ;;
    *)
      err "Unsupported deployment type: $deployment"
      exit 1
      ;;
  esac

  if [[ -d "$template_dir" ]]; then
    (
      cd "$GENERATOR_DIR" || exit 1
      node generator.mjs \
        --dir "$template_rel_path" \
        --out-dir "../../dist" \
        --app-stack "$app_stack" \
        --infra-only "$infra_only" \
        --enable-observability "$enable_observability"
    )
  else
    err "Templates directory not found: $template_dir"
    exit 1
  fi
}

execute_install_script() {
  # Execute install.sh if it exists in the dist directory
  local install_script="$DIST_DIR/install.sh"
  if [[ ! -f "$install_script" && -f "$DIST_DIR/native/install.sh" ]]; then
    install_script="$DIST_DIR/native/install.sh"
  fi
  local install_args=("$@")
  if [[ -f "$install_script" ]]; then
    info "Executing install script: $install_script"
    chmod +x "$install_script"
    "$install_script" "${install_args[@]}"
  else
    warn "install.sh not found in $DIST_DIR, skipping installation step"
  fi
}

main() {
  local app_stack="local.compose"
  local skip_build=false
  local infra_only=false
  local infra_only_specified=false
  local enable_observability=true
  local enable_observability_specified=false
  local debug_template=false

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        usage 0
        ;;
      --app-stack=*)
        app_stack="${1#--app-stack=}"
        if [[ -z "$app_stack" ]]; then
          err "Missing value for --app-stack"
          usage 1
        fi
        shift
        ;;
      --app-stack)
        echo "Invalid usage: use --app-stack=<stack-name>" >&2
        exit 1
        ;;
      --infra-only)
        infra_only=true
        infra_only_specified=true
        skip_build=true
        shift
        ;;
      --enable-observability=*)
        enable_observability="${1#--enable-observability=}"
        if [[ -z "$enable_observability" ]]; then
          err "Missing value for --enable-observability"
          usage 1
        fi
        enable_observability_specified=true
        shift
        ;;
      --enable-observability)
        echo "Invalid usage: use --enable-observability=true|false" >&2
        exit 1
        ;;
      --skip-build)
        skip_build=true
        shift
        ;;
      --debug-template)
        debug_template=true
        shift
        ;;
      -*)
        err "Unsupported option: $1"
        usage 1
        ;;
      *)
        err "Unsupported argument: $1"
        usage 1
        ;;
    esac
  done

  enable_observability="$(echo "$enable_observability" | tr '[:upper:]' '[:lower:]')"
  if [[ "$enable_observability" != "true" && "$enable_observability" != "false" ]]; then
    err "Invalid value for --enable-observability: $enable_observability"
    usage 1
  fi

  local install_args=()
  if [[ "$skip_build" == "true" ]]; then
    install_args+=("--skip-build")
  fi

  local config_file="$CONFIG_DIR/app-stack.${app_stack}.yml"
  
  info "Starting Atlas deployment for app-stack: $app_stack"
  
  # Step 1: Check if config file exists
  if [[ ! -f "$config_file" ]]; then
    err "Configuration file not found: $config_file"
    err "Available configurations:"
    if [[ -d "$CONFIG_DIR" ]]; then
      find "$CONFIG_DIR" -name "app-stack.*.yml" -exec basename {} \; | sed 's/app-stack\.\(.*\)\.yml/  \1/'
    fi
    exit 1
  fi
  
  info "Reading configuration from: $config_file"
  
  # Step 2: Read deployment value from config
  local deployment
  deployment=$(read_deployment_from_config "$config_file")
  info "Deployment type: $deployment"

  # Step 3: Reset dist directory
  reset_dist_dir

  # Step 4: Generate templates
  generate_templates "$deployment" "$infra_only" "$enable_observability" "$app_stack"
  info "Generated files are available in: $DIST_DIR"

  # Step 5: Normalize line endings
  normalize_line_endings_to_lf "$DIST_DIR"

  # Step 6: Execute install.sh
  if [[ "$debug_template" == "true" ]]; then
    info "Debug template mode enabled. Skipping install script execution."
  else
    execute_install_script "${install_args[@]}"
    info "Atlas installation completed successfully"
  fi  
}

main "$@"
