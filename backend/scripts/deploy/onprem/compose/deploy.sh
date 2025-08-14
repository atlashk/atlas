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
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Source logger and common utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

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
# CONFIGURATION FUNCTIONS
# =============================================================================

# Read configuration from app-stack.cfg
read_app_stack_config() {
    # Use common function to read platform config
    read_platform_config "$APP_STACK_CONFIG"
    
    # Read additional configuration values specific to this deployment
    DATASOURCE=$(grep "^datasource=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    MESSAGING=$(grep "^messaging=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    NOTIFICATION_EMAIL=$(grep "^notification.email=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_LOGGING_STACK=$(grep "^observability.logging.stack=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_METRICS=$(grep "^observability.metrics=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_TRACING=$(grep "^observability.tracing=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
}

# List services based on configuration
list_services() {
    local services=(
        "eureka-server"
        "user-service"
        "product-service"
        "order-service"
        "notification-service"
        "api-gateway"
        "nginx"
    )
    
    case "$DATASOURCE" in
        mysql)
            services+=("mysql")
            ;;
        postgres)
            services+=("postgres")
            ;;
        *)
            log_warning "  ? Unknown datasource: $DATASOURCE"
            ;;
    esac

    services+=("redis")
    
    case "$MESSAGING" in
        kafka)
            services+=("kafka")
            ;;
        rabbitmq)
            services+=("rabbitmq")
            ;;
        *)
            log_warning "  ? Unknown messaging: $MESSAGING"
            ;;
    esac
    
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        services+=("smtp4dev")
    fi
    
    if [[ "$OBSERVABILITY_LOGGING_STACK" == "loki" ]]; then
        services+=("loki")
        services+=("promtail")
    fi

    if [[ "$OBSERVABILITY_METRICS" == "prometheus" ]]; then
        services+=("prometheus")
    fi
    
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        services+=("zipkin")
    fi

    services+=("grafana")

    # Store services list for later use
    SERVICES=("${services[@]}")
    log_info "Services based on configuration: ${SERVICES[*]}"
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    log_section "Building Services"

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Build script not found: $build_script"
        exit 1
    fi

    log_info "Granting execute permission to build script..."
    chmod +x "$build_script"

    log_info "Invoking build script..."
    if "$build_script"; then
        log_success "Build completed successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# =============================================================================
# START FUNCTIONS
# =============================================================================

start_services() {
    log_section "Starting Atlas services..."

    log_info "Using compose file: $COMPOSE_FILE"

    if docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d "${SERVICES[@]}"; then
        log_success "Services started successfully!"
    else
        log_error "Failed to start services."
        exit 1
    fi
}

# Wait for all services to be healthy
wait_for_services_healthy() {
    log_section "Waiting for services to be healthy..."
    
    local max_wait_time=900  # 15 minutes
    local check_interval=10  # 10 seconds
    local elapsed_time=0
    local all_healthy=false

    while [[ $elapsed_time -lt $max_wait_time ]] && [[ $all_healthy == false ]]; do
        all_healthy=true
        local unhealthy_services=()
        
        log_info "Checking service health status... (${elapsed_time}s/${max_wait_time}s)"
        
        for service in "${SERVICES[@]}"; do
            # Get container status
            local container_status=$(docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps -q "$service" 2>/dev/null | xargs docker inspect --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
            
            # Check if container is running
            if [[ "$container_status" != "running" ]]; then
                all_healthy=false
                unhealthy_services+=("$service (status: $container_status)")
                continue
            fi
            
            # For services with health checks, verify they are healthy
            local health_status=$(docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps -q "$service" 2>/dev/null | xargs docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no_healthcheck{{end}}' 2>/dev/null || echo "unknown")
            
            if [[ "$health_status" == "unhealthy" ]]; then
                all_healthy=false
                unhealthy_services+=("$service (health: unhealthy)")
            elif [[ "$health_status" == "starting" ]]; then
                all_healthy=false
                unhealthy_services+=("$service (health: starting)")
            fi
        done
        
        if [[ $all_healthy == true ]]; then
            log_success "All services are healthy!"
            break
        else
            log_info "Waiting for services: ${unhealthy_services[*]}"
            sleep $check_interval
            elapsed_time=$((elapsed_time + check_interval))
        fi
    done
    
    if [[ $all_healthy == false ]]; then
        log_error "Timeout waiting for services to be healthy after ${max_wait_time} seconds"
        log_info "You can check service logs with: docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME logs [service_name]"
        exit 1
    fi
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    # Read configuration and list services
    read_app_stack_config
    list_services

    # Build step (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
    else
        log_info "Skipping build step (--skip-build flag provided)"
    fi

    # Start services
    start_services

    # Wait for services to be healthy
    wait_for_services_healthy

    # Display service URLs
    log_section "Service URLs:"
    log_info "Direct Access:"
    log_info "  - API Gateway: http://localhost:8080"
    log_info "  - Prometheus: http://localhost:9090"
    log_info "  - Grafana: http://localhost:3000"
    log_info "  - Zipkin: http://localhost:9411"
    log_info ""
    log_info "Via Nginx (requires hosts file configuration):"
    log_info "  - API Gateway: http://api.atlas.local"
    log_info "  - Grafana: http://grafana.atlas.local"
    log_info "  - Prometheus: http://prometheus.atlas.local"
    log_info "  - Zipkin: http://zipkin.atlas.local"
    log_info "  - SMTP4Dev: http://smtp4dev.atlas.local"
}

# Execute main function
main "$@"
