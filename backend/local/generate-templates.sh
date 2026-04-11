#!/usr/bin/env bash
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
readonly CONFIG_DIR="$BACKEND_DIR/app/app-stack"
readonly GENERATOR_DIR="$SCRIPT_DIR/generator"
readonly TEMPLATES_DIR="$SCRIPT_DIR/templates"

APP_STACK="local.dev"
OUT_DIR="$SCRIPT_DIR/dist"
NORMALIZE_LINE_ENDINGS=true

info() { printf "[INFO] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*" >&2; }
error() { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

command_exists() { command -v "$1" >/dev/null 2>&1; }

show_usage() {
  cat <<EOF
Usage: $0 [OPTIONS]

Generate Atlas templates into a dist directory (no installation is executed).

Options:
  --app-stack=<name>    Pick app-stack.<name>.yml (default: local.compose)
  --out-dir=<path>      Output directory (default: backend/dist)
  --no-normalize        Skip line endings normalization
  -h, --help            Show this help message

Examples:
  $0
  $0 --app-stack=local.k8s
  $0 --app-stack=local.compose --out-dir=/tmp/atlas-dist
EOF
  exit "${1:-0}"
}

ensure_generator_deps() {
  command_exists node || error "Node.js is required to render templates. Please install Node.js."

  if (cd "$GENERATOR_DIR" && node -e "require('handlebars')" &>/dev/null); then
    return 0
  fi

  warn "Missing Node package 'handlebars' required by generator.mjs."
  command_exists npm || error "npm is required to install dependencies. Please install npm."

  info "Installing 'handlebars' in generator directory..."
  (
    cd "$GENERATOR_DIR" || exit 1
    [[ -f package.json ]] || { npm init -y &>/dev/null || true; }
    npm install handlebars --save || error "Failed to install 'handlebars'"
  )
}

reset_out_dir() {
  rm -rf "$OUT_DIR"
  mkdir -p "$OUT_DIR"
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

generate_templates() {
  ensure_generator_deps

  [[ -d "$TEMPLATES_DIR" ]] || error "Templates directory not found: $TEMPLATES_DIR"

  info "Generating dist files from templates..."
  (
    cd "$GENERATOR_DIR" || exit 1
    node generator.mjs \
      --dir "../templates" \
      --out-dir "$OUT_DIR" \
      --app-stack "$APP_STACK"
  )
}

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
      --out-dir=*)
        OUT_DIR="${1#--out-dir=}"
        [[ -n "$OUT_DIR" ]] || error "Missing value for --out-dir"
        shift
        ;;
      --out-dir)
        error "Invalid usage: use --out-dir=<path>"
        ;;
      --no-normalize)
        NORMALIZE_LINE_ENDINGS=false
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

main() {
  parse_arguments "$@"

  local config_file="$CONFIG_DIR/app-stack.${APP_STACK}.yml"
  [[ -f "$config_file" ]] || error "Configuration file not found: $config_file"

  info "Generating templates for app-stack: $APP_STACK"
  info "Reading configuration from: $config_file"
  info "Output directory: $OUT_DIR"

  reset_out_dir
  generate_templates

  if [[ "$NORMALIZE_LINE_ENDINGS" == true ]]; then
    normalize_line_endings "$OUT_DIR"
  fi

  info "Templates generated successfully."
}

main "$@"
