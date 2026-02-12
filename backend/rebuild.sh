#!/bin/bash
set -euo pipefail

SERVICE_NAME=""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR"
DIST_DIR="$BACKEND_DIR/dist"
REBUILD_SCRIPT="$DIST_DIR/rebuild.sh"

info() { printf "[INFO] %s\n" "$*"; }
err()  { printf "[ERROR] %s\n" "$*"; }

show_usage() {
  echo "Usage: $0 <service-name>"
  echo ""
  echo "Arguments:"
  echo "  <service-name>     Service name to rebuild (required)"
  echo ""
  echo "Examples:"
  echo "  $0 product-service"
  echo "  $0 api-gateway"
}

parse_args() {
  if [[ $# -eq 0 ]]; then
    err "Missing required argument: service-name"
    echo ""
    show_usage
    exit 1
  fi

  case "$1" in
    -h|--help)
      show_usage
      exit 0
      ;;
    *)
      SERVICE_NAME="$1"
      ;;
  esac
}

parse_args "$@"

if [[ -z "$SERVICE_NAME" ]]; then
  err "Missing service name"
  echo ""
  show_usage
  exit 1
fi

if [[ ! -f "$REBUILD_SCRIPT" ]]; then
  err "Rebuild script not found: $REBUILD_SCRIPT"
  err "Please run ./install.sh first to generate deployment scripts"
  exit 1
fi

info "Executing rebuild script for service: $SERVICE_NAME"
chmod +x "$REBUILD_SCRIPT"

"$REBUILD_SCRIPT" "$SERVICE_NAME"

info "Atlas rebuild completed successfully"
