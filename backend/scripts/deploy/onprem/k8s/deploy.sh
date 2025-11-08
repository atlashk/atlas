#!/bin/bash

# =============================================================================
# Atlas Kubernetes Deployment Script (Optimized)
# =============================================================================
# This script generates Kubernetes ConfigMaps and deploys the Atlas microservices 
# platform on Kubernetes. It includes integrated ConfigMap generation 
# functionality for optimal performance.
# =============================================================================

set -euo pipefail

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas-onprem-k8s"
BASE_DIR="${SCRIPT_DIR}/services"
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"
ENV_DIR="$SCRIPT_DIR/.env"

# Default options
NAMESPACE="atlas-onprem-k8s"
SKIP_BUILD=false
FORCE_RECREATE=false

# Configuration variables (populated by read_app_stack_config)
declare -g DATASOURCE MESSAGING STORAGE API_CLIENT NOTIFICATION_EMAIL OBSERVABILITY_LOGGING_STACK OBSERVABILITY_METRICS OBSERVABILITY_TRACING

# Service configuration arrays for ConfigMap generation
declare -A SERVICE_CONFIGS=(
    ["api-gateway"]="api-gateway-config"
    ["user-service"]="user-service-config"
    ["product-service"]="product-service-config"
    ["order-service"]="order-service-config"
    ["payment-service"]="payment-service-config"
    ["notification-service"]="notification-service-config"
)

declare -A SERVICE_DATABASES=(
    ["user-service"]="db_user"
    ["product-service"]="db_product"
    ["order-service"]="db_order"
    ["payment-service"]="db_payment"
    ["notification-service"]="db_notification"
)

# Define service-specific configurations
declare -A SERVICE_SPECIFIC_CONFIGS=(
    ["api-gateway"]="jwt_config"
    ["notification-service"]="email_config"
)

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Kubernetes Deployment Script - Generates ConfigMaps and deploys the Atlas microservices platform"
    echo ""
    echo "This script automatically sets up:"
    echo "  - Kubernetes ConfigMaps for service configuration"
    echo "  - NGINX Ingress Controller (if not present)"
    echo "  - All Atlas services with Ingress routing"
    echo ""
    echo "Options:"
    echo "  --skip-build        Skip all build steps (backend JAR, Docker images)"
    echo "  --force-recreate    Force recreate ConfigMaps and deployments"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                  # Full deployment with ConfigMap generation"
    echo "  $0 --skip-build     # Deploy without builds (uses existing images)"
    echo "  $0 --force-recreate # Force recreate all resources with fresh ConfigMaps"
    echo ""
    echo "Features:"
    echo "  - Integrated ConfigMap generation"
    echo "  - Configurable infrastructure stack (database, messaging, observability)"
    echo "  - Enhanced health checks and service dependency management"
    echo "  - Comprehensive error handling and validation"
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
            --force-recreate)
                FORCE_RECREATE=true
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
# PREREQUISITE CHECKS
# =============================================================================

check_java_version() {
    if ! command -v java &> /dev/null; then
        echo "Java is not installed. Please install Java 17 or later." >&2
        return 1
    fi

    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    
    local major_version
    major_version=$(echo "$java_version" | cut -d'.' -f1)
    
    # Handle both old (1.8) and new (17) version formats
    if [[ $major_version == "1" ]]; then
        major_version=$(echo "$java_version" | cut -d'.' -f2)
    fi

    if [[ $major_version -lt 17 ]]; then
        echo "Java version $java_version is not supported. Please install Java 17 or later." >&2
        return 1
    fi
    
    echo "Java found: $java_version"
    return 0
}

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "Docker is not running. Please start Docker and try again." >&2
        return 1
    fi
    echo "Docker found and running"
    return 0
}

check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        echo "kubectl is not installed" >&2
        return 1
    fi
    echo "kubectl found"
    return 0
}

check_kubernetes_cluster() {
    if ! kubectl cluster-info &> /dev/null; then
        echo "Cannot connect to Kubernetes cluster. Make sure you have a running Kubernetes cluster (minikube, kind, etc.)" >&2
        return 1
    fi
    echo "Kubernetes cluster found"
    return 0
}

check_prerequisites() {
    echo "Checking Prerequisites..."
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_version || exit 1
    fi

    check_docker || exit 1
    check_kubectl || exit 1
    check_kubernetes_cluster || exit 1
    
    echo "Prerequisites check passed"
    echo
}

show_cluster_info() {
    echo "Cluster Information:"
    kubectl cluster-info
    kubectl get nodes -o wide
    echo
}

# =============================================================================
# CONFIGURATION FUNCTIONS
# =============================================================================

read_config_value() {
    local key="$1"
    local default_value="$2"
    local value
    
    value=$(grep "^${key}=" "$APP_STACK_CONFIG" 2>/dev/null | cut -d'=' -f2 | tr -d '[:space:]')
    echo "${value:-$default_value}"
}

read_app_stack_config() {
    echo "Reading application stack configuration..."
    
    if [[ ! -f "$APP_STACK_CONFIG" ]]; then
        echo "Configuration file not found: $APP_STACK_CONFIG" >&2
        echo "Please run the configuration script first: backend/scripts/app-stack-config.sh"
        exit 1
    fi
    
    if [[ ! -r "$APP_STACK_CONFIG" ]]; then
        echo "Configuration file is not readable: $APP_STACK_CONFIG" >&2
        exit 1
    fi
    
    # Read configuration values with improved error handling
    DATASOURCE=$(read_config_value "datasource" "mysql")
    MESSAGING=$(read_config_value "messaging" "kafka")
    API_CLIENT=$(read_config_value "api-client" "rest-restclient")
    STORAGE=$(read_config_value "storage" "filesystem")
    NOTIFICATION_EMAIL=$(read_config_value "notification.email" "spring")
    OBSERVABILITY_LOGGING_STACK=$(read_config_value "observability.logging.stack" "loki")
    OBSERVABILITY_METRICS=$(read_config_value "observability.metrics" "prometheus")
    OBSERVABILITY_TRACING=$(read_config_value "observability.tracing" "zipkin")
    
    # Validate critical configuration values
    if [[ ! "$DATASOURCE" =~ ^(mysql|postgres)$ ]]; then
        echo "Warning: Invalid datasource '$DATASOURCE', defaulting to 'mysql'"
        DATASOURCE="mysql"
    fi
    
    if [[ ! "$MESSAGING" =~ ^(kafka|rabbitmq)$ ]]; then
        echo "Warning: Invalid messaging system '$MESSAGING', defaulting to 'kafka'"
        MESSAGING="kafka"
    fi
    
    # Validate storage option
    if [[ ! "$STORAGE" =~ ^(filesystem|minio)$ ]]; then
        echo "Warning: Invalid storage '$STORAGE', defaulting to 'filesystem'"
        STORAGE="filesystem"
    fi

    echo "Configuration loaded:"
    echo "  - Datasource: $DATASOURCE"
    echo "  - Messaging: $MESSAGING"
    echo "  - Storage: $STORAGE"
    echo "  - API Client: $API_CLIENT"
    echo "  - Email: $NOTIFICATION_EMAIL"
    echo "  - Logging: $OBSERVABILITY_LOGGING_STACK"
    echo "  - Metrics: $OBSERVABILITY_METRICS"
    echo "  - Tracing: $OBSERVABILITY_TRACING"
    echo
}

# =============================================================================
# CONFIGMAP GENERATION FUNCTIONS
# =============================================================================

generate_database_config() {
    local service_name="$1"
    local database_name="${SERVICE_DATABASES[$service_name]:-}"

    if [[ -z "$database_name" ]]; then
        return 0  # No database config needed for this service
    fi

    case "$DATASOURCE" in
        mysql)
            cat << EOF
  # Database Configuration (MySQL) for $service_name
  # Note: DB_PASSWORD is provided via secretKeyRef in deployment YAML
  DB_URL: "jdbc:mysql://mysql:3306/$database_name?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
  DB_USERNAME: "root"
  DB_QUARTZ_URL: "jdbc:mysql://mysql:3306/db_quartz?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
  DB_QUARTZ_USERNAME: "root"
EOF
            ;;
        postgres)
            cat << EOF
  # Database Configuration (PostgreSQL) for $service_name
  # Note: DB_PASSWORD is provided via secretKeyRef in deployment YAML
  DB_URL: "jdbc:postgresql://postgres:5432/$database_name"
  DB_USERNAME: "root"
  DB_QUARTZ_URL: "jdbc:postgresql://postgres:5432/db_quartz"
  DB_QUARTZ_USERNAME: "root"
EOF
            ;;
    esac
}

generate_messaging_config() {
    case "$MESSAGING" in
        kafka)
            cat << EOF

  # Messaging Configuration (Kafka)
  KAFKA_BOOTSTRAP_SERVERS: "kafka:29092"
EOF
            ;;
        rabbitmq)
            cat << EOF

  # Messaging Configuration (RabbitMQ)
  # Note: RABBITMQ_PASSWORD is provided via secretKeyRef in deployment YAML
  RABBITMQ_HOST: "rabbitmq"
  RABBITMQ_PORT: "5672"
  RABBITMQ_USERNAME: "admin"
EOF
            ;;
    esac
}

generate_common_infrastructure_config() {
    cat << EOF

  # Redis Configuration
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
EOF
}

generate_storage_config() {
    local service_name="$1"

    if [[ "$STORAGE" == "minio" ]]; then
        # Only product-service currently uses object storage
        if [[ "$service_name" == "product-service" ]]; then
            cat << EOF

  # Object Storage Configuration (MinIO)
  APP_STORAGE_MINIO_ENDPOINT: "http://minio:9000"
  APP_STORAGE_MINIO_ACCESS_KEY: "admin"
  APP_STORAGE_MINIO_SECRET_KEY: "admin123"
EOF
        fi
    fi
}

generate_observability_config() {
    if [[ "$OBSERVABILITY_TRACING" == "zipkin" ]]; then
        cat << EOF

  # Tracing Configuration (Zipkin)
  ZIPKIN_ENDPOINT: "http://zipkin:9411/api/v2/spans"
EOF
    fi
}

generate_jwt_config() {
    cat << EOF

  # JWT Configuration
  JWK_SET_URI: "http://user-service:8081/.well-known/jwks.json"
EOF
}

generate_api_client_config() {
    local api_client_prefix="$1"
    
    if [[ "$api_client_prefix" == "rest" ]]; then
        cat << EOF

  # REST API Configuration
  API_CLIENT_REST_USER_SERVICE_BASE_URL: "http://user-service:8081"
  API_CLIENT_REST_PRODUCT_SERVICE_BASE_URL: "http://product-service:8082"
EOF
    elif [[ "$api_client_prefix" == "grpc" ]]; then
        cat << EOF

  # gRPC Configuration
  GRPC_CLIENT_USER_ADDRESS: "static://user-service:50051"
  GRPC_CLIENT_PRODUCT_ADDRESS: "static://product-service:50052"
EOF
    fi
}

generate_email_config() {
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        cat << EOF

  # Email Configuration
  MAIL_SERVER_HOST: "smtp4dev"
EOF
    fi
}

generate_api_gateway_configmap() {
    local namespace="$1"
    local configmap_file="$ENV_DIR/api-gateway-configmap.yaml"
    echo "Creating $configmap_file"
    
    {
        cat << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-gateway-config
  namespace: $namespace
  labels:
    app: api-gateway
    component: config
data:
  # Redis Configuration
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
EOF
        generate_jwt_config
        generate_observability_config
    } > "$configmap_file"
    
    echo "Generated $configmap_file"
}

generate_service_configmap() {
    local service_name="$1"
    local namespace="$2"
    local configmap_name="${SERVICE_CONFIGS[$service_name]}"
    local configmap_file="$ENV_DIR/${service_name}-configmap.yaml"
    
    echo "Creating $configmap_file"
    
    {
        cat << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: $configmap_name
  namespace: $namespace
  labels:
    app: $service_name
    component: config
data:
EOF
        generate_database_config "$service_name"
        generate_messaging_config
        generate_common_infrastructure_config
        generate_api_client_config "${API_CLIENT%%-*}"
        generate_storage_config "$service_name"
        
        # Add service-specific configurations
        local specific_config="${SERVICE_SPECIFIC_CONFIGS[$service_name]:-}"
        if [[ -n "$specific_config" ]]; then
            case "$specific_config" in
                "email_config") generate_email_config ;;
            esac
        fi
        
        generate_observability_config
    } > "$configmap_file"
    
    echo "Generated $configmap_file"
}

# =============================================================================
# SECRET GENERATION FUNCTIONS
# =============================================================================

generate_service_secret() {
    local service_name="$1"
    local namespace="$2"
    local secret_name="${service_name}-secret"
    local secret_file="$ENV_DIR/${service_name}-secret.yaml"
    
    echo "Creating $secret_file"

    # Generate base64 encoded passwords
    local db_password_b64=$(echo -n "root" | base64)
    local db_quartz_password_b64=$(echo -n "root" | base64)
    
    {
        cat << EOF
apiVersion: v1
kind: Secret
metadata:
  name: $secret_name
  namespace: $namespace
  labels:
    app: $service_name
    component: secret
type: Opaque
data:
  # Database passwords (base64 encoded)
  DB_PASSWORD: "$db_password_b64"
  DB_QUARTZ_PASSWORD: "$db_quartz_password_b64"
EOF
        
        # Add RabbitMQ password if messaging is RabbitMQ
        if [[ "$MESSAGING" == "rabbitmq" ]]; then
            local rabbitmq_password_b64=$(echo -n "admin" | base64)
            cat << EOF
  RABBITMQ_PASSWORD: "$rabbitmq_password_b64"
EOF
        fi
    } > "$secret_file"
    
    echo "Generated $secret_file"
}

generate_configmaps_and_secrets() {
    local namespace="$1"
    echo "Generating ConfigMaps and Secrets for each service..."

    # Create env directory if it doesn't exist
    if ! mkdir -p "$ENV_DIR"; then
        echo "Failed to create ConfigMap directory: $ENV_DIR" >&2
        exit 1
    fi

    # Remove existing ConfigMap and Secret files if force recreate is enabled
    if [[ "$FORCE_RECREATE" == true ]]; then
        if ! rm -f "$ENV_DIR/"*-configmap.yaml 2>/dev/null; then
            echo "Warning: Could not clean existing ConfigMap files"
        fi
        if ! rm -f "$ENV_DIR/"*-secret.yaml 2>/dev/null; then
            echo "Warning: Could not clean existing Secret files"
        fi
    fi

    echo "Creating service-specific ConfigMaps with configuration from app-stack.cfg"
    
    # Generate API Gateway ConfigMap (special case)
    if ! generate_api_gateway_configmap "$namespace"; then
        echo "Failed to generate API Gateway ConfigMap" >&2
        exit 1
    fi

    # Generate ConfigMaps and Secrets for other services
    local services=("user-service" "product-service" "order-service" "payment-service" "notification-service")
    for service in "${services[@]}"; do
        if ! generate_service_configmap "$service" "$namespace"; then
            echo "Failed to generate ConfigMap for service: $service" >&2
            exit 1
        fi
        
        # Generate service-specific secret
        if ! generate_service_secret "$service" "$namespace"; then
            echo "Failed to generate Secret for service: $service" >&2
            exit 1
        fi
    done
    
    # Verify all expected ConfigMap files were created
    local expected_configmap_files=("api-gateway-configmap.yaml" "user-service-configmap.yaml" "product-service-configmap.yaml" "order-service-configmap.yaml" "payment-service-configmap.yaml" "notification-service-configmap.yaml")
    for file in "${expected_configmap_files[@]}"; do
        if [[ ! -f "$ENV_DIR/$file" ]]; then
            echo "Error: Expected ConfigMap file not found: $ENV_DIR/$file" >&2
            exit 1
        fi
    done
    
    # Verify all expected Secret files were created
    local expected_secret_files=("user-service-secret.yaml" "product-service-secret.yaml" "order-service-secret.yaml" "payment-service-secret.yaml" "notification-service-secret.yaml")
    for file in "${expected_secret_files[@]}"; do
        if [[ ! -f "$ENV_DIR/$file" ]]; then
            echo "Error: Expected Secret file not found: $ENV_DIR/$file" >&2
            exit 1
        fi
    done
    
    echo "ConfigMaps and Secrets generated successfully (${#expected_configmap_files[@]} ConfigMaps, ${#expected_secret_files[@]} Secrets)"
    echo
}

apply_configmaps_and_secrets() {
    local namespace="$1"
    echo "Applying ConfigMaps and Secrets to Kubernetes..."
    
    # Apply all ConfigMap files
    local configmap_files=("$ENV_DIR/"*-configmap.yaml)
    if [[ ${#configmap_files[@]} -eq 0 ]]; then
        echo "No ConfigMap files found in $ENV_DIR/" >&2
        exit 1
    fi
    
    for configmap_file in "${configmap_files[@]}"; do
        if [[ -f "$configmap_file" ]]; then
            echo "Applying $(basename "$configmap_file")..."
            if ! kubectl apply -f "$configmap_file" -n "$namespace"; then
                echo "Failed to apply ConfigMap: $configmap_file" >&2
                exit 1
            fi
        fi
    done
    
    # Apply all Secret files
    local secret_files=("$ENV_DIR/"*-secret.yaml)
    if [[ ${#secret_files[@]} -eq 0 ]]; then
        echo "No Secret files found in $ENV_DIR/" >&2
        exit 1
    fi

    for secret_file in "${secret_files[@]}"; do
        if [[ -f "$secret_file" ]]; then
            echo "Applying $(basename "$secret_file")..."
            if ! kubectl apply -f "$secret_file" -n "$namespace"; then
                echo "Failed to apply Secret: $secret_file" >&2
                exit 1
            fi
        fi
    done
    
    echo "All ConfigMaps and Secrets applied successfully"
    echo
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    echo "Building services..."

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [[ ! -f "$build_script" ]]; then
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
    echo
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
    
    echo "Deploying ${category^} services..."
    
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
    echo "$category services deployed..."
    
    if ! WAIT_TIMEOUT="$timeout" wait_for_pods_ready "${services[@]}"; then
        echo "Some $category services failed to become ready" >&2
        exit 1
    fi

    echo "All $category services are ready"
    echo
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
# DEPLOYMENT FUNCTIONS
# =============================================================================

create_namespace() {
    echo "Creating namespace..."
    
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
    echo
}

apply_security_config() {
    echo "Applying security configurations..."
    if [[ -f "$BASE_DIR/application/security.yaml" ]]; then
        kubectl apply -f "$BASE_DIR/application/security.yaml" -n "$NAMESPACE"
        echo "Security configurations applied"
    else
        echo "Security configuration file not found, skipping..."
    fi
    echo
}

get_infrastructure_services() {
    local services=("redis")
    
    case "$DATASOURCE" in
        mysql) services+=("mysql") ;;
        postgres|postgresql) services+=("postgres") ;;
        *) 
            echo "Warning: Unknown datasource: $DATASOURCE, defaulting to mysql"
            services+=("mysql")
            ;;
    esac
    
    case "$MESSAGING" in
        kafka) services+=("kafka") ;;
        rabbitmq) services+=("rabbitmq") ;;
        *)
            echo "Warning: Unknown messaging system: $MESSAGING, defaulting to kafka"
            services+=("kafka")
            ;;
    esac

    # Object storage
    if [[ "$STORAGE" == "minio" ]]; then
        services+=("minio")
    fi
    
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
    local services=("user-service" "product-service" "order-service" "payment-service" "notification-service")
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
    # Deploy application services
    local application_services
    read -ra application_services <<< "$(get_application_services)"
    if [[ ${#application_services[@]} -gt 0 ]]; then
        deploy_service_category "application" "600s" "${application_services[@]}"
    fi

    # Deploy API Gateway last
    deploy_service_category "application" "300s" "api-gateway"
}

# =============================================================================
# INGRESS SETUP
# =============================================================================

setup_and_deploy_ingress() {
    echo "Setting up and deploying ingress..."

    local platform
    platform=$(detect_k8s_platform)
    echo "Detected platform: $platform"

    # Install NGINX Ingress Controller
    install_ingress_controller "$platform"

    # Deploy ingress
    deploy_ingress

    echo "Ingress setup completed"
    echo
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
    if [[ -f "$BASE_DIR/ingress/nginx-ingress.yaml" ]]; then
        kubectl apply -f "$BASE_DIR/ingress/nginx-ingress.yaml" -n "$NAMESPACE"
    else
        echo "Ingress configuration file not found, skipping..."
        return 0
    fi

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

    echo "=== Deployment Summary ==="
    echo "Atlas platform deployment completed successfully!"
    echo "Total execution time: ${minutes}m ${seconds}s"
    echo

    show_access_information
    show_management_commands
}

show_access_information() {
    echo "Access via Ingress (recommended):"
    echo "  API Gateway:   http://api.atlas.local"
    echo "  Grafana:       http://grafana.atlas.local (admin/admin)"
    echo "  Prometheus:    http://prometheus.atlas.local"
    echo "  Zipkin:        http://zipkin.atlas.local"
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        echo "  SMTP4Dev:      http://smtp4dev.atlas.local"
    fi
    echo

    echo "Alternative access via port-forwarding:"
    local port_forwards=(
        "API Gateway:   kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
        "Grafana:       kubectl port-forward -n $NAMESPACE svc/grafana 3000:3000"
        "Prometheus:    kubectl port-forward -n $NAMESPACE svc/prometheus 9090:9090"
        "Zipkin:        kubectl port-forward -n $NAMESPACE svc/zipkin 9411:9411"
    )
    if [[ "$NOTIFICATION_EMAIL" == "spring" ]]; then
        port_forwards+=("SMTP4Dev:      kubectl port-forward -n $NAMESPACE svc/smtp4dev 80:80")
    fi
    if [[ "$STORAGE" == "minio" ]]; then
        port_forwards+=("MinIO Console: kubectl port-forward -n $NAMESPACE svc/minio 9001:9001")
        port_forwards+=("MinIO S3 API:  kubectl port-forward -n $NAMESPACE svc/minio 9000:9000")
    fi
    for pf in "${port_forwards[@]}"; do
        echo "  $pf"
    done
    echo
}

show_management_commands() {
    echo "Management commands:"
    echo "  Status:        kubectl get pods -n $NAMESPACE"
    echo "  Services:      kubectl get services -n $NAMESPACE"
    echo "  ConfigMaps:    kubectl get configmaps -n $NAMESPACE"
    echo "  Logs:          kubectl logs -n $NAMESPACE deployment/[service-name] -f"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    parse_arguments "$@"
    check_prerequisites
    show_cluster_info

    local start_time=$(date +%s)

    echo "Atlas On-Premise Kubernetes Platform - Starting..."
    echo "Namespace: $NAMESPACE"
    echo

    # Build step (if not skipped)
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
    else
        echo "Skipping build step (--skip-build flag provided)"
    fi

    read_app_stack_config
    generate_configmaps_and_secrets "$NAMESPACE"

    create_namespace
    apply_configmaps_and_secrets "$NAMESPACE"
    apply_security_config
    deploy_infrastructure
    deploy_observability
    deploy_applications
    setup_and_deploy_ingress
    
    show_deployment_summary "$start_time"
}

# Execute main function
main "$@"
