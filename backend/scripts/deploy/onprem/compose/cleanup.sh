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

# Source logger
source "$PROJECT_ROOT/backend/scripts/logger.sh"

# Docker Compose command (will be set by check_docker_compose)
DOCKER_COMPOSE_CMD=""

# =============================================================================
# CONFIGURATION - Centralized resource definitions
# =============================================================================

# Atlas container names as they appear in the compose file
declare -ra ATLAS_CONTAINERS=(
    "mysql"
    "redis"
    "kafka"
    "smtp4dev"
    "eureka-server"
    "api-gateway"
    "user-service"
    "product-service"
    "order-service"
    "notification-service"
    "loki"
    "promtail"
    "prometheus"
    "zipkin"
    "grafana"
    "frontend"
)

# External/infrastructure images that should be preserved
declare -ra EXTERNAL_IMAGES=(
    "mysql:8.0"
    "redis:7"
    "confluentinc/cp-zookeeper:7.0.1"
    "confluentinc/cp-kafka:7.0.1"
    "confluentinc/cp-kafka:7.9.0"
    "quay.io/keycloak/keycloak:26.2"
    "rnwood/smtp4dev"
    "grafana/loki:main"
    "grafana/promtail:main"
    "prom/prometheus"
    "openzipkin/zipkin"
    "grafana/grafana-oss"
)

# Custom Atlas images (built by this project)
declare -ra ATLAS_IMAGES=(
    "eureka-server:latest"
    "api-gateway:latest"
    "user-service:latest"
    "product-service:latest"
    "order-service:latest"
    "notification-service:latest"
    "frontend:latest"
)

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Docker Compose Cleanup Script - Removes all Atlas-related Docker resources"
    log_info ""
    log_info "This script removes:"
    log_info "  - Containers: Only those defined in the Atlas compose file"
    log_info "  - Volumes: Only those with the Atlas project prefix"
    log_info "  - Images: Only custom Atlas images (preserves external/infrastructure images)"
    log_info "  - Networks: Only Atlas-specific networks"
    log_info ""
    log_info "Options:"
    log_info "  -h, --help              Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                      # Clean all Atlas resources"
    log_info ""
    log_warn "⚠️  WARNING: This operation is DESTRUCTIVE and will delete Atlas data!"
    log_warn "Other Docker resources on your system will be preserved."
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
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
    
    check_docker || exit 1
    check_docker_compose || exit 1
    
    log_success "Prerequisites check passed"
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
    
    log_info "Removing Atlas containers..."
    
    # Stop and remove containers using docker compose
    $DOCKER_COMPOSE_CMD -f "$compose_file" -p "$project_prefix" down 2>/dev/null || true
    
    # Collect container IDs from both project prefix and explicit names
    local container_ids_by_prefix
    container_ids_by_prefix=$(get_container_ids "${project_prefix}_")
    
    local container_ids_by_name=""
    for container_name in "${ATLAS_CONTAINERS[@]}"; do
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
        log_info "Found Atlas containers to remove:"
        for container_id in $all_container_ids; do
            if [ -n "$container_id" ]; then
                local container_name
                container_name=$(docker inspect --format='{{.Name}}' "$container_id" 2>/dev/null | sed 's|^/||' || echo "unknown")
                log_info "  - $container_name ($container_id)"
            fi
        done
        
        log_info "Stopping and removing Atlas containers..."
        for container_id in $all_container_ids; do
            if [ -n "$container_id" ]; then
                docker stop "$container_id" 2>/dev/null || true
                docker rm "$container_id" 2>/dev/null || true
            fi
        done
    else
        log_info "No additional Atlas containers found to remove"
    fi

    log_success "Atlas containers removed successfully!"
}

# Remove volumes for services
remove_volumes() {
    local project_prefix=$2
    
    log_info "Removing Atlas volumes..."
    
    local volume_names
    volume_names=$(docker volume ls --format "{{.Name}}" | grep "^${project_prefix}_" || true)
    
    if [ -n "$volume_names" ]; then
        log_info "Found volumes to remove:"
        for volume_name in $volume_names; do
            log_info "  - $volume_name"
        done
        
        for volume_name in $volume_names; do
            log_info "Removing volume: $volume_name"
            docker volume rm "$volume_name" 2>/dev/null || true
        done
    else
        log_info "No Atlas volumes found"
    fi

    log_success "Atlas volumes removed successfully!"
}

# Remove Docker images for services
remove_images() {
    log_info "Removing Atlas Docker images..."
    
    log_info "Removing custom Atlas images (preserving external/infrastructure images):"
    
    # Remove only custom Atlas images
    for image in "${ATLAS_IMAGES[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log_info "  - Removing: $image"
            docker image rm "$image" 2>/dev/null || true
        else
            log_info "  - Not found: $image"
        fi
    done
    
    # Show preserved external images for transparency
    log_info "Preserved external/infrastructure images:"
    for image in "${EXTERNAL_IMAGES[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log_info "  ✓ Preserved: $image"
        fi
    done
    
    # Remove dangling Atlas images
    log_info "Removing dangling Atlas images..."
    local dangling_images
    dangling_images=$(docker images --filter "dangling=true" \
        --filter "reference=*atlas*" \
        --filter "reference=*-service:*" \
        --filter "reference=*-server:*" \
        --filter "reference=frontend:*" \
        -q 2>/dev/null || true)
    
    if [ -n "$dangling_images" ]; then
        echo "$dangling_images" | xargs -r docker rmi 2>/dev/null || true
        log_info "Removed dangling Atlas images"
    else
        log_info "No dangling Atlas images found"
    fi

    log_success "Atlas images removed successfully!"
}

# Remove Docker networks
remove_networks() {
    local project_prefix=$2
    
    log_info "Removing Atlas networks..."
    
    local network_names
    network_names=$(docker network ls --format "{{.Name}}" | grep -E "(^${project_prefix}_|atlas-network)" || true)
    
    if [ -n "$network_names" ]; then
        log_info "Found networks to remove:"
        for network_name in $network_names; do
            log_info "  - $network_name"
        done
        
        for network_name in $network_names; do
            log_info "Removing network: $network_name"
            docker network rm "$network_name" 2>/dev/null || true
        done
    else
        log_info "No Atlas networks found"
    fi

    log_success "Atlas networks removed successfully!"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    log_section "Atlas Docker Compose - Cleanup"
    log_info "Compose file: $COMPOSE_FILE"
    log_info "This script will remove ALL Atlas-related Docker resources:"
    log_info "  ✓ Containers (stopped and running)"
    log_info "  ✓ Volumes and data"
    log_info "  ✓ Custom Docker images (preserving external images)"
    log_info "  ✓ Networks"
    log_info ""
    log_info "Other Docker resources on your system will be preserved."
    log_info ""

    # Execute cleanup for all resources
    remove_containers "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_volumes "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_images "$COMPOSE_FILE" "$PROJECT_NAME"
    remove_networks "$COMPOSE_FILE" "$PROJECT_NAME"
    
    log_success "All Atlas resources removed successfully!"
    log_success "Atlas platform cleanup completed!"
}

# Execute main function
main "$@"
