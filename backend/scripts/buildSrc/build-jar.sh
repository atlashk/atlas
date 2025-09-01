#!/bin/bash

# Ensures that the script exits immediately if any command fails, or if you try to use an undefined variable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
GRADLEW="$PROJECT_ROOT/backend/gradlew"

# Default values
SKIP_TESTS="true"

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --skip-tests=BOOL       Skip tests during build (default: true)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                      # Build JAR files with defaults"
    echo "  $0 --skip-tests=false   # Build JAR files with tests enabled"
}

# Parse named parameters
for arg in "$@"; do
    case $arg in
        --skip-tests=*)
            SKIP_TESTS="${arg#*=}"
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

# Build with Gradle
echo "Starting Gradle build..."
if [ "$SKIP_TESTS" = "true" ]; then
    if (cd "$PROJECT_ROOT/backend" && "$GRADLEW" clean build -x test); then
        echo "Gradle build completed successfully."
    else
        echo "Gradle build failed." >&2
        exit 1
    fi
else
    if (cd "$PROJECT_ROOT/backend" && "$GRADLEW" clean build); then
        echo "Gradle build completed successfully."
    else
        echo "Gradle build failed." >&2
        exit 1
    fi
fi

echo "JAR build process completed successfully."
