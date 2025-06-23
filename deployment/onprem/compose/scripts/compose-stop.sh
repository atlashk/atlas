#!/bin/bash

# =============================================================================
# Atlas Docker Compose Stop Script
# =============================================================================
# This script stops the Atlas microservices platform using Docker Compose
# =============================================================================

set -euo pipefail

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/deployment/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0"
    log_info ""
    log_info "Atlas Docker Compose Stop Script - Stops Atlas services"
    log_info ""
    log_info "This script STOPS Atlas services but preserves containers for easy restart."
    log_info "To completely remove resources, use ./compose-clean.sh instead"
    exit 0
fi

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# =============================================================================
# STOP FUNCTIONS
# =============================================================================

# Stop all services
stop_services() {
    log_info "Using compose file: $COMPOSE_FILE"
    log_info "Stopping all Atlas services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" stop
    log_success "All services stopped successfully!"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    log_section "Atlas Docker Compose Platform - Stopping"
    
    check_prerequisites
    stop_services
    
    log_success "Atlas platform stopped successfully!"
}

# Run main function
main
