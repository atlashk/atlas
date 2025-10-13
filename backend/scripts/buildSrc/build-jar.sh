#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
GRADLEW="$PROJECT_ROOT/backend/gradlew"

# Default values
SKIP_TESTS="true"
APP_STACK_FILE=""

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --skip-tests=BOOL       Skip tests during build (default: true)"
    echo "  --app-stack-file=FILE   App stack config file name (optional)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Build JAR files with defaults"
    echo "  $0 --skip-tests=false                # Build JAR files with tests enabled"
    echo "  $0 --app-stack-file=app-stack.dev.cfg # Build with custom config file"
}

# Parse named parameters
for arg in "$@"; do
    case $arg in
        --skip-tests=*)
            SKIP_TESTS="${arg#*=}"
            ;;
        --app-stack-file=*)
            APP_STACK_FILE="${arg#*=}"
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

# Ensure gradlew is executable
chmod +x "$GRADLEW"

# Prepare Gradle arguments
GRADLE_ARGS="clean build"
if [ "$SKIP_TESTS" = "true" ]; then
    GRADLE_ARGS="$GRADLE_ARGS -x test"
fi

# Add app stack file parameter if provided
if [ -n "$APP_STACK_FILE" ]; then
    GRADLE_ARGS="$GRADLE_ARGS -PappStackFile=$APP_STACK_FILE"
fi

# Build with Gradle
echo "Starting Gradle build..."
if (cd "$PROJECT_ROOT/backend" && "$GRADLEW" $GRADLE_ARGS); then
    echo "Gradle build completed successfully."
else
    echo "Gradle build failed." >&2
    exit 1
fi

echo "JAR build process completed successfully."
