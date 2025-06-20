#!/bin/bash

set -euo pipefail

# Project configuration (previously in config.sh)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/deployment/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Check Docker Compose prerequisites (previously in docker-helper.sh)
check_docker_compose_prerequisites() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
}

# Check Java prerequisites
check_java_prerequisites() {
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 17 or later."
        exit 1
    else
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        # Extract major version number
        major_version=$(echo $java_version | cut -d'.' -f1)
        # Handle both old (1.8) and new (17) version formats
        if [[ $major_version == "1" ]]; then
            major_version=$(echo $java_version | cut -d'.' -f2)
        fi
        
        if [ "$major_version" -lt 17 ]; then
            log_error "Java version $java_version is not supported. Please install Java 17 or later."
            exit 1
        fi
        log_success "Java found: $java_version"
    fi
}

# Check Node.js prerequisites
check_node_prerequisites() {
    if ! command -v node &> /dev/null; then
        log_error "Node.js is not installed. Please install Node.js 22 or later."
        exit 1
    else
        node_version=$(node --version | cut -d'v' -f2)  # Remove 'v' prefix
        major_version=$(echo $node_version | cut -d'.' -f1)
        
        if [ "$major_version" -lt 22 ]; then
            log_error "Node.js version $node_version is not supported. Please install Node.js 22 or later."
            exit 1
        fi
        log_success "Node.js found: v$node_version"
    fi
}

# Parse command line arguments
SKIP_BUILD=false

usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --skip-build    Skip all build steps (backend JAR, frontend, Docker images)"
    echo "  -h, --help      Show this help message"
    exit 1
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check Java
    check_java_prerequisites

    # Check Node.js for frontend
    check_node_prerequisites

    # Check Docker and Docker Compose
    check_docker_compose_prerequisites
}

# Build backend using existing script
build_backend() {
    log_section "Building backend JAR files..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-backend.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Backend build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking backend build script..."
    if "$build_script"; then
        log_success "Backend build completed successfully."
    else
        log_error "Backend build failed."
        exit 1
    fi
}

# Build frontend using existing script
build_frontend() {
    log_section "Building frontend..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-frontend.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Frontend build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking frontend build script..."
    if "$build_script"; then
        log_success "Frontend build completed successfully."
    else
        log_error "Frontend build failed."
        exit 1
    fi
}

# Build Docker images using existing script
build_docker_images() {
    log_section "Building Docker images..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-docker-images.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Docker images build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking Docker images build script..."
    if "$build_script" all; then
        log_success "Docker images build completed successfully."
    else
        log_error "Docker images build failed."
        exit 1
    fi
}

# Start all services
start_services() {
    log_section "Starting Atlas services..."
    
    log_info "Using unified compose file: $COMPOSE_FILE"
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

# Main execution
main() {
    log_section "Atlas Development Environment Setup"
    
    check_prerequisites
    
    if [ "$SKIP_BUILD" = true ]; then
        log_info "Skipping all build steps (--skip-build flag provided)"
    else
        build_backend
        build_frontend
        build_docker_images
    fi
    
    start_services
    
    log_section "Development Environment Ready!"
    log_success "Your Atlas development environment is now set up and running!"
}

# Run main function
main "$@"
