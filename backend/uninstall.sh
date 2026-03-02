#!/usr/bin/env bash
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly DIST_DIR="$SCRIPT_DIR/dist"
readonly UNINSTALL_SCRIPT="$DIST_DIR/uninstall.sh"

info() { printf "[INFO] %s\n" "$*"; }
die()  { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

[[ -f "$UNINSTALL_SCRIPT" ]] || die "Uninstall script not found: $UNINSTALL_SCRIPT\nPlease run ./install.sh first to generate deployment scripts"

info "Executing uninstall script: $UNINSTALL_SCRIPT"
chmod +x "$UNINSTALL_SCRIPT"
"$UNINSTALL_SCRIPT" "$@"
info "Uninstallation completed successfully! 🚀"
