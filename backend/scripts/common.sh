#!/bin/bash

# =============================================================================
# Atlas Common Utilities
# =============================================================================

# Get deployment script path based on platform and action
get_deployment_script() {
    local project_root="$1"
    local platform="$2"
    local action="$3"  # deploy, stop, cleanup
    
    local script_path=""
    
    case "$platform" in
        onprem-compose)
            script_path="$project_root/backend/scripts/deploy/onprem/compose/${action}.sh"
            ;;
        onprem-k8s)
            script_path="$project_root/backend/scripts/deploy/onprem/k8s/${action}.sh"
            ;;
        aws-ecs)
            script_path="$project_root/backend/scripts/deploy/aws/ecs/cdk/${action}.sh"
            ;;
        *)
            log_error "Unknown platform: $platform"
            log_error "Supported platforms: onprem-compose, onprem-k8s, aws-ecs"
            exit 1
            ;;
    esac
    
    echo "$script_path"
}

# Validate deployment script exists
validate_deployment_script() {
    local script_path="$1"
    local action="$2"
    
    if [ ! -f "$script_path" ]; then
        log_error "${action^} script not found: $script_path"
        exit 1
    fi
}

# Execute deployment script with arguments
execute_deployment_script() {
    local script_path="$1"
    local platform="$2"
    local action="$3"
    shift 3
    local script_args=("$@")
    
    log_info "Starting $platform $action..."
    if ! "$script_path" "${script_args[@]}"; then
        log_error "$platform $action failed. Exiting..."
        exit 1
    fi
}
