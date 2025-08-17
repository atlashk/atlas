#!/bin/bash

# =============================================================================
# Atlas Environment File Generator
# =============================================================================
# This script generates environment files for Atlas microservices based on
# configuration from app-stack.cfg
# =============================================================================

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"

# Source required utilities
source "$PROJECT_ROOT/backend/scripts/logger.sh"
source "$PROJECT_ROOT/backend/scripts/common.sh"

# Project configuration
PROJECT_NAME="atlas"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"
ENV_DIR="$SCRIPT_DIR/env"

# Service configuration arrays
declare -A SERVICE_CONFIGS=(
    ["api-gateway"]="api-gateway.env"
    ["user-service"]="user-service.env"
    ["product-service"]="product-service.env"
    ["order-service"]="order-service.env"
    ["notification-service"]="notification-service.env"
)

declare -A SERVICE_DATABASES=(
    ["user-service"]="db_user"
    ["product-service"]="db_product"
    ["order-service"]="db_order"
    ["notification-service"]="db_notification"
)

# Define service-specific configurations
declare -A SERVICE_SPECIFIC_CONFIGS=(
    ["api-gateway"]="jwt_config"
    ["order-service"]="api_client_config"
    ["notification-service"]="email_config"
)

# Configuration variables (populated by read_app_stack_config)
DATASOURCE=""
MESSAGING=""
API_CLIENT=""
NOTIFICATION_EMAIL=""
OBSERVABILITY_LOGGING_STACK=""
OBSERVABILITY_METRICS=""
OBSERVABILITY_TRACING=""

# =============================================================================
# CONFIGURATION READING
# =============================================================================

read_app_stack_config() {
    log_section "Reading application stack configuration..."
    
    # Use common function to read platform config
    read_platform_config "$APP_STACK_CONFIG"
    
    # Read additional configuration values specific to this deployment
    DATASOURCE=$(grep "^datasource=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    MESSAGING=$(grep "^messaging=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    API_CLIENT=$(grep "^api-client=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    NOTIFICATION_EMAIL=$(grep "^notification.email=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_LOGGING_STACK=$(grep "^observability.logging.stack=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_METRICS=$(grep "^observability.metrics=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    OBSERVABILITY_TRACING=$(grep "^observability.tracing=" "$APP_STACK_CONFIG" | cut -d'=' -f2)
    
    log_success "Configuration loaded: datasource=$DATASOURCE, messaging=$MESSAGING, api-client=$API_CLIENT"
}

# =============================================================================
# REUSABLE CONFIGURATION COMPONENTS
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
DB_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
DB_URL=jdbc:mysql://mysql:3306/$database_name?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=root
DB_QUARTZ_URL=jdbc:mysql://mysql:3306/db_quartz?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
DB_QUARTZ_USERNAME=root
DB_QUARTZ_PASSWORD=root
DB_QUARTZ_DRIVER_DELEGATE_CLASS=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
EOF
            ;;
        postgres)
            cat << EOF
# Database Configuration (PostgreSQL) for $service_name
DB_DRIVER_CLASS_NAME=org.postgresql.Driver
DB_URL=jdbc:postgresql://postgres:5432/$database_name
DB_USERNAME=root
DB_PASSWORD=root
DB_QUARTZ_URL=jdbc:postgresql://postgres:5432/db_quartz
DB_QUARTZ_USERNAME=root
DB_QUARTZ_PASSWORD=root
DB_QUARTZ_DRIVER_DELEGATE_CLASS=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
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
EOF
    elif [[ "$api_client_prefix" == "grpc" ]]; then
        cat << EOF

# gRPC Configuration
GRPC_CLIENT_USER_ADDRESS=static://user-service:50051
GRPC_CLIENT_PRODUCT_ADDRESS=static://product-service:50052
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

# =============================================================================
# SERVICE-SPECIFIC ENVIRONMENT FILE GENERATION
# =============================================================================

generate_api_gateway_env() {
    local env_file="$ENV_DIR/api-gateway.env"
    log_info "Creating $env_file"
    
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
    
    log_success "Generated $env_file"
}

generate_service_env() {
    local service_name="$1"
    local env_file="$ENV_DIR/${SERVICE_CONFIGS[$service_name]}"
    
    log_info "Creating $env_file"
    
    {
        generate_database_config "$service_name"
        generate_messaging_config
        generate_common_infrastructure_config
        
        # Add service-specific configurations
        local specific_config="${SERVICE_SPECIFIC_CONFIGS[$service_name]:-}"
        if [[ -n "$specific_config" ]]; then
            case "$specific_config" in
                "api_client_config") 
                    # Extract prefix from API_CLIENT configuration (e.g., "rest-restclient" -> "rest")
                    local api_client_prefix="${API_CLIENT%%-*}"
                    generate_api_client_config "$api_client_prefix" 
                    ;;
                "email_config") generate_email_config ;;
            esac
        fi
        
        generate_observability_config
    } > "$env_file"
    
    log_success "Generated $env_file"
}

generate_env_files() {
    log_section "Generating .env files for each service..."
    
    # Create env directory if it doesn't exist
    mkdir -p "$ENV_DIR"
    
    # Remove existing .env files
    rm -f "$ENV_DIR/"*.env
    
    log_info "Creating service-specific .env files with configuration from app-stack.cfg"
    
    # Generate API Gateway env file (special case)
    generate_api_gateway_env
    
    # Generate env files for other services
    for service in "user-service" "product-service" "order-service" "notification-service"; do
        generate_service_env "$service"
    done
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    read_app_stack_config
    
    generate_env_files
}

# Execute main function if script is run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
