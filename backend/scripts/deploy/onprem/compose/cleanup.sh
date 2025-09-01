#!/bin/bash

# =============================================================================
# Atlas Docker Compose Cleanup Script
# =============================================================================
# This script safely removes ONLY Atlas-related Docker resources
# =============================================================================

set -euo pipefail

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/backend/scripts/deploy/onprem/compose/docker-compose.yml"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD=""

# Configuration variables (populated by read_app_stack_config)
declare -g DATASOURCE MESSAGING API_CLIENT NOTIFICATION_EMAIL OBSERVABILITY_LOGGING_STACK OBSERVABILITY_METRICS OBSERVABILITY_TRACING

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
# DYNAMIC SERVICE DETERMINATION
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
    local services=("eureka-server" "api-gateway" "user-service" "product-service" "order-service" "notification-service")
    echo "${services[@]}"
}

get_all_atlas_containers() {
    local all_services=()
    
    # Get infrastructure services
    local infrastructure_services
    read -ra infrastructure_services <<< "$(get_infrastructure_services)"
    all_services+=("${infrastructure_services[@]}")
    
    # Get observability services
    local observability_services
    read -ra observability_services <<< "$(get_observability_services)"
    all_services+=("${observability_services[@]}")
    
    # Get application services
    local application_services
    read -ra application_services <<< "$(get_application_services)"
    all_services+=("${application_services[@]}")
    
    # Add frontend and nginx
    all_services+=("frontend" "nginx")
    
    echo "${all_services[@]}"
}

get_external_images() {
    local images=("redis:7")
    
    case "$DATASOURCE" in
        mysql) images+=("mysql:8.0") ;;
        postgres|postgresql) images+=("postgres:15") ;;
    esac
    
    case "$MESSAGING" in
        kafka) images+=("confluentinc/cp-zookeeper:7.0.1" "confluentinc/cp-kafka:7.0.1" "confluentinc/cp-kafka:7.9.0") ;;
        rabbitmq) images+=("rabbitmq:3-management") ;;
    esac
    
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        images+=("rnwood/smtp4dev")
    fi
    
    if [[ "$OBSERVABILITY_LOGGING_STACK" == "loki" ]]; then
        images+=("grafana/loki:main" "grafana/promtail:main")
    fi
    
    if [[ "$OBSERVABILITY_METRICS" == "prometheus" ]]; then
        images+=("prom/prometheus")
    fi
    
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        images+=("openzipkin/zipkin")
    fi
    
    # Add grafana if any observability services are enabled
    local observability_services
    read -ra observability_services <<< "$(get_observability_services)"
    if [[ ${#observability_services[@]} -gt 0 ]]; then
        images+=("grafana/grafana-oss")
    fi
    
    echo "${images[@]}"
}

get_atlas_images() {
    local images=("eureka-server:latest" "api-gateway:latest" "user-service:latest" "product-service:latest" "order-service:latest" "notification-service:latest" "frontend:latest")
    echo "${images[@]}"
}

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Docker Compose Cleanup Script - Removes all Atlas-related Docker resources"
    echo ""
    echo "This script dynamically determines which resources to remove based on your app-stack.cfg configuration:"
    echo "  - Containers: Only those defined in the Atlas compose file and enabled in configuration"
    echo "  - Volumes: Only those with the Atlas project prefix"
    echo "  - Images: Only custom Atlas images (preserves external/infrastructure images)"
    echo "  - Networks: Only Atlas-specific networks"
    echo ""
    echo "The script reads configuration from: backend/app-stack.cfg"
    echo "Supported configurations: datasource, messaging, observability components, email notifications"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                      # Clean all Atlas resources based on current configuration"
    echo ""
    echo "⚠️  WARNING: This operation is DESTRUCTIVE and will delete Atlas data!"
    echo "Other Docker resources on your system will be preserved."
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
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
# CHECK PRE-REQUISITES
# =============================================================================

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
    
    check_docker || exit 1
    check_docker_compose || exit 1
    
    echo "Prerequisites check passed"
    echo
}

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

# Get container IDs by name pattern
get_container_ids() {
    local pattern=$1
    docker ps -a --filter "name=${pattern}" --format "{{.ID}}" 2>/dev/null || true
}

# Get container status by name pattern
get_container_status() {
    local pattern=$1
    docker ps -a --filter "name=${pattern}" --format "{{.Names}} ({{.Status}})" 2>/dev/null || true
}

# =============================================================================
# CLEANUP FUNCTIONS
# =============================================================================

# Remove containers for services
remove_containers() {
    local compose_file=$1
    local project_prefix=$2
    
    echo "Removing Atlas containers..."
    
    # Stop and remove containers using docker compose
    $DOCKER_COMPOSE_CMD -f "$compose_file" -p "$project_prefix" down 2>/dev/null || true
    
    # Get dynamic list of Atlas containers based on configuration
    local atlas_containers
    read -ra atlas_containers <<< "$(get_all_atlas_containers)"
    
    # Collect container IDs from both project prefix and explicit names
    local container_ids_by_prefix
    container_ids_by_prefix=$(get_container_ids "${project_prefix}_")
    
    local container_ids_by_name=""
    for container_name in "${atlas_containers[@]}"; do
        local container_id
        container_id=$(get_container_ids "^${container_name}$")
        if [ -n "$container_id" ]; then
            container_ids_by_name="$container_ids_by_name $container_id"
        fi
    done
    
    # Combine and deduplicate container IDs
    local all_container_ids
    all_container_ids=$(echo "$container_ids_by_prefix $container_ids_by_name" | tr ' ' '\n' | sort -u | tr '\n' ' ')
    
    if [ -n "$all_container_ids" ] && [ "$all_container_ids" != " " ]; then
        echo "Found Atlas containers to remove:"
        for container_id in $all_container_ids; do
            if [ -n "$container_id" ]; then
                local container_name
                container_name=$(docker inspect --format='{{.Name}}' "$container_id" 2>/dev/null | sed 's|^/||' || echo "unknown")
                echo "  - $container_name ($container_id)"
            fi
        done
        
        echo "Stopping and removing Atlas containers..."
        for container_id in $all_container_ids; do
            if [ -n "$container_id" ]; then
                docker stop "$container_id" 2>/dev/null || true
                docker rm "$container_id" 2>/dev/null || true
            fi
        done
    else
        echo "No additional Atlas containers found to remove"
    fi

    echo "Atlas containers removed successfully!"
    echo
}

# Remove volumes for services
remove_volumes() {
    local project_prefix=$2
    
    echo "Removing Atlas volumes..."
    
    local volume_names
    volume_names=$(docker volume ls --format "{{.Name}}" | grep "^${project_prefix}_" || true)
    
    if [ -n "$volume_names" ]; then
        echo "Found volumes to remove:"
        for volume_name in $volume_names; do
            echo "  - $volume_name"
        done
        
        for volume_name in $volume_names; do
            echo "Removing volume: $volume_name"
            docker volume rm "$volume_name" 2>/dev/null || true
        done
    else
        echo "No Atlas volumes found"
    fi

    echo "Atlas volumes removed successfully!"
    echo
}

# Remove Docker images for services
remove_images() {
    echo "Removing Atlas Docker images..."

    echo "Removing custom Atlas images (preserving external/infrastructure images):"
    
    # Get dynamic list of Atlas images
    local atlas_images
    read -ra atlas_images <<< "$(get_atlas_images)"
    
    # Remove only custom Atlas images
    for image in "${atlas_images[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            echo "  - Removing: $image"
            docker image rm "$image" 2>/dev/null || true
        else
            echo "  - Not found: $image"
        fi
    done

    # Get dynamic list of external images and show preserved ones for transparency
    local external_images
    read -ra external_images <<< "$(get_external_images)"
    
    echo "Preserved external/infrastructure images:"
    for image in "${external_images[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            echo "  ✓ Preserved: $image"
        fi
    done

    # Remove dangling Atlas images
    echo "Removing dangling Atlas images..."
    local dangling_images
    dangling_images=$(docker images --filter "dangling=true" \
        --filter "reference=*atlas*" \
        --filter "reference=*-service:*" \
        --filter "reference=*-server:*" \
        --filter "reference=frontend:*" \
        -q 2>/dev/null || true)
    
    if [ -n "$dangling_images" ]; then
        echo "$dangling_images" | xargs -r docker rmi 2>/dev/null || true
        echo "Removed dangling Atlas images"
    else
        echo "No dangling Atlas images found"
    fi

    echo "Atlas images removed successfully!"
    echo
}

# Remove Docker networks
remove_networks() {
    local project_prefix=$2
    
    echo "Removing Atlas networks..."
    
    local network_names
    network_names=$(docker network ls --format "{{.Name}}" | grep -E "(^${project_prefix}_|atlas-network)" || true)
    
    if [ -n "$network_names" ]; then
        echo "Found networks to remove:"
        for network_name in $network_names; do
            echo "  - $network_name"
        done
        
        for network_name in $network_names; do
            echo "Removing network: $network_name"
            docker network rm "$network_name" 2>/dev/null || true
        done
    else
        echo "No Atlas networks found"
    fi

    echo "Atlas networks removed successfully!"
    echo
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"

    check_prerequisites

    read_app_stack_config

    echo "=== Atlas Docker Compose - Cleanup ==="
    echo "Compose file: $COMPOSE_FILE"
    echo "This script will remove ALL Atlas-related Docker resources based on your configuration:"
    echo "  ✓ Containers (stopped and running)"
    echo "  ✓ Volumes and data"
    echo "  ✓ Custom Docker images (preserving external images)"
    echo "  ✓ Networks"
    echo ""
    echo "Other Docker resources on your system will be preserved."
    echo ""

    # Execute cleanup for all resources
    remove_containers "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_volumes "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_images "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_networks "$COMPOSE_FILE" "$PROJECT_NAME"
    
    echo "All Atlas resources removed successfully!"
    echo "Atlas platform cleanup completed!"
}

# Execute main function
main "$@"
