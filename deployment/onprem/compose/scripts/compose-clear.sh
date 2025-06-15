#!/bin/bash

set -euo pipefail

# Source config and helper scripts
source "$(dirname "${BASH_SOURCE[0]}")/config.sh"
source "$(dirname "${BASH_SOURCE[0]}")/docker-helper.sh"

# Clear all volumes
clear_services() {
    log_info "Using unified compose file: $COMPOSE_FILE"
    log_info "Clearing all Atlas volumes..."
    remove_volumes "$COMPOSE_FILE" "$PROJECT_STACK"
    log_success "All volumes cleared successfully!"
}

# Main execution
main() {
    log_info "Clearing Atlas volumes..."
    check_docker_compose_prerequisites
    clear_services
}

# Run main function
main "$@"
