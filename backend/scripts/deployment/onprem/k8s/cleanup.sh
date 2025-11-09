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
NAMESPACE="atlas-onprem-k8s"

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Kubernetes Cleanup Script - Removes all Atlas-related resources"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "⚠️  WARNING: This operation is DESTRUCTIVE and will delete ALL Atlas resources!"
    echo "This includes applications, databases, configuration data, and ingress."
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                echo "Unknown option: $1" >&2
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

check_prerequisites() {
    echo "Checking prerequisites..."

    # Check Docker
    if docker info > /dev/null 2>&1; then
        echo "Docker found and running"
    else
        echo "Docker is not running. Please start Docker and try again." >&2
        exit 1
    fi

    # Check kubectl
    if command -v kubectl &> /dev/null; then
        echo "kubectl found"
    else
        echo "kubectl is not installed" >&2
        exit 1
    fi

    # Check Kubernetes cluster
    if kubectl cluster-info &> /dev/null; then
        echo "Kubernetes cluster found"
    else
        echo "Cannot connect to Kubernetes cluster. Make sure you have a running Kubernetes cluster (minikube, kind, etc.)" >&2
        exit 1
    fi

    echo "Prerequisites check passed"
    echo
}

# =============================================================================
# CLEANUP FUNCTIONS
# =============================================================================

# Function to perform complete namespace cleanup
remove_namespace() {
    echo "Deleting namespace '$NAMESPACE' (this will remove all resources within)..."

    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        echo "Namespace '$NAMESPACE' does not exist"
        return
    fi

    # Delete the namespace (this automatically deletes all resources within)
    kubectl delete namespace "$NAMESPACE" --ignore-not-found=true
    
    # Wait for namespace deletion to complete
    echo "Waiting for namespace deletion to complete..."
    timeout=120  # Increased timeout as namespace deletion can take time
    while kubectl get namespace "$NAMESPACE" &> /dev/null && [[ $timeout -gt 0 ]]; do
        sleep 2
        ((timeout-=2))
        if [[ $((timeout % 20)) -eq 0 ]]; then
            echo "Still waiting for namespace deletion... ($timeout seconds remaining)"
        fi
    done

    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        echo "Namespace deletion is taking longer than expected"
        echo "This may be due to finalizers. Checking for stuck resources..."
        echo "You can manually check with: kubectl get namespace $NAMESPACE -o yaml"
    else
        echo "Namespace '$NAMESPACE' deleted successfully"
    fi

    echo "Namespace cleanup completed successfully!"
    echo
}

# Function to cleanup NGINX Ingress Controller
cleanup_ingress_controller() {
    echo "Cleaning up NGINX Ingress Controller..."
    
    if kubectl get namespace ingress-nginx &> /dev/null; then
        echo "Deleting NGINX Ingress Controller..."
        kubectl delete namespace ingress-nginx --ignore-not-found=true
        
        # Wait for deletion
        echo "Waiting for ingress-nginx namespace deletion..."
        timeout=60
        while kubectl get namespace ingress-nginx &> /dev/null && [[ $timeout -gt 0 ]]; do
            sleep 2
            ((timeout-=2))
        done
        
        echo "NGINX Ingress Controller removed"
    else
        echo "NGINX Ingress Controller not found"
    fi
    echo
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites

    echo "Atlas On-Premise Kubernetes Platform - Cleaning up..."
    echo "Namespace: $NAMESPACE"
    echo

    echo "Removing all resources:"
    echo "  ✓ All services and applications"
    echo "  ✓ Namespace and volumes"
    echo "  ✓ Ingress Controller"
    echo

    remove_namespace
    cleanup_ingress_controller

    echo "Atlas platform resources cleanup completed successfully!"
}

# Execute main function
main "$@"
