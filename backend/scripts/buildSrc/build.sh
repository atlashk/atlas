#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
GRADLEW="$PROJECT_ROOT/backend/gradlew"

# Load logger.sh
source "$PROJECT_ROOT/backend/scripts/logger.sh"

# Default values
SKIP_TESTS="true"
BUILD_JAR="true"
BUILD_DOCKER="true"

# Service definitions with name and build context
SERVICES=(
    "user-service:$PROJECT_ROOT/backend/application/user-application"
    "product-service:$PROJECT_ROOT/backend/application/product-application"
    "order-service:$PROJECT_ROOT/backend/application/order-application"
    "notification-service:$PROJECT_ROOT/backend/application/notification-application"
    "eureka-server:$PROJECT_ROOT/backend/edge/discovery-server/discovery-server-eureka"
    "api-gateway:$PROJECT_ROOT/backend/edge/api-gateway/api-gateway-spring-cloud-gateway"
)

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --skip-tests=BOOL       Skip tests during JAR build (default: true)"
    echo "  --build-jar=BOOL        Build JAR files (default: true)"
    echo "  --build-docker=BOOL     Build Docker images (default: false)"
    echo "  --service=NAME          Build only specific Docker service (optional)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                           # Build JAR files only"
    echo "  $0 --build-docker=true                      # Build JAR files and Docker images"
    echo "  $0 --build-jar=false --build-docker=true    # Build Docker images only"
    echo "  $0 --skip-tests=false                       # Build JAR files with tests enabled"
    echo "  $0 --build-docker=true --service=user-service # Build JAR files and specific Docker image"
}

# Parse named parameters
DOCKER_ARGS=()
for arg in "$@"; do
    case $arg in
        --skip-tests=*)
            SKIP_TESTS="${arg#*=}"
            ;;
        --build-jar=*)
            BUILD_JAR="${arg#*=}"
            ;;
        --build-docker=*)
            BUILD_DOCKER="${arg#*=}"
            ;;
        --service=*)
            DOCKER_ARGS+=("$arg")
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

# Build JAR files if requested
if [ "$BUILD_JAR" = "true" ]; then
    log_info "Starting JAR build process..."
    if "$SCRIPT_DIR/build-jar.sh" --skip-tests="$SKIP_TESTS"; then
        log_success "JAR build completed successfully."
    else
        log_error "JAR build failed."
        exit 1
    fi
fi

# Build Docker images if requested
if [ "$BUILD_DOCKER" = "true" ]; then
    log_info "Starting Docker build process..."
    if "$SCRIPT_DIR/build-docker.sh" "${DOCKER_ARGS[@]}"; then
        log_success "Docker build completed successfully."
    else
        log_error "Docker build failed."
        exit 1
    fi
fi

log_success "Build process completed successfully."
