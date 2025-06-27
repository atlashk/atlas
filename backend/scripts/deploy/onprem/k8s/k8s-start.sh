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
source "$PROJECT_ROOT/backend/scripts/utils/logger.sh"

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
    
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "Atlas hostnames already configured in /etc/hosts"
        return
    fi
    
    log_info "Setting up local hostnames with IP: $ingress_ip"
    
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

# Simplified deployment functions using the generic deployer
deploy_infrastructure() {
    local services=("mysql" "redis" "kafka" "smtp4dev")
    log_section "Deploying Infrastructure Services"

    # Deploy all services in parallel
    local apply_pids=()
    for service in "${services[@]}"; do
        {
            kubectl apply -f "$BASE_DIR/infrastructure/$service.yaml" -n "$NAMESPACE" || {
                log_warn "Failed to deploy $service, continuing..."
            }
        } &
        apply_pids+=($!)
    done
    
    # Wait for all apply commands to complete
    for pid in "${apply_pids[@]}"; do
        wait "$pid"
    done
    
    # Wait for deployments to be ready (in parallel)
    local wait_pids=()
    for service in "${services[@]}"; do
        {
            if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
                log_info "Waiting for $service to be ready..."
                kubectl wait --for=condition=available --timeout=120s deployment/"$service" -n "$NAMESPACE" || {
                    log_warn "$service deployment timeout, continuing..."
                }
            fi
        } &
        wait_pids+=($!)
    done
    for pid in "${wait_pids[@]}"; do
        wait "$pid"
    done
    
    log_success "Infrastructure services deployed"
}

deploy_observability() {
    local services=("zipkin" "loki" "promtail" "prometheus" "grafana")
    log_section "Deploying Observability Services"
    
    # Deploy all services in parallel
    local apply_pids=()
    for service in "${services[@]}"; do
        {
            kubectl apply -f "$BASE_DIR/observability/$service.yaml" -n "$NAMESPACE" || {
                log_warn "Failed to deploy $service, continuing..."
            }
        } &
        apply_pids+=($!)
    done
    
    # Wait for all apply commands to complete
    for pid in "${apply_pids[@]}"; do
        wait "$pid"
    done
    
    # Wait for deployments to be ready (in parallel)
    local wait_pids=()
    for service in "${services[@]}"; do
        {
            if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
                log_info "Waiting for $service to be ready..."
                kubectl wait --for=condition=available --timeout=120s deployment/"$service" -n "$NAMESPACE" || {
                    log_warn "$service deployment timeout, continuing..."
                }
            elif kubectl get daemonset "$service" -n "$NAMESPACE" &>/dev/null; then
                # Handle DaemonSets (like promtail)
                log_info "Waiting for $service DaemonSet to be ready..."
                kubectl wait --for=condition=ready --timeout=60s pod -l app="$service" -n "$NAMESPACE" || {
                    log_warn "$service DaemonSet timeout, continuing..."
                }
            fi
        } &
        wait_pids+=($!)
    done
    for pid in "${wait_pids[@]}"; do
        wait "$pid"
    done
    
    log_success "Observability services deployed"
}

deploy_applications() {
    log_section "Deploying Application Services"

    # Deploy all services except API gateway in parallel first
    local backend_services=("auth-server" "user-service" "product-service" "order-service" "notification-service")
    
    # Deploy backend services in parallel
    local apply_pids=()
    for service in "${backend_services[@]}"; do
        {
            kubectl apply -f "$BASE_DIR/application/$service.yaml" -n "$NAMESPACE" || {
                log_warn "Failed to deploy $service, continuing..."
            }
        } &
        apply_pids+=($!)
    done

    # Wait for all apply commands to complete
    for pid in "${apply_pids[@]}"; do
        wait "$pid"
    done
    
    # Wait for deployments to be ready (in parallel)
    log_info "Waiting for services to be ready..."
    local wait_pids=()
    for service in "${backend_services[@]}"; do
        {
            if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
                log_info "Waiting for $service to be ready..."
                kubectl wait --for=condition=available --timeout=300s deployment/"$service" -n "$NAMESPACE" || {
                    log_warn "$service deployment timeout, continuing..."
                }
            fi
        } &
        wait_pids+=($!)
    done
    for pid in "${wait_pids[@]}"; do
        wait "$pid"
    done

    # Deploy API gateway last
    log_info "Deploying API gateway (last)..."
    kubectl apply -f "$BASE_DIR/application/api-gateway.yaml" -n "$NAMESPACE"

    log_info "Waiting for API gateway to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/api-gateway -n "$NAMESPACE" || {
        log_warn "API gateway deployment timeout, continuing..."
    }

    log_success "Application services deployed"
}

# Optimized ingress setup
setup_and_deploy_ingress() {
    log_section "Setting up Ingress"
    
    # Install NGINX Ingress Controller if needed
    if ! kubectl get namespace ingress-nginx &>/dev/null; then
        log_info "Installing NGINX Ingress Controller..."
        
        local platform
        platform=$(detect_k8s_platform)
        log_info "Detected platform: $platform"
        
        local ingress_url
        case $platform in
            "minikube")
                if command -v minikube &> /dev/null; then
                    minikube addons enable ingress
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
            kubectl apply -f "$ingress_url"
        fi
        
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
    
    # Setup hosts and deploy ingress in parallel
    local platform
    platform=$(detect_k8s_platform)
    local ingress_ip
    ingress_ip=$(get_ingress_ip "$platform")
    
    {
        setup_hosts_file "$ingress_ip"
    } &
    {
        log_info "Deploying Atlas Ingress..."
        kubectl apply -f "$BASE_DIR/ingress.yaml" -n "$NAMESPACE"
    } &
    wait
    
    log_success "Ingress setup completed"

    # Show access URLs
    if grep -q "atlas.local" /etc/hosts 2>/dev/null; then
        log_info "=== Ingress Access URLs ==="
        log_info "  API Gateway:   http://api.atlas.local"
        log_info "  Grafana:       http://grafana.atlas.local"
        log_info "  Prometheus:    http://prometheus.atlas.local"
        log_info "  Zipkin:        http://zipkin.atlas.local"
        log_info "  SMTP4Dev:      http://mail.atlas.local"
    fi
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

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    local start_time=$(date +%s)
    
    log_section "Atlas OnPrem K8s Platform - Starting"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $NAMESPACE"
    
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

    create_namespace
    apply_security_config
    apply_environment_config
    deploy_infrastructure
    deploy_observability
    deploy_applications
    setup_and_deploy_ingress

    # Show access information
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
    log_info "Or use port-forwarding:"
    log_info "  API Gateway:   kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
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
