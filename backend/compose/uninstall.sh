#!/usr/bin/env bash
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly DIST_DIR="$SCRIPT_DIR/local/dist"
readonly UNINSTALL_SCRIPT="$DIST_DIR/uninstall.sh"

# Default options
REMOVE_IMAGES=false

info() { printf "[INFO] %s\n" "$*"; }
error() { printf "[ERROR] %s\n" "$*" >&2; exit 1; }

usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Options:
    --remove-images    Remove Docker application images (default: false)
    -h, --help         Show this help message

Examples:
    $(basename "$0")                    # Uninstall without removing Docker application images
    $(basename "$0") --remove-images    # Uninstall and remove Docker application images
EOF
    exit 0
}

# Parse arguments
PASSTHROUGH_ARGS=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --remove-images)
            REMOVE_IMAGES=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            PASSTHROUGH_ARGS+=("$1")
            shift
            ;;
    esac
done

[[ -f "$UNINSTALL_SCRIPT" ]] || error "Uninstall script not found: $UNINSTALL_SCRIPT\nPlease run ./install.sh first."

info "Executing uninstall script: $UNINSTALL_SCRIPT"
info "Remove application images: $REMOVE_IMAGES"
chmod +x "$UNINSTALL_SCRIPT"

# Pass remove-images option to uninstall script
if [[ "$REMOVE_IMAGES" == "true" ]]; then
    "$UNINSTALL_SCRIPT" --remove-images "${PASSTHROUGH_ARGS[@]}"
else
    "$UNINSTALL_SCRIPT" "${PASSTHROUGH_ARGS[@]}"
fi

info "Uninstallation completed successfully!"
