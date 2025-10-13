#!/bin/bash

# Atlas Backend AWS ECS Deployment Script
# This script automates the deployment of Atlas backend to AWS ECS using Terraform

set -e

# Configuration
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="atlas"
AWS_REGION="${AWS_REGION:-us-west-2}"

check_prerequisites() {
    echo "Checking prerequisites..."
    
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
    
    CONFIG_FILE="../../../app-stack.aws.cfg"
    
    if [ ! -f "$CONFIG_FILE" ]; then
        echo "ERROR: Configuration file $CONFIG_FILE not found!"
        exit 1
    fi
    
    # Run the configuration reading script
    if [ -f "$TERRAFORM_DIR/read-config.sh" ]; then
        echo "Updating database configuration based on app-stack.aws.cfg..."
        bash "$TERRAFORM_DIR/read-config.sh"
    else
        echo "WARNING: read-config.sh not found. Using default database configuration."
    fi
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
    
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service")
    
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

main() {
    echo "Starting Atlas Backend AWS ECS Deployment"
    
    # Parse command line arguments
    case "${1:-deploy}" in
        "init")
            check_prerequisites
            check_terraform_vars
            terraform_init
            ;;
        "plan")
            check_prerequisites
            terraform_plan
            ;;
        "apply")
            check_prerequisites
            terraform_apply
            show_outputs
            ;;
        "deploy")
            check_prerequisites
            read_app_stack_config
            check_terraform_vars
            create_ecr_repositories
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
            ;;
        "destroy")
            echo "WARNING: About to destroy all Atlas Backend AWS resources"
            read -p "Are you sure? This cannot be undone! (y/N): " -n 1 -r
            echo
            
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                cd "$TERRAFORM_DIR"
                terraform destroy
                echo "Resources destroyed"
            else
                echo "Destroy cancelled"
            fi
            ;;
        "outputs")
            show_outputs
            ;;
        *)
            echo "Usage: $0 {init|plan|apply|deploy|destroy|outputs}"
            echo
            echo "Commands:"
            echo "  init     - Initialize Terraform"
            echo "  plan     - Plan Terraform changes"
            echo "  apply    - Apply Terraform changes"
            echo "  deploy   - Full deployment (init + plan + apply)"
            echo "  destroy  - Destroy all resources"
            echo "  outputs  - Show deployment outputs"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"