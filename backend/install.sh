#!/bin/bash

set -euo pipefail

# -------------------------------------------------------------
# Atlas Deployment Script
# -------------------------------------------------------------
# Goal:
# - Read app-stack configuration from config directory
# - Generate files from templates in buildSrc and deployment directories
# - Execute install.sh script
#
# Usage:
#   ./install.sh [--app-stack <name>] [--skip-build] [--infra-only]
#
# Parameters:
#   app-stack: Configuration name (e.g., onprem.compose, dev, onprem.k8s.native); via --app-stack
#   skip-build: Optional; default false
#   infra-only: Optional; default false
#              If app-stack is 'dev' and infra-only not specified, default true
#
# Notes:
# - Requires Node.js; will install 'ejs' if missing (via npm).
# - On Windows, recommended to run in Git Bash or WSL.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR"

CONFIG_DIR="$BACKEND_DIR/config"
GENERATOR_DIR="$BACKEND_DIR/deployment/generator"
TEMPLATES_DIR="$BACKEND_DIR/deployment/templates"
BUILDSRC_TEMPLATES_DIR="$TEMPLATES_DIR/buildSrc"
DEPLOYMENT_TEMPLATES_DIR="$TEMPLATES_DIR/deployment"
DIST_DIR="$BACKEND_DIR/deployment/dist"

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
  echo "  - app-stack: onprem.compose"
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
      warn "No deployment field found in $config_file, defaulting to onprem-compose"
      echo "onprem-compose"
    fi
  else
    err "Config file not found: $config_file"
    exit 1
  fi
}

convert_yaml_to_cfg() {
  # Convert YAML config to .cfg format for the generator
  local yaml_file="$1"
  local cfg_file="$2"
  
  info "Converting $yaml_file to $cfg_file"
  
  # Create a temporary Node.js script for conversion
  local temp_script="$DIST_DIR/yaml_converter.js"
  cat > "$temp_script" << 'EOF'
const fs = require('fs');
const path = require('path');

function parseYaml(content) {
  const lines = content.split(/\r?\n/);
  const result = {};
  const stack = [];
  
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    
    const indent = line.match(/^\s*/)[0].length;
    const level = Math.floor(indent / 2);
    
    // Adjust stack to current level
    stack.length = level;
    
    const colonIndex = trimmed.indexOf(':');
    if (colonIndex === -1) continue;
    
    const key = trimmed.substring(0, colonIndex).trim();
    const value = trimmed.substring(colonIndex + 1).trim();
    
    const fullKey = stack.length > 0 ? stack.join('.') + '.' + key : key;
    
    if (value) {
      result[fullKey] = value;
    } else {
      stack.push(key);
    }
  }
  
  return result;
}

const yamlFile = process.argv[2];
const cfgFile = process.argv[3];

try {
  const yamlContent = fs.readFileSync(yamlFile, 'utf8');
  const parsed = parseYaml(yamlContent);
  
  const cfgContent = Object.entries(parsed)
    .map(([key, value]) => `${key}=${value}`)
    .join('\n');
  
  fs.writeFileSync(cfgFile, cfgContent, 'utf8');
  console.log('Conversion successful');
} catch (error) {
  console.error('Error:', error.message);
  process.exit(1);
}
EOF

  # Run the conversion script
  node "$temp_script" "$yaml_file" "$cfg_file" || {
    err "Failed to convert YAML to cfg format"
    exit 1
  }
  
  # Clean up temporary script
  rm -f "$temp_script"
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
  local cfg_file="$1"
  local deployment="$2"
  local infra_only="$3"
  local enable_observability="$4"
  local app_stack="$5"
  
  ensure_generator_deps
  
  info "Generating buildSrc templates..."
  if [[ -d "$BUILDSRC_TEMPLATES_DIR" ]]; then
    (
      cd "$GENERATOR_DIR" || exit 1
      node generator.mjs \
        --dir "../templates/buildSrc" \
        --out-dir "../dist" \
        --cfg "$cfg_file" \
        --infra-only "$infra_only" \
        --enable-observability "$enable_observability" \
        --app-stack "$app_stack"
    )
  else
    warn "buildSrc templates directory not found: $BUILDSRC_TEMPLATES_DIR"
  fi
  
  info "Generating deployment templates for: $deployment"
  local deployment_template_dir
  local deployment_template_rel_path
  case "$deployment" in
    onprem-compose)
      deployment_template_dir="$DEPLOYMENT_TEMPLATES_DIR/onprem/compose"
      deployment_template_rel_path="../templates/deployment/onprem/compose"
      ;;
    onprem-k8s-native)
      deployment_template_dir="$DEPLOYMENT_TEMPLATES_DIR/onprem/k8s"
      deployment_template_rel_path="../templates/deployment/onprem/k8s"
      ;;
    *)
      err "Unsupported deployment type: $deployment"
      exit 1
      ;;
  esac
  
  if [[ -d "$deployment_template_dir" ]]; then
    (
      cd "$GENERATOR_DIR" || exit 1
      node generator.mjs \
        --dir "$deployment_template_rel_path" \
        --out-dir "../dist" \
        --cfg "$cfg_file" \
        --infra-only "$infra_only" \
        --enable-observability "$enable_observability" \
        --app-stack "$app_stack"
    )
  else
    err "Deployment templates directory not found: $deployment_template_dir"
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
  local app_stack="onprem.compose"
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
  local temp_cfg_file="$DIST_DIR/app-stack.cfg"
  
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

  # Step 4: Convert YAML to cfg format for generator
  convert_yaml_to_cfg "$config_file" "$temp_cfg_file"

  # Step 5: Generate templates
  generate_templates "$temp_cfg_file" "$deployment" "$infra_only" "$enable_observability" "$app_stack"
  info "Generated files are available in: $DIST_DIR"

  # Step 6: Normalize line endings
  normalize_line_endings_to_lf "$DIST_DIR"

  # Step 7: Execute install.sh
  if [[ "$debug_template" == "true" ]]; then
    info "Debug template mode enabled. Skipping install script execution."
  else
    execute_install_script "${install_args[@]}"
    info "Atlas installation completed successfully"
  fi  
}

main "$@"
