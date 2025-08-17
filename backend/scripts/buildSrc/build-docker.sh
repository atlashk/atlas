#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

# Load logger.sh
source "$PROJECT_ROOT/backend/scripts/logger.sh"

# Service definitions with name and build context
SERVICES=(
    "user-service:$PROJECT_ROOT/backend/application/user-application"
    "product-service:$PROJECT_ROOT/backend/application/product-application"
    "order-service:$PROJECT_ROOT/backend/application/order-application"
    "notification-service:$PROJECT_ROOT/backend/application/notification-application"
    "eureka-server:$PROJECT_ROOT/backend/edge/discovery-server/discovery-server-eureka"
    "api-gateway:$PROJECT_ROOT/backend/edge/api-gateway/api-gateway-spring-cloud-gateway"
)

# Default values
SERVICE_FILTER=""

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --service=NAME          Build only specific service (optional)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Available services:"
    for service in "${SERVICES[@]}"; do
        name="${service%%:*}"
        echo "    - $name"
    done
    echo ""
    echo "Examples:"
    echo "  $0                              # Build all Docker images"
    echo "  $0 --service=user-service       # Build only user-service Docker image"
}

# Parse named parameters
for arg in "$@"; do
    case $arg in
        --service=*)
            SERVICE_FILTER="${arg#*=}"
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown parameter: $arg"
            usage
            exit 1
            ;;
    esac
done

# Build Docker images
log_info "Starting Docker image builds..."

# Validate services
if [ "${#SERVICES[@]}" -eq 0 ]; then
    log_error "No services specified to build."
    exit 1
fi

# Build images
for service in "${SERVICES[@]}"; do
    name="${service%%:*}"
    context="${service#*:}"

    # Skip if service filter is specified and doesn't match
    if [ -n "$SERVICE_FILTER" ] && [ "$name" != "$SERVICE_FILTER" ]; then
        continue
    fi

    log_info "Building Docker image for atlas-$name..."
    if ! docker build -t "atlas-$name" "$context"; then
        log_error "Failed to build Docker image for atlas-$name." 
        exit 1
    fi
    log_success "Built Docker image for atlas-$name successfully."
done

log_success "Docker build process completed successfully."
