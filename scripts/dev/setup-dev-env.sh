#!/bin/bash

# Development Environment Setup Script
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Source common functions
source "$PROJECT_ROOT/scripts/lib/common.sh"
source "$PROJECT_ROOT/scripts/lib/logger.sh"

# Setup signal handlers
setup_signal_handlers

log_section "Atlas Development Environment Setup"

# Check prerequisites
log_info "Checking prerequisites..."

# Check Java
if ! command_exists java; then
    log_error "Java is not installed. Please install Java 17 or later."
    exit 1
else
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_success "Java found: $java_version"
fi

# Check Docker
if ! check_docker; then
    log_error "Docker is required for local development"
    exit 1
else
    docker_version=$(docker --version | cut -d' ' -f3 | cut -d',' -f1)
    log_success "Docker found: $docker_version"
fi

# Check Docker Compose
if ! command_exists docker-compose; then
    log_warn "docker-compose not found, checking for 'docker compose' plugin..."
    if ! docker compose version >/dev/null 2>&1; then
        log_error "Docker Compose is not available"
        exit 1
    else
        log_success "Docker Compose plugin found"
    fi
else
    compose_version=$(docker-compose --version | cut -d' ' -f3 | cut -d',' -f1)
    log_success "Docker Compose found: $compose_version"
fi

# Check kubectl (optional)
if command_exists kubectl; then
    kubectl_version=$(kubectl version --client --short 2>/dev/null | cut -d' ' -f3)
    log_success "kubectl found: $kubectl_version"
else
    log_warn "kubectl not found (optional for local development)"
fi

# Setup local environment files
log_info "Setting up local environment configuration..."

LOCAL_COMPOSE_DIR="$PROJECT_ROOT/deployment/environments/local/compose"
if [ -f "$LOCAL_COMPOSE_DIR/env.local.example" ] && [ ! -f "$LOCAL_COMPOSE_DIR/.env" ]; then
    log_info "Creating .env file from example..."
    cp "$LOCAL_COMPOSE_DIR/env.local.example" "$LOCAL_COMPOSE_DIR/.env"
    log_success "Created $LOCAL_COMPOSE_DIR/.env"
    log_warn "Please review and update the .env file with your preferences"
else
    log_info ".env file already exists"
fi

# Create local data directories
log_info "Creating local data directories..."
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/mysql"
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/redis"
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/kafka"
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/rabbitmq"
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/prometheus"
mkdir -p "$LOCAL_COMPOSE_DIR/volumes/grafana"
log_success "Local data directories created"

# Build the project
log_info "Building the project..."
if [ -f "$PROJECT_ROOT/scripts/build/build-all.sh" ]; then
    bash "$PROJECT_ROOT/scripts/build/build-all.sh" --skip-tests
else
    log_warn "Build script not found, skipping build"
fi

# Start local environment
log_info "Starting local development environment..."
if [ -f "$PROJECT_ROOT/scripts/deploy/local/compose-up.sh" ]; then
    bash "$PROJECT_ROOT/scripts/deploy/local/compose-up.sh"
else
    log_warn "Local deployment script not found"
fi

log_section "Development Environment Ready!"
log_success "Your Atlas development environment is now set up and running!"

log_info "Quick start commands:"
log_info "  - Build project: scripts/build/build-all.sh"
log_info "  - Start services: scripts/deploy/local/compose-up.sh"
log_info "  - Stop services: scripts/deploy/local/compose-down.sh"
log_info "  - View logs: docker-compose logs -f"
log_info "  - Reset data: scripts/dev/reset-data.sh"

log_info "Service URLs:"
log_info "  - RabbitMQ Management: http://localhost:15672 (admin/admin123)"
log_info "  - Prometheus: http://localhost:9090"
log_info "  - Grafana: http://localhost:3000"
log_info "  - Zipkin: http://localhost:9411"

log_info "Database connections:"
log_info "  - MySQL: localhost:3306 (atlas/atlas123)"
log_info "  - Redis: localhost:6379" 