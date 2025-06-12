#!/bin/bash

# Atlas OnPrem Compose Deployment Script
# Usage: ./deploy.sh [environment] [action]
# Environments: local, dev, stg, prod
# Actions: up, down, restart, logs, status

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
CONFIGS_DIR="${PROJECT_ROOT}/configs"
ENVIRONMENTS_DIR="${PROJECT_ROOT}/environments"

# Default values
ENVIRONMENT="${1:-local}"
ACTION="${2:-up}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate environment
validate_environment() {
    case $ENVIRONMENT in
        local|dev|stg|prod)
            log_info "Using environment: $ENVIRONMENT"
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT"
            log_error "Valid environments: local, dev, stg, prod"
            exit 1
            ;;
    esac
}

# Load environment variables
load_environment() {
    local env_file="${ENVIRONMENTS_DIR}/env.${ENVIRONMENT}"
    
    if [[ ! -f "$env_file" ]]; then
        log_error "Environment file not found: $env_file"
        exit 1
    fi
    
    log_info "Loading environment from: $env_file"
    set -a  # automatically export all variables
    source "$env_file"
    set +a
}

# Check prerequisites
check_prerequisites() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

# Deploy infrastructure
deploy_infrastructure() {
    log_info "Deploying infrastructure services..."
    cd "$CONFIGS_DIR"
    docker-compose -f docker-compose.infra.yml -p "${COMPOSE_PROJECT_NAME}-infra" up -d
    log_success "Infrastructure services deployed"
}

# Deploy observability
deploy_observability() {
    log_info "Deploying observability services..."
    cd "$CONFIGS_DIR"
    docker-compose -f docker-compose.observability.yml -p "${COMPOSE_PROJECT_NAME}-observability" up -d
    log_success "Observability services deployed"
}

# Deploy backend services
deploy_backend() {
    log_info "Deploying backend services..."
    cd "$CONFIGS_DIR"
    docker-compose -f docker-compose.backend.yml -p "${COMPOSE_PROJECT_NAME}-backend" up -d
    log_success "Backend services deployed"
}

# Stop all services
stop_services() {
    log_info "Stopping all services..."
    cd "$CONFIGS_DIR"
    
    docker-compose -f docker-compose.backend.yml -p "${COMPOSE_PROJECT_NAME}-backend" down || true
    docker-compose -f docker-compose.observability.yml -p "${COMPOSE_PROJECT_NAME}-observability" down || true
    docker-compose -f docker-compose.infra.yml -p "${COMPOSE_PROJECT_NAME}-infra" down || true
    
    log_success "All services stopped"
}

# Show service status
show_status() {
    log_info "Service status for environment: $ENVIRONMENT"
    cd "$CONFIGS_DIR"
    
    echo
    echo "=== Infrastructure Services ==="
    docker-compose -f docker-compose.infra.yml -p "${COMPOSE_PROJECT_NAME}-infra" ps
    
    echo
    echo "=== Observability Services ==="
    docker-compose -f docker-compose.observability.yml -p "${COMPOSE_PROJECT_NAME}-observability" ps
    
    echo
    echo "=== Backend Services ==="
    docker-compose -f docker-compose.backend.yml -p "${COMPOSE_PROJECT_NAME}-backend" ps
}

# Show logs
show_logs() {
    local service="${3:-}"
    log_info "Showing logs for environment: $ENVIRONMENT"
    cd "$CONFIGS_DIR"
    
    if [[ -n "$service" ]]; then
        log_info "Showing logs for service: $service"
        docker-compose -f docker-compose.infra.yml -f docker-compose.observability.yml -f docker-compose.backend.yml -p "${COMPOSE_PROJECT_NAME}" logs -f "$service"
    else
        log_info "Showing logs for all services"
        docker-compose -f docker-compose.infra.yml -f docker-compose.observability.yml -f docker-compose.backend.yml -p "${COMPOSE_PROJECT_NAME}" logs -f
    fi
}

# Main execution
main() {
    log_info "Atlas OnPrem Compose Deployment"
    log_info "Environment: $ENVIRONMENT, Action: $ACTION"
    
    validate_environment
    check_prerequisites
    load_environment
    
    case $ACTION in
        up)
            deploy_infrastructure
            sleep 30  # Wait for infrastructure to be ready
            deploy_observability
            sleep 15  # Wait for observability to be ready
            deploy_backend
            log_success "Deployment completed successfully!"
            ;;
        down)
            stop_services
            ;;
        restart)
            stop_services
            sleep 10
            main "up"
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$@"
            ;;
        *)
            log_error "Invalid action: $ACTION"
            log_error "Valid actions: up, down, restart, status, logs"
            exit 1
            ;;
    esac
}

# Run main function
main "$@" 