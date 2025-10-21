#!/bin/bash

# Atlas Backend AWS ECS Deployment Script
# This script automates the deployment of Atlas backend to AWS ECS using Terraform

set -euo pipefail  # Exit on error, undefined vars, pipe failures

# Configuration
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TERRAFORM_DIR/../../../../.." && pwd)"
PROJECT_NAME="atlas"
AWS_REGION="${AWS_REGION:-us-east-1}"

# Default options
SKIP_BUILD=false

# Error handling
cleanup_on_error() {
    local exit_code=$?
    echo "ERROR: Script failed with exit code $exit_code" >&2
    
    # Clean up temporary files
    if [[ -f "$TERRAFORM_DIR/tfplan" ]]; then
        echo "Cleaning up Terraform plan file"
        rm -f "$TERRAFORM_DIR/tfplan"
    fi
    
    exit $exit_code
}

trap cleanup_on_error ERR

check_java_version() {
    echo "Checking Java version..."
    
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
    echo "Checking Docker..."
    
    if ! command -v docker &> /dev/null; then
        echo "ERROR: Docker is not installed. Please install Docker first." >&2
        return 1
    fi
    
    if ! docker info > /dev/null 2>&1; then
        echo "ERROR: Docker is not running. Please start Docker and try again." >&2
        return 1
    fi
    
    local docker_version
    docker_version=$(docker --version | cut -d' ' -f3 | cut -d',' -f1)
    echo "Docker found and running: $docker_version"
    return 0
}

check_aws_cli() {
    echo "Checking AWS CLI..."
    
    if ! command -v aws &> /dev/null; then
        echo "ERROR: AWS CLI is not installed. Please install it first." >&2
        return 1
    fi
    
    local aws_version
    aws_version=$(aws --version 2>&1 | cut -d' ' -f1 | cut -d'/' -f2)
    echo "AWS CLI found: $aws_version"
    
    # Check AWS credentials
    if ! aws sts get-caller-identity --no-cli-pager &> /dev/null; then
        echo "ERROR: AWS credentials not configured. Please run 'aws configure' first." >&2
        return 1
    fi
    
    echo "AWS credentials configured"
    return 0
}

check_terraform() {
    echo "Checking Terraform..."
    
    if ! command -v terraform &> /dev/null; then
        echo "ERROR: Terraform is not installed. Please install it first." >&2
        return 1
    fi
    
    local terraform_version
    terraform_version=$(terraform --version | head -n1 | cut -d'v' -f2)
    
    # Check minimum version (1.11)
    local min_version="1.11.0"
    if ! printf '%s\n%s\n' "$min_version" "$terraform_version" | sort -V -C; then
        echo "ERROR: Terraform version $terraform_version is too old. Please install version $min_version or later." >&2
        return 1
    fi
    
    echo "Terraform found: $terraform_version"
    return 0
}

check_prerequisites() {
    echo "Checking prerequisites..."
    
    # Always check core tools
    check_aws_cli || exit 1
    check_terraform || exit 1
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        check_java_version || exit 1
        check_docker || exit 1
    fi
    
    echo "All prerequisites check passed!"
}

read_app_stack_config() {
    echo "Reading application stack configuration..."
    
    local CONFIG_FILE="$PROJECT_ROOT/backend/app-stack.aws.cfg"
    local TFVARS_FILE="$TERRAFORM_DIR/terraform.tfvars"
    
    # Function to read configuration value
    read_config() {
        local key=$1
        local config_file=$2
        local value
        value=$(grep "^${key}=" "$config_file" 2>/dev/null | cut -d'=' -f2 | tr -d ' ')
        
        if [[ -z "$value" ]]; then
            echo "ERROR: Configuration key '$key' not found or empty in $config_file" >&2
            exit 1
        fi
        
        echo "$value"
    }
    
    # Check if config file exists
    if [[ ! -f "$CONFIG_FILE" ]]; then
        echo "ERROR: Configuration file $CONFIG_FILE not found!" >&2
        echo "Please ensure app-stack.aws.cfg exists in the project root"
        exit 1
    fi
    
    echo "Using configuration file: $CONFIG_FILE"
    
    # Read datasource configuration
    local DATASOURCE
    DATASOURCE=$(read_config "datasource" "$CONFIG_FILE")
    
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
            echo "ERROR: Unsupported datasource '$DATASOURCE'. Supported: mysql, postgres" >&2
            exit 1
            ;;
    esac
    
    # Read API client configuration
    local API_CLIENT
    API_CLIENT=$(read_config "api-client" "$CONFIG_FILE")
    
    echo "Detected api-client: $API_CLIENT"
    
    # Set API client type and endpoints based on configuration
    # Note: Endpoints will use AWS Cloud Map service discovery DNS names
    # The DNS names follow the pattern: service-name.namespace.local (e.g., user-service.atlas-dev.local)
    local API_CLIENT_TYPE USER_SERVICE_ENDPOINT PRODUCT_SERVICE_ENDPOINT ORDER_SERVICE_ENDPOINT PAYMENT_SERVICE_ENDPOINT
    
    # Determine the Cloud Map namespace based on project and environment
    # This should match the namespace created in Terraform: ${name_prefix}-${environment}.local
    local ENVIRONMENT
    ENVIRONMENT=$(grep "^environment" "$TFVARS_FILE" 2>/dev/null | cut -d'=' -f2 | tr -d ' "' || echo "dev")
    local CLOUDMAP_NAMESPACE="${PROJECT_NAME}-${ENVIRONMENT}.local"
    
    case "$API_CLIENT" in
        "rest-"*)
            API_CLIENT_TYPE="rest"
            # Use Cloud Map DNS names with HTTP protocol and standard ports
            USER_SERVICE_ENDPOINT="http://user-service.${CLOUDMAP_NAMESPACE}:8081"
            PRODUCT_SERVICE_ENDPOINT="http://product-service.${CLOUDMAP_NAMESPACE}:8082"
            ORDER_SERVICE_ENDPOINT="http://order-service.${CLOUDMAP_NAMESPACE}:8083"
            PAYMENT_SERVICE_ENDPOINT="http://payment-service.${CLOUDMAP_NAMESPACE}:8084"
            echo "Configuring for REST API client with AWS Cloud Map service discovery"
            echo "  Cloud Map namespace: $CLOUDMAP_NAMESPACE"
            ;;
        "grpc")
            API_CLIENT_TYPE="grpc"
            # Use Cloud Map DNS names for gRPC services with gRPC ports
            USER_SERVICE_ENDPOINT="static://user-service.${CLOUDMAP_NAMESPACE}:50051"
            PRODUCT_SERVICE_ENDPOINT="static://product-service.${CLOUDMAP_NAMESPACE}:50052"
            ORDER_SERVICE_ENDPOINT="static://order-service.${CLOUDMAP_NAMESPACE}:50053"
            PAYMENT_SERVICE_ENDPOINT="static://payment-service.${CLOUDMAP_NAMESPACE}:50054"
            echo "Configuring for gRPC API client with AWS Cloud Map service discovery"
            echo "  Cloud Map namespace: $CLOUDMAP_NAMESPACE"
            ;;
        *)
            echo "ERROR: Unsupported api-client '$API_CLIENT'. Supported: rest-*, grpc" >&2
            exit 1
            ;;
    esac
    
    # Create or update terraform.tfvars with database and API client configuration
    if [[ -f "$TFVARS_FILE" ]]; then
        echo "Updating existing terraform.tfvars with database and API client configuration"
        
        # Remove existing auto-generated configuration sections
        sed -i '/^# Database Configuration (auto-generated from app-stack.aws.cfg)/,/^$/d' "$TFVARS_FILE"
        sed -i '/^# API Client Configuration (auto-generated from app-stack.aws.cfg)/,/^$/d' "$TFVARS_FILE"
        
        # Remove any orphaned configuration lines
        sed -i '/^db_engine[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^db_engine_version[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^db_port[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^db_parameter_group_family[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^api_client_type[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^user_service_endpoint[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^product_service_endpoint[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^order_service_endpoint[[:space:]]*=/d' "$TFVARS_FILE"
        sed -i '/^payment_service_endpoint[[:space:]]*=/d' "$TFVARS_FILE"
    else
        echo "Creating new terraform.tfvars from template"
        if [[ -f "$TERRAFORM_DIR/terraform.tfvars.example" ]]; then
            cp "$TERRAFORM_DIR/terraform.tfvars.example" "$TFVARS_FILE"
            echo "Copied from terraform.tfvars.example"
        fi
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
    
    # Check for terraform.tfvars in the terraform directory
    local TFVARS_FILE="$TERRAFORM_DIR/terraform.tfvars"
    echo "Looking for terraform.tfvars file: $TFVARS_FILE"
    
    # Verify the terraform.tfvars file exists and is readable
    if [[ ! -f "$TFVARS_FILE" ]]; then
        echo "ERROR: terraform.tfvars file not found: $TFVARS_FILE" >&2
        echo "Please create terraform.tfvars file manually in the terraform directory" >&2
        echo "You can use terraform.tfvars.example as a template if available" >&2
        exit 1
    fi
    
    if [[ ! -r "$TFVARS_FILE" ]]; then
        echo "ERROR: terraform.tfvars file is not readable: $TFVARS_FILE" >&2
        exit 1
    fi
    
    echo "terraform.tfvars file validation passed"
}

create_ecr_repositories() {
    echo "Creating ECR repositories if they don't exist..."
    
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service")
    local created_count=0
    local existing_count=0
    
    for service in "${services[@]}"; do
        local repo_name="${PROJECT_NAME}/${service}"
        
        echo "Checking ECR repository: $repo_name"
        
        # Use a more compatible way to check repository existence
        if aws ecr describe-repositories --repository-names "$repo_name" --region "$AWS_REGION" --no-cli-pager >/dev/null 2>&1; then
            echo "ECR repository already exists: $repo_name"
            existing_count=$((existing_count + 1))
        else
            echo "Creating ECR repository: $repo_name"
            
            if aws ecr create-repository --repository-name "$repo_name" --region "$AWS_REGION" --no-cli-pager >/dev/null 2>&1; then
                echo "Created ECR repository: $repo_name"
                created_count=$((created_count + 1))
            else
                echo "ERROR: Failed to create ECR repository: $repo_name" >&2
                exit 1
            fi
        fi
    done
    
    echo "ECR repositories ready: $created_count created, $existing_count existing"
}

build_services() {
    echo "Building services..."

    local build_script="$PROJECT_ROOT/backend/scripts/buildSrc/build.sh"
    if [[ ! -f "$build_script" ]]; then
        echo "ERROR: Build script not found: $build_script" >&2
        echo "Expected location: $build_script"
        exit 1
    fi

    echo "Granting execute permission to build script..."
    chmod +x "$build_script"

    echo "Invoking build script with Docker build enabled..."
    echo "Build script: $build_script"
    
    if "$build_script" --build-docker=true; then
        echo "Build completed successfully"
    else
        echo "ERROR: Build failed" >&2
        echo "Check the build output above for specific errors"
        exit 1
    fi
}

push_images_to_ecr() {
    echo "Pushing Docker images to ECR..."
    
    # Get ECR login token
    echo "Logging in to ECR..."
    if ! aws ecr get-login-password --region "$AWS_REGION" --no-cli-pager | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"; then
        echo "ERROR: Failed to login to ECR" >&2
        exit 1
    fi
    echo "Successfully logged in to ECR"
    
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service")
    local pushed_count=0
    local total_services=${#services[@]}
    
    for service in "${services[@]}"; do
        local local_image="${PROJECT_NAME}-${service}:latest"
        local ecr_repo="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}/${service}"
        local ecr_image="${ecr_repo}:latest"

        echo "Processing $service ($(($pushed_count + 1))/$total_services)..."

        # Check if local image exists
        if ! docker image inspect "$local_image" &> /dev/null; then
            echo "ERROR: Local image not found: $local_image" >&2
            echo "Make sure the build completed successfully"
            exit 1
        fi

        # Tag the local image for ECR
        echo "Tagging $local_image as $ecr_image"
        if docker tag "$local_image" "$ecr_image"; then
            echo "Tagged $local_image as $ecr_image"
        else
            echo "ERROR: Failed to tag $local_image" >&2
            exit 1
        fi

        # Push to ECR
        echo "Pushing $ecr_image..."
        if docker push "$ecr_image"; then
            echo "Pushed $service successfully"
            ((pushed_count++))
        else
            echo "ERROR: Failed to push $ecr_image" >&2
            exit 1
        fi
    done

    echo "All $pushed_count images pushed to ECR successfully"
}

terraform_init() {
    echo "Initializing Terraform..."
    cd "$TERRAFORM_DIR" || {
        echo "ERROR: Failed to change to Terraform directory: $TERRAFORM_DIR" >&2
        exit 1
    }
    
    echo "Working directory: $(pwd)"
    echo "Running: terraform init"

    if terraform init; then
        echo "Terraform initialization completed successfully"
    else
        echo "ERROR: Terraform initialization failed" >&2
        echo "Common solutions:"
        echo "  - Check your AWS credentials and permissions"
        echo "  - Verify the backend configuration in main.tf"
        echo "  - Ensure the S3 bucket for state exists and is accessible"
        exit 1
    fi
}

terraform_plan() {
    echo "Running Terraform plan..."
    cd "$TERRAFORM_DIR" || {
        echo "ERROR: Failed to change to Terraform directory: $TERRAFORM_DIR" >&2
        exit 1
    }
    
    local terraform_cmd="terraform plan -var-file=\"terraform.tfvars\""
    
    echo "Working directory: $(pwd)"
    echo "Running: $terraform_cmd"
    
    if terraform plan -var-file="terraform.tfvars"; then
        echo "Terraform plan completed successfully"
        echo "Review the plan above to understand what resources will be created/modified"
    else
        echo "ERROR: Terraform plan failed" >&2
        echo "Common solutions:"
        echo "  - Check terraform.tfvars for correct variable values"
        echo "  - Verify AWS credentials and permissions"
        echo "  - Ensure all required variables are set"
        exit 1
    fi
}

terraform_apply() {
    echo "Applying Terraform configuration..."
    cd "$TERRAFORM_DIR" || {
        echo "ERROR: Failed to change to Terraform directory: $TERRAFORM_DIR" >&2
        exit 1
    }
    
    local terraform_cmd="terraform apply -auto-approve -var-file=\"terraform.tfvars\""
    
    echo "Working directory: $(pwd)"
    echo "Running: $terraform_cmd"
    
    local start_time=$(date +%s)
    
    if terraform apply -auto-approve -var-file="terraform.tfvars"; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        echo "Terraform apply completed successfully in ${duration}s"
    else
        echo "ERROR: Terraform apply failed" >&2
        echo "Common solutions:"
        echo "  - Check the error messages above for specific issues"
        echo "  - Verify AWS service limits and quotas"
        echo "  - Check for resource naming conflicts"
        echo "  - Run 'terraform plan' to identify issues"
        exit 1
    fi
}

show_outputs() {
    echo "Retrieving deployment outputs..."
    cd "$TERRAFORM_DIR" || {
        echo "ERROR: Failed to change to Terraform directory: $TERRAFORM_DIR" >&2
        exit 1
    }
    
    if terraform output > /dev/null 2>&1; then
        echo
        echo "=== DEPLOYMENT OUTPUTS ==="
        terraform output
        echo "=========================="
        echo
    else
        echo "WARNING: No outputs available or Terraform state not found"
        echo "This is normal for a fresh deployment or if no outputs are defined"
    fi
}

get_aws_account_id() {
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text --no-cli-pager)
    if [[ -z "$AWS_ACCOUNT_ID" ]]; then
        echo "ERROR: Failed to get AWS Account ID" >&2
        exit 1
    fi
    echo "AWS Account ID: $AWS_ACCOUNT_ID"
}

show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Atlas Backend AWS ECS Deployment Script

This script automates the complete deployment of Atlas backend to AWS ECS using Terraform.
It handles building Docker images, pushing to ECR, and deploying infrastructure.

OPTIONS:
  --skip-build        Skip all build steps (JAR compilation, Docker images)
  --dry-run          Perform Terraform plan only, do not apply changes
  -h, --help         Show this help message and exit

EXAMPLES:
  $0                           # Deploy using terraform.tfvars
  $0 --skip-build              # Deploy without building
  $0 --dry-run                 # Plan deployment without applying

PREREQUISITES:
  - AWS CLI v2.x configured with credentials
  - Terraform >= 1.11.0
  - Docker (if not using --skip-build)
  - Java 17+ (if not using --skip-build)
  - app-stack.aws.cfg in project root
  - terraform.tfvars file in terraform directory (must be created manually)

CONFIGURATION:
  The script reads configuration from:
  - app-stack.aws.cfg to determine database engine and API client type
  - terraform.tfvars for deployment variables (must be created manually)

RELATED SCRIPTS:
  ./cleanup.sh                 # Destroy all AWS resources
  ./bootstrap/deploy.sh        # Setup Terraform state backend

For more information, see README.md
EOF
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-build)
                SKIP_BUILD=true
                echo "Build steps will be skipped"
                shift
                ;;

            -h|--help)
                show_help
                exit 0
                ;;
            *)
                echo "ERROR: Unknown option: $1" >&2
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
    
    check_prerequisites
    get_aws_account_id
    read_app_stack_config
    check_terraform_vars
    
    # Create ECR repositories
    create_ecr_repositories
    
    # Build and push images if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
        push_images_to_ecr
    else
        echo "WARNING: Skipping build step (--skip-build flag provided)"
        echo "Assuming Docker images already exist in ECR"
    fi

    # Terraform workflow
    terraform_init
    terraform_plan

    # Confirmation for actual deployment
    echo
    echo "WARNING: About to deploy Atlas Backend to AWS ECS"
    echo "WARNING: This will create AWS resources that may incur costs"
    echo "Estimated monthly cost: \$50-200 depending on usage"
    
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Proceeding with deployment..."
        terraform_apply
        show_outputs
        
        echo
        echo "Deployment completed successfully!"
        echo "Your services will be available at the load balancer DNS shown above"
        echo "It may take 5-10 minutes for all services to become healthy"
        
        # Show next steps
        echo
        echo "Next steps:"
        echo "1. Check service health: aws ecs describe-services --cluster <cluster-name>"
        echo "2. View logs: aws logs tail /ecs/<service-log-group> --follow"
        echo "3. Test endpoints using the ALB DNS name shown above"
    else
        echo "Deployment cancelled by user"
        exit 0
    fi
}

# Run main function
main "$@"