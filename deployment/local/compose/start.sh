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
INFRA_SERVICES=(mysql redis kafka)
OBSERVABILITY_SERVICES=(loki promtail prometheus zipkin grafana)

# Logger
if [ ! -f "$PROJECT_ROOT/deployment/util/logger.sh" ]; then
    echo "Error: logger.sh not found at $PROJECT_ROOT/deployment/util/logger.sh"
    exit 1
fi
source "$PROJECT_ROOT/deployment/util/logger.sh"

usage() {
    echo "Usage: $0 {backend|infra|observability|all}"
    exit 1
}

if [ $# -ne 1 ]; then
    error "Exactly one argument is required."
    usage
fi

case "$1" in
    backend)
        COMPOSE_FILES=("-f" "$BACKEND_COMPOSE_FILE" "-f" "$INFRA_COMPOSE_FILE")  # backend may depend on infra
        SERVICES=("${BACKEND_SERVICES[@]}")
        ;;
    infra)
        COMPOSE_FILES=("-f" "$INFRA_COMPOSE_FILE")
        SERVICES=("${INFRA_SERVICES[@]}")
        ;;
    observability)
        COMPOSE_FILES=("-f" "$OBSERVABILITY_COMPOSE_FILE" "-f" "$INFRA_COMPOSE_FILE")  # zipkin may depend on mysql
        SERVICES=("${OBSERVABILITY_SERVICES[@]}")
        ;;
    all)
        COMPOSE_FILES=("-f" "$INFRA_COMPOSE_FILE" "-f" "$BACKEND_COMPOSE_FILE" "-f" "$OBSERVABILITY_COMPOSE_FILE")
        SERVICES=("${INFRA_SERVICES[@]}" "${BACKEND_SERVICES[@]}" "${OBSERVABILITY_SERVICES[@]}")
        ;;
    *)
        error "Invalid argument: $1"
        usage
        ;;
esac

if [ "${#SERVICES[@]}" -eq 0 ]; then
    error "No services specified to start."
    exit 1
fi

log "Starting services: ${SERVICES[*]}..."
if ! docker-compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" up -d "${SERVICES[@]}"; then
    error "Failed to start services."
    exit 1
fi

log "Checking service status..."
for service in "${SERVICES[@]}"; do
    if ! docker-compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" ps "$service" | grep -q "Up"; then
        error "Service $service is not running."
        exit 1
    fi
    log "Service $service is running."
done

log "Services started successfully."
