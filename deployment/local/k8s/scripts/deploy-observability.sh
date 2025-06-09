#!/bin/bash

# Atlas Observability Deployment Script
# Deploys monitoring and observability services (Prometheus, Grafana, Zipkin, etc.)

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_ROOT="$(dirname "$SCRIPT_DIR")"
BASE_DIR="$K8S_ROOT/base/observability"
MANIFEST_DIR="$K8S_ROOT/manifest"

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
    local manifest_file="$MANIFEST_DIR/$component.yaml"
    
    # Check if component exists in base directory
    if [[ -d "$component_dir" ]]; then
        log "Deploying observability/$component from base directory..."
        
        if [[ -f "$component_dir/kustomization.yaml" ]]; then
            kubectl apply -k "$component_dir"
        else
            kubectl apply -f "$component_dir/"
        fi
        
        log "$component deployed successfully from base"
        return 0
    fi
    
    # Fallback to manifest file
    if [[ -f "$manifest_file" ]]; then
        log "Deploying $component from manifest file..."
        kubectl apply -f "$manifest_file"
        log "$component deployed successfully from manifest"
        return 0
    fi
    
    warn "Component $component not found in base directory or manifest file, skipping"
    return 0
}

# Function to wait for deployment to be ready
wait_for_deployment() {
    local deployment=$1
    local namespace=${2:-default}
    local timeout=${3:-180}
    
    log "Waiting for deployment $deployment to be ready..."
    
    if kubectl wait --for=condition=available --timeout="${timeout}s" deployment/"$deployment" -n "$namespace" &> /dev/null; then
        log "Deployment $deployment is ready"
        return 0
    else
        error "Deployment $deployment failed to become ready within ${timeout} seconds"
        return 1
    fi
}

# Main deployment function
main() {
    log "Starting Atlas observability deployment..."
    
    check_prerequisites
    
    # Observability deployment order
    local components=(
        "zipkin"
        "prometheus"
        "grafana"
        "smtp4dev"
    )
    
    # Deploy components
    for component in "${components[@]}"; do
        deploy_component "$component"
    done
    
    log "Waiting for observability deployments to be ready..."
    
    # Wait for critical observability services
    local deployments=("zipkin" "prometheus" "grafana")
    for deployment in "${deployments[@]}"; do
        wait_for_deployment "$deployment" "default" 180 || true
    done
    
    log "Observability deployment completed successfully!"
    log ""
    log "Monitoring endpoints:"
    log "  - Grafana:    http://localhost:3000 (admin/admin)"
    log "  - Prometheus: http://localhost:9090"
    log "  - Zipkin:     http://localhost:9411"
    log "  - SMTP4Dev:   http://localhost:5000"
    log ""
    log "To access services:"
    log "  kubectl port-forward service/grafana 3000:3000"
    log "  kubectl port-forward service/prometheus 9090:9090"
    log "  kubectl port-forward service/zipkin 9411:9411"
    log "  kubectl port-forward service/smtp4dev 5000:80"
}

# Handle script interruption
trap 'error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@" 