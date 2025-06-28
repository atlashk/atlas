#!/bin/bash

# =============================================================================
# Atlas OnPrem K8s Stop Script
# =============================================================================
# This script stops the Atlas microservices platform on Kubernetes
# =============================================================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../../.." && pwd)"
BASE_DIR="${SCRIPT_DIR}/base"

# Load logger
source "$PROJECT_ROOT/backend/scripts/log/logger.sh"

# Default environment
ENVIRONMENT="local"

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Kubernetes Stop Script - Stops Atlas services"
    log_info ""
    log_info "This script STOPS Atlas services by scaling deployments to 0 replicas."
    log_info "Resources (ConfigMaps, Services, PVCs) are preserved for easy restart."
    log_info ""
    log_info "Options:"
    log_info "  --env ENVIRONMENT       Target environment (default: local)"
    log_info "  -h, --help              Show this help message"
    log_info ""
    log_info "Environments:"
    log_info "  local (default)         Local environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                      # Stop local environment"
    log_info "  $0 --env local          # Stop local environment"
    log_info ""
    log_info "Note: To completely remove resources, use ./k8s-clean.sh instead"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --env)
            if [[ -n "${2:-}" && ! "$2" =~ ^-- ]]; then
                ENVIRONMENT="$2"
                shift 2
            else
                log_error "--env requires an environment value"
                exit 1
            fi
            ;;
        *)
            log_error "Unknown option: $1"
            log_info "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Set namespace based on environment
NAMESPACE="atlas-${ENVIRONMENT}"

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

# Function to check prerequisites
check_prerequisites() {
    log_section "Checking Prerequisites"

    # Check Docker
    if docker info > /dev/null 2>&1; then
        log_success "Docker found and running"
    else
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi

    # Check kubectl
    if command -v kubectl &> /dev/null; then
        log_success "kubectl found"
    else
        log_error "kubectl is not installed"
        exit 1
    fi

    # Check Kubernetes cluster
    if kubectl cluster-info &> /dev/null; then
        log_success "Kubernetes cluster found"
    else
        log_error "Cannot connect to Kubernetes cluster. Make sure you have a running Kubernetes cluster (minikube, kind, etc.)"
        exit 1
    fi

    log_success "Prerequisites check passed"
}

# =============================================================================
# STOP FUNCTIONS
# =============================================================================

# Function to gracefully stop services
graceful_stop() {
    log_info "Gracefully stopping Atlas platform..."
    
    # Scale down deployments to 0 for graceful shutdown
    stop_applications
    stop_observability
    stop_infrastructure
    
    # Wait for pods to terminate
    log_info "Waiting for pods to terminate..."
    kubectl wait --for=delete pods --all -n "$NAMESPACE" --timeout=120s 2>/dev/null || {
        log_warn "Some pods may still be terminating..."
    }
    
    log_success "All services stopped (scaled to 0 replicas)"
}

# Function to stop observability services
stop_observability() {
    log_section "Stopping Observability Services"

    local services=("grafana" "prometheus" "loki" "zipkin")

    for service in "${services[@]}"; do
        log_info "Stopping $service..."
        kubectl scale deployment "$service" --replicas=0 -n "$NAMESPACE" 2>/dev/null || {
            log_warn "Failed to stop $service (may not be running)"
        }
    done
    
    # Stop DaemonSet by deleting it (DaemonSets can't be scaled to 0)
    log_info "Stopping promtail (DaemonSet)..."
    kubectl delete daemonset promtail -n "$NAMESPACE" --ignore-not-found=true

    log_success "Observability services stopped"
}

# Function to stop application services
stop_applications() {
    log_section "Stopping Application Services"
    
    # Stop in reverse dependency order
    local services=("user-service" "order-service" "product-service" "notification-service" "auth-server" "api-gateway")
    
    for service in "${services[@]}"; do
        log_info "Stopping $service..."
        kubectl scale deployment "$service" --replicas=0 -n "$NAMESPACE" 2>/dev/null || {
            log_warn "Failed to stop $service (may not be running)"
        }
    done

    log_success "Application services stopped"
}

# Function to stop infrastructure services
stop_infrastructure() {
    log_section "Stopping Infrastructure Services"
    
    # Infrastructure services that are StatefulSets
    local statefulsets=(
        "mysql"
        "redis"
        "kafka"
        # "rabbitmq"
        "smtp4dev"
    )

    for service in "${statefulsets[@]}"; do
        log_info "Stopping $service (StatefulSet)..."
        kubectl scale statefulset "$service" --replicas=0 -n "$NAMESPACE" 2>/dev/null || {
            log_warn "Failed to stop $service (may not be running)"
        }
    done

    log_success "Infrastructure services stopped"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    log_section "Atlas OnPrem K8s Platform - Stopping"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $NAMESPACE"

    check_prerequisites

    graceful_stop
    log_success "Atlas platform stopped successfully!"
}

# Run main function
main
