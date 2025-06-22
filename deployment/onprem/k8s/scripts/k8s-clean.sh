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
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"

# Load logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Default environment
ENVIRONMENT="${1:-local}"

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Function to perform complete cleanup
complete_cleanup() {
    local namespace="atlas-${ENVIRONMENT}"
    log_info "Performing complete cleanup..."

    # Force delete any remaining pods
    log_info "Force deleting any remaining pods..."
    kubectl get pods -n "$namespace" --no-headers 2>/dev/null | awk '{print $1}' | while read -r pod; do
        if [[ -n "$pod" ]]; then
            kubectl delete pod "$pod" -n "$namespace" --force --grace-period=0 2>/dev/null || true
        fi
    done

    # Delete persistent volume claims (this will delete all data!)
    log_info "Deleting persistent volume claims..."
    kubectl delete pvc --all -n "$namespace" 2>/dev/null || true

    # Delete all remaining resources in namespace
    log_info "Deleting all remaining resources..."
    kubectl delete all --all -n "$namespace" 2>/dev/null || true

    # Delete ingress resources
    log_info "Deleting ingress resources..."
    kubectl delete ingress --all -n "$namespace" 2>/dev/null || true

    # Delete secrets and configmaps
    log_info "Deleting secrets and configmaps..."
    kubectl delete secrets --all -n "$namespace" 2>/dev/null || true
    kubectl delete configmaps --all -n "$namespace" 2>/dev/null || true

    # Delete the namespace itself
    log_info "Deleting namespace..."
    kubectl delete namespace "$namespace" 2>/dev/null || true
    
    # Wait for namespace deletion
    log_info "Waiting for namespace deletion to complete..."
    timeout=60
    while kubectl get namespace "$namespace" &> /dev/null && [[ $timeout -gt 0 ]]; do
        sleep 2
        ((timeout-=2))
    done

    if kubectl get namespace "$namespace" &> /dev/null; then
        log_warn "Namespace deletion is taking longer than expected"
        log_info "You may need to manually check for finalizers: kubectl get namespace $namespace -o yaml"
    fi

    # Clean up any orphaned persistent volumes (usually not needed in local environments)
    log_info "Checking for orphaned persistent volumes..."
    orphaned_pvs=$(kubectl get pv -o json 2>/dev/null | jq -r ".items[]? | select(.spec.claimRef.namespace == \"$namespace\") | .metadata.name" 2>/dev/null || true)

    if [[ -n "$orphaned_pvs" ]]; then
        log_info "Cleaning up orphaned persistent volumes..."
        echo "$orphaned_pvs" | while read -r pv; do
            if [[ -n "$pv" ]]; then
                kubectl delete pv "$pv" 2>/dev/null || true
                log_info "  Deleted PV: $pv"
            fi
        done
    else
        log_info "No orphaned persistent volumes found"
    fi
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
        local atlas_hosts=("atlas.local" "api.atlas.local" "grafana.atlas.local" "prometheus.atlas.local" "zipkin.atlas.local" "mail.atlas.local")
        
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

# Main function
main() {
    log_section "Atlas OnPrem K8s Platform - Complete Cleanup"
    log_info "Environment: $ENVIRONMENT"

    check_prerequisites

    complete_cleanup
    cleanup_ingress_controller
    cleanup_hosts_file

    log_success "Atlas platform cleaned up successfully!"
}

# Show usage if help is requested
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    log_info "Usage: $0 [environment]"
    log_info ""
    log_info "Environments:"
    log_info "  local (default) - Local development environment"
    log_info "  dev             - Development environment"
    log_info "  stg             - Staging environment"
    log_info "  prod            - Production environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                              # Clean local environment"
    log_info "  $0 dev                          # Clean dev environment"
    log_info ""
    log_info "⚠️  WARNING: This operation is DESTRUCTIVE and will delete ALL data!"
    log_info "This includes applications, databases, NGINX Ingress Controller, and /etc/hosts entries."
    log_info ""
    exit 0
fi

# Run main function
main "$@"
