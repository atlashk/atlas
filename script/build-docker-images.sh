#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Service definitions with name and build context
BACKEND_SERVICES=(
    "user-service:$PROJECT_ROOT/application/spring-boot/user-application"
    "product-service:$PROJECT_ROOT/application/spring-boot/product-application"
    "order-service:$PROJECT_ROOT/application/spring-boot/order-application"
    "notification-service:$PROJECT_ROOT/application/spring-boot/notification-application"
    "discovery-server:$PROJECT_ROOT/edge/discovery-server/discovery-server-eureka"
    "auth-server:$PROJECT_ROOT/edge/auth-server/auth-server-spring-security-jwt"
    "api-gateway:$PROJECT_ROOT/edge/api-gateway/api-gateway-spring-cloud-gateway"
)
INFRA_SERVICES=(
    "rabbitmq:$PROJECT_ROOT/deployment/local/compose/rabbitmq"
)

# Check for logger script
if [ ! -f "$PROJECT_ROOT/deployment/util/logger.sh" ]; then
    echo "Error: logger.sh not found at $PROJECT_ROOT/deployment/util/logger.sh"
    exit 1
fi
source "$PROJECT_ROOT/deployment/util/logger.sh"

# Function to print usage
usage() {
    echo "Usage: $0 {backend|infra|all}"
    echo "  backend: Build images for backend services"
    echo "  infra: Build images for infrastructure services"
    echo "  all: Build all images"
    exit 1
}

# Validate input
if [ $# -ne 1 ]; then
    error "Exactly one argument is required."
    usage
fi

# Determine services to build based on input
case "$1" in
    backend)
        SERVICES=("${BACKEND_SERVICES[@]}")
        ;;
    infra)
        SERVICES=("${INFRA_SERVICES[@]}")
        ;;
    all)
        SERVICES=("${BACKEND_SERVICES[@]}" "${INFRA_SERVICES[@]}")
        ;;
    *)
        error "Invalid argument: $1"
        usage
        ;;
esac

# Validate services
if [ "${#SERVICES[@]}" -eq 0 ]; then
    error "No services specified to build."
    exit 1
fi

# Build images
for service in "${SERVICES[@]}"; do
    name="${service%%:*}"
    context="${service#*:}"
    log "Building Docker image for $name..."
    if ! docker build -t "$name" "$context"; then
        error "Failed to build Docker image for $name."
        exit 1
    fi
    log "Built Docker image for $name successfully."
done
