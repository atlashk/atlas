#!/bin/bash

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Load logger and common utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

# Default options
FORCE_CLEANUP=false

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Platform Cleanup Script - Cleans up Atlas platform resources"
    log_info ""
    log_info "Options:"
    log_info "  --force             Force cleanup without confirmation"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Cleanup Atlas platform with confirmation"
    log_info "  $0 --force          # Force cleanup without confirmation"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --force)
            FORCE_CLEANUP=true
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            log_info "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Cleanup based on platform configuration
cleanup_platform() {
    local cleanup_args=()
    
    # Add force argument if specified
    if [[ "$FORCE_CLEANUP" == true ]]; then
        cleanup_args+=("--force")
    fi
    
    local cleanup_script
    cleanup_script=$(get_deployment_script "$PROJECT_ROOT" "$PLATFORM" "cleanup")
    validate_deployment_script "$cleanup_script" "cleanup"
    execute_deployment_script "$cleanup_script" "$PLATFORM" "cleanup" "${cleanup_args[@]}"
}

# Main execution
read_platform_config "$APP_STACK_CONFIG"
cleanup_platform
