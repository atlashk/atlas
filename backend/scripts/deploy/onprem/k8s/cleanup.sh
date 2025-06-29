#!/bin/bash

# =============================================================================
# Atlas OnPrem K8s Clean Script
# =============================================================================
# This script completely cleans up the Atlas microservices platform on Kubernetes
# including volumes, secrets, and namespace
# =============================================================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../../.." && pwd)"

# Load logger
source "$PROJECT_ROOT/backend/scripts/log/logger.sh"

# Default environment
ENVIRONMENT="local"

# =============================================================================
# CONFIGURATION - Centralized resource definitions
# =============================================================================

# Atlas application services
declare -ra ATLAS_APPLICATIONS=(
    "user-service"
    "product-service"
    "order-service"
    "notification-service"
    "auth-server"
    "api-gateway"
)

# Infrastructure services
declare -ra INFRASTRUCTURE_SERVICES=(
    "mysql"
    "redis"
    "kafka"
    "smtp4dev"
)

# Observability services
declare -ra OBSERVABILITY_SERVICES=(
    "zipkin"
    "loki"
    "promtail"
    "prometheus"
    "grafana"
)

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Kubernetes Cleanup Script - Removes all Atlas-related resources"
    log_info ""
    log_info "Options:"
    log_info "  --env ENVIRONMENT       Target environment (default: local)"
    log_info "  -h, --help              Show this help message"
    log_info ""
    log_info "Environments:"
    log_info "  local (default)         Local environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                      # Clean all resources in local env"
    log_info "  $0 --env local          # Clean all resources in local env"
    log_info ""
    log_warn "⚠️  WARNING: This operation is DESTRUCTIVE and will delete ALL Atlas resources!"
    log_warn "This includes applications, databases, configuration data, ingress, and host entries."
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
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
}

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

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
# CLEANUP FUNCTIONS
# =============================================================================

# Function to perform complete namespace cleanup
remove_namespace() {
    local namespace="atlas-${ENVIRONMENT}"
    log_info "Deleting namespace '$namespace' (this will remove all resources within)..."

    if ! kubectl get namespace "$namespace" &> /dev/null; then
        log_info "Namespace '$namespace' does not exist"
        return
    fi

    # Delete the namespace (this automatically deletes all resources within)
    kubectl delete namespace "$namespace" --ignore-not-found=true
    
    # Wait for namespace deletion to complete
    log_info "Waiting for namespace deletion to complete..."
    timeout=120  # Increased timeout as namespace deletion can take time
    while kubectl get namespace "$namespace" &> /dev/null && [[ $timeout -gt 0 ]]; do
        sleep 2
        ((timeout-=2))
        if [[ $((timeout % 20)) -eq 0 ]]; then
            log_info "Still waiting for namespace deletion... ($timeout seconds remaining)"
        fi
    done

    if kubectl get namespace "$namespace" &> /dev/null; then
        log_warn "Namespace deletion is taking longer than expected"
        log_info "This may be due to finalizers. Checking for stuck resources..."
        log_info "You can manually check with: kubectl get namespace $namespace -o yaml"
    else
        log_success "Namespace '$namespace' deleted successfully"
    fi

    log_success "Namespace cleanup completed successfully!"
}

# Function to cleanup NGINX Ingress Controller
cleanup_ingress_controller() {
    log_info "Cleaning up NGINX Ingress Controller..."
    
    if kubectl get namespace ingress-nginx &> /dev/null; then
        log_info "Deleting NGINX Ingress Controller..."
        kubectl delete namespace ingress-nginx --ignore-not-found=true
        
        # Wait for deletion
        log_info "Waiting for ingress-nginx namespace deletion..."
        timeout=60
        while kubectl get namespace ingress-nginx &> /dev/null && [[ $timeout -gt 0 ]]; do
            sleep 2
            ((timeout-=2))
        done
        
        log_success "NGINX Ingress Controller removed"
    else
        log_info "NGINX Ingress Controller not found"
    fi
}

# Function to cleanup /etc/hosts entries
cleanup_hosts_file() {
    log_info "Cleaning up /etc/hosts entries..."
    
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Removing Atlas entries from /etc/hosts..."

        # Atlas local hostnames
        local atlas_hosts=("api.atlas.local" "grafana.atlas.local" "prometheus.atlas.local" "zipkin.atlas.local" "mail.atlas.local")
        
        if sudo -n true 2>/dev/null; then
            # Remove Atlas entries
            for host in "${atlas_hosts[@]}"; do
                sudo sed -i.bak "/$host/d" /etc/hosts 2>/dev/null || true
            done
            sudo sed -i.bak "/# Atlas Kubernetes Ingress/d" /etc/hosts 2>/dev/null || true
            sudo sed -i.bak "/# End Atlas entries/d" /etc/hosts 2>/dev/null || true
            
            log_success "/etc/hosts entries cleaned up"
        else
            log_warn "Cannot modify /etc/hosts without sudo access"
            log_info "Please remove these entries from /etc/hosts manually:"
            for host in "${atlas_hosts[@]}"; do
                log_info "  Remove lines containing: $host"
            done
        fi
    else
        log_info "No Atlas entries found in /etc/hosts"
    fi
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    local namespace="atlas-${ENVIRONMENT}"

    log_section "Atlas OnPrem K8s Platform - Cleanup"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $namespace"

    log_section "Removing all Atlas resources"
    log_info "  ✓ All services and applications"
    log_info "  ✓ Namespace and volumes"
    log_info "  ✓ Ingress Controller"
    log_info "  ✓ /etc/hosts entries"

    remove_namespace
    cleanup_ingress_controller
    cleanup_hosts_file
    
    log_success "All Atlas resources removed successfully!"
    log_success "Atlas platform cleanup completed!"
}

# Execute main function
main "$@"
