#!/bin/bash

# =============================================================================
# Atlas OnPrem K8s Start Script
# =============================================================================
# This script starts the Atlas microservices platform on Kubernetes
# =============================================================================

set -e

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
readonly BASE_DIR="${SCRIPT_DIR}/services"
readonly APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"
readonly ENV_DIR="$SCRIPT_DIR/env"

# Load logger
# source "$PROJECT_ROOT/backend/scripts/common.sh"

# Default options
NAMESPACE="atlas-onprem-k8s"
SKIP_BUILD=false

# Configuration variables (populated by read_app_stack_config)
declare -g DATASOURCE MESSAGING NOTIFICATION_EMAIL OBSERVABILITY_LOGGING_STACK OBSERVABILITY_METRICS OBSERVABILITY_TRACING

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Kubernetes Start Script - Starts the Atlas microservices platform"
    echo ""
    echo "This script automatically sets up:"
    echo "  - NGINX Ingress Controller (if not present)"
    echo "  - Local hostnames in /etc/hosts (atlas.local, api.atlas.local, etc.)"
    echo "  - All Atlas services with Ingress routing"
    echo ""
    echo "Options:"
    echo "  --skip-build        Skip all build steps (backend JAR, Docker images)"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Note: You may be prompted for sudo password to modify /etc/hosts"
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
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
    echo "=== Checking Prerequisites ==="
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        # Check Java
        if command -v java &> /dev/null; then
            local java_version
            java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
            local major_version
            major_version=$(echo $java_version | cut -d'.' -f1)
            
            if [[ $major_version == "1" ]]; then
                major_version=$(echo $java_version | cut -d'.' -f2)
            fi

            if [ "$major_version" -lt 17 ]; then
                errors+=("Java version $java_version is not supported. Please install Java 17 or later.")
            else
                echo "Java found: $java_version"
            fi
        else
            errors+=("Java is not installed. Please install Java 17 or later.")
        fi
    fi

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
}

show_cluster_info() {
    echo "=== Cluster Information ==="
    kubectl cluster-info
    kubectl get nodes -o wide
}

# =============================================================================
# CONFIGURATION FUNCTIONS
# =============================================================================

read_app_stack_config() {
    echo "=== Reading application stack configuration... ==="
    
    if [[ ! -f "$APP_STACK_CONFIG" ]]; then
        echo "Configuration file not found: $APP_STACK_CONFIG" >&2
        echo "Please run the configuration script first: backend/scripts/app-stack-config.sh"
        exit 1
    fi

    # Read additional configuration values specific to this deployment
    DATASOURCE=$(grep "^datasource=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "mysql")
    MESSAGING=$(grep "^messaging=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "kafka")
    NOTIFICATION_EMAIL=$(grep "^notification.email=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "spring")
    OBSERVABILITY_LOGGING_STACK=$(grep "^observability.logging.stack=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "loki")
    OBSERVABILITY_METRICS=$(grep "^observability.metrics=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "prometheus")
    OBSERVABILITY_TRACING=$(grep "^observability.tracing=" "$APP_STACK_CONFIG" | cut -d'=' -f2 || echo "zipkin")

    echo "Configuration loaded:"
    echo "  - Datasource: $DATASOURCE"
    echo "  - Messaging: $MESSAGING"
    echo "  - Email: $NOTIFICATION_EMAIL"
    echo "  - Logging: $OBSERVABILITY_LOGGING_STACK"
    echo "  - Metrics: $OBSERVABILITY_METRICS"
    echo "  - Tracing: $OBSERVABILITY_TRACING"
}

generate_env_configmaps() {
    echo "Generating environment ConfigMaps..."

    local env_generator="$SCRIPT_DIR/env-generator.sh"
    if [[ ! -f "$env_generator" ]]; then
        echo "Environment generator script not found: $env_generator" >&2
        exit 1
    fi

    chmod +x "$env_generator"

    if ! "$env_generator" "$NAMESPACE"; then
        echo "Failed to generate environment ConfigMaps" >&2
        exit 1
    fi

    echo "Environment ConfigMaps generated successfully"
}

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

wait_for_pods_ready() {
    local services=("$@")
    local timeout="${WAIT_TIMEOUT:-300s}"
    local timeout_seconds=${timeout%s}
    
    for service in "${services[@]}"; do        
        # Wait for pods to exist with exponential backoff
        local wait_time=1
        local max_wait=60
        local elapsed=0
        
        while true; do
            local pod_count
            pod_count=$(kubectl get pods -l app="$service" -n "$NAMESPACE" --no-headers 2>/dev/null | wc -l)
            
            if [[ $pod_count -gt 0 ]]; then
                echo "Found $pod_count pod(s) for $service, waiting for readiness..."
                break
            fi
            
            if [[ $elapsed -ge $max_wait ]]; then
                echo "No pods were created for $service after ${max_wait} seconds" >&2
                echo "Checking deployed resources:"
                kubectl get all -l app="$service" -n "$NAMESPACE" || true
                return 1
            fi
            
            if [[ $elapsed -eq 0 ]]; then
                echo "No pods found for $service, waiting for them to be created..."
            fi
            
            sleep $wait_time
            elapsed=$((elapsed + wait_time))
            wait_time=$((wait_time < 8 ? wait_time * 2 : 8))  # Cap at 8 seconds
        done
        
        # Wait for pod readiness
        if ! kubectl wait --for=condition=ready pod -l app="$service" -n "$NAMESPACE" --timeout="$timeout" 2>/dev/null; then
            echo "$service pods failed to become ready within $timeout" >&2
            kubectl get pods -l app="$service" -n "$NAMESPACE" || true
            return 1
        fi

        echo "$service pods are ready"
    done
    return 0
}

deploy_service_category() {
    local category="$1"
    local timeout="$2"
    shift 2
    local services=("$@")
    
    echo "=== Deploying ${category^} Services ==="
    
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
                local error_msg="${result#FAILED:$service:}"
                error_messages+=("$service: $error_msg")
            fi
            rm -f "/tmp/deploy_result_$$_$service"
        fi
    done

    if [[ ${#failed_services[@]} -gt 0 ]]; then
        echo "Failed to deploy the following $category services: ${failed_services[*]}" >&2
        for error_msg in "${error_messages[@]}"; do
            echo "  $error_msg" >&2
        done
        exit 1
    fi

    # Wait for services to be ready
    echo "$category services deployed. Waiting for pod readiness..."
    
    if ! WAIT_TIMEOUT="$timeout" wait_for_pods_ready "${services[@]}"; then
        echo "Some $category services failed to become ready" >&2
        exit 1
    fi

    echo "All $category services are ready"
}

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
    echo "=== Building Services ==="

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [ ! -f "$build_script" ]; then
        echo "Build script not found: $build_script" >&2
        exit 1
    fi

    echo "Granting execute permission to build script..."
    chmod +x "$build_script"

    echo "Invoking build script..."
    if "$build_script"; then
        echo "Build completed successfully"
    else
        echo "Build failed" >&2
        exit 1
    fi
}

# =============================================================================
# DEPLOYMENT FUNCTIONS
# =============================================================================

create_namespace() {
    echo "=== Creating Namespace ==="
    
    # Check if namespace already exists
    if kubectl get namespace "$NAMESPACE" &>/dev/null; then
        echo "Namespace $NAMESPACE already exists, updating labels..."
    else
        echo "Creating new namespace $NAMESPACE..."
    fi
    
    # Create or update namespace with standard Kubernetes labels
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | \
    kubectl label --local -f - \
        app.kubernetes.io/name=atlas \
        app.kubernetes.io/instance="${NAMESPACE}" \
        app.kubernetes.io/version="1.0.0" \
        app.kubernetes.io/managed-by=kubectl -o yaml | \
    kubectl annotate --local -f - \
        atlas.org/version="1.0.0" \
        atlas.org/deployment-method="kubectl" -o yaml | \
    kubectl apply -f -
    
    echo "Namespace $NAMESPACE ready"
}

apply_security_config() {
    echo "=== Applying Security Configurations ==="
    kubectl apply -f "$BASE_DIR/application/security.yaml" -n "$NAMESPACE"
    echo "Security configurations applied"
}

get_infrastructure_services() {
    local services=("redis")
    
    case "$DATASOURCE" in
        mysql) services+=("mysql") ;;
        postgres|postgresql) services+=("postgres") ;;
        *) 
            log_warn "Unknown datasource: $DATASOURCE, defaulting to mysql"
            services+=("mysql")
            ;;
    esac
    
    case "$MESSAGING" in
        kafka) services+=("kafka") ;;
        rabbitmq) services+=("rabbitmq") ;;
        *)
            log_warn "Unknown messaging system: $MESSAGING, defaulting to kafka"
            services+=("kafka")
            ;;
    esac
    
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        services+=("smtp4dev")
    fi
    
    echo "${services[@]}"
}

get_observability_services() {
    local services=()
    
    if [[ "$OBSERVABILITY_LOGGING_STACK" == "loki" ]]; then
        services+=("loki" "promtail")
    fi
    
    if [[ "$OBSERVABILITY_METRICS" == "prometheus" ]]; then
        services+=("prometheus")
    fi
    
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        services+=("zipkin")
    fi
    
    if [[ ${#services[@]} -gt 0 ]]; then
        services+=("grafana")
    fi
    
    echo "${services[@]}"
}

get_application_services() {
    local services=("user-service" "product-service" "order-service" "notification-service")
    echo "${services[@]}"
}

deploy_infrastructure() {
    local infrastructure_services
    read -ra infrastructure_services <<< "$(get_infrastructure_services)"
    if [[ ${#infrastructure_services[@]} -gt 0 ]]; then
        deploy_service_category "infrastructure" "300s" "${infrastructure_services[@]}"
    fi
}

deploy_observability() {
    local observability_services
    read -ra observability_services <<< "$(get_observability_services)"
    if [[ ${#observability_services[@]} -gt 0 ]]; then
        deploy_service_category "observability" "300s" "${observability_services[@]}"
    fi
}

deploy_applications() {
    local application_services
    read -ra application_services <<< "$(get_application_services)"
    if [[ ${#application_services[@]} -gt 0 ]]; then
        deploy_service_category "application" "600s" "${application_services[@]}"
    fi

    # Deploy API Gateway separately
    deploy_service_category "application" "300s" "api-gateway"
}

# =============================================================================
# INGRESS SETUP
# =============================================================================

setup_and_deploy_ingress() {
    echo "=== Setting Up and Deploying Ingress ==="

    local platform
    platform=$(detect_k8s_platform)
    echo "Detected platform: $platform"

    local ingress_ip
    ingress_ip=$(get_ingress_ip "$platform")

    # Install NGINX Ingress Controller
    install_ingress_controller "$platform"

    # Deploy ingress
    deploy_ingress

    echo "Ingress setup completed"
}

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

install_ingress_controller() {
    local platform="$1"

    if kubectl get namespace ingress-nginx &>/dev/null; then
        echo "NGINX Ingress Controller already deployed"
        return 0
    fi

    echo "Installing NGINX Ingress Controller..."

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

    echo "Waiting for NGINX Ingress Controller readiness..."
    if ! kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=controller -n ingress-nginx --timeout=300s 2>/dev/null; then
        echo "NGINX Ingress Controller failed to become ready within 5 minutes" >&2
        kubectl get pods -n ingress-nginx 2>/dev/null || true
        exit 1
    fi

    echo "NGINX Ingress Controller installed"
}

deploy_ingress() {
    # Check if ingress is already deployed
    if kubectl get ingress atlas-ingress -n "$NAMESPACE" &>/dev/null; then
        echo "Ingress already deployed"
        return 0
    fi

    echo "Deploying Ingress..."
    kubectl apply -f "$BASE_DIR/ingress/nginx-ingress.yaml" -n "$NAMESPACE"

    # Wait for ingress to be ready
    echo "Waiting for ingress to be ready..."
    if ! kubectl wait --for=condition=ready ingress atlas-ingress -n "$NAMESPACE" --timeout=60s 2>/dev/null; then
        echo "Ingress readiness check timed out, but this is normal for some platforms"
    fi

    echo "Ingress deployed successfully"
}

# =============================================================================
# SUMMARY AND REPORTING
# =============================================================================

show_deployment_summary() {
    local start_time="$1"
    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))
    local minutes=$((total_time / 60))
    local seconds=$((total_time % 60))

    show_access_information
    show_management_commands
    
    echo "=== Deployment Summary ==="
    echo "Atlas platform deployment completed successfully!"
    echo "Total execution time: ${minutes}m ${seconds}s"
}

show_access_information() {
    echo "=== Access Information ==="

    echo "Access via Ingress (recommended):"
    echo "  API Gateway:   http://api.atlas.local"
    echo "  Grafana:       http://grafana.atlas.local (admin/admin)"
    echo "  Prometheus:    http://prometheus.atlas.local"
    echo "  Zipkin:        http://zipkin.atlas.local"
    echo "  SMTP4Dev:      http://smtp4dev.atlas.local"
    echo ""

    echo "Alternative access via port-forwarding:"
    local port_forwards=(
        "API Gateway:   kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
        "Grafana:       kubectl port-forward -n $NAMESPACE svc/grafana 3000:3000"
        "Prometheus:    kubectl port-forward -n $NAMESPACE svc/prometheus 9090:9090"
        "Zipkin:        kubectl port-forward -n $NAMESPACE svc/zipkin 9411:9411"
        "SMTP4Dev:      kubectl port-forward -n $NAMESPACE svc/smtp4dev 80:80"
    )
    for pf in "${port_forwards[@]}"; do
        echo "  $pf"
    done
}

show_management_commands() {
    echo ""
    echo "Management commands:"
    echo "  Status:        kubectl get pods -n $NAMESPACE"
    echo "  Services:      kubectl get services -n $NAMESPACE"
    echo "  Logs:          kubectl logs -n $NAMESPACE deployment/[service-name] -f"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Main function
main() {
    parse_arguments "$@"
    check_prerequisites
    show_cluster_info

    local start_time=$(date +%s)

    echo "=== Atlas OnPrem K8s Platform - Starting ==="
    echo "Namespace: $NAMESPACE"

    # Build step (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        local build_start=$(date +%s)
        build_services
        local build_end=$(date +%s)
        echo "Build completed in $((build_end - build_start)) seconds"
    else
        echo "Skipping build step (--skip-build flag provided)"
    fi

    read_app_stack_config
    generate_env_configmaps

    create_namespace
    apply_security_config
    deploy_infrastructure
    deploy_observability
    deploy_applications
    setup_and_deploy_ingress
    show_deployment_summary "$start_time"
}

# Execute main function
main "$@"
