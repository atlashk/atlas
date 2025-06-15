#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Load logger
source "$PROJECT_ROOT/scripts/logger.sh"

# Service definitions with name and build context
BACKEND_SERVICES=(
    "user-service:$PROJECT_ROOT/backend/application/spring-boot/user-application"
    "product-service:$PROJECT_ROOT/backend/application/spring-boot/product-application"
    "order-service:$PROJECT_ROOT/backend/application/spring-boot/order-application"
    "notification-service:$PROJECT_ROOT/backend/application/spring-boot/notification-application"
    "discovery-server:$PROJECT_ROOT/backend/edge/discovery-server/discovery-server-eureka"
    "auth-server:$PROJECT_ROOT/backend/edge/auth-server/auth-server-spring-security-jwt"
    "api-gateway:$PROJECT_ROOT/backend/edge/api-gateway/api-gateway-spring-cloud-gateway"
)
INFRASTRUCTURE_SERVICES=(
    # "rabbitmq:$PROJECT_ROOT/deployment/onprem/compose/configs/rabbitmq"
)
FRONTEND_SERVICES=(
    "frontend:$PROJECT_ROOT/frontend"
)

# Function to print usage
usage() {
    echo "Usage: $0 {frontend|backend|infrastructure|all}"
    echo "  backend: Build images for backend services"
    echo "  infrastructure: Build images for infrastructure services"
    echo "  frontend: Build images for frontend services"
    echo "  all: Build all images"
    exit 1
}

# Validate input
if [ $# -ne 1 ]; then
    log_error "Exactly one argument is required."
    usage
fi

# Determine services to build based on input
case "$1" in
    backend)
        SERVICES=("${BACKEND_SERVICES[@]}")
        ;;
    infrastructure)
        SERVICES=("${INFRASTRUCTURE_SERVICES[@]}")
        ;;
    frontend)
        SERVICES=("${FRONTEND_SERVICES[@]}")
        ;;
    all)
        SERVICES=("${FRONTEND_SERVICES[@]}" "${BACKEND_SERVICES[@]}" "${INFRASTRUCTURE_SERVICES[@]}")
        ;;
    *)
        log_error "Invalid argument: $1"
        usage
        ;;
esac

# Validate services
if [ "${#SERVICES[@]}" -eq 0 ]; then
    log_error "No services specified to build."
    exit 1
fi

# Build images
for service in "${SERVICES[@]}"; do
    name="${service%%:*}"
    context="${service#*:}"
    log_info "Building Docker image for $name..."
    if ! docker build -t "$name" "$context"; then
        log_error "Failed to build Docker image for $name." 
        exit 1
    fi
    log_success "Built Docker image for $name successfully."
done
