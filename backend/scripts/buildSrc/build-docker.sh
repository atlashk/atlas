#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
PROJECT_NAME="atlas"

# Service definitions with name and build context
SERVICES=(
    "user-service:$PROJECT_ROOT/backend/application/user-application"
    "product-service:$PROJECT_ROOT/backend/application/product-application"
    "order-service:$PROJECT_ROOT/backend/application/order-application"
    "payment-service:$PROJECT_ROOT/backend/application/payment-application"
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
            echo "Unknown parameter: $arg" >&2
            usage
            exit 1
            ;;
    esac
done

# Build Docker images
echo "Starting Docker image builds..."

# Validate services
if [ "${#SERVICES[@]}" -eq 0 ]; then
    echo "No services specified to build." >&2
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

    echo "Building Docker image for $PROJECT_NAME-$name..."
    if ! docker build -t "$PROJECT_NAME-$name" "$context"; then
        echo "Failed to build Docker image for $PROJECT_NAME-$name." >&2
        exit 1
    fi
    echo "Built Docker image for $PROJECT_NAME-$name successfully."
    echo
done

echo "Docker build process completed successfully."
