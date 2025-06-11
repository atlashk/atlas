#!/bin/bash

# Atlas Cleanup Script
# Removes all Atlas platform components from the Kubernetes cluster

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_ROOT="$(dirname "$SCRIPT_DIR")"

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

header() {
    echo -e "${BLUE}[CLEANUP]${NC} $1"
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

# Function to delete resources by label
delete_by_label() {
    local label=$1
    local resource_type=${2:-"all"}
    
    log "Deleting $resource_type resources with label $label..."
    
    if kubectl get "$resource_type" -l "$label" &> /dev/null; then
        kubectl delete "$resource_type" -l "$label" --ignore-not-found=true
        log "Deleted $resource_type resources with label $label"
    else
        warn "No $resource_type resources found with label $label"
    fi
}

# Function to delete PVCs
delete_pvcs() {
    log "Deleting Atlas PVCs..."
    
    local pvcs=(
        "mysql-data"
        "redis-data"
        "kafka-data"
        "user-service-logs"
        "product-service-logs"
        "order-service-logs"
        "notification-service-logs"
        "discovery-server-logs"
    )
    
    for pvc in "${pvcs[@]}"; do
        if kubectl get pvc "$pvc" &> /dev/null; then
            kubectl delete pvc "$pvc" --ignore-not-found=true
            log "Deleted PVC: $pvc"
        fi
    done
}

# Function to delete secrets
delete_secrets() {
    log "Deleting Atlas secrets..."
    
    local secrets=(
        "mysql-secret"
    )
    
    for secret in "${secrets[@]}"; do
        if kubectl get secret "$secret" &> /dev/null; then
            kubectl delete secret "$secret" --ignore-not-found=true
            log "Deleted secret: $secret"
        fi
    done
}

# Function to delete configmaps
delete_configmaps() {
    log "Deleting Atlas ConfigMaps..."
    
    local configmaps=(
        "mysql-init"
        "redis-config"
        "atlas-config"
        "local-config"
    )
    
    for cm in "${configmaps[@]}"; do
        if kubectl get configmap "$cm" &> /dev/null; then
            kubectl delete configmap "$cm" --ignore-not-found=true
            log "Deleted ConfigMap: $cm"
        fi
    done
}

# Function to force delete stuck resources
force_delete() {
    local resource_type=$1
    local resource_name=$2
    
    warn "Force deleting stuck $resource_type: $resource_name"
    kubectl patch "$resource_type" "$resource_name" -p '{"metadata":{"finalizers":[]}}' --type=merge || true
    kubectl delete "$resource_type" "$resource_name" --force --grace-period=0 || true
}

# Function to wait for resource deletion
wait_for_deletion() {
    local resource_type=$1
    local label=$2
    local timeout=${3:-60}
    
    log "Waiting for $resource_type deletion (timeout: ${timeout}s)..."
    
    local count=0
    while [[ $count -lt $timeout ]]; do
        if ! kubectl get "$resource_type" -l "$label" &> /dev/null; then
            log "$resource_type deletion completed"
            return 0
        fi
        
        sleep 2
        ((count+=2))
    done
    
    warn "$resource_type deletion timed out after ${timeout}s"
    return 1
}

# Main cleanup function
main() {
    header "Starting Atlas platform cleanup..."
    
    # Parse command line arguments
    local force_delete=false
    local delete_pvs=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --force)
                force_delete=true
                shift
                ;;
            --delete-pvs)
                delete_pvs=true
                shift
                ;;
            -h|--help)
                echo "Usage: $0 [OPTIONS]"
                echo
                echo "Options:"
                echo "  --force       Force delete stuck resources"
                echo "  --delete-pvs  Also delete persistent volumes"
                echo "  -h, --help    Show this help message"
                echo
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    check_prerequisites
    
    # Confirm deletion
    if [[ "${CI:-false}" != "true" ]]; then
        echo -e "${YELLOW}This will delete all Atlas platform components from the cluster.${NC}"
        read -p "Are you sure you want to continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log "Cleanup cancelled by user"
            exit 0
        fi
    fi
    
    # Delete resources in reverse dependency order
    header "Deleting Atlas deployments and services..."
    
    # Delete services first to stop traffic
    delete_by_label "project=atlas" "service"
    
    # Delete deployments
    delete_by_label "project=atlas" "deployment"
    wait_for_deletion "deployment" "project=atlas" 120
    
    # Delete pods (if any are stuck)
    delete_by_label "project=atlas" "pod"
    
    # Delete PVCs
    delete_pvcs
    
    # Delete secrets and configmaps
    delete_secrets
    delete_configmaps
    
    # Delete persistent volumes if requested
    if [[ "$delete_pvs" == "true" ]]; then
        warn "Deleting persistent volumes..."
        delete_by_label "project=atlas" "pv"
    fi
    
    # Force delete if requested and there are stuck resources
    if [[ "$force_delete" == "true" ]]; then
        header "Checking for stuck resources..."
        
        # Check for stuck deployments
        local stuck_deployments
        stuck_deployments=$(kubectl get deployments -l "project=atlas" -o name 2>/dev/null || true)
        
        if [[ -n "$stuck_deployments" ]]; then
            warn "Found stuck deployments, force deleting..."
            for deployment in $stuck_deployments; do
                force_delete "deployment" "$(basename "$deployment")"
            done
        fi
    fi
    
    # Final verification
    log "Verifying cleanup completion..."
    
    local remaining_resources
    remaining_resources=$(kubectl get all -l "project=atlas" --ignore-not-found=true 2>/dev/null || true)
    
    if [[ -z "$remaining_resources" ]]; then
        log "Atlas platform cleanup completed successfully!"
    else
        warn "Some resources may still be present:"
        echo "$remaining_resources"
        warn "You may need to run with --force option"
    fi
    
    log "Cleanup completed"
}

# Handle script interruption
trap 'error "Cleanup interrupted"; exit 1' INT TERM

# Run main function
main "$@" 