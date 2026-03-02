#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# CONFIGURATION
# =============================================================================

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly DIST_DIR="$SCRIPT_DIR/dist"
readonly REBUILD_SCRIPT="$DIST_DIR/rebuild.sh"

# =============================================================================
# UTILITIES
# =============================================================================

info() { printf "[INFO] %s\n" "$*"; }
die()  { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

# =============================================================================
# USAGE
# =============================================================================

show_usage() {
  cat <<EOF
Usage: $0 <service-name>

Rebuild and redeploy a single service

Arguments:
  <service-name>     Service name to rebuild (required)

Examples:
  $0 identity-service
  $0 api-gateway
EOF
  exit "${1:-0}"
}

# =============================================================================
# MAIN
# =============================================================================

main() {
  [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]] && show_usage 0
  [[ $# -eq 0 ]] && { die "Missing required argument: service-name"; }

  local service_name="$1"

  [[ -f "$REBUILD_SCRIPT" ]] || die "Rebuild script not found: $REBUILD_SCRIPT\nPlease run ./install.sh first to generate deployment scripts"

  info "Executing rebuild script for service: $service_name"
  chmod +x "$REBUILD_SCRIPT"
  "$REBUILD_SCRIPT" "$service_name"

  info "Rebuild completed successfully!"
}

main "$@"
