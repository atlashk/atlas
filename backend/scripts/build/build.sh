#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
GRADLEW="$PROJECT_ROOT/backend/gradlew"

# Load logger.sh
source "$PROJECT_ROOT/backend/scripts/utils/logger.sh"

# Default values
INFRA_STACK="onprem-compose-observability"
SKIP_TESTS="true"
BUILD_DOCKER="true"

# Service definitions with name and build context
SERVICES=(
    "user-service:$PROJECT_ROOT/backend/application/spring-boot/user-application"
    "product-service:$PROJECT_ROOT/backend/application/spring-boot/product-application"
    "order-service:$PROJECT_ROOT/backend/application/spring-boot/order-application"
    "notification-service:$PROJECT_ROOT/backend/application/spring-boot/notification-application"
    "discovery-server:$PROJECT_ROOT/backend/edge/discovery-server/discovery-server-eureka"
    "auth-server:$PROJECT_ROOT/backend/edge/auth-server/auth-server-spring-security-jwt"
    "api-gateway:$PROJECT_ROOT/backend/edge/api-gateway/api-gateway-spring-cloud-gateway"
)

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --infra-stack=STACK     Infrastructure stack to use (default: onprem-compose-observability)"
    echo "  --skip-tests=BOOL       Skip tests during build (default: true)"
    echo "  --build-docker=BOOL     Build Docker images after Gradle build (default: false)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                           # Build with defaults"
    echo "  $0 --build-docker=true                      # Build and create Docker images"
    echo "  $0 --infra-stack=aws-ecs --skip-tests=false # Use AWS ECS stack with tests"
}

# Parse named parameters
for arg in "$@"; do
    case $arg in
        --infra-stack=*)
            INFRA_STACK="${arg#*=}"
            ;;
        --skip-tests=*)
            SKIP_TESTS="${arg#*=}"
            ;;
        --build-docker=*)
            BUILD_DOCKER="${arg#*=}"
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

# Ensure gradlew is executable
chmod +x "$GRADLEW"

# Build with Gradle
log_info "Starting Gradle build with infra stack '$INFRA_STACK'..."
if [ "$SKIP_TESTS" = "true" ]; then
    if (cd "$PROJECT_ROOT/backend" && "$GRADLEW" clean build -PinfraStack="$INFRA_STACK" -x test); then
        log_success "Gradle build completed successfully."
    else
        log_error "Gradle build failed."
        exit 1
    fi
else
    if (cd "$PROJECT_ROOT/backend" && "$GRADLEW" clean build -PinfraStack="$INFRA_STACK"); then
        log_success "Gradle build completed successfully."
    else
        log_error "Gradle build failed."
        exit 1
    fi
fi

# Build Docker images if requested
if [ "$BUILD_DOCKER" = "true" ]; then
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
        log_info "Building Docker image for $name..."
        if ! docker build -t "$name" "$context"; then
            log_error "Failed to build Docker image for $name." 
            exit 1
        fi
        log_success "Built Docker image for $name successfully."
    done
    
    log_success "All Docker images built successfully."
fi

log_success "Build process completed successfully."
