#!/bin/bash

set -euo pipefail

# Source config and helper scripts
source "$(dirname "${BASH_SOURCE[0]}")/config.sh"
source "$(dirname "${BASH_SOURCE[0]}")/docker-helper.sh"

# Start all services
start_services() {
    log_info "Using unified compose file: $COMPOSE_FILE"
    log_info "Starting all Atlas services..."

    # Start all services defined in the compose file
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" up -d

    log_success "All services started successfully!"

    # Display service URLs
    log_info "Service URLs:"
    log_info "  - API Gateway: http://localhost:8080"
    log_info "  - Prometheus: http://localhost:9090"
    log_info "  - Grafana: http://localhost:3000"
    log_info "  - Zipkin: http://localhost:9411"
    log_info "  - Frontend: http://localhost:80"
}

# Main execution
main() {
    log_info "Starting Atlas services..."
    
    check_docker_compose_prerequisites
    start_services
}

# Run main function
main "$@"
