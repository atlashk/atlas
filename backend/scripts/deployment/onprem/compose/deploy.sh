#!/usr/bin/env bash

# =============================================================================
# Atlas Docker Compose Deployment Script (Template, Simplified)
# =============================================================================
# This script checks prerequisites, builds services, starts the current Compose
# stack, and shows a deployment summary.
# =============================================================================

set -euo pipefail

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD="docker-compose"

# =============================================================================
# PREREQUISITE CHECKS
# =============================================================================

check_java_version() {
    if ! command -v java &> /dev/null; then
        echo "Java is not installed. Please install Java 17 or later." >&2
        return 1
    fi

    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'\"' -f2)
    
    local major_version
    major_version=$(echo "$java_version" | cut -d'.' -f1)
    
    # Handle both old (1.8) and new (17) version formats
    if [[ $major_version == "1" ]]; then
        major_version=$(echo "$java_version" | cut -d'.' -f2)
    fi

    if [[ $major_version -lt 17 ]]; then
        echo "Java version $java_version is not supported. Please install Java 17 or later." >&2
        return 1
    fi
    
    echo "Java found: $java_version"
    return 0
}

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "Docker is not running. Please start Docker and try again." >&2
        return 1
    fi
    echo "Docker found and running"
    return 0
}

check_docker_compose() {
    # Check for both docker-compose and docker compose (newer version)
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
        echo "Docker Compose (standalone) found"
        return 0
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker compose"
        echo "Docker Compose (plugin) found"
        return 0
    else
        echo "Docker Compose is not installed or not available" >&2
        echo "Please install Docker Compose or ensure Docker Desktop is running"
        return 1
    fi
}

check_prerequisites() {
    echo "Checking prerequisites..."
    check_java_version || exit 1
    check_docker || exit 1
    check_docker_compose || exit 1
    echo "Prerequisites check passed"
    echo
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    echo "Building services..."

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [[ ! -f "$build_script" ]]; then
        echo "Build script not found: $build_script" >&2
        exit 1
    fi

    echo "Granting execute permissions to build scripts..."
    chmod +x "$PROJECT_ROOT/backend/scripts/buildSrc/"*.sh

    echo "Invoking build script..."
    if "$build_script"; then
        echo "Build completed successfully"
    else
        echo "Build failed" >&2
        exit 1
    fi
    echo
}

# =============================================================================
# SERVICE STARTUP
# =============================================================================

start_current_stack() {
    echo "Starting current Compose stack..."
    echo "Using compose file: $COMPOSE_FILE"
    if $DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d; then
        echo "Compose stack started"
    else
        echo "Failed to start Compose stack" >&2
        echo "You can check logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs"
        exit 1
    fi
    echo
}

# =============================================================================
# SUMMARY AND REPORTING
# =============================================================================

show_deployment_summary() {
    local start_time="$1"
    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))
    local minutes=$((total_time / 60))
    local seconds=$((total_time % 60))

    echo "=== Deployment Summary ==="
    echo "Atlas platform deployment completed successfully!"
    echo "Total execution time: ${minutes}m ${seconds}s"
    echo

    show_access_information

    show_management_commands
}

show_access_information() {
    echo "Service Access URLs:"
    echo "  API Gateway:   http://api.atlas.local"
    
    echo "  Grafana:       http://grafana.atlas.local (admin/admin)"
    
    
    echo "  Prometheus:    http://prometheus.atlas.local"
    
    
    echo "  Zipkin:        http://zipkin.atlas.local"
    
    
    echo "  SMTP4Dev:      http://smtp4dev.atlas.local"
    
    echo
}

show_management_commands() {
    echo "Management commands:"
    echo "  Status:        $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME ps"
    echo "  Services:      $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME ps --services"
    echo "  Logs:          $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs -f [service_name]"
    echo "  Stop:          $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME down"
    echo "  Restart:       $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME restart [service_name]"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    check_prerequisites

    local start_time=$(date +%s)

    echo "Atlas Docker Compose Platform - Starting..."

    build_services

    start_current_stack

    show_deployment_summary "$start_time"
}

# Execute main function
main "$@"