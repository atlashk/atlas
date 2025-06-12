#!/bin/bash

# Common utility functions

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if Docker is running
check_docker() {
    if ! command_exists docker; then
        log_error "Docker is not installed"
        return 1
    fi
    
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker is not running"
        return 1
    fi
    
    return 0
}

# Check if kubectl is available and configured
check_kubectl() {
    if ! command_exists kubectl; then
        log_error "kubectl is not installed"
        return 1
    fi
    
    if ! kubectl cluster-info >/dev/null 2>&1; then
        log_error "kubectl is not configured or cluster is not accessible"
        return 1
    fi
    
    return 0
}

# Wait for service to be ready
wait_for_service() {
    local service_name=$1
    local host=$2
    local port=$3
    local timeout=${4:-60}
    local interval=${5:-5}
    
    log_info "Waiting for $service_name to be ready at $host:$port..."
    
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if nc -z "$host" "$port" 2>/dev/null; then
            log_success "$service_name is ready!"
            return 0
        fi
        
        log_progress "Waiting for $service_name... (${elapsed}s/${timeout}s)"
        sleep $interval
        elapsed=$((elapsed + interval))
    done
    
    clear_progress
    log_error "$service_name failed to start within ${timeout}s"
    return 1
}

# Wait for HTTP endpoint to be ready
wait_for_http() {
    local service_name=$1
    local url=$2
    local timeout=${3:-60}
    local interval=${4:-5}
    
    log_info "Waiting for $service_name to be ready at $url..."
    
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            log_success "$service_name is ready!"
            return 0
        fi
        
        log_progress "Waiting for $service_name... (${elapsed}s/${timeout}s)"
        sleep $interval
        elapsed=$((elapsed + interval))
    done
    
    clear_progress
    log_error "$service_name failed to start within ${timeout}s"
    return 1
}

# Get project root directory
get_project_root() {
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    echo "$(cd "$script_dir/../.." && pwd)"
}

# Validate environment
validate_environment() {
    local env=$1
    local valid_envs=("local" "dev" "staging" "production")
    
    for valid_env in "${valid_envs[@]}"; do
        if [ "$env" = "$valid_env" ]; then
            return 0
        fi
    done
    
    log_error "Invalid environment: $env. Valid environments: ${valid_envs[*]}"
    return 1
}

# Get environment configuration
get_env_config() {
    local env=$1
    local config_key=$2
    local config_file="$(get_project_root)/deployment/environments/$env/config.yaml"
    
    if [ -f "$config_file" ]; then
        # Use yq to extract configuration if available
        if command_exists yq; then
            yq eval ".$config_key" "$config_file" 2>/dev/null
        else
            log_warn "yq not available, cannot read configuration from $config_file"
        fi
    fi
}

# Cleanup function for trap
cleanup() {
    log_info "Cleaning up..."
    # Add any cleanup logic here
}

# Set up signal handlers
setup_signal_handlers() {
    trap cleanup EXIT
    trap 'log_error "Script interrupted"; exit 130' INT
    trap 'log_error "Script terminated"; exit 143' TERM
} 