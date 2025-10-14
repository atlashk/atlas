#!/bin/bash

# Atlas Backend AWS ECS Deployment Script
# This script automates the deployment of Atlas backend to AWS ECS using Terraform

set -e

# Configuration
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TERRAFORM_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas"
AWS_REGION="${AWS_REGION:-us-east-1}"

# Default options
SKIP_BUILD=false

check_java_version() {
    if ! command -v java &> /dev/null; then
        echo "ERROR: Java is not installed. Please install Java 17 or later." >&2
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
        echo "ERROR: Java version $java_version is not supported. Please install Java 17 or later." >&2
        return 1
    fi
    
    echo "Java found: $java_version"
    return 0
}

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "ERROR: Docker is not running. Please start Docker and try again." >&2
        return 1
    fi
    echo "Docker found and running"
    return 0
}

check_prerequisites() {
    echo "Checking prerequisites..."
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_version || exit 1
        check_docker || exit 1
    fi
    
    # Check if AWS CLI is installed
    if ! command -v aws &> /dev/null; then
        echo "ERROR: AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check if Terraform is installed
    if ! command -v terraform &> /dev/null; then
        echo "ERROR: Terraform is not installed. Please install it first."
        exit 1
    fi
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        echo "ERROR: AWS credentials not configured. Please run 'aws configure' first."
        exit 1
    fi
    
    echo "Prerequisites check passed"
}

read_app_stack_config() {
    echo "Reading application stack configuration..."
    
    local CONFIG_FILE="../../../app-stack.aws.cfg"
    local TFVARS_FILE="$TERRAFORM_DIR/terraform.tfvars"
    
    # Function to read configuration value
    read_config() {
        local key=$1
        local config_file=$2
        grep "^${key}=" "$config_file" | cut -d'=' -f2
    }
    
    # Check if config file exists
    if [ ! -f "$CONFIG_FILE" ]; then
        echo "ERROR: Configuration file $CONFIG_FILE not found!"
        exit 1
    fi
    
    # Read datasource configuration
    local DATASOURCE=$(read_config "datasource" "$CONFIG_FILE")
    
    echo "Detected datasource: $DATASOURCE"
    
    # Set database engine and related configurations based on datasource
    local DB_ENGINE DB_ENGINE_VERSION DB_PORT DB_PARAMETER_GROUP_FAMILY
    case "$DATASOURCE" in
        "mysql")
            DB_ENGINE="mysql"
            DB_ENGINE_VERSION="8.0"
            DB_PORT="3306"
            DB_PARAMETER_GROUP_FAMILY="mysql8.0"
            echo "Configuring for MySQL database"
            ;;
        "postgres")
            DB_ENGINE="postgres"
            DB_ENGINE_VERSION="15.4"
            DB_PORT="5432"
            DB_PARAMETER_GROUP_FAMILY="postgres15"
            echo "Configuring for PostgreSQL database"
            ;;
        *)
            echo "ERROR: Unsupported datasource '$DATASOURCE'. Supported: mysql, postgres"
            exit 1
            ;;
    esac
    
    # Read API client configuration
    local API_CLIENT=$(read_config "api-client" "$CONFIG_FILE")
    
    echo "Detected api-client: $API_CLIENT"
    
    # Set API client type and endpoints based on configuration
    local API_CLIENT_TYPE USER_SERVICE_ENDPOINT PRODUCT_SERVICE_ENDPOINT PAYMENT_SERVICE_ENDPOINT
    case "$API_CLIENT" in
        "rest-"*)
            API_CLIENT_TYPE="rest"
            USER_SERVICE_ENDPOINT="http://user-service.atlas.local:8081"
            PRODUCT_SERVICE_ENDPOINT="http://product-service.atlas.local:8082"
            ORDER_SERVICE_ENDPOINT="http://order-service.atlas.local:8083"
            PAYMENT_SERVICE_ENDPOINT="http://payment-service.atlas.local:8084"
            echo "Configuring for REST API client"
            ;;
        "grpc")
            API_CLIENT_TYPE="grpc"
            USER_SERVICE_ENDPOINT="static://user-service.atlas.local:50051"
            PRODUCT_SERVICE_ENDPOINT="static://product-service.atlas.local:50052"
            ORDER_SERVICE_ENDPOINT="static://order-service.atlas.local:50053"
            PAYMENT_SERVICE_ENDPOINT="static://payment-service.atlas.local:50054"
            echo "Configuring for gRPC API client"
            ;;
        *)
            echo "ERROR: Unsupported api-client '$API_CLIENT'. Supported: rest-*, grpc"
            exit 1
            ;;
    esac
    
    # Create or update terraform.tfvars with database and API client configuration
    if [ -f "$TFVARS_FILE" ]; then
        echo "Updating existing terraform.tfvars with database and API client configuration"
        # Remove existing database configuration lines
        sed -i '/^db_engine/d' "$TFVARS_FILE"
        sed -i '/^db_engine_version/d' "$TFVARS_FILE"
        sed -i '/^db_port/d' "$TFVARS_FILE"
        sed -i '/^db_parameter_group_family/d' "$TFVARS_FILE"
        # Remove existing API client configuration lines
        sed -i '/^api_client_type/d' "$TFVARS_FILE"
        sed -i '/^user_service_endpoint/d' "$TFVARS_FILE"
        sed -i '/^product_service_endpoint/d' "$TFVARS_FILE"
        sed -i '/^order_service_endpoint/d' "$TFVARS_FILE"
        sed -i '/^payment_service_endpoint/d' "$TFVARS_FILE"
    else
        echo "Creating new terraform.tfvars"
    fi
    
    # Append database and API client configuration
    cat >> "$TFVARS_FILE" << EOF

# Database Configuration (auto-generated from app-stack.aws.cfg)
db_engine                  = "$DB_ENGINE"
db_engine_version         = "$DB_ENGINE_VERSION"
db_port                   = $DB_PORT
db_parameter_group_family = "$DB_PARAMETER_GROUP_FAMILY"

# API Client Configuration (auto-generated from app-stack.aws.cfg)
api_client_type           = "$API_CLIENT_TYPE"
user_service_endpoint     = "$USER_SERVICE_ENDPOINT"
product_service_endpoint  = "$PRODUCT_SERVICE_ENDPOINT"
order_service_endpoint    = "$ORDER_SERVICE_ENDPOINT"
payment_service_endpoint  = "$PAYMENT_SERVICE_ENDPOINT"
EOF
    
    echo "Database configuration added to terraform.tfvars:"
    echo "  Engine: $DB_ENGINE"
    echo "  Version: $DB_ENGINE_VERSION"
    echo "  Port: $DB_PORT"
    echo "  Parameter Group Family: $DB_PARAMETER_GROUP_FAMILY"
    
    echo "API Client configuration added to terraform.tfvars:"
    echo "  Type: $API_CLIENT_TYPE"
    echo "  User Service Endpoint: $USER_SERVICE_ENDPOINT"
    echo "  Product Service Endpoint: $PRODUCT_SERVICE_ENDPOINT"
    echo "  Payment Service Endpoint: $PAYMENT_SERVICE_ENDPOINT"
}

check_terraform_vars() {
    echo "Checking Terraform variables..."
    
    if [ ! -f "$TERRAFORM_DIR/terraform.tfvars" ]; then
        echo "WARNING: terraform.tfvars not found. Creating from example..."
        cp "$TERRAFORM_DIR/terraform.tfvars.example" "$TERRAFORM_DIR/terraform.tfvars"
        echo "WARNING: Please edit terraform.tfvars with your specific values before continuing."
        echo "WARNING: Especially make sure to set a secure db_password!"
        read -p "Press Enter to continue after editing terraform.tfvars..."
    fi
}

create_ecr_repositories() {
    echo "Creating ECR repositories if they don't exist..."
    
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service" "eureka-server")
    
    for service in "${services[@]}"; do
        local repo_name="${PROJECT_NAME}/${service}"
        
        if ! aws ecr describe-repositories --repository-names "$repo_name" --region "$AWS_REGION" &> /dev/null; then
            echo "Creating ECR repository: $repo_name"
            aws ecr create-repository --repository-name "$repo_name" --region "$AWS_REGION" > /dev/null
            echo "Created ECR repository: $repo_name"
        else
            echo "ECR repository already exists: $repo_name"
        fi
    done
}

build_services() {
    echo "Building services..."

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [[ ! -f "$build_script" ]]; then
        echo "ERROR: Build script not found: $build_script" >&2
        exit 1
    fi

    echo "Granting execute permission to build script..."
    chmod +x "$build_script"

    echo "Invoking build script..."
    
    if "$build_script" --build-docker=true; then
        echo "Build completed successfully"
    else
        echo "ERROR: Build failed" >&2
        exit 1
    fi
    echo
}

push_images_to_ecr() {
    echo "Pushing Docker images to ECR..."
    
    # Get ECR login token
    echo "Logging in to ECR..."
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service" "eureka-server")
    
    for service in "${services[@]}"; do
        local local_image="${service}:latest"
        local ecr_repo="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}/${service}"
        local ecr_image="${ecr_repo}:latest"
        
        echo "Tagging and pushing $service..."
        
        # Tag the local image for ECR
        if docker tag "$local_image" "$ecr_image"; then
            echo "Tagged $local_image as $ecr_image"
        else
            echo "ERROR: Failed to tag $local_image" >&2
            exit 1
        fi
        
        # Push to ECR
        if docker push "$ecr_image"; then
            echo "Pushed $ecr_image successfully"
        else
            echo "ERROR: Failed to push $ecr_image" >&2
            exit 1
        fi
    done
    
    echo "All images pushed to ECR successfully"
    echo
}

terraform_init() {
    echo "Initializing Terraform..."
    cd "$TERRAFORM_DIR"
    terraform init
    echo "Terraform initialized"
}

terraform_plan() {
    echo "Planning Terraform deployment..."
    cd "$TERRAFORM_DIR"
    terraform plan -out=tfplan
    echo "Terraform plan completed"
}

terraform_apply() {
    echo "Applying Terraform configuration..."
    cd "$TERRAFORM_DIR"
    
    if [ -f "tfplan" ]; then
        terraform apply tfplan
    else
        terraform apply -auto-approve
    fi
    
    echo "Terraform apply completed"
}

show_outputs() {
    echo "Deployment outputs:"
    cd "$TERRAFORM_DIR"
    terraform output
}

get_aws_account_id() {
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    if [[ -z "$AWS_ACCOUNT_ID" ]]; then
        echo "ERROR: Failed to get AWS Account ID" >&2
        exit 1
    fi
    echo "AWS Account ID: $AWS_ACCOUNT_ID"
}

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas Backend AWS ECS Deployment Script"
    echo ""
    echo "This script builds and deploys Atlas backend to AWS ECS using Terraform."
    echo "To destroy/cleanup AWS resources, use the separate cleanup.sh script."
    echo ""
    echo "Options:"
    echo "  --skip-build        Skip all build steps (JAR, Docker images)"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                           # Deploy with build (default)"
    echo "  $0 --skip-build              # Deploy without building"
    echo ""
    echo "Related scripts:"
    echo "  ./cleanup.sh                 # Destroy all AWS resources"
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
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

main() {
    echo "Starting Atlas Backend AWS ECS Deployment"
    
    # Parse command line arguments
    parse_arguments "$@"
    
    # Always execute deploy workflow
    check_prerequisites
    get_aws_account_id
    read_app_stack_config
    check_terraform_vars
    create_ecr_repositories
    
    # Build and push images if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
        push_images_to_ecr
    else
        echo "Skipping build step (--skip-build flag provided)"
    fi
    
    terraform_init
    terraform_plan
    
    echo
    echo "WARNING: About to deploy Atlas Backend to AWS ECS"
    echo "WARNING: This will create AWS resources that may incur costs"
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        terraform_apply
        show_outputs
        echo "Deployment completed successfully!"
        echo "Your services will be available at the load balancer DNS shown above"
    else
        echo "Deployment cancelled"
        exit 0
    fi
}

# Run main function
main "$@"