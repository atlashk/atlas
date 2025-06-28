#!/bin/bash

# =============================================================================
# Atlas OnPrem K8s Start Script
# =============================================================================
# This script starts the Atlas microservices platform on Kubernetes
# =============================================================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
BASE_DIR="${SCRIPT_DIR}/base"

# Load logger
source "$PROJECT_ROOT/backend/scripts/log/logger.sh"

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
    log_info "  --skip-build        Skip all build steps (backend JAR, Docker images)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Environments:"
    log_info "  local (default)     Local environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                          # Start with local environment"
    log_info "  $0 --env local              # Start with local environment"
    log_info "  $0 --skip-build             # Start local env, skip builds"
    log_info "  $0 --env local --skip-build # Start local env, skip builds"
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

# Function to wait for pods to be ready
wait_for_pods_ready() {
    local services=("$@")
    local timeout="${WAIT_TIMEOUT:-300s}"
    
    # Convert timeout to seconds for calculations
    local timeout_seconds=${timeout%s}
    
    for service in "${services[@]}"; do
        log_info "Waiting for $service pods to be ready..."
        
        # Wait for pods to exist with exponential backoff
        local wait_time=1
        local max_wait=60
        local elapsed=0
        
        while true; do
            local pod_count
            pod_count=$(kubectl get pods -l app="$service" -n "$NAMESPACE" --no-headers 2>/dev/null | wc -l)
            
            if [[ $pod_count -gt 0 ]]; then
                log_info "Found $pod_count pod(s) for $service, waiting for readiness..."
                break
            fi
            
            if [[ $elapsed -ge $max_wait ]]; then
                log_error "No pods were created for $service after ${max_wait} seconds"
                log_info "Checking deployed resources:"
                kubectl get all -l app="$service" -n "$NAMESPACE" || true
                return 1
            fi
            
            if [[ $elapsed -eq 0 ]]; then
                log_warn "No pods found for $service, waiting for them to be created..."
            fi
            
            sleep $wait_time
            elapsed=$((elapsed + wait_time))
            wait_time=$((wait_time < 8 ? wait_time * 2 : 8))  # Cap at 8 seconds
        done
        
        # Wait for pod readiness
        if ! kubectl wait --for=condition=ready pod -l app="$service" -n "$NAMESPACE" --timeout="$timeout" 2>/dev/null; then
            log_error "$service pods failed to become ready within $timeout"
            kubectl get pods -l app="$service" -n "$NAMESPACE" || true
            return 1
        fi

        log_success "$service pods are ready"
    done
    return 0
}

# Generic function to deploy and wait for services
deploy_service_category() {
    local category="$1"
    local timeout="$2"
    shift 2
    local services=("$@")
    
    log_section "Deploying ${category^} Services"
    
    # Deploy all services in parallel
    local apply_pids=()
    for service in "${services[@]}"; do
        {
            local error_output
            if error_output=$(kubectl apply -f "$BASE_DIR/$category/$service.yaml" -n "$NAMESPACE" 2>&1); then
                echo "SUCCESS:$service" > "/tmp/deploy_result_$$_$service"
            else
                echo "FAILED:$service:$error_output" > "/tmp/deploy_result_$$_$service"
            fi
        } &
        apply_pids+=($!)
    done

    # Wait for all apply commands to complete and check results
    for pid in "${apply_pids[@]}"; do
        wait "$pid"
    done
    
    # Check deployment results
    local failed_services=()
    local error_messages=()
    for service in "${services[@]}"; do
        if [[ -f "/tmp/deploy_result_$$_$service" ]]; then
            local result=$(cat "/tmp/deploy_result_$$_$service")
            if [[ "$result" == FAILED:* ]]; then
                failed_services+=("$service")
                # Extract error message after the second colon
                local error_msg="${result#FAILED:$service:}"
                error_messages+=("$service: $error_msg")
            fi
            rm -f "/tmp/deploy_result_$$_$service"
        fi
    done

    if [[ ${#failed_services[@]} -gt 0 ]]; then
        log_error "Failed to deploy the following $category services: ${failed_services[*]}"
        
        # Show error messages
        for error_msg in "${error_messages[@]}"; do
            log_error "  $error_msg"
        done
        exit 1
    fi

    # Wait for services to be ready
    log_info "$category services deployed. Waiting for pod readiness..."
    
    if ! WAIT_TIMEOUT="$timeout" wait_for_pods_ready "${services[@]}"; then
        log_error "Some $category services failed to become ready"
        exit 1
    fi

    log_success "All $category services are ready"
}

# Function to check prerequisites
check_prerequisites() {
    log_section "Checking Prerequisites"

    local errors=()
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        # Check Java
        if command -v java &> /dev/null; then
            java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
            major_version=$(echo $java_version | cut -d'.' -f1)
            if [[ $major_version == "1" ]]; then
                major_version=$(echo $java_version | cut -d'.' -f2)
            fi

            if [ "$major_version" -lt 17 ]; then
                errors+=("Java version $java_version is not supported. Please install Java 17 or later.")
            else
                log_success "Java found: $java_version"
            fi
        else
            errors+=("Java is not installed. Please install Java 17 or later.")
        fi
    fi

    # Check Docker
    if docker info > /dev/null 2>&1; then
        log_success "Docker found and running"
    else
        errors+=("Docker is not running. Please start Docker and try again.")
    fi

    # Check kubectl
    if command -v kubectl &> /dev/null; then
        log_success "kubectl found"
    else
        errors+=("kubectl is not installed")
    fi
    
    # Check Kubernetes cluster
    if kubectl cluster-info &> /dev/null; then
        log_success "Kubernetes cluster found"
    else
        errors+=("Cannot connect to Kubernetes cluster. Make sure you have a running Kubernetes cluster (minikube, kind, etc.)")
    fi

    # Report all errors at once
    if [[ ${#errors[@]} -gt 0 ]]; then
        log_error "Prerequisites check failed:"
        for error in "${errors[@]}"; do
            log_error "  - $error"
        done
        exit 1
    fi

    log_success "Prerequisites check passed"
}

# Function to display cluster info
show_cluster_info() {
    log_section "Cluster Information"
    kubectl cluster-info
    kubectl get nodes -o wide
}

# K8s platform detection
detect_k8s_platform() {
    local node_name
    node_name=$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "unknown")
    
    case "$node_name" in
        *minikube*) echo "minikube" ;;
        *kind*) echo "kind" ;;
        *docker-desktop*) echo "docker-desktop" ;;
        *k3s*) echo "k3s" ;;
        *) echo "generic" ;;
    esac
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    log_section "Building services"

    local build_script="$PROJECT_ROOT/backend/scripts/build/build.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Build script not found: $build_script"
        exit 1
    fi

    log_info "Granting execute permission to build script..."
    chmod +x "$build_script"

    log_info "Invoking build script..."
    if "$build_script" --infra-stack=onprem-k8s; then
        log_success "Build completed successfully."
    else
        log_error "Build failed."
        exit 1
    fi
}

# =============================================================================
# DEPLOYMENT FUNCTIONS
# =============================================================================

# Function to create namespace
create_namespace() {
    log_info "Creating namespace: $NAMESPACE"
    
    # Check if namespace already exists
    if kubectl get namespace "$NAMESPACE" &>/dev/null; then
        log_info "Namespace $NAMESPACE already exists, updating labels..."
    else
        log_info "Creating new namespace $NAMESPACE..."
    fi
    
    # Create or update namespace with standard Kubernetes labels
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
    
    log_success "Namespace $NAMESPACE ready"
}

# Function to apply security configurations (RBAC, ServiceAccounts, etc.)
apply_security_config() {
    log_section "Applying Security Configurations"
    kubectl apply -f "$BASE_DIR/application/security.yaml" -n "$NAMESPACE"
    log_success "Security configurations applied"
}

# Function to apply environment-specific configurations
apply_environment_config() {
    log_section "Applying Environment Configurations"
    local env_dir="${SCRIPT_DIR}/environments/${ENVIRONMENT}"
    if [[ -d "$env_dir" ]]; then
        log_info "Applying $ENVIRONMENT environment configurations..."
        
        # Apply environment-specific ConfigMaps
        if [[ -f "$env_dir/configmap.yaml" ]]; then
            kubectl apply -f "$env_dir/configmap.yaml" -n "$NAMESPACE"
        fi
        
        log_success "Environment configurations applied"
    else
        log_info "No specific configurations found for $ENVIRONMENT environment"
    fi
}

# Simplified deployment functions using the generic deployer
deploy_infrastructure() {
    local services=("mysql" "redis" "kafka" "smtp4dev")
    deploy_service_category "infrastructure" "300s" "${services[@]}"
}

deploy_observability() {
    local services=("zipkin" "loki" "promtail" "prometheus" "grafana")
    deploy_service_category "observability" "300s" "${services[@]}"
}

deploy_applications() {
    local backend_services=("auth-server" "user-service" "product-service" "order-service" "notification-service" "api-gateway")
    deploy_service_category "application" "600s" "${backend_services[@]}"
}

# Optimized ingress setup
setup_and_deploy_ingress() {
    log_section "Setting up Ingress"

    local platform
    platform=$(detect_k8s_platform)
    log_info "Detected platform: $platform"

    local ingress_ip
    ingress_ip=$(get_ingress_ip "$platform")

    # Setup hosts file first
    setup_hosts_file "$ingress_ip"

    # Install NGINX Ingress Controller
    install_ingress_controller "$platform"

    # Deploy Atlas ingress
    deploy_atlas_ingress

    log_success "Ingress setup completed"
}

# Get ingress IP based on platform
get_ingress_ip() {
    local platform="$1"
    
    case "$platform" in
        "minikube")
            if command -v minikube &> /dev/null; then
                minikube ip 2>/dev/null || echo "127.0.0.1"
            else
                echo "127.0.0.1"
            fi
            ;;
        *)
            echo "127.0.0.1"
            ;;
    esac
}

# Hosts file management
setup_hosts_file() {
    local ingress_ip="$1"
    local hosts=("api.atlas.local" "grafana.atlas.local" "prometheus.atlas.local" "zipkin.atlas.local" "mail.atlas.local")
    
    # Check if running on Windows (Git Bash, WSL, etc.)
    local is_windows=false
    if [[ "$OS" == "Windows_NT" ]] || [[ "$(uname -s)" == MINGW* ]] || [[ "$(uname -s)" == CYGWIN* ]] || [[ "$(uname -s)" == MSYS* ]]; then
        is_windows=true
    fi
    
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Atlas hostnames already configured in /etc/hosts"
        return
    fi
    
    log_info "Setting up local hostnames with IP: $ingress_ip"
    
    if [[ "$is_windows" == true ]]; then
        log_warn "Windows detected - Manual hosts file configuration required"
        log_info "Please add these entries to your Windows hosts file:"
        log_info "  Location: C:\\Windows\\System32\\drivers\\etc\\hosts"
        log_info ""
        log_info "Steps to modify hosts file on Windows:"
        log_info "  1. Open Notepad as Administrator"
        log_info "  2. Open file: C:\\Windows\\System32\\drivers\\etc\\hosts"
        log_info "  3. Add these lines at the end:"
        log_info ""
        for host in "${hosts[@]}"; do
            log_info "     $ingress_ip $host"
        done
        log_info ""
        log_info "  4. Save the file"
        log_info ""
        log_warn "Note: You must run Notepad as Administrator to edit the hosts file"
        return
    fi
    
    # Backup hosts file if not already backed up
    if [ ! -f /etc/hosts.backup ] && sudo -n true 2>/dev/null; then
        log_info "Creating backup of /etc/hosts..."
        sudo cp /etc/hosts /etc/hosts.backup 2>/dev/null || {
            log_warn "Cannot backup /etc/hosts"
        }
    fi
    
    # Add hostnames
    if sudo -n true 2>/dev/null; then
        # Remove existing Atlas entries first
        for host in "${hosts[@]}"; do
            sudo sed -i.bak "/$host/d" /etc/hosts 2>/dev/null || true
        done
        
        # Add new entries in one operation
        {
            echo ""
            echo "# Atlas Kubernetes Ingress - Added by k8s-start.sh"
            printf "%s %s\n" $(for host in "${hosts[@]}"; do echo "$ingress_ip $host"; done)
            echo "# End Atlas entries"
        } | sudo tee -a /etc/hosts > /dev/null 2>&1
        
        log_success "Hostnames configured successfully"
    else
        log_warn "Cannot modify /etc/hosts without sudo access"
        log_info "Please add these entries to your /etc/hosts file manually:"
        for host in "${hosts[@]}"; do
            log_info "  $ingress_ip $host"
        done
    fi
}

# Function to install ingress controller
install_ingress_controller() {
    local platform="$1"

    if kubectl get namespace ingress-nginx &>/dev/null; then
        log_info "NGINX Ingress Controller already installed"
        return 0
    fi
    
    log_info "Installing NGINX Ingress Controller..."

    local ingress_url
    case $platform in
        "minikube")
            if command -v minikube &> /dev/null; then
                minikube addons enable ingress
                return 0
            else
                ingress_url="https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/baremetal/deploy.yaml"
            fi
            ;;
        "kind")
            ingress_url="https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/kind/deploy.yaml"
            ;;
        "docker-desktop")
            ingress_url="https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml"
            ;;
        *)
            ingress_url="https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/baremetal/deploy.yaml"
            ;;
    esac

    if [[ -n "$ingress_url" ]]; then
        kubectl apply -f "$ingress_url" &>/dev/null
    fi

    log_info "Waiting for NGINX Ingress Controller readiness..."
    if ! kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=controller -n ingress-nginx --timeout=300s 2>/dev/null; then
        log_error "NGINX Ingress Controller failed to become ready within 5 minutes"
        kubectl get pods -n ingress-nginx 2>/dev/null || true
        exit 1
    fi

    log_success "NGINX Ingress Controller installed"
}

deploy_atlas_ingress() {
    log_info "Deploying Atlas Ingress..."
    kubectl apply -f "$BASE_DIR/ingress/nginx-ingress.yaml" -n "$NAMESPACE"
    
    # Check if ingress was created successfully
    log_info "Verifying ingress deployment..."
    if ! kubectl get ingress atlas-ingress -n "$NAMESPACE" &>/dev/null; then
        log_error "Failed to deploy Atlas ingress - ingress resource not found"
        log_info "Available ingress resources in namespace $NAMESPACE:"
        kubectl get ingress -n "$NAMESPACE" 2>/dev/null || log_warn "No ingress resources found"
        exit 1
    fi
    
    # Wait for ingress to be ready
    log_info "Waiting for ingress to be ready..."
    if ! kubectl wait --for=condition=ready ingress atlas-ingress -n "$NAMESPACE" --timeout=60s 2>/dev/null; then
        log_warn "Ingress readiness check timed out, but this is normal for some platforms"
        kubectl get ingress -n "$NAMESPACE" 2>/dev/null || true
    fi
    
    log_success "Atlas ingress deployed successfully"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    local start_time=$(date +%s)
    
    log_section "Atlas OnPrem K8s Platform - Starting"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $NAMESPACE"
    
    # Prerequisites and build
    check_prerequisites
    show_cluster_info

    # Build step (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        local build_start=$(date +%s)
        build_services
        local build_end=$(date +%s)
        log_info "Build completed in $((build_end - build_start)) seconds"
    else
        log_info "Skipping build step (--skip-build flag provided)"
    fi

    # Setup namespace and configs
    create_namespace
    apply_security_config &
    apply_environment_config &
    wait
    
    # Deploy services in order (infrastructure -> observability -> applications -> ingress)
    deploy_infrastructure
    deploy_observability
    deploy_applications
    setup_and_deploy_ingress

    # Show final information
    show_deployment_summary "$start_time"
}

# Function to show deployment summary
show_deployment_summary() {
    local start_time="$1"
    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))
    local minutes=$((total_time / 60))
    local seconds=$((total_time % 60))

    log_section "Access Information"
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Access via Ingress (recommended):"
        log_info "  API Gateway:   http://api.atlas.local"
        log_info "  Grafana:       http://grafana.atlas.local (admin/admin)"
        log_info "  Prometheus:    http://prometheus.atlas.local"
        log_info "  Zipkin:        http://zipkin.atlas.local"
        log_info "  SMTP4Dev:      http://mail.atlas.local"
        log_info ""
    fi
    
    log_info "Alternative access via port-forwarding:"
    local port_forwards=(
        "API Gateway:   kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
        "Grafana:       kubectl port-forward -n $NAMESPACE svc/grafana 3000:3000"
        "Prometheus:    kubectl port-forward -n $NAMESPACE svc/prometheus 9090:9090"
        "Zipkin:        kubectl port-forward -n $NAMESPACE svc/zipkin 9411:9411"
    )
    
    for pf in "${port_forwards[@]}"; do
        log_info "  $pf"
    done
    
    log_info ""
    log_info "Management commands:"
    log_info "  Status:        kubectl get pods -n $NAMESPACE"
    log_info "  Services:      kubectl get services -n $NAMESPACE"
    log_info "  Logs:          kubectl logs -n $NAMESPACE deployment/[service-name] -f"

    log_section "Frontend Configuration"
    log_info "To run the frontend with K8s backend:"
    log_info "  cd frontend"
    log_info "  cp env.k8s .env  # or set VITE_API_BASE_URL=http://api.atlas.local"
    log_info "  npm install && npm run dev"
    log_info ""
    log_info "Frontend will be available at: http://localhost:9000"

    log_section "Deployment Summary"
    log_success "Atlas platform deployment completed successfully!"
    log_info "Total execution time: ${minutes}m ${seconds}s"
}

# Run main function
main
