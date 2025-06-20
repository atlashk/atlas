#!/bin/bash

set -euo pipefail

# Project configuration (previously in config.sh)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-compose"
COMPOSE_FILE="$PROJECT_ROOT/deployment/onprem/compose/docker-compose.yml"

# Source logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Check Docker Compose prerequisites (previously in docker-helper.sh)
check_docker_compose_prerequisites() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
}

# Print usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --containers-only Stop and remove only containers"
    echo "  --volumes-only    Remove only volumes"
    echo "  --images-only     Remove only Docker images"
    echo "  --networks-only   Remove only networks"
    echo "  --all             Remove everything (default)"
    echo "  -h, --help        Show this help message"
    exit 1
}

# Remove containers for services
remove_containers() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Removing containers for services in $compose_file"
    
    # Stop and remove only containers from this specific project
    docker-compose -f "$compose_file" -p "$project_prefix" down 2>/dev/null || true
    
    # Additionally, find any remaining containers with our project prefix
    local container_ids
    container_ids=$(docker ps -a --filter "name=${project_prefix}_" --format "{{.ID}}" || true)
    
    if [ -n "$container_ids" ]; then
        log_info "Found additional containers to remove:"
        for container_id in $container_ids; do
            local container_name
            container_name=$(docker inspect --format='{{.Name}}' "$container_id" | sed 's|^/||')
            log_info "  - $container_name"
        done
        echo
        
        log_info "Stopping and removing additional containers..."
        echo "$container_ids" | xargs -r docker stop 2>/dev/null || true
        echo "$container_ids" | xargs -r docker rm 2>/dev/null || true
    fi

    log_success "All Atlas containers removed successfully!"
}

# Remove volumes for services
remove_volumes() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Removing volumes for services in $compose_file"
    
    # Get all existing volumes with our project prefix
    local volume_names
    volume_names=$(docker volume ls --format "{{.Name}}" | grep "^${project_prefix}_" || true)
    
    if [ -n "$volume_names" ]; then
        log_info "Found volumes to remove:"
        for volume_name in $volume_names; do
            log_info "  - $volume_name"
        done
        echo
        
        for volume_name in $volume_names; do
            log_info "Removing volume: $volume_name"
            docker volume rm "$volume_name" 2>/dev/null || true
        done
    else
        log_info "No volumes found with prefix: $project_prefix"
    fi

    log_success "All volumes removed successfully!"
}

# Remove Docker images for services
remove_images() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Removing Docker images for services in $compose_file"
    
    # Define external/infrastructure images that should be skipped
    local external_images=(
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
    
    # Define custom Atlas images (images built by this project)
    local atlas_images=(
        "eureka-server:latest"
        "auth-server:latest"
        "api-gateway:latest"
        "user-service:latest"
        "product-service:latest"
        "order-service:latest"
        "notification-service:latest"
        "frontend:latest"
    )
    
    log_info "Removing only custom Atlas images (skipping external/infrastructure images)"
    
    # Remove only custom Atlas images
    for image in "${atlas_images[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log_info "Removing Atlas image: $image"
            docker image rm "$image" 2>/dev/null || true
        else
            log_info "Atlas image not found (already removed or not built): $image"
        fi
    done
    
    # List preserved external images for transparency
    log_info "Preserved external/infrastructure images:"
    for image in "${external_images[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log_info "  ✓ Preserved: $image"
        fi
    done
    
    # Optional: Remove any dangling images related to Atlas services
    log_info "Removing dangling Atlas images..."
    docker images --filter "dangling=true" --filter "reference=*-service:*" --filter "reference=*-server:*" --filter "reference=frontend:*" -q | xargs -r docker rmi 2>/dev/null || true

    log_success "Custom Atlas images removed successfully! (External infrastructure images preserved)"
}

# Remove Docker networks
remove_networks() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Removing Docker networks for services in $compose_file"
    
    # Get all existing networks related to our project
    local network_names
    network_names=$(docker network ls --format "{{.Name}}" | grep -E "(^${project_prefix}_|atlas-network)" || true)
    
    if [ -n "$network_names" ]; then
        log_info "Found networks to remove:"
        for network_name in $network_names; do
            log_info "  - $network_name"
        done
        echo
        
        for network_name in $network_names; do
            log_info "Removing network: $network_name"
            docker network rm "$network_name" 2>/dev/null || true
        done
    else
        log_info "No networks found for project: $project_prefix"
    fi

    log_success "Networks removed successfully!"
}

# Main execution
main() {
    local mode="all"
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --containers-only)
                mode="containers"
                shift
                ;;
            --volumes-only)
                mode="volumes"
                shift
                ;;
            --images-only)
                mode="images"
                shift
                ;;
            --networks-only)
                mode="networks"
                shift
                ;;
            --all)
                mode="all"
                shift
                ;;
            -h|--help)
                usage
                ;;
            *)
                echo "Unknown option: $1"
                usage
                ;;
        esac
    done
    
    log_info "Starting cleanup of resources using compose file: $COMPOSE_FILE"
    echo

    check_docker_compose_prerequisites

    case "$mode" in
        containers)
            remove_containers "$COMPOSE_FILE" "$PROJECT_NAME"
            ;;
        volumes)
            remove_volumes "$COMPOSE_FILE" "$PROJECT_NAME"
            ;;
        images)
            remove_images "$COMPOSE_FILE" "$PROJECT_NAME"
            ;;
        networks)
            remove_networks "$COMPOSE_FILE" "$PROJECT_NAME"
            ;;
        all)
            log_info "Removing all Atlas resources"
            log_info "  - Atlas containers (stopped and running)"
            log_info "  - Atlas volumes and data"
            log_info "  - Atlas Docker images"
            log_info "  - Atlas networks"
            log_info "  - Dangling Docker resources"
            
            # Remove containers, volumes, images, and networks
            remove_containers "$COMPOSE_FILE" "$PROJECT_NAME"
            remove_volumes "$COMPOSE_FILE" "$PROJECT_NAME"
            remove_images "$COMPOSE_FILE" "$PROJECT_NAME"
            remove_networks "$COMPOSE_FILE" "$PROJECT_NAME"
            
            # Clean up dangling resources (only Atlas-related)
            log_info "Cleaning up dangling Docker resources..."
            docker system prune -f >/dev/null 2>&1 || true
            
            log_success "All Atlas resources removed successfully!"
            ;;
    esac
}

# Run main function
main "$@"
