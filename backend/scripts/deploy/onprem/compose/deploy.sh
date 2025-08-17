#!/bin/bash

# =============================================================================
# Atlas Docker Compose Start Script (Refactored)
# =============================================================================
# This script starts the Atlas microservices platform using Docker Compose
# =============================================================================

set -euo pipefail

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/backend/scripts/deploy/onprem/compose/docker-compose.yml"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"
ENV_DIR="$SCRIPT_DIR/env"

# Source logger and common utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

# Default options
SKIP_BUILD=false
FORCE_RECREATE=false

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD="docker-compose"

# Configuration variables (populated by read_app_stack_config)
declare -g DATASOURCE MESSAGING NOTIFICATION_EMAIL OBSERVABILITY_LOGGING_STACK OBSERVABILITY_METRICS OBSERVABILITY_TRACING

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
    log_info "  --force-recreate    Force recreate containers even if they exist"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Start with builds"
    log_info "  $0 --skip-build     # Start without builds"
    log_info "  $0 --force-recreate # Force recreate all containers"
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
            --force-recreate)
                FORCE_RECREATE=true
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
# PREREQUISITE CHECKS
# =============================================================================

check_java_version() {
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 17 or later."
        return 1
    fi

    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    
    local major_version
    major_version=$(echo "$java_version" | cut -d'.' -f1)
    
    # Handle both old (1.8) and new (17) version formats
    if [[ $major_version == "1" ]]; then
        major_version=$(echo "$java_version" | cut -d'.' -f2)
    fi

    if [[ $major_version -lt 17 ]]; then
        log_error "Java version $java_version is not supported. Please install Java 17 or later."
        return 1
    fi
    
    log_success "Java found: $java_version"
    return 0
}

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        return 1
    fi
    log_success "Docker found and running"
    return 0
}

check_docker_compose() {
    # Check for both docker-compose and docker compose (newer version)
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
        log_success "Docker Compose (standalone) found"
        return 0
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker compose"
        log_success "Docker Compose (plugin) found"
        return 0
    else
        log_error "Docker Compose is not installed or not available"
        log_info "Please install Docker Compose or ensure Docker Desktop is running"
        return 1
    fi
}

check_prerequisites() {
    log_section "Checking prerequisites..."

    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_version || exit 1
    fi

    check_docker || exit 1
    check_docker_compose || exit 1
    
    log_success "Prerequisites check passed"
}

# =============================================================================
# CONFIGURATION FUNCTIONS
# =============================================================================

read_app_stack_config() {
    log_section "Reading application stack configuration..."
    
    if [[ ! -f "$APP_STACK_CONFIG" ]]; then
        log_error "Configuration file not found: $APP_STACK_CONFIG"
        log_info "Please run the configuration script first: backend/scripts/app-stack-config.sh"
        exit 1
    fi
    
    # Use common function to read platform config
    read_platform_config "$APP_STACK_CONFIG"
    
    # Read additional configuration values specific to this deployment
    DATASOURCE=$(grep "^datasource=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "mysql")
    MESSAGING=$(grep "^messaging=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "kafka")
    NOTIFICATION_EMAIL=$(grep "^notification.email=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "spring")
    OBSERVABILITY_LOGGING_STACK=$(grep "^observability.logging.stack=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "loki")
    OBSERVABILITY_METRICS=$(grep "^observability.metrics=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "prometheus")
    OBSERVABILITY_TRACING=$(grep "^observability.tracing=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "zipkin")
    
    log_info "Configuration loaded:"
    log_info "  - Datasource: $DATASOURCE"
    log_info "  - Messaging: $MESSAGING"
    log_info "  - Email: $NOTIFICATION_EMAIL"
    log_info "  - Logging: $OBSERVABILITY_LOGGING_STACK"
    log_info "  - Metrics: $OBSERVABILITY_METRICS"
    log_info "  - Tracing: $OBSERVABILITY_TRACING"
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    log_section "Building Services"

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [[ ! -f "$build_script" ]]; then
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
# SERVICE STARTUP FUNCTIONS
# =============================================================================

get_infrastructure_services() {
    local services=("redis")
    
    case "$DATASOURCE" in
        mysql) services+=("mysql") ;;
        postgres|postgresql) services+=("postgres") ;;
        *) 
            log_warn "Unknown datasource: $DATASOURCE, defaulting to mysql"
            services+=("mysql")
            ;;
    esac
    
    case "$MESSAGING" in
        kafka) services+=("kafka") ;;
        rabbitmq) services+=("rabbitmq") ;;
        *)
            log_warn "Unknown messaging system: $MESSAGING, defaulting to kafka"
            services+=("kafka")
            ;;
    esac
    
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        services+=("smtp4dev")
    fi
    
    echo "${services[@]}"
}

get_observability_services() {
    local services=()
    
    if [[ "$OBSERVABILITY_LOGGING_STACK" == "loki" ]]; then
        services+=("loki" "promtail")
    fi
    
    if [[ "$OBSERVABILITY_METRICS" == "prometheus" ]]; then
        services+=("prometheus")
    fi
    
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        services+=("zipkin")
    fi
    
    if [[ ${#services[@]} -gt 0 ]]; then
        services+=("grafana")
    fi
    
    echo "${services[@]}"
}

get_backend_services() {
    local services=("user-service" "product-service" "order-service" "notification-service")
    echo "${services[@]}"
}

start_services() {
    log_section "Starting Atlas services..."
    log_info "Using compose file: $COMPOSE_FILE"

    # Infrastructure services
    local infrastructure_services
    read -ra infrastructure_services <<< "$(get_infrastructure_services)"
    if [[ ${#infrastructure_services[@]} -gt 0 ]]; then
        start_service_group "Infrastructure" "${infrastructure_services[@]}"
    fi

    # Observability services
    local observability_services
    read -ra observability_services <<< "$(get_observability_services)"
    if [[ ${#observability_services[@]} -gt 0 ]]; then
        start_service_group "Observability" "${observability_services[@]}"
    fi

    # Service discovery
    start_service_group "Service Discovery" "eureka-server"
    
    # Backend services
    local backend_services
    read -ra backend_services <<< "$(get_backend_services)"
    if [[ ${#backend_services[@]} -gt 0 ]]; then
        start_service_group "Backend" "${backend_services[@]}"
    fi

    # API gateway
    start_service_group "API Gateway" "api-gateway"

    # Proxy
    start_service_group "Proxy" "nginx"
}

start_service_group() {
    local group_name="$1"
    shift
    local services=("$@")

    log_info "Starting $group_name services: ${services[*]}"

    local compose_args=()
    if [[ "$FORCE_RECREATE" == true ]]; then
        compose_args+=("--force-recreate")
    fi

    if $DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d "${compose_args[@]}" "${services[@]}"; then
        log_success "$group_name services started"
        wait_for_service_group_healthy "${services[@]}"
    else
        log_error "Failed to start $group_name services"
        log_info "You can check logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs"
        exit 1
    fi
}

wait_for_service_group_healthy() {
    local services=("$@")
    local max_wait_time=300  # 5 minutes per group
    local check_interval=10  # 10 seconds
    local elapsed_time=0
    local all_healthy=false

    log_info "Waiting for services to be healthy: ${services[*]}"

    while [[ $elapsed_time -lt $max_wait_time ]] && [[ $all_healthy == false ]]; do
        all_healthy=true
        local unhealthy_services=()
        
        for service in "${services[@]}"; do
            if ! check_service_health "$service"; then
                all_healthy=false
                unhealthy_services+=("$service")
            fi
        done
        
        if [[ $all_healthy == true ]]; then
            log_success "All services in group are healthy: ${services[*]}"
            break
        else
            log_info "Waiting for services: ${unhealthy_services[*]} (${elapsed_time}s/${max_wait_time}s)"
            sleep $check_interval
            elapsed_time=$((elapsed_time + check_interval))
        fi
    done
    
    if [[ $all_healthy == false ]]; then
        log_error "Timeout waiting for service group to be healthy after ${max_wait_time} seconds: ${services[*]}"
        log_info "You can check service logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs [service_name]"
        log_info "You can check service status with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME ps"
        exit 1
    fi
}

check_service_health() {
    local service="$1"
    
    # Get container status
    local container_status
    container_status=$($DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps -q "$service" 2>/dev/null | xargs docker inspect --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    
    # Check if container is running
    if [[ "$container_status" != "running" ]]; then
        return 1
    fi
    
    # For services with health checks, verify they are healthy
    local health_status
    health_status=$($DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps -q "$service" 2>/dev/null | xargs docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no_healthcheck{{end}}' 2>/dev/null || echo "unknown")
    
    if [[ "$health_status" == "unhealthy" || "$health_status" == "starting" ]]; then
        return 1
    fi
    
    return 0
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

generate_environment_files() {
    log_info "Generating environment files..."
    
    local env_generator="$SCRIPT_DIR/env-generator.sh"
    if [[ ! -f "$env_generator" ]]; then
        log_error "Environment generator script not found: $env_generator"
        exit 1
    fi
    
    chmod +x "$env_generator"

    if ! "$env_generator"; then
        log_error "Failed to generate environment files"
        exit 1
    fi
    
    log_success "Environment files generated successfully"
}

main() {
    parse_arguments "$@"

    check_prerequisites

    read_app_stack_config
    
    generate_environment_files

    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
    else
        log_info "Skipping build step (--skip-build flag provided)"
    fi

    start_services
    
    log_section "Deployment completed successfully!"
    log_info "All Atlas services are now running and healthy."
    log_info "You can check service status with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME ps"
    log_info "You can view logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs -f [service_name]"
    log_info "Network 'atlas-network' is available for all services to communicate"

    # Display access URLs
    log_info ""
    log_info "Service Access URLs:"
    log_info "  - API Gateway: http://localhost:8080"
    log_info "  - Grafana: http://localhost:3000 (admin/admin)"
    log_info "  - Prometheus: http://localhost:9090"
    log_info "  - Zipkin: http://localhost:9411"
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        log_info "  - SMTP4Dev: http://localhost:5000"
    fi
}

# Execute main function
main "$@"
