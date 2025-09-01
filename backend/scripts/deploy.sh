#!/bin/bash

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Load logger and common utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

# Default options
SKIP_BUILD=false

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Platform Startup Script - Starts the complete Atlas platform"
    log_info ""
    log_info "Options:"
    log_info "  --skip-build        Skip all build steps (backend JAR, Docker images)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Start with builds"
    log_info "  $0 --skip-build     # Start without builds"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            log_info "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Main execution
read_platform_config "$APP_STACK_CONFIG"

# Add skip-build argument if specified
if [[ "$SKIP_BUILD" == true ]]; then
    deploy_args+=("--skip-build")
fi

deploy_script=$(get_deployment_script "$PROJECT_ROOT" "$PLATFORM" "deploy")

validate_deployment_script "$deploy_script" "deploy"

execute_deployment_script "$deploy_script" "$PLATFORM" "deployment" "${deploy_args[@]}"
