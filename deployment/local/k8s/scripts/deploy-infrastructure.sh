#!/bin/bash

# Atlas Infrastructure Deployment Script
# Deploys all infrastructure services (databases, caching, messaging, etc.)

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_ROOT="$(dirname "$SCRIPT_DIR")"
BASE_DIR="$K8S_ROOT/base/infrastructure"

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

# Function to deploy a component
deploy_component() {
    local component=$1
    local component_dir="$BASE_DIR/$component"
    
    if [[ ! -d "$component_dir" ]]; then
        warn "Component directory $component_dir does not exist, skipping"
        return 0
    fi
    
    log "Deploying $component..."
    
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
        return 1
    fi
}

# Function to check service health
check_service_health() {
    local service=$1
    local port=$2
    local namespace=${3:-default}
    
    log "Checking health of service $service..."
    
    # Port-forward and check if service responds
    kubectl port-forward "service/$service" "$port:$port" -n "$namespace" &
    local pf_pid=$!
    
    sleep 5
    
    if curl -f "http://localhost:$port" &> /dev/null; then
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
    log "Starting Atlas infrastructure deployment..."
    
    check_prerequisites
    
    # Infrastructure deployment order (dependencies first)
    local components=(
        "mysql"
        "redis" 
        "kafka"
        "rabbitmq"
    )
    
    # Deploy components
    for component in "${components[@]}"; do
        deploy_component "$component"
    done
    
    log "Waiting for deployments to be ready..."
    
    # Wait for critical infrastructure
    wait_for_deployment "mysql" "default" 300
    wait_for_deployment "redis" "default" 180
    wait_for_deployment "kafka" "default" 180
    wait_for_deployment "rabbitmq" "default" 180
    
    # Optional health checks
    if [[ "${SKIP_HEALTH_CHECK:-false}" != "true" ]]; then
        log "Performing health checks..."
        check_service_health "mysql" 3306 || true
        check_service_health "redis" 6379 || true
    fi
    
    log "Infrastructure deployment completed successfully!"
    log "You can check the status with: kubectl get all -l component=database"
}

# Handle script interruption
trap 'error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@" 