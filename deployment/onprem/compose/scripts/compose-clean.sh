#!/bin/bash

set -euo pipefail

# Source config and helper scripts
source "$(dirname "${BASH_SOURCE[0]}")/config.sh"
source "$(dirname "${BASH_SOURCE[0]}")/docker-helper.sh"

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
    
    log_info "Atlas Cleanup Tool"
    log_info "Using unified compose file: $COMPOSE_FILE"
    echo
    
    check_docker_compose_prerequisites

    case "$mode" in
        containers)
            docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" down --remove-orphans 2>/dev/null || true
            ;;
        volumes)
            remove_volumes "$COMPOSE_FILE" "$PROJECT_STACK"
            ;;
        images)
            remove_images "$COMPOSE_FILE" "$PROJECT_STACK"
            ;;
        networks)
            remove_networks "$COMPOSE_FILE" "$PROJECT_STACK"
            ;;
        all)
            log_info "Removing all resources"
            log_info "  - All containers (stopped and running)"
            log_info "  - All volumes and data"
            log_info "  - All Docker images"
            log_info "  - All networks"
            log_info "  - Dangling Docker resources"
            
            # Stop and remove containers
            log_info "Stopping and removing containers..."
            docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_STACK" down --remove-orphans 2>/dev/null || true
            
            # Remove volumes, images, and networks
            remove_volumes "$COMPOSE_FILE" "$PROJECT_STACK"
            remove_images "$COMPOSE_FILE" "$PROJECT_STACK"
            remove_networks "$COMPOSE_FILE" "$PROJECT_STACK"
            
            # Clean up dangling resources
            log_info "Cleaning up dangling Docker resources..."
            docker system prune -f >/dev/null 2>&1 || true
            
            log_success "All resources removed successfully!"
            ;;
    esac
}

# Run main function
main "$@"
