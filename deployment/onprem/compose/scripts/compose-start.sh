#!/bin/bash

set -euo pipefail

# Source config and helper scripts
source "$(dirname "${BASH_SOURCE[0]}")/config.sh"
source "$(dirname "${BASH_SOURCE[0]}")/docker-helper.sh"

# Start all services
start_services() {
    log_info "Using unified compose file: $COMPOSE_FILE"
    log_info "Starting all Atlas services in dependency order..."
    
    log_info "Step 1: Starting infrastructure services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" up -d "${INFRASTRUCTURE_SERVICES[@]}"
    
    # Wait for infrastructure services to be ready
    wait_for_services_ready "$PROJECT_STACK" "$COMPOSE_FILE" "${INFRASTRUCTURE_SERVICES[@]}"
    
    log_info "Step 2: Starting observability services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" up -d "${OBSERVABILITY_SERVICES[@]}"
    
    log_info "Step 3: Starting backend services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" up -d "${BACKEND_SERVICES[@]}"
    
    log_info "Step 4: Starting frontend service..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" up -d "${FRONTEND_SERVICES[@]}"
    
    # Check service health
    log_info "Checking service health..."
    for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
        check_service_health "$service" -f "$COMPOSE_FILE"
    done
    for service in "${OBSERVABILITY_SERVICES[@]}"; do
        check_service_health "$service" -f "$COMPOSE_FILE"
    done
    for service in "${BACKEND_SERVICES[@]}"; do
        check_service_health "$service" -f "$COMPOSE_FILE"
    done
    for service in "${FRONTEND_SERVICES[@]}"; do
        check_service_health "$service" -f "$COMPOSE_FILE"
    done
    
    log_success "All services started successfully!"
    
    # Display service URLs
    log_info "Services available at:"
    log_info "  - MySQL: localhost:3306"
    log_info "  - Redis: localhost:6379"
    log_info "  - Kafka: localhost:9092"
    log_info "  - SMTP4Dev: http://localhost:5000"
    log_info "  - Eureka Server: http://localhost:8761"
    log_info "  - Auth Server: http://localhost:8091"
    log_info "  - API Gateway: http://localhost:8080"
    log_info "  - User Service: http://localhost:8081"
    log_info "  - Product Service: http://localhost:8082"
    log_info "  - Order Service: http://localhost:8083"
    log_info "  - Notification Service: http://localhost:8084"
    log_info "  - Loki: http://localhost:3100"
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
