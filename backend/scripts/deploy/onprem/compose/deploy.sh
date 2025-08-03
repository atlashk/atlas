#!/bin/bash

# =============================================================================
# Atlas Docker Compose Start Script
# =============================================================================
# This script starts the Atlas microservices platform using Docker Compose
# =============================================================================

set -euo pipefail

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/backend/scripts/deploy/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/backend/scripts/log/logger.sh"

# Default options
SKIP_BUILD=false

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Docker Compose Start Script - Starts the Atlas microservices platform"
    log_info ""
    log_info "Options:"
    log_info "  --skip-build        Skip all build steps (backend JAR, frontend, Docker images)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Start with builds"
    log_info "  $0 --skip-build     # Start without builds"
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                log_info "Use --help for usage information"
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

check_prerequisites() {
    log_section "Checking prerequisites..."

    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        # Check Java
        if command -v java &> /dev/null; then
            # Get Java version
            java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
            # Extract major version number
            major_version=$(echo $java_version | cut -d'.' -f1)
            # Handle both old (1.8) and new (17) version formats
            if [[ $major_version == "1" ]]; then
                major_version=$(echo $java_version | cut -d'.' -f2)
            fi

            # Check Java version
            if [ "$major_version" -lt 17 ]; then
                log_error "Java version $java_version is not supported. Please install Java 17 or later."
                exit 1
            fi
            log_success "Java found: $java_version"
        else
            log_error "Java is not installed. Please install Java 17 or later."
            exit 1
        fi
    fi

    # Check Docker
    if docker info > /dev/null 2>&1; then
        log_success "Docker found and running"
    else
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi

    # Check Docker Compose
    if command -v docker-compose &> /dev/null; then
        log_success "Docker Compose found"
    else
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    log_section "Building Services"

    local build_script="$PROJECT_ROOT/backend/scripts/build/build.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Build script not found: $build_script"
        exit 1
    fi

    log_info "Granting execute permission to build script..."
    chmod +x "$build_script"

    log_info "Invoking build script..."
    if "$build_script" --infra-stack=onprem-compose; then
        log_success "Build completed successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# =============================================================================
# START FUNCTIONS
# =============================================================================

# Start all services
start_services() {
    log_section "Starting Atlas services..."
    
    log_info "Using compose file: $COMPOSE_FILE"
    log_info "Starting all Atlas services..."

    # Start all services defined in the compose file
    if docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d; then
        log_success "All services started successfully!"
    else
        log_error "Failed to start services."
        exit 1
    fi

    # Display service URLs
    log_section "Service URLs:"
    log_info "  - API Gateway: http://localhost:8080"
    log_info "  - Prometheus: http://localhost:9090"
    log_info "  - Grafana: http://localhost:3000"
    log_info "  - Zipkin: http://localhost:9411"
    log_info "  - Frontend: http://localhost:9000"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    log_section "Atlas Docker Compose Platform - Starting"

    # Build step (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
    else
        log_info "Skipping build step (--skip-build flag provided)"
    fi

    start_services

    log_success "Atlas platform started successfully!"
    log_success "Your Atlas development environment is now ready to use!"
}

# Execute main function
main "$@"
