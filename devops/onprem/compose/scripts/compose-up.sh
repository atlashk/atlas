#!/bin/bash

# Local Docker Compose Deployment Script
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
COMPOSE_DIR="$PROJECT_ROOT/deployment/environments/local/compose"

# Source common functions
source "$PROJECT_ROOT/devops/scripts/lib/common.sh"
source "$PROJECT_ROOT/devops/scripts/lib/logger.sh"

# Configuration
ENVIRONMENT="local"
COMPOSE_PROJECT_NAME="atlas-local"

log_info "Starting Atlas local environment with Docker Compose..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Navigate to compose directory
cd "$COMPOSE_DIR"

# Check if .env file exists
if [ ! -f ".env" ]; then
    if [ -f "env.local.example" ]; then
        log_info "Creating .env file from example..."
        cp env.local.example .env
        log_warn "Please review and update the .env file with your local configuration"
    else
        log_error ".env file not found. Please create one based on env.local.example"
        exit 1
    fi
fi

# Start infrastructure services first
log_info "Starting infrastructure services..."
docker-compose -f docker-compose.infra.yml up -d

# Wait for infrastructure to be ready
log_info "Waiting for infrastructure services to be ready..."
sleep 30

# Start backend services
log_info "Starting backend services..."
docker-compose -f docker-compose.backend.yml up -d

# Start observability services
log_info "Starting observability services..."
docker-compose -f docker-compose.observability.yml up -d

log_success "Atlas local environment started successfully!"
log_info "Services available at:"
log_info "  - MySQL: localhost:3306"
log_info "  - Redis: localhost:6379"
log_info "  - RabbitMQ Management: http://localhost:15672"
log_info "  - Kafka: localhost:9092"
log_info "  - Prometheus: http://localhost:9090"
log_info "  - Grafana: http://localhost:3000"
log_info "  - Zipkin: http://localhost:9411"

log_info "To stop the environment, run: $0/../compose-down.sh" 