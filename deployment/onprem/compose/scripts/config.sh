#!/bin/bash

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="${PROJECT_NAME:-atlas-onprem-compose}"

# Unified compose file path
COMPOSE_FILE="$PROJECT_ROOT/deployment/onprem/compose/docker-compose.yml"

# Single project stack (unified)
PROJECT_STACK="${PROJECT_NAME}"

# Service arrays
INFRASTRUCTURE_SERVICES=(
  "mysql" 
  "redis" 
  "kafka"
  # "zookeeper"
  # "rabbitmq"
  # "keycloak"
  "smtp4dev"
)

BACKEND_SERVICES=(
  "discovery-server" 
  "user-service"
  "product-service" 
  "order-service"
  "notification-service"
  "auth-server"
  "api-gateway"
)

OBSERVABILITY_SERVICES=(
  "loki"
  "promtail"
  "prometheus"
  "zipkin"
  "grafana"
)

FRONTEND_SERVICES=(
  "frontend"
)
