#!/bin/bash

set -euo pipefail

# Project configuration (previously in config.sh)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/deployment/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Check Docker Compose prerequisites (previously in docker-helper.sh)
check_docker_compose_prerequisites() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
}

# Stop all services
stop_services() {
    log_info "Using unified compose file: $COMPOSE_FILE"
    log_info "Stopping all Atlas services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" stop
    log_success "All services stopped successfully!"
}

# Main execution
main() {
    log_info "Stopping Atlas services..."
    check_docker_compose_prerequisites
    stop_services
}

# Run main function
main "$@"
