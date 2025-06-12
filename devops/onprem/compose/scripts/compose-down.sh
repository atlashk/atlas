#!/bin/bash

# Local Docker Compose Shutdown Script
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

log_info "Stopping Atlas local environment..."

# Navigate to compose directory
cd "$COMPOSE_DIR"

# Stop all services
log_info "Stopping all services..."
docker-compose -f docker-compose.backend.yml down
docker-compose -f docker-compose.observability.yml down
docker-compose -f docker-compose.infra.yml down

# Optional: Remove volumes (uncomment if needed)
# log_warn "Removing volumes (this will delete all data)..."
# docker-compose -f docker-compose.infra.yml down -v

log_success "Atlas local environment stopped successfully!"

# Show cleanup options
log_info "Cleanup options:"
log_info "  - To remove all containers: docker-compose down --remove-orphans"
log_info "  - To remove volumes (data): docker-compose down -v"
log_info "  - To remove images: docker-compose down --rmi all" 