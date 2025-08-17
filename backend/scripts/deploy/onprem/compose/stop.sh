#!/bin/bash

# =============================================================================
# Atlas Docker Compose Stop Script
# =============================================================================
# This script stops the Atlas microservices platform using Docker Compose
# =============================================================================

set -euo pipefail

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/backend/scripts/deploy/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/backend/scripts/logger.sh"

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD=""

# =============================================================================
# ARGUMENT PARSING & PRE-CHECKS
# =============================================================================

show_help() {
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Docker Compose Stop Script - Stops Atlas services"
    log_info ""
    log_info "This script STOPS Atlas services but preserves containers for easy restart."
    log_info ""
    log_info "Options:"
    log_info "  -h, --help              Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                      # Stop all Atlas services"
    log_info ""
    log_info "Note: To completely remove resources, use ./cleanup.sh instead"
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                log_info "Use --help for usage information"
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        return 1
    fi
    log_success "Docker found and running"
    return 0
}

check_docker_compose() {
    # Check for both docker-compose and docker compose (newer version)
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
        log_success "Docker Compose (standalone) found"
        return 0
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker compose"
        log_success "Docker Compose (plugin) found"
        return 0
    else
        log_error "Docker Compose is not installed or not available"
        log_info "Please install Docker Compose or ensure Docker Desktop is running"
        return 1
    fi
}

check_prerequisites() {
    log_section "Checking prerequisites..."
    
    check_docker || exit 1
    check_docker_compose || exit 1
    
    log_success "Prerequisites check passed"
}

# =============================================================================
# STOP FUNCTIONS
# =============================================================================

# Stop all services
stop_services() {
    log_info "Using compose file: $COMPOSE_FILE"
    log_info "Stopping all Atlas services..."
    
    if ! $DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" stop; then
        log_error "Failed to stop Atlas services"
        log_info "You can check running containers with: docker ps"
        log_info "You can force stop with: docker stop \$(docker ps -q --filter name=$PROJECT_NAME)"
        exit 1
    fi
    
    log_success "All services stopped successfully!"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    log_section "Atlas Docker Compose Platform - Stopping"
    
    stop_services
    
    log_success "Atlas platform stopped successfully!"
}

# Execute main function
main "$@"
