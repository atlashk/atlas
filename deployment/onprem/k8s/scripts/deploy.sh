#!/bin/bash

# Atlas OnPrem K8s Deployment Script
# Usage: ./deploy.sh [environment] [action]
# Environments: local, dev, stg, prod
# Actions: apply, delete, restart, logs, status

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BASE_DIR="${PROJECT_ROOT}/base"
ENVIRONMENTS_DIR="${PROJECT_ROOT}/environments"

# Default values
ENVIRONMENT="${1:-local}"
ACTION="${2:-apply}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate environment
validate_environment() {
    case $ENVIRONMENT in
        local|dev|stg|prod)
            log_info "Using environment: $ENVIRONMENT"
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT"
            log_error "Valid environments: local, dev, stg, prod"
            exit 1
            ;;
    esac
}

# Check prerequisites
check_prerequisites() {
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    
    if ! command -v kustomize &> /dev/null; then
        log_warning "kustomize not found, using kubectl kustomize"
    fi
    
    # Check if kubectl can connect to cluster
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

# Create namespace if it doesn't exist
ensure_namespace() {
    local namespace="atlas-${ENVIRONMENT}"
    
    if ! kubectl get namespace "$namespace" &> /dev/null; then
        log_info "Creating namespace: $namespace"
        kubectl create namespace "$namespace"
    else
        log_info "Namespace already exists: $namespace"
    fi
}

# Deploy infrastructure components
deploy_infrastructure() {
    log_info "Deploying infrastructure components..."
    cd "${BASE_DIR}/infrastructure"
    kubectl apply -k . -n "atlas-${ENVIRONMENT}"
    log_success "Infrastructure components deployed"
}

# Deploy microservices
deploy_microservices() {
    log_info "Deploying microservices..."
    cd "${BASE_DIR}/microservices"
    kubectl apply -k . -n "atlas-${ENVIRONMENT}"
    log_success "Microservices deployed"
}

# Deploy observability components
deploy_observability() {
    log_info "Deploying observability components..."
    cd "${BASE_DIR}/observability"
    kubectl apply -k . -n "atlas-${ENVIRONMENT}"
    log_success "Observability components deployed"
}

# Deploy environment-specific overlays
deploy_environment_overlay() {
    local env_overlay_dir="${ENVIRONMENTS_DIR}/${ENVIRONMENT}"
    
    if [[ -d "$env_overlay_dir" ]]; then
        log_info "Deploying environment overlay for: $ENVIRONMENT"
        cd "$env_overlay_dir"
        kubectl apply -k . -n "atlas-${ENVIRONMENT}"
        log_success "Environment overlay deployed"
    else
        log_warning "No environment overlay found for: $ENVIRONMENT"
    fi
}

# Delete all resources
delete_resources() {
    log_info "Deleting all resources for environment: $ENVIRONMENT"
    local namespace="atlas-${ENVIRONMENT}"
    
    # Delete environment overlay first
    local env_overlay_dir="${ENVIRONMENTS_DIR}/${ENVIRONMENT}"
    if [[ -d "$env_overlay_dir" ]]; then
        cd "$env_overlay_dir"
        kubectl delete -k . -n "$namespace" --ignore-not-found=true || true
    fi
    
    # Delete base components
    cd "${BASE_DIR}/observability"
    kubectl delete -k . -n "$namespace" --ignore-not-found=true || true
    
    cd "${BASE_DIR}/microservices"
    kubectl delete -k . -n "$namespace" --ignore-not-found=true || true
    
    cd "${BASE_DIR}/infrastructure"
    kubectl delete -k . -n "$namespace" --ignore-not-found=true || true
    
    log_success "All resources deleted"
}

# Show deployment status
show_status() {
    local namespace="atlas-${ENVIRONMENT}"
    log_info "Deployment status for environment: $ENVIRONMENT"
    
    echo
    echo "=== Namespace ==="
    kubectl get namespace "$namespace" 2>/dev/null || echo "Namespace not found"
    
    echo
    echo "=== Deployments ==="
    kubectl get deployments -n "$namespace" 2>/dev/null || echo "No deployments found"
    
    echo
    echo "=== Services ==="
    kubectl get services -n "$namespace" 2>/dev/null || echo "No services found"
    
    echo
    echo "=== Pods ==="
    kubectl get pods -n "$namespace" 2>/dev/null || echo "No pods found"
    
    echo
    echo "=== ConfigMaps ==="
    kubectl get configmaps -n "$namespace" 2>/dev/null || echo "No configmaps found"
    
    echo
    echo "=== Secrets ==="
    kubectl get secrets -n "$namespace" 2>/dev/null || echo "No secrets found"
}

# Show logs
show_logs() {
    local namespace="atlas-${ENVIRONMENT}"
    local service="${3:-}"
    
    if [[ -n "$service" ]]; then
        log_info "Showing logs for service: $service in namespace: $namespace"
        kubectl logs -f -l app="$service" -n "$namespace"
    else
        log_info "Available services in namespace: $namespace"
        kubectl get pods -n "$namespace" -o custom-columns=NAME:.metadata.name,STATUS:.status.phase
        echo
        log_info "To show logs for a specific service, use: ./deploy.sh $ENVIRONMENT logs [service-name]"
    fi
}

# Wait for deployments to be ready
wait_for_deployments() {
    local namespace="atlas-${ENVIRONMENT}"
    log_info "Waiting for deployments to be ready..."
    
    # Wait for all deployments to be ready
    kubectl wait --for=condition=available --timeout=300s deployment --all -n "$namespace" || {
        log_warning "Some deployments may not be ready yet"
        show_status
    }
    
    log_success "Deployments are ready"
}

# Main execution
main() {
    log_info "Atlas OnPrem K8s Deployment"
    log_info "Environment: $ENVIRONMENT, Action: $ACTION"
    
    validate_environment
    check_prerequisites
    
    case $ACTION in
        apply)
            ensure_namespace
            deploy_infrastructure
            sleep 30  # Wait for infrastructure to be ready
            deploy_observability
            sleep 15  # Wait for observability to be ready
            deploy_microservices
            deploy_environment_overlay
            wait_for_deployments
            log_success "Deployment completed successfully!"
            ;;
        delete)
            delete_resources
            ;;
        restart)
            delete_resources
            sleep 10
            main "$1" "apply"
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$@"
            ;;
        *)
            log_error "Invalid action: $ACTION"
            log_error "Valid actions: apply, delete, restart, status, logs"
            exit 1
            ;;
    esac
}

# Run main function
main "$@" 