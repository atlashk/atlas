#!/bin/bash

# =============================================================================
# Atlas OnPrem K8s Start Script
# =============================================================================
# This script starts the Atlas microservices platform on Kubernetes
# =============================================================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
BASE_DIR="${SCRIPT_DIR}/../base"

# Load logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

# Default options
ENVIRONMENT="local"
SKIP_BUILD=false

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Kubernetes Start Script - Starts the Atlas microservices platform"
    log_info ""
    log_info "This script automatically sets up:"
    log_info "  - NGINX Ingress Controller (if not present)"
    log_info "  - Local hostnames in /etc/hosts (atlas.local, api.atlas.local, etc.)"
    log_info "  - All Atlas services with Ingress routing"
    log_info ""
    log_info "Options:"
    log_info "  --env ENVIRONMENT   Target environment (default: local)"
    log_info "  --skip-build        Skip all build steps (backend JAR, frontend, Docker images)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Environments:"
    log_info "  local (default)     Local development environment"
    log_info "  dev                 Development environment"
    log_info "  stg                 Staging environment"
    log_info "  prod                Production environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                          # Start with local environment"
    log_info "  $0 --env dev                # Start with dev environment"
    log_info "  $0 --skip-build             # Start local env, skip builds"
    log_info "  $0 --env dev --skip-build   # Start dev env, skip builds"
    log_info ""
    log_info "Note: You may be prompted for sudo password to modify /etc/hosts"
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
        --skip-build)
            SKIP_BUILD=true
            shift
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
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        log_info "Make sure you have a running Kubernetes cluster (minikube, kind, etc.)"
        exit 1
    fi
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_prerequisites
        check_node_prerequisites
        check_docker_prerequisites
    fi
    
    log_success "Prerequisites check passed"
}

# Check Java prerequisites
check_java_prerequisites() {
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 17 or later."
        exit 1
    else
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        # Extract major version number
        major_version=$(echo $java_version | cut -d'.' -f1)
        # Handle both old (1.8) and new (17) version formats
        if [[ $major_version == "1" ]]; then
            major_version=$(echo $java_version | cut -d'.' -f2)
        fi
        
        if [ "$major_version" -lt 17 ]; then
            log_error "Java version $java_version is not supported. Please install Java 17 or later."
            exit 1
        fi
        log_success "Java found: $java_version"
    fi
}

# Check Node.js prerequisites
check_node_prerequisites() {
    if ! command -v node &> /dev/null; then
        log_error "Node.js is not installed. Please install Node.js 22 or later."
        exit 1
    else
        node_version=$(node --version | cut -d'v' -f2)  # Remove 'v' prefix
        major_version=$(echo $node_version | cut -d'.' -f1)
        
        if [ "$major_version" -lt 22 ]; then
            log_error "Node.js version $node_version is not supported. Please install Node.js 22 or later."
            exit 1
        fi
        log_success "Node.js found: v$node_version"
    fi
}

# Check Docker prerequisites
check_docker_prerequisites() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    log_success "Docker found and running"
}

# Function to display cluster info
show_cluster_info() {
    log_section "Cluster Information"
    kubectl cluster-info
    log_info ""
    kubectl get nodes -o wide
    log_info ""
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

# Build backend using existing script
build_backend() {
    log_section "Building backend JAR files..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-backend.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Backend build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking backend build script..."
    if "$build_script"; then
        log_success "Backend build completed successfully."
    else
        log_error "Backend build failed."
        exit 1
    fi
}

# Build frontend using existing script
build_frontend() {
    log_section "Building frontend..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-frontend.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Frontend build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking frontend build script..."
    if "$build_script"; then
        log_success "Frontend build completed successfully."
    else
        log_error "Frontend build failed."
        exit 1
    fi
}

# Build Docker images using existing script
build_docker_images() {
    log_section "Building Docker images..."
    
    local build_script="$PROJECT_ROOT/deployment/build/build-docker-images.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Docker images build script not found: $build_script"
        exit 1
    fi
    
    log_info "Invoking Docker images build script..."
    if "$build_script" all; then
        log_success "Docker images build completed successfully."
    else
        log_error "Docker images build failed."
        exit 1
    fi
}

# =============================================================================
# DEPLOYMENT FUNCTIONS
# =============================================================================

# Function to create namespace
create_namespace() {
    log_info "Creating namespace: $NAMESPACE"
    
    # Create namespace with standard Kubernetes labels
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | \
    kubectl label --local -f - \
        app.kubernetes.io/name=atlas \
        app.kubernetes.io/instance="atlas-${ENVIRONMENT}" \
        app.kubernetes.io/version="1.0.0" \
        app.kubernetes.io/managed-by=kubectl -o yaml | \
    kubectl annotate --local -f - \
        atlas.org/version="1.0.0" \
        atlas.org/deployment-method="kubectl" -o yaml | \
    kubectl apply -f -
    
    log_success "Namespace $NAMESPACE created"
}

# Function to deploy infrastructure services
deploy_infrastructure() {
    log_section "Deploying Infrastructure Services"
    
    local services=("mysql" "redis" "kafka" "smtp4dev")
    
    for service in "${services[@]}"; do
        log_info "Deploying $service..."
        kubectl apply -f "$BASE_DIR/infrastructure/$service.yaml" -n "$NAMESPACE"
        
        # Wait for deployment to be ready
        if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
            log_info "Waiting for $service to be ready..."
            # Increase timeout for infrastructure services, especially Kafka and Redis
            local timeout="120s"
            if [[ "$service" == "kafka" || "$service" == "redis" ]]; then
                timeout="300s"
            fi
            kubectl wait --for=condition=available --timeout="$timeout" deployment/"$service" -n "$NAMESPACE" || {
                log_warn "$service deployment timeout, continuing..."
            }
        fi
    done
    
    log_success "Infrastructure services deployed"
}

# Function to deploy application services
deploy_applications() {
    log_section "Deploying Application Services"
    
    # Deploy in dependency order
    local services=("auth-server" "api-gateway" "user-service" "product-service" "order-service" "notification-service" "frontend")
    
    for service in "${services[@]}"; do
        log_info "Deploying $service..."
        
        # Create temporary file with environment substitution
        temp_file=$(mktemp)
        ENVIRONMENT="$ENVIRONMENT" envsubst < "$BASE_DIR/application/$service.yaml" > "$temp_file"
        kubectl apply -f "$temp_file" -n "$NAMESPACE"
        rm "$temp_file"
        
        # Wait for deployment to be ready
        log_info "Waiting for $service to be ready..."
        kubectl wait --for=condition=available --timeout=120s deployment/"$service" -n "$NAMESPACE" || {
            log_warn "$service deployment timeout, continuing..."
        }
    done
    
    log_success "Application services deployed"
}

# Function to deploy observability services
deploy_observability() {
    log_section "Deploying Observability Services"
    
    local services=("zipkin" "loki" "promtail" "prometheus" "grafana")
    
    for service in "${services[@]}"; do
        log_info "Deploying $service..."
        kubectl apply -f "$BASE_DIR/observability/$service.yaml" -n "$NAMESPACE"
        
        # Wait for deployment to be ready (skip promtail as it's a DaemonSet)
        if [[ "$service" != "promtail" ]]; then
            log_info "Waiting for $service to be ready..."
            kubectl wait --for=condition=available --timeout=120s deployment/"$service" -n "$NAMESPACE" || {
                log_warn "$service deployment timeout, continuing..."
            }
        fi
    done
    
    log_success "Observability services deployed"
}

# Function to setup and deploy ingress
setup_and_deploy_ingress() {
    log_section "Setting up Ingress"
    
    # Check if NGINX Ingress Controller is installed
    if ! kubectl get namespace ingress-nginx &>/dev/null; then
        log_info "NGINX Ingress Controller not found - installing automatically..."
        
        # Detect Kubernetes platform
        local platform="unknown"
        if kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | grep -q "minikube"; then
            platform="minikube"
        elif kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | grep -q "kind"; then
            platform="kind"
        elif kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | grep -q "docker-desktop"; then
            platform="docker-desktop"
        elif kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | grep -q "k3s"; then
            platform="k3s"
        fi
        
        log_info "Detected platform: $platform"
        
        # Install NGINX Ingress Controller
        case $platform in
            "minikube")
                log_info "Installing NGINX Ingress for Minikube..."
                if command -v minikube &> /dev/null; then
                    minikube addons enable ingress
                else
                    log_warn "minikube command not found, using generic installation"
                    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/baremetal/deploy.yaml
                fi
                ;;
            "kind")
                log_info "Installing NGINX Ingress for kind..."
                kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/kind/deploy.yaml
                ;;
            "docker-desktop")
                log_info "Installing NGINX Ingress for Docker Desktop..."
                kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml
                ;;
            *)
                log_info "Installing generic NGINX Ingress..."
                kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/baremetal/deploy.yaml
                ;;
        esac
        
        log_info "Waiting for NGINX Ingress Controller to be ready..."
        kubectl wait --namespace ingress-nginx \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/component=controller \
            --timeout=120s || {
            log_warn "NGINX Ingress Controller setup timeout, continuing..."
        }
        
        log_success "NGINX Ingress Controller installed"
    else
        log_info "NGINX Ingress Controller already installed"
    fi
    
    # Setup hostnames if not already configured
    if ! grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Setting up local hostnames..."
        
        # Get ingress IP based on detected platform
        local ingress_ip="127.0.0.1"
        local detected_platform="unknown"
        if kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | grep -q "minikube"; then
            detected_platform="minikube"
            if command -v minikube &> /dev/null; then
                ingress_ip=$(minikube ip 2>/dev/null || echo "127.0.0.1")
            fi
        else
            ingress_ip="127.0.0.1"
        fi
        
        log_info "Using Ingress IP: $ingress_ip"
        
        # Atlas local hostnames
        local atlas_hosts=("atlas.local" "api.atlas.local" "grafana.atlas.local" "prometheus.atlas.local" "zipkin.atlas.local" "mail.atlas.local")
        
        # Backup hosts file if not already backed up
        if [ ! -f /etc/hosts.backup ]; then
            log_info "Creating backup of /etc/hosts..."
            sudo cp /etc/hosts /etc/hosts.backup 2>/dev/null || {
                log_warn "Cannot backup /etc/hosts - you may need to run with sudo or add entries manually"
            }
        fi
        
        # Add hostnames
        if sudo -n true 2>/dev/null; then
            log_info "Adding Atlas hostnames to /etc/hosts..."
            
            # Remove existing Atlas entries first
            for host in "${atlas_hosts[@]}"; do
                sudo sed -i.bak "/$host/d" /etc/hosts 2>/dev/null || true
            done
            
            # Add new entries
            echo "" | sudo tee -a /etc/hosts > /dev/null 2>&1 || true
            echo "# Atlas Kubernetes Ingress - Added by k8s-start.sh" | sudo tee -a /etc/hosts > /dev/null 2>&1 || true
            for host in "${atlas_hosts[@]}"; do
                echo "$ingress_ip $host" | sudo tee -a /etc/hosts > /dev/null 2>&1 || {
                    log_warn "Failed to add $host - you may need to add it manually"
                }
            done
            echo "# End Atlas entries" | sudo tee -a /etc/hosts > /dev/null 2>&1 || true
            
            log_success "Hostnames configured successfully"
        else
            log_warn "Cannot modify /etc/hosts without sudo access"
            log_info "Please add these entries to your /etc/hosts file manually:"
            for host in "${atlas_hosts[@]}"; do
                log_info "  $ingress_ip $host"
            done
        fi
    else
        log_info "Atlas hostnames already configured in /etc/hosts"
    fi
    
    # Deploy Atlas Ingress
    log_info "Deploying Atlas Ingress..."
    kubectl apply -f "$BASE_DIR/ingress.yaml" -n "$NAMESPACE"
    
    log_success "Ingress setup completed"
    
    # Show access URLs
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "=== Ingress Access URLs ==="
        log_info "  Frontend:      http://atlas.local"
        log_info "  API Gateway:   http://api.atlas.local"
        log_info "  Grafana:       http://grafana.atlas.local"
        log_info "  Prometheus:    http://prometheus.atlas.local"
        log_info "  Zipkin:        http://zipkin.atlas.local"
        log_info "  SMTP4Dev:      http://mail.atlas.local"
    fi
}

# Function to apply environment-specific configurations
apply_environment_config() {
    local env_dir="${SCRIPT_DIR}/../environments/${ENVIRONMENT}"
    
    if [[ -d "$env_dir" ]]; then
        log_info "Applying $ENVIRONMENT environment configurations..."
        
        # Apply environment-specific ConfigMaps
        if [[ -f "$env_dir/configmap.yaml" ]]; then
            kubectl apply -f "$env_dir/configmap.yaml" -n "$NAMESPACE"
        fi
        
        # Apply environment-specific patches
        if [[ -f "$env_dir/patches.yaml" ]]; then
            kubectl apply -f "$env_dir/patches.yaml" -n "$NAMESPACE"
        fi
        
        log_success "Environment configurations applied"
    else
        log_info "No specific configurations found for $ENVIRONMENT environment"
    fi
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    log_section "Atlas OnPrem K8s Platform - Starting"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $NAMESPACE"
    
    check_prerequisites
    show_cluster_info
    
    log_info "Starting Atlas platform deployment..."
    
    # Build steps (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        build_backend
        build_frontend
        build_docker_images
    else
        log_info "Skipping all build steps (--skip-build flag provided)"
    fi
    
    # Deploy in order
    create_namespace
    deploy_infrastructure
    deploy_observability
    deploy_applications
    setup_and_deploy_ingress
    apply_environment_config
    
    log_section "Deployment Summary"
    log_success "Atlas platform started successfully!"
    
    # Show deployment status
    log_info "=== Deployment Status ==="
    kubectl get all -n "$NAMESPACE"
    log_info ""
    
    # Show access information
    log_info "=== Access Information ==="
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Access via Ingress (recommended):"
        log_info "  Frontend:      http://atlas.local"
        log_info "  API Gateway:   http://api.atlas.local"
        log_info "  Grafana:       http://grafana.atlas.local (admin/admin)"
        log_info "  Prometheus:    http://prometheus.atlas.local"
        log_info "  Zipkin:        http://zipkin.atlas.local"
        log_info "  SMTP4Dev:      http://mail.atlas.local"
        log_info ""
    fi
    log_info "Or use port-forwarding:"
    log_info "  API Gateway:   kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
    log_info "  Frontend:      kubectl port-forward -n $NAMESPACE svc/frontend 9000:9000"
    log_info "  Grafana:       kubectl port-forward -n $NAMESPACE svc/grafana 3000:3000"
    log_info "  Prometheus:    kubectl port-forward -n $NAMESPACE svc/prometheus 9090:9090"
    log_info "  Zipkin:        kubectl port-forward -n $NAMESPACE svc/zipkin 9411:9411"
    log_info ""
    log_info "To check status:"
    log_info "  kubectl get pods -n $NAMESPACE"
    log_info "  kubectl get services -n $NAMESPACE"
    log_info ""
    log_info "To view logs:"
    log_info "  kubectl logs -n $NAMESPACE deployment/[service-name] -f"
    log_info ""
    log_success "Atlas platform is ready to use!"
}

# Run main function
main
