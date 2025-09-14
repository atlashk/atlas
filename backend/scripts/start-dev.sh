#!/bin/bash

# =============================================================================
# Atlas Development Environment Startup Script
# =============================================================================
# Simple script to start docker-compose-dev.yml
# =============================================================================

# Configuration
PROJECT_NAME="atlas-dev"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/deploy/onprem/compose/docker-compose-dev.yml"

echo "Starting Atlas development environment..."
echo "Compose file: $COMPOSE_FILE"

cd "$(dirname "$COMPOSE_FILE")"

if command -v docker-compose >/dev/null 2>&1; then
    docker-compose -f docker-compose-dev.yml -p "$PROJECT_NAME" up
elif docker compose version >/dev/null 2>&1; then
    docker compose -f docker-compose-dev.yml -p "$PROJECT_NAME" up
else
    echo "Error: Neither 'docker-compose' nor 'docker compose' is available."
    echo "Please install Docker Compose and try again."
    exit 1
fi
