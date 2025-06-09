#!/bin/bash

# Atlas Services Deployment Script
# Deploys all application microservices

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_ROOT="$(dirname "$SCRIPT_DIR")"
BASE_DIR="$K8S_ROOT/base"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Function to check if kubectl is available
check_prerequisites() {
    if ! command -v kubectl &> /dev/null; then
        error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        error "Unable to connect to Kubernetes cluster"
        exit 1
    fi
    
    log "Prerequisites check passed"
}

# Function to check infrastructure readiness
check_infrastructure() {
    log "Checking infrastructure readiness..."
    
    local required_services=("mysql" "redis" "kafka")
    local missing_services=()
    
    for service in "${required_services[@]}"; do
        if ! kubectl get service "$service" &> /dev/null; then
            missing_services+=("$service")
        fi
    done
    
    if [[ ${#missing_services[@]} -gt 0 ]]; then
        error "Missing infrastructure services: ${missing_services[*]}"
        error "Please run ./deploy-infrastructure.sh first"
        exit 1
    fi
    
    log "Infrastructure services are available"
}

# Function to deploy a component
deploy_component() {
    local component_type=$1
    local component=$2
    local component_dir="$BASE_DIR/$component_type/$component"
    
    if [[ ! -d "$component_dir" ]]; then
        warn "Component directory $component_dir does not exist, skipping"
        return 0
    fi
    
    log "Deploying $component_type/$component..."
    
    if [[ -f "$component_dir/kustomization.yaml" ]]; then
        # Use kustomize
        kubectl apply -k "$component_dir"
    else
        # Apply all yaml files
        kubectl apply -f "$component_dir/"
    fi
    
    log "$component deployed successfully"
}

# Function to wait for deployment to be ready
wait_for_deployment() {
    local deployment=$1
    local namespace=${2:-default}
    local timeout=${3:-300}
    
    log "Waiting for deployment $deployment to be ready..."
    
    if kubectl wait --for=condition=available --timeout="${timeout}s" deployment/"$deployment" -n "$namespace" &> /dev/null; then
        log "Deployment $deployment is ready"
        return 0
    else
        error "Deployment $deployment failed to become ready within ${timeout} seconds"
        kubectl describe deployment "$deployment" -n "$namespace"
        kubectl logs -l app="$deployment" --tail=50 -n "$namespace" || true
        return 1
    fi
}

# Function to check service health
check_service_health() {
    local service=$1
    local port=$2
    local health_path=${3:-"/actuator/health"}
    local namespace=${4:-default}
    
    log "Checking health of service $service..."
    
    # Port-forward and check if service responds
    kubectl port-forward "service/$service" "$port:$port" -n "$namespace" &
    local pf_pid=$!
    
    sleep 10
    
    if curl -f "http://localhost:$port$health_path" &> /dev/null; then
        log "Service $service is healthy"
        kill $pf_pid 2>/dev/null || true
        return 0
    else
        warn "Service $service may not be fully ready"
        kill $pf_pid 2>/dev/null || true
        return 1
    fi
}

# Main deployment function
main() {
    log "Starting Atlas services deployment..."
    
    check_prerequisites
    check_infrastructure
    
    # All microservices are now in microservices directory
    log "Deploying all microservices..."
    
    # All microservices deployment order (considering dependencies)
    local all_services=(
        "auth-server"          # Deploy auth first
        "api-gateway"          # Then gateway
        "user-service"
        "product-service"
        "order-service"        # Depends on user and product services
        "notification-service" # Can be deployed last
    )
    
    # Deploy all microservices
    for service in "${all_services[@]}"; do
        deploy_component "microservices" "$service"
    done
    
    log "Waiting for microservices to be ready..."
    
    # Wait for all microservices to be ready
    for service in "${all_services[@]}"; do
        wait_for_deployment "$service" "default" 300
    done
    
    # Optional health checks
    if [[ "${SKIP_HEALTH_CHECK:-false}" != "true" ]]; then
        log "Performing health checks..."
        
        # Check all microservices
        check_service_health "auth-server" 8091 || true
        check_service_health "api-gateway" 8080 || true
        check_service_health "user-service" 8081 || true
        check_service_health "product-service" 8082 || true
        check_service_health "order-service" 8083 || true
        check_service_health "notification-service" 8084 || true
    fi
    
    log "Microservices deployment completed successfully!"
    log ""
    log "Application endpoints:"
    log "  - API Gateway: http://localhost:8080"
    log "  - Auth Server: http://localhost:8091"
    log ""
    log "To check microservice status: kubectl get all -l tier=backend"
    log "To view logs: kubectl logs -f deployment/user-service"
}

# Handle script interruption
trap 'error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@" 