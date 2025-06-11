#!/bin/bash

# Atlas Complete Deployment Script
# Orchestrates the deployment of the entire Atlas platform

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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
    echo -e "${PURPLE}[ATLAS]${NC} $1"
}

# Function to print banner
print_banner() {
    echo
    echo -e "${PURPLE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${PURPLE}║                        ATLAS PLATFORM                       ║${NC}"
    echo -e "${PURPLE}║                   Kubernetes Deployment                     ║${NC}"
    echo -e "${PURPLE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo
}

# Function to check prerequisites
check_prerequisites() {
    header "Checking prerequisites..."
    
    local required_tools=("kubectl" "docker" "minikube")
    local missing_tools=()
    
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        fi
    done
    
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        error "Missing required tools: ${missing_tools[*]}"
        echo
        echo "Please install the missing tools:"
        echo "  - kubectl: https://kubernetes.io/docs/tasks/tools/"
        echo "  - docker: https://docs.docker.com/get-docker/"
        echo "  - minikube: https://minikube.sigs.k8s.io/docs/start/"
        exit 1
    fi
    
    # Check if minikube is running
    if ! minikube status &> /dev/null; then
        warn "Minikube is not running. Starting minikube..."
        minikube start --cpus=4 --memory=8g
    fi
    
    log "Prerequisites check passed"
}

# Function to check and load images
check_and_load_images() {
    header "Checking and loading application images..."
    
    local images=("user-service" "product-service" "order-service" "notification-service" "auth-server" "api-gateway")
    
    if [[ "${SKIP_BUILD:-false}" == "true" ]]; then
        log "Skipping image check (SKIP_BUILD=true)"
        return 0
    fi
    
    # Check if images exist
    local missing_images=()
    for image in "${images[@]}"; do
        if ! docker images "$image" | grep -q "$image"; then
            missing_images+=("$image")
        fi
    done
    
    if [[ ${#missing_images[@]} -gt 0 ]]; then
        warn "Missing images: ${missing_images[*]}"
        echo
        echo "Please build the images first using the existing build scripts:"
        echo "  1. Build JARs:    ./script/build-jar.sh"
        echo "  2. Build images:  ./script/build-docker-images.sh backend"
        echo
        echo "Or skip this check with:"
        echo "  SKIP_BUILD=true $0"
        exit 1
    fi
    
    # Load images into minikube
    for image in "${images[@]}"; do
        log "Loading $image into minikube..."
        minikube image load "$image"
    done
    
    log "Images loaded successfully"
}

# Function to deploy infrastructure
deploy_infrastructure() {
    header "Deploying infrastructure services..."
    
    if [[ -f "$SCRIPT_DIR/deploy-infrastructure.sh" ]]; then
        bash "$SCRIPT_DIR/deploy-infrastructure.sh"
    else
        error "Infrastructure deployment script not found"
        exit 1
    fi
}

# Function to deploy services
deploy_services() {
    header "Deploying application services..."
    
    if [[ -f "$SCRIPT_DIR/deploy-services.sh" ]]; then
        bash "$SCRIPT_DIR/deploy-services.sh"
    else
        error "Services deployment script not found"
        exit 1
    fi
}

# Function to deploy observability
deploy_observability() {
    header "Deploying observability stack..."
    
    if [[ -f "$SCRIPT_DIR/deploy-observability.sh" ]]; then
        bash "$SCRIPT_DIR/deploy-observability.sh"
    else
        warn "Observability deployment script not found, skipping..."
    fi
}

# Function to print deployment summary
print_summary() {
    header "Deployment Summary"
    echo
    log "Atlas platform deployed successfully!"
    echo
    echo -e "${BLUE}Application Endpoints:${NC}"
    echo "  🌐 API Gateway:      http://localhost:8080"
    echo "  🔐 Auth Server:      http://localhost:8091"
    echo "  👥 User Service:     http://localhost:8081"
    echo "  📦 Product Service:  http://localhost:8082"
    echo "  🛒 Order Service:    http://localhost:8083"
    echo "  📧 Notification:     http://localhost:8084"
    echo
    echo -e "${BLUE}Monitoring & Observability:${NC}"
    echo "  📈 Grafana:         http://localhost:3000 (admin/admin)"
    echo "  🔍 Prometheus:      http://localhost:9090"
    echo "  🔬 Zipkin:          http://localhost:9411"
    echo
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "  kubectl get all                    # View all resources"
    echo "  kubectl get pods --watch           # Watch pod status"
    echo "  kubectl logs -f deployment/user-service  # View service logs"
    echo "  minikube dashboard                 # Open K8s dashboard"
    echo
    echo -e "${BLUE}To access services from outside the cluster:${NC}"
    echo "  kubectl port-forward service/api-gateway 8080:8080"
    echo "  kubectl port-forward service/grafana 3000:3000"
    echo
    echo -e "${BLUE}To rebuild images:${NC}"
    echo "  ./script/build-jar.sh                 # Build JAR files"
    echo "  ./script/build-docker-images.sh backend  # Build Docker images"
    echo
}

# Main deployment function
main() {
    print_banner
    
    local start_time=$(date +%s)
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-build)
                export SKIP_BUILD=true
                shift
                ;;
            --skip-observability)
                export SKIP_OBSERVABILITY=true
                shift
                ;;
            --skip-health-check)
                export SKIP_HEALTH_CHECK=true
                shift
                ;;
            -h|--help)
                echo "Usage: $0 [OPTIONS]"
                echo
                echo "Options:"
                echo "  --skip-build           Skip Docker image checking and loading"
                echo "  --skip-observability   Skip observability deployment"
                echo "  --skip-health-check    Skip health checks"
                echo "  -h, --help             Show this help message"
                echo
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Deployment steps
    check_prerequisites
    check_and_load_images
    deploy_infrastructure
    deploy_services
    
    if [[ "${SKIP_OBSERVABILITY:-false}" != "true" ]]; then
        deploy_observability
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo
    log "Total deployment time: ${duration} seconds"
    
    print_summary
}

# Handle script interruption
trap 'error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@" 