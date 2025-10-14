#!/bin/bash

# =============================================================================
# Atlas AWS ECS Terraform Cleanup Script
# =============================================================================
# This script safely removes ONLY Atlas-related AWS resources
# =============================================================================

set -euo pipefail

# Project configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
PROJECT_NAME="atlas"
AWS_REGION="${AWS_REGION:-us-east-1}"
TERRAFORM_DIR="$SCRIPT_DIR"

# Configuration variables (populated by read_app_stack_config)
declare -g AWS_ACCOUNT_ID

# =============================================================================
# CONFIGURATION FUNCTIONS
# =============================================================================

read_config_value() {
    local key="$1"
    local default_value="$2"
    local config_file="$PROJECT_ROOT/backend/app-stack.aws.cfg"
    local value
    
    if [[ -f "$config_file" ]]; then
        value=$(grep "^${key}=" "$config_file" 2>/dev/null | cut -d'=' -f2 | tr -d '[:space:]')
    fi
    echo "${value:-$default_value}"
}

read_app_stack_config() {
    echo "Reading application stack configuration..."
    
    local config_file="$PROJECT_ROOT/backend/app-stack.aws.cfg"
    
    if [[ ! -f "$config_file" ]]; then
        echo "Configuration file not found: $config_file" >&2
        echo "Using default configuration values"
        return 0
    fi
    
    if [[ ! -r "$config_file" ]]; then
        echo "Configuration file is not readable: $config_file" >&2
        return 1
    fi
    
    echo "Configuration loaded from: $config_file"
    echo
}

# =============================================================================
# AWS RESOURCE DETERMINATION
# =============================================================================

get_atlas_services() {
    local services=("api-gateway" "user-service" "product-service" "order-service" "payment-service" "eureka-server")
    echo "${services[@]}"
}

get_ecr_repositories() {
    local services
    read -ra services <<< "$(get_atlas_services)"
    
    local repositories=()
    for service in "${services[@]}"; do
        repositories+=("${PROJECT_NAME}/${service}")
    done
    
    echo "${repositories[@]}"
}

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Atlas AWS ECS Terraform Cleanup Script - Removes all Atlas-related AWS resources"
    echo ""
    echo "This script removes the following AWS resources:"
    echo "  - ECS Services and Tasks"
    echo "  - ECS Cluster"
    echo "  - Application Load Balancer and Target Groups"
    echo "  - RDS Database Instance"
    echo "  - ElastiCache Redis Cluster"
    echo "  - VPC, Subnets, Security Groups, and Networking components"
    echo "  - ECR Repositories and Docker images"
    echo "  - CloudWatch Log Groups"
    echo "  - IAM Roles and Policies"
    echo ""
    echo "The script uses Terraform to destroy infrastructure and AWS CLI to clean up ECR repositories."
    echo ""
    echo "Options:"
    echo "  --keep-ecr              Keep ECR repositories and images (only destroy Terraform resources)"
    echo "  --force                 Skip confirmation prompts (use with caution)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                      # Clean all Atlas AWS resources with confirmation"
    echo "  $0 --keep-ecr           # Destroy infrastructure but keep ECR repositories"
    echo "  $0 --force              # Clean all resources without confirmation"
    echo ""
    echo "⚠️  WARNING: This operation is DESTRUCTIVE and will delete Atlas AWS resources!"
    echo "⚠️  This may result in data loss and cannot be undone!"
    echo "Other AWS resources in your account will be preserved."
}

parse_arguments() {
    KEEP_ECR=false
    FORCE=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --keep-ecr)
                KEEP_ECR=true
                shift
                ;;
            --force)
                FORCE=true
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

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        echo "ERROR: AWS CLI is not installed. Please install it first." >&2
        return 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        echo "ERROR: AWS credentials not configured. Please run 'aws configure' first." >&2
        return 1
    fi
    
    echo "AWS CLI found and configured"
    return 0
}

check_terraform() {
    if ! command -v terraform &> /dev/null; then
        echo "ERROR: Terraform is not installed. Please install it first." >&2
        return 1
    fi
    
    echo "Terraform found"
    return 0
}

check_prerequisites() {
    echo "Checking prerequisites..."
    
    check_aws_cli || exit 1
    check_terraform || exit 1
    
    echo "Prerequisites check passed"
    echo
}

# =============================================================================
# AWS RESOURCE FUNCTIONS
# =============================================================================

get_aws_account_id() {
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    if [[ -z "$AWS_ACCOUNT_ID" ]]; then
        echo "ERROR: Failed to get AWS Account ID" >&2
        exit 1
    fi
    echo "AWS Account ID: $AWS_ACCOUNT_ID"
}

# =============================================================================
# CLEANUP FUNCTIONS
# =============================================================================

destroy_terraform_resources() {
    echo "Destroying Terraform-managed AWS resources..."
    
    cd "$TERRAFORM_DIR"
    
    if [[ ! -f "terraform.tfstate" ]] && [[ ! -f ".terraform/terraform.tfstate" ]]; then
        echo "No Terraform state found. Checking if resources exist..."
        
        # Initialize Terraform to check for remote state
        if terraform init > /dev/null 2>&1; then
            echo "Terraform initialized successfully"
        else
            echo "WARNING: Failed to initialize Terraform. No resources to destroy."
            return 0
        fi
    fi
    
    echo "Planning Terraform destroy..."
    if terraform plan -destroy -out=destroy.tfplan; then
        echo "Terraform destroy plan created successfully"
        
        if [[ "$FORCE" == true ]]; then
            echo "Force mode: Applying destroy plan without confirmation..."
            terraform apply destroy.tfplan
        else
            echo ""
            echo "WARNING: About to destroy all Atlas AWS infrastructure resources"
            echo "This includes ECS services, RDS database, VPC, and all associated resources"
            read -p "Are you sure you want to continue? (y/N): " -n 1 -r
            echo
            
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                terraform apply destroy.tfplan
                echo "Terraform resources destroyed successfully"
            else
                echo "Terraform destroy cancelled"
                rm -f destroy.tfplan
                return 1
            fi
        fi
        
        # Clean up plan file
        rm -f destroy.tfplan
    else
        echo "ERROR: Failed to create Terraform destroy plan" >&2
        return 1
    fi
    
    echo "Terraform resources destroyed successfully!"
    echo
}

remove_ecr_repositories() {
    if [[ "$KEEP_ECR" == true ]]; then
        echo "Skipping ECR repository cleanup (--keep-ecr flag provided)"
        return 0
    fi
    
    echo "Removing ECR repositories and images..."
    
    local repositories
    read -ra repositories <<< "$(get_ecr_repositories)"
    
    for repo in "${repositories[@]}"; do
        echo "Checking ECR repository: $repo"
        
        if aws ecr describe-repositories --repository-names "$repo" --region "$AWS_REGION" &> /dev/null; then
            echo "Found ECR repository: $repo"
            
            # List images in repository
            local images
            images=$(aws ecr list-images --repository-name "$repo" --region "$AWS_REGION" --query 'imageIds[*]' --output text 2>/dev/null || true)
            
            if [[ -n "$images" ]]; then
                echo "  - Deleting all images in repository: $repo"
                aws ecr batch-delete-image --repository-name "$repo" --region "$AWS_REGION" --image-ids "$(aws ecr list-images --repository-name "$repo" --region "$AWS_REGION" --query 'imageIds')" > /dev/null 2>&1 || true
            fi
            
            echo "  - Deleting repository: $repo"
            if aws ecr delete-repository --repository-name "$repo" --region "$AWS_REGION" --force > /dev/null 2>&1; then
                echo "  ✓ Deleted ECR repository: $repo"
            else
                echo "  ✗ Failed to delete ECR repository: $repo"
            fi
        else
            echo "  - Repository not found: $repo"
        fi
    done
    
    echo "ECR repositories cleanup completed!"
    echo
}

remove_cloudwatch_logs() {
    echo "Removing CloudWatch Log Groups..."
    
    local log_groups
    log_groups=$(aws logs describe-log-groups --region "$AWS_REGION" --query "logGroups[?starts_with(logGroupName, '/aws/ecs/${PROJECT_NAME}')].logGroupName" --output text 2>/dev/null || true)
    
    if [[ -n "$log_groups" ]]; then
        echo "Found CloudWatch Log Groups to remove:"
        for log_group in $log_groups; do
            echo "  - $log_group"
        done
        
        for log_group in $log_groups; do
            echo "Deleting log group: $log_group"
            aws logs delete-log-group --log-group-name "$log_group" --region "$AWS_REGION" 2>/dev/null || true
        done
    else
        echo "No Atlas CloudWatch Log Groups found"
    fi
    
    echo "CloudWatch Log Groups cleanup completed!"
    echo
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    parse_arguments "$@"
    
    check_prerequisites
    
    read_app_stack_config
    
    get_aws_account_id
    
    echo "=== Atlas AWS ECS Terraform - Cleanup ==="
    echo "AWS Region: $AWS_REGION"
    echo "AWS Account: $AWS_ACCOUNT_ID"
    echo "This script will remove ALL Atlas-related AWS resources:"
    echo "  ✓ ECS Services, Tasks, and Cluster"
    echo "  ✓ Application Load Balancer and Target Groups"
    echo "  ✓ RDS Database Instance (⚠️  DATA LOSS)"
    echo "  ✓ ElastiCache Redis Cluster"
    echo "  ✓ VPC, Subnets, Security Groups"
    echo "  ✓ IAM Roles and Policies"
    if [[ "$KEEP_ECR" == false ]]; then
        echo "  ✓ ECR Repositories and Docker Images"
    else
        echo "  ⏭ ECR Repositories (preserved)"
    fi
    echo "  ✓ CloudWatch Log Groups"
    echo ""
    echo "Other AWS resources in your account will be preserved."
    echo ""
    
    if [[ "$FORCE" == false ]]; then
        echo "⚠️  WARNING: This operation is DESTRUCTIVE and cannot be undone!"
        echo "⚠️  You will lose all data in the Atlas database and other resources!"
        read -p "Do you want to continue with the cleanup? (y/N): " -n 1 -r
        echo
        
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Cleanup cancelled by user"
            exit 0
        fi
    fi
    
    # Execute cleanup operations
    destroy_terraform_resources
    remove_ecr_repositories
    remove_cloudwatch_logs
    
    echo "=== Cleanup Summary ==="
    echo "✓ Terraform infrastructure destroyed"
    if [[ "$KEEP_ECR" == false ]]; then
        echo "✓ ECR repositories removed"
    else
        echo "⏭ ECR repositories preserved"
    fi
    echo "✓ CloudWatch log groups removed"
    echo ""
    echo "All Atlas AWS resources have been successfully removed!"
    echo "Atlas AWS cleanup completed!"
}

# Execute main function
main "$@"