#!/bin/bash

set -euo pipefail

# Source config and helper scripts
source "$(dirname "${BASH_SOURCE[0]}")/config.sh"
source "$(dirname "${BASH_SOURCE[0]}")/docker-helper.sh"

# Stop all services
stop_services() {
    log_info "Using unified compose file: $COMPOSE_FILE"
    log_info "Stopping all Atlas services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" down
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
