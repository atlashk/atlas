#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

# Get the absolute path to the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Get the project root (one level up from this script)
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

INFRA_STACK_NAME=${1:-local-compose-simple}
GRADLEW="$PROJECT_ROOT/gradlew"

# Load logger.sh
source "$PROJECT_ROOT/deployment/util/logger.sh"

log "Starting Gradle build with infra stack '$INFRA_STACK_NAME'..."
if (cd "$PROJECT_ROOT" && "$GRADLEW" clean build -PinfraStackName="$INFRA_STACK_NAME"); then
    log "Gradle build completed successfully."
else
    error "Gradle build failed." >&2
    exit 1
fi
