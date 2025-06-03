#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
PROJECT_NAME="${PROJECT_NAME:-atlas-local-compose}"

# Compose file paths
BACKEND_COMPOSE_FILE="$PROJECT_ROOT/deployment/local/compose/docker-compose.backend.yml"
INFRA_COMPOSE_FILE="$PROJECT_ROOT/deployment/local/compose/docker-compose.infra.yml"
OBSERVABILITY_COMPOSE_FILE="$PROJECT_ROOT/deployment/local/compose/docker-compose.observability.yml"

# Service categories
BACKEND_SERVICES=(discovery-server user-service product-service order-service notification-service auth-server api-gateway)
INFRA_SERVICES=(mysql redis kafka zookeeper rabbitmq keycloak smtp4dev)
OBSERVABILITY_SERVICES=(loki promtail prometheus zipkin grafana)

# Custom images that should be removed (built locally)
CUSTOM_IMAGES=(
    discovery-server:latest
    user-service:latest
    product-service:latest
    order-service:latest
    notification-service:latest
    auth-server:latest
    api-gateway:latest
)

# All volumes used across compose files
ALL_VOLUMES=(
    mysql-data
    redis-data
    zookeeper-data
    zookeeper-log
    kafka-data
    rabbitmq-data
    keycloak-data
    smtp4dev-data
    discovery-server-log
    user-service-log
    product-service-log
    order-service-log
    notification-service-log
    auth-server-log
    api-gateway-log
    grafana-data
)

# Logger
if [ ! -f "$PROJECT_ROOT/deployment/util/logger.sh" ]; then
    echo "Error: logger.sh not found at $PROJECT_ROOT/deployment/util/logger.sh"
    exit 1
fi
source "$PROJECT_ROOT/deployment/util/logger.sh"

usage() {
    echo "Usage: $0 {backend|infra|observability|all} [options]"
    echo "Options:"
    echo "  --remove-images    Remove custom Docker images"
    echo "  --remove-volumes   Remove Docker volumes"
    echo "  --remove-networks  Remove Docker networks"
    echo "  --remove-all       Remove images, volumes, and networks (equivalent to all three above)"
    echo ""
    echo "Examples:"
    echo "  $0 all                    # Stop all containers"
    echo "  $0 all --remove-all       # Stop containers and remove images, volumes, networks"
    echo "  $0 backend --remove-images # Stop backend services and remove custom images"
    exit 1
}

if [ $# -lt 1 ]; then
    error "At least one argument is required."
    usage
fi

# Parse arguments
CATEGORY="$1"
shift

REMOVE_IMAGES=false
REMOVE_VOLUMES=false
REMOVE_NETWORKS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --remove-images)
            REMOVE_IMAGES=true
            shift
            ;;
        --remove-volumes)
            REMOVE_VOLUMES=true
            shift
            ;;
        --remove-networks)
            REMOVE_NETWORKS=true
            shift
            ;;
        --remove-all)
            REMOVE_IMAGES=true
            REMOVE_VOLUMES=true
            REMOVE_NETWORKS=true
            shift
            ;;
        *)
            error "Unknown option: $1"
            usage
            ;;
    esac
done

case "$CATEGORY" in
    backend)
        COMPOSE_FILES=("-f" "$BACKEND_COMPOSE_FILE")
        SERVICES=("${BACKEND_SERVICES[@]}")
        VOLUMES_TO_REMOVE=(discovery-server-log user-service-log product-service-log order-service-log notification-service-log auth-server-log api-gateway-log)
        ;;
    infra)
        COMPOSE_FILES=("-f" "$INFRA_COMPOSE_FILE")
        SERVICES=("${INFRA_SERVICES[@]}")
        VOLUMES_TO_REMOVE=(mysql-data redis-data zookeeper-data zookeeper-log kafka-data rabbitmq-data keycloak-data smtp4dev-data)
        ;;
    observability)
        COMPOSE_FILES=("-f" "$OBSERVABILITY_COMPOSE_FILE")
        SERVICES=("${OBSERVABILITY_SERVICES[@]}")
        VOLUMES_TO_REMOVE=(grafana-data)
        ;;
    all)
        COMPOSE_FILES=("-f" "$INFRA_COMPOSE_FILE" "-f" "$BACKEND_COMPOSE_FILE" "-f" "$OBSERVABILITY_COMPOSE_FILE")
        SERVICES=("${INFRA_SERVICES[@]}" "${BACKEND_SERVICES[@]}" "${OBSERVABILITY_SERVICES[@]}")
        VOLUMES_TO_REMOVE=("${ALL_VOLUMES[@]}")
        ;;
    *)
        error "Invalid argument: $CATEGORY"
        usage
        ;;
esac

# Stop and remove containers
log "Stopping and removing containers for: ${SERVICES[*]}..."
if ! docker-compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" down --remove-orphans; then
    error "Failed to stop and remove containers."
    exit 1
fi

log "Containers stopped and removed successfully."

# Remove custom images if requested
if [ "$REMOVE_IMAGES" = true ]; then
    log "Removing custom Docker images..."
    
    for image in "${CUSTOM_IMAGES[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log "Removing image: $image"
            if ! docker rmi "$image" 2>/dev/null; then
                error "Failed to remove image: $image (it may be in use by other containers)"
            fi
        else
            debug "Image $image not found, skipping..."
        fi
    done
    
    # Remove dangling images
    log "Removing dangling images..."
    docker image prune -f >/dev/null 2>&1 || true
    
    log "Custom images removal completed."
fi

# Remove volumes if requested
if [ "$REMOVE_VOLUMES" = true ]; then
    log "Removing Docker volumes..."
    
    for volume in "${VOLUMES_TO_REMOVE[@]}"; do
        FULL_VOLUME_NAME="${PROJECT_NAME}_${volume}"
        if docker volume inspect "$FULL_VOLUME_NAME" >/dev/null 2>&1; then
            log "Removing volume: $FULL_VOLUME_NAME"
            if ! docker volume rm "$FULL_VOLUME_NAME" 2>/dev/null; then
                error "Failed to remove volume: $FULL_VOLUME_NAME (it may be in use)"
            fi
        else
            debug "Volume $FULL_VOLUME_NAME not found, skipping..."
        fi
    done
    
    # Remove dangling volumes
    log "Removing dangling volumes..."
    docker volume prune -f >/dev/null 2>&1 || true
    
    log "Volumes removal completed."
fi

# Remove networks if requested
if [ "$REMOVE_NETWORKS" = true ]; then
    log "Removing Docker networks..."
    
    # Remove project-specific network
    PROJECT_NETWORK="${PROJECT_NAME}_default"
    if docker network inspect "$PROJECT_NETWORK" >/dev/null 2>&1; then
        log "Removing network: $PROJECT_NETWORK"
        if ! docker network rm "$PROJECT_NETWORK" 2>/dev/null; then
            error "Failed to remove network: $PROJECT_NETWORK (it may be in use)"
        fi
    else
        debug "Network $PROJECT_NETWORK not found, skipping..."
    fi
    
    # Remove dangling networks
    log "Removing dangling networks..."
    docker network prune -f >/dev/null 2>&1 || true
    
    log "Networks removal completed."
fi

log "Stop script completed successfully."
