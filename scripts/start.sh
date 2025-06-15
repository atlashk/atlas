#!/bin/bash

# Development Environment Setup Script
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Source common functions and helpers
source "$PROJECT_ROOT/scripts/logger.sh"
source "$PROJECT_ROOT/deployment/onprem/compose/scripts/docker-helper.sh"

log_section "Atlas Development Environment Setup"

# Check prerequisites
log_info "Checking prerequisites..."

# Check Java
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

# Check Node.js for frontend
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

# Check Docker and Docker Compose
check_docker_compose_prerequisites

# Build backend (JAR files)
log_section "Building backend JAR files..."
if [ -f "$PROJECT_ROOT/build/build-backend.sh" ]; then
    bash "$PROJECT_ROOT/build/build-backend.sh" --infra-stack="onprem-compose-observability" --skip-tests="true"
else
    log_error "Backend build script not found at $PROJECT_ROOT/build/build-backend.sh"
    exit 1
fi

# Build frontend
log_section "Building frontend..."
if [ -f "$PROJECT_ROOT/build/build-frontend.sh" ]; then
    bash "$PROJECT_ROOT/build/build-frontend.sh"
else
    log_error "Frontend build script not found at $PROJECT_ROOT/build/build-frontend.sh"
    exit 1
fi

# Build Docker images
log_section "Building Docker images..."
if [ -f "$PROJECT_ROOT/build/build-docker-images.sh" ]; then
    bash "$PROJECT_ROOT/build/build-docker-images.sh" all
else
    log_error "Docker images build script not found at $PROJECT_ROOT/build/build-docker-images.sh"
    exit 1
fi

# Start local environment
log_info "Starting local development environment..."
if [ -f "$PROJECT_ROOT/deployment/onprem/compose/scripts/compose-start.sh" ]; then
    cd "$PROJECT_ROOT/deployment/onprem/compose/scripts"
    bash "./compose-start.sh"
else
    log_warn "Local deployment script not found"
fi

log_section "Development Environment Ready!"
log_success "Your Atlas development environment is now set up and running!"

log_info "Quick start commands:"
log_info "  - Build backend: build/build-backend.sh"
log_info "  - Build frontend: build/build-frontend.sh"
log_info "  - Build Docker images: build/build-docker-images.sh all"
log_info "  - Start services: deployment/onprem/compose/scripts/compose-start.sh"
log_info "  - Stop services: deployment/onprem/compose/scripts/compose-stop.sh"
log_info "  - Clear volumes: deployment/onprem/compose/scripts/compose-clear.sh"
log_info "  - View logs: docker-compose -f deployment/onprem/compose/docker-compose.yml logs -f"
log_info "  - Check status: docker-compose -f deployment/onprem/compose/docker-compose.yml ps"

log_info "Service URLs:"
log_info "  - API Gateway: http://localhost:8080"
log_info "  - Prometheus: http://localhost:9090"
log_info "  - Grafana: http://localhost:3000"
log_info "  - Zipkin: http://localhost:9411"
log_info "  - Frontend: http://localhost:80"
