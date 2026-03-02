#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR"
DIST_DIR="$BACKEND_DIR/dist"
UNINSTALL_SCRIPT="$DIST_DIR/uninstall.sh"

info() { printf "[INFO] %s\n" "$*"; }
err()  { printf "[ERROR] %s\n" "$*"; }

if [[ ! -f "$UNINSTALL_SCRIPT" ]]; then
  err "Uninstall script not found: $UNINSTALL_SCRIPT"
  exit 1
fi

info "Executing uninstall script: $UNINSTALL_SCRIPT"
chmod +x "$UNINSTALL_SCRIPT"
"$UNINSTALL_SCRIPT" "$@"
info "Uninstallation completed successfully! 🚀"
