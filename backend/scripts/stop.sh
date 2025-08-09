#!/bin/bash

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Load logger and common utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Platform Stop Script - Stops the complete Atlas platform"
    log_info ""
    log_info "Options:"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Stop Atlas platform"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        *)
            log_error "Unknown option: $1"
            log_info "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Stop services based on platform configuration
stop_platform() {
    local stop_script
    stop_script=$(get_deployment_script "$PROJECT_ROOT" "$PLATFORM" "stop")
    validate_deployment_script "$stop_script" "stop"
    execute_deployment_script "$stop_script" "$PLATFORM" "stop" "$@"
}

# Main execution
read_platform_config "$APP_STACK_CONFIG"
stop_platform "$@"

log_success "Atlas platform stopped successfully with $PLATFORM!"
