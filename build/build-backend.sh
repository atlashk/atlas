#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GRADLEW="$PROJECT_ROOT/backend/gradlew"

# Load logger.sh first
source "$PROJECT_ROOT/scripts/logger.sh"

# Default values
INFRA_STACK="onprem-compose-simple"
SKIP_TESTS="false"

# Parse named parameters
for arg in "$@"; do
    case $arg in
        --infra-stack=*)
            INFRA_STACK="${arg#*=}"
            ;;
        --skip-tests=*)
            SKIP_TESTS="${arg#*=}"
            ;;
        *)
            log_error "Unknown parameter: $arg"
            exit 1
            ;;
    esac
done

# Ensure gradlew is executable
chmod +x "$GRADLEW"

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
