#!/bin/bash

# =============================================================================
# Atlas Docker Compose Deployment Script (Optimized)
# =============================================================================
# This script generates environment files and starts the Atlas microservices 
# platform using Docker Compose. It includes integrated environment file 
# generation functionality for optimal performance.
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
ENV_DIR="$SCRIPT_DIR/.env"

# Default options
SKIP_BUILD=false
FORCE_RECREATE=false
DISABLE_OBSERVABILITY=false

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD="docker-compose"

# Configuration variables (populated by read_app_stack_config)
declare -g DATASOURCE MESSAGING API_CLIENT NOTIFICATION_EMAIL OBSERVABILITY_LOGGING_STACK OBSERVABILITY_METRICS OBSERVABILITY_TRACING

# Service configuration arrays for environment file generation
declare -A SERVICE_CONFIGS=(
    ["api-gateway"]="api-gateway.env"
    ["user-service"]="user-service.env"
    ["product-service"]="product-service.env"
    ["order-service"]="order-service.env"
    ["payment-service"]="payment-service.env"
)

declare -A SERVICE_DATABASES=(
    ["user-service"]="db_user"
    ["product-service"]="db_product"
    ["order-service"]="db_order"
    ["payment-service"]="db_payment"
)

# Define service-specific configurations
declare -A SERVICE_SPECIFIC_CONFIGS=(
    ["api-gateway"]="jwt_config"
    ["order-service"]="email_config"
)

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Docker Compose Deployment Script - Generates environment files and starts the Atlas microservices platform"
    echo ""
    echo "Options:"
    echo "  --skip-build        Skip all build steps (JAR, Docker images)"
    echo "  --force-recreate    Force recreate containers even if they exist"
    echo "  --disable-observability  Disable all observability services (Grafana, Prometheus, Loki, Zipkin)"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                  # Full deployment with parallel startup and environment generation"
    echo "  $0 --skip-build     # Deploy without builds (uses existing images)"
    echo "  $0 --force-recreate # Force recreate all containers with fresh environment files"
    echo "  $0 --disable-observability # Deploy without observability services (Grafana, Prometheus, etc.)"

    echo ""
    echo "Features:"
    echo "  - Integrated environment file generation"
    echo "  - Configurable infrastructure stack (database, messaging, observability)"
    echo "  - Enhanced health checks and service dependency management"
    echo "  - Comprehensive error handling and validation"
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
            --disable-observability)
                DISABLE_OBSERVABILITY=true
                shift
                ;;
            *)
                echo "Unknown option: $1" >&2
                echo "Use --help for usage information"
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
        echo "Java is not installed. Please install Java 17 or later." >&2
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

    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_version || exit 1
    fi

    check_docker || exit 1
    check_docker_compose || exit 1
    
    echo "Prerequisites check passed"
    echo
}

# =============================================================================
# CONFIGURATION FUNCTIONS
# =============================================================================

read_config_value() {
    local key="$1"
    local default_value="$2"
    local value
    
    value=$(grep "^${key}=" "$APP_STACK_CONFIG" 2>/dev/null | cut -d'=' -f2 | tr -d '[:space:]')
    echo "${value:-$default_value}"
}

read_app_stack_config() {
    echo "Reading application stack configuration..."
    
    if [[ ! -f "$APP_STACK_CONFIG" ]]; then
        echo "Configuration file not found: $APP_STACK_CONFIG" >&2
        echo "Please run the configuration script first: backend/scripts/app-stack-config.sh"
        exit 1
    fi
    
    if [[ ! -r "$APP_STACK_CONFIG" ]]; then
        echo "Configuration file is not readable: $APP_STACK_CONFIG" >&2
        exit 1
    fi
    
    # Read configuration values with improved error handling
    DATASOURCE=$(read_config_value "datasource" "mysql")
    MESSAGING=$(read_config_value "messaging" "kafka")
    API_CLIENT=$(read_config_value "api-client" "rest-restclient")
    NOTIFICATION_EMAIL=$(read_config_value "notification.email" "spring")
    OBSERVABILITY_LOGGING_STACK=$(read_config_value "observability.logging.stack" "loki")
    OBSERVABILITY_METRICS=$(read_config_value "observability.metrics" "prometheus")
    OBSERVABILITY_TRACING=$(read_config_value "observability.tracing" "zipkin")
    
    # Validate critical configuration values
    if [[ ! "$DATASOURCE" =~ ^(mysql|postgres)$ ]]; then
        echo "Warning: Invalid datasource '$DATASOURCE', defaulting to 'mysql'"
        DATASOURCE="mysql"
    fi
    
    if [[ ! "$MESSAGING" =~ ^(kafka|rabbitmq)$ ]]; then
        echo "Warning: Invalid messaging system '$MESSAGING', defaulting to 'kafka'"
        MESSAGING="kafka"
    fi
    
    echo "Configuration loaded:"
    echo "  - Datasource: $DATASOURCE"
    echo "  - Messaging: $MESSAGING"
    echo "  - API Client: $API_CLIENT"
    echo "  - Email: $NOTIFICATION_EMAIL"
    echo "  - Logging: $OBSERVABILITY_LOGGING_STACK"
    echo "  - Metrics: $OBSERVABILITY_METRICS"
    echo "  - Tracing: $OBSERVABILITY_TRACING"
    echo
}

# =============================================================================
# ENVIRONMENT FILE GENERATION FUNCTIONS
# =============================================================================

generate_database_config() {
    local service_name="$1"
    local database_name="${SERVICE_DATABASES[$service_name]:-}"

    if [[ -z "$database_name" ]]; then
        return 0  # No database config needed for this service
    fi

    case "$DATASOURCE" in
        mysql)
            cat << EOF
# Database Configuration (MySQL) for $service_name
DB_URL=jdbc:mysql://mysql:3306/$database_name?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=root
DB_QUARTZ_URL=jdbc:mysql://mysql:3306/db_quartz?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
DB_QUARTZ_USERNAME=root
DB_QUARTZ_PASSWORD=root
EOF
            ;;
        postgres)
            cat << EOF
# Database Configuration (PostgreSQL) for $service_name
DB_URL=jdbc:postgresql://postgres:5432/$database_name
DB_USERNAME=root
DB_PASSWORD=root
DB_QUARTZ_URL=jdbc:postgresql://postgres:5432/db_quartz
DB_QUARTZ_USERNAME=root
DB_QUARTZ_PASSWORD=root
EOF
            ;;
    esac
}

generate_messaging_config() {
    case "$MESSAGING" in
        kafka)
            cat << EOF

# Messaging Configuration (Kafka)
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
EOF
            ;;
        rabbitmq)
            cat << EOF

# Messaging Configuration (RabbitMQ)
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
EOF
            ;;
    esac
}

generate_common_infrastructure_config() {
    cat << EOF

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379

# Service Discovery Configuration
EUREKA_DEFAULT_ZONE=http://eureka-server:8761/eureka
EOF
}

generate_observability_config() {
    # Skip observability configuration if disabled
    if [[ "$DISABLE_OBSERVABILITY" == true ]]; then
        return 0
    fi
    
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        cat << EOF

# Tracing Configuration (Zipkin)
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
EOF
    fi
}

generate_jwt_config() {
    cat << EOF

# JWT Configuration
JWK_SET_URI=http://user-service:8081/.well-known/jwks.json
EOF
}

generate_api_client_config() {
    local api_client_prefix="$1"
    
    if [[ "$api_client_prefix" == "rest" ]]; then
        cat << EOF

# REST API Configuration
API_CLIENT_REST_USER_SERVICE_BASE_URL=http://user-service:8081
API_CLIENT_REST_PRODUCT_SERVICE_BASE_URL=http://product-service:8082
API_CLIENT_REST_ORDER_SERVICE_BASE_URL=http://order-service:8083
API_CLIENT_REST_PAYMENT_SERVICE_BASE_URL=http://payment-service:8084
EOF
    elif [[ "$api_client_prefix" == "grpc" ]]; then
        cat << EOF

# gRPC Configuration
GRPC_CLIENT_USER_ADDRESS=static://user-service:50051
GRPC_CLIENT_PRODUCT_ADDRESS=static://product-service:50052
GRPC_CLIENT_ORDER_ADDRESS=static://order-service:50053
GRPC_CLIENT_PAYMENT_ADDRESS=static://payment-service:50054
EOF
    fi
}

generate_email_config() {
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        cat << EOF

# Email Configuration
MAIL_SERVER_HOST=smtp4dev
EOF
    fi
}

generate_api_gateway_env() {
    local env_file="$ENV_DIR/api-gateway.env"
    echo "Creating $env_file"
    
    {
        echo "# Service Discovery Configuration"
        echo "EUREKA_DEFAULT_ZONE=http://eureka-server:8761/eureka"
        echo ""
        echo "# Redis Configuration"
        echo "REDIS_HOST=redis"
        echo "REDIS_PORT=6379"
        generate_jwt_config
        generate_observability_config
    } > "$env_file"
    
    echo "Generated $env_file"
}

generate_service_env() {
    local service_name="$1"
    local env_file="$ENV_DIR/${SERVICE_CONFIGS[$service_name]}"
    
    echo "Creating $env_file"
    
    {
        generate_database_config "$service_name"
        generate_messaging_config
        generate_common_infrastructure_config
        generate_api_client_config "${API_CLIENT%%-*}"

        # Add service-specific configurations
        local specific_config="${SERVICE_SPECIFIC_CONFIGS[$service_name]:-}"
        if [[ -n "$specific_config" ]]; then
            # Handle multiple configs separated by space
            for config in $specific_config; do
                case "$config" in
                    "email_config") generate_email_config ;;
                esac
            done
        fi
        
        generate_observability_config
    } > "$env_file"
    
    echo "Generated $env_file"
}

generate_environment_files() {
    echo "Generating environment files..."
    
    # Create env directory if it doesn't exist
    if ! mkdir -p "$ENV_DIR"; then
        echo "Failed to create environment directory: $ENV_DIR" >&2
        exit 1
    fi
    
    # Remove existing .env files
    if ! rm -f "$ENV_DIR/"*.env 2>/dev/null; then
        echo "Warning: Could not clean existing environment files"
    fi
    
    echo "Creating service-specific .env files with configuration from app-stack.cfg"
    
    # Generate API Gateway env file (special case)
    if ! generate_api_gateway_env; then
        echo "Failed to generate API Gateway environment file" >&2
        exit 1
    fi
    
    # Generate env files for other services
    local services=("user-service" "product-service" "order-service" "payment-service")
    for service in "${services[@]}"; do
        if ! generate_service_env "$service"; then
            echo "Failed to generate environment file for service: $service" >&2
            exit 1
        fi
    done
    
    # Verify all expected files were created
    local expected_files=("api-gateway.env" "user-service.env" "product-service.env" "order-service.env" "payment-service.env")
    for file in "${expected_files[@]}"; do
        if [[ ! -f "$ENV_DIR/$file" ]]; then
            echo "Error: Expected environment file not found: $ENV_DIR/$file" >&2
            exit 1
        fi
    done
    
    echo "Environment files generated successfully (${#expected_files[@]} files created)"
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

    echo "Granting execute permission to build script..."
    chmod +x "$build_script"

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
# SERVICE STARTUP FUNCTIONS
# =============================================================================

get_infrastructure_services() {
    local services=("redis")
    
    case "$DATASOURCE" in
        mysql) services+=("mysql") ;;
        postgres|postgresql) services+=("postgres") ;;
        *) 
            echo "Warning: Unknown datasource: $DATASOURCE, defaulting to mysql"
            services+=("mysql")
            ;;
    esac
    
    case "$MESSAGING" in
        kafka) services+=("kafka") ;;
        rabbitmq) services+=("rabbitmq") ;;
        *)
            echo "Warning: Unknown messaging system: $MESSAGING, defaulting to kafka"
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
    
    # Skip observability services if disabled
    if [[ "$DISABLE_OBSERVABILITY" == true ]]; then
        echo "${services[@]}"
        return 0
    fi
    
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

get_application_services() {
    local services=("user-service" "product-service" "order-service" "payment-service")
    echo "${services[@]}"
}

start_services() {
    echo "Starting services..."
    echo "Using compose file: $COMPOSE_FILE"

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

    # Application services
    local application_services
    read -ra application_services <<< "$(get_application_services)"
    if [[ ${#application_services[@]} -gt 0 ]]; then
        start_service_group "Application" "${application_services[@]}"
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

    echo "Starting $group_name services: ${services[*]}"

    local compose_args=()
    if [[ "$FORCE_RECREATE" == true ]]; then
        compose_args+=("--force-recreate")
    fi

    if $DOCKER_COMPOSE_CMD -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d "${compose_args[@]}" "${services[@]}"; then
        echo "$group_name services started"
        wait_for_service_group_healthy "${services[@]}"
    else
        echo "Failed to start $group_name services" >&2
        echo "You can check logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs"
        exit 1
    fi
    echo
}

wait_for_service_group_healthy() {
    local services=("$@")
    local max_wait_time=300  # 5 minutes per group
    local check_interval=10  # 10 seconds
    local elapsed_time=0
    local all_healthy=false

    echo "Waiting for services to be healthy: ${services[*]}"

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
            echo "All services in group are healthy: ${services[*]}"
            break
        else
            echo "Waiting for services: ${unhealthy_services[*]} (${elapsed_time}s/${max_wait_time}s)"
            sleep $check_interval
            elapsed_time=$((elapsed_time + check_interval))
        fi
    done
    
    if [[ $all_healthy == false ]]; then
        echo "Timeout waiting for service group to be healthy after ${max_wait_time} seconds: ${services[*]}"
        echo "You can check service logs with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME logs [service_name]"
        echo "You can check service status with: $DOCKER_COMPOSE_CMD -f $COMPOSE_FILE -p $PROJECT_NAME ps"
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
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        echo "  SMTP4Dev:      http://smtp4dev.atlas.local"
    fi
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
    parse_arguments "$@"

    check_prerequisites

    local start_time=$(date +%s)

    echo "Atlas Docker Compose Platform - Starting..."

    read_app_stack_config
    
    generate_environment_files

    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
    else
        echo "Skipping build step (--skip-build flag provided)"
    fi

    start_services

    show_deployment_summary "$start_time"
}

# Execute main function
main "$@"
