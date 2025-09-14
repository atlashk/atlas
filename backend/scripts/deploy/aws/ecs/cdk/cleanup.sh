#!/bin/bash

# =============================================================================
# Atlas CDK Cleanup Script
# =============================================================================
# This script destroys all Atlas CDK stacks and cleans up resources
# =============================================================================

set -euo pipefail

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../.." && pwd)"

# Default options
ENVIRONMENT="dev"
REGION="us-east-1"
PROFILE="default"

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    log_section "Atlas CDK Cleanup Script"
    log_info ""
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas CDK Cleanup Script - Destroys all Atlas CDK stacks and cleans up resources"
    log_info ""
    log_info "This script will:"
    log_info "  - Destroy service stacks (api-gateway)"
    log_info "  - Destroy infrastructure stack (VPC, RDS, ElastiCache, ECS, etc.)"
    log_info "  - Clean up ECR repositories (optional)"
    log_info ""
    log_info "Options:"
    log_info "  --env ENVIRONMENT   Target environment (default: dev)"
    log_info "  --region REGION     AWS region (default: us-east-1)"
    log_info "  --profile PROFILE   AWS profile (default: default)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                                    # Cleanup dev environment"
    log_info "  $0 --env stg --region us-west-2      # Cleanup staging"
    log_info ""
}

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --env)
                if [[ -n "${2:-}" && ! "$2" =~ ^-- ]]; then
                    ENVIRONMENT="$2"
                    shift 2
                else
                    log_error "--env requires an environment value"
                    exit 1
                fi
                ;;
            --region)
                if [[ -n "${2:-}" && ! "$2" =~ ^-- ]]; then
                    REGION="$2"
                    shift 2
                else
                    log_error "--region requires a region value"
                    exit 1
                fi
                ;;
            --profile)
                if [[ -n "${2:-}" && ! "$2" =~ ^-- ]]; then
                    PROFILE="$2"
                    shift 2
                else
                    log_error "--profile requires a profile value"
                    exit 1
                fi
                ;;
            *)
                log_error "Unknown option: $1"
                log_info "Use --help for usage information"
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# CHECK PRE-REQUISITES
# =============================================================================

check_prerequisites() {
    log_section "Checking Prerequisites"

    # Check AWS CLI
    if command -v aws &> /dev/null; then
        log_success "AWS CLI found"
    else
        log_error "AWS CLI is not installed"
        exit 1
    fi

    # Check Node.js and npm
    if command -v node &> /dev/null && command -v npm &> /dev/null; then
        log_success "Node.js and npm found"
    else
        log_error "Node.js and npm are required for CDK"
        exit 1
    fi

    # Check CDK
    if command -v cdk &> /dev/null; then
        log_success "AWS CDK found"
    else
        log_error "AWS CDK is not installed. Please install CDK globally: npm install -g aws-cdk"
        exit 1
    fi

    # Verify AWS credentials
    if aws sts get-caller-identity --profile ${PROFILE} &> /dev/null; then
        log_success "AWS credentials verified"
    else
        log_error "AWS credentials not configured for profile: ${PROFILE}"
        exit 1
    fi

    log_success "Prerequisites check passed"
}

# =============================================================================
# CLEANUP FUNCTIONS
# =============================================================================

destroy_service_stacks() {
    log_section "Destroying Service Stacks"
    
    cd "$SCRIPT_DIR"
    
    # Get AWS account ID
    local account_id
    account_id=$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)
    
    # Destroy API Gateway Stack
    log_info "Destroying api-gateway stack..."
    if aws cloudformation describe-stacks --profile ${PROFILE} --region ${REGION} --stack-name atlas-api-gateway-${ENVIRONMENT} &>/dev/null; then
        cdk destroy atlas-api-gateway-${ENVIRONMENT} \
            --profile ${PROFILE} \
            --context environment=${ENVIRONMENT} \
            --context region=${REGION} \
            --context account=${account_id} \
            --force
        log_success "API Gateway stack destroyed"
    else
        log_info "API Gateway stack does not exist"
    fi
}

destroy_infrastructure_stack() {
    log_section "Destroying Infrastructure Stack"
    
    cd "$SCRIPT_DIR"
    
    # Get AWS account ID
    local account_id
    account_id=$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)
    
    log_info "Destroying infrastructure stack..."
    if aws cloudformation describe-stacks --profile ${PROFILE} --region ${REGION} --stack-name atlas-infrastructure-${ENVIRONMENT} &>/dev/null; then
        cdk destroy atlas-infrastructure-${ENVIRONMENT} \
            --profile ${PROFILE} \
            --context environment=${ENVIRONMENT} \
            --context region=${REGION} \
            --context account=${account_id} \
            --force
        log_success "Infrastructure stack destroyed"
    else
        log_info "Infrastructure stack does not exist"
    fi
}

cleanup_ecr_repositories() {
    log_section "Cleaning up ECR Repositories"
    
    local services=("api-gateway")
    
    for service in "${services[@]}"; do
        local repo_name="atlas-${service}"
        
        if aws ecr describe-repositories --profile ${PROFILE} --region ${REGION} --repository-names ${repo_name} &>/dev/null; then
            log_info "Deleting ECR repository: ${repo_name}"
            
            # Delete all images first
            aws ecr batch-delete-image \
                --profile ${PROFILE} \
                --region ${REGION} \
                --repository-name ${repo_name} \
                --image-ids "$(aws ecr list-images --profile ${PROFILE} --region ${REGION} --repository-name ${repo_name} --query 'imageIds[*]' --output json)" \
                &>/dev/null || true
            
            # Delete repository
            aws ecr delete-repository \
                --profile ${PROFILE} \
                --region ${REGION} \
                --repository-name ${repo_name} \
                --force
            
            log_success "ECR repository ${repo_name} deleted"
        else
            log_info "ECR repository ${repo_name} does not exist"
        fi
    done
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    parse_arguments "$@"
    check_prerequisites

    log_section "Atlas CDK Cleanup"
    log_info "Environment: $ENVIRONMENT"
    log_info "Region: $REGION"
    log_info "Profile: $PROFILE"

    destroy_service_stacks
    destroy_infrastructure_stack
    cleanup_ecr_repositories
    
    log_section "Cleanup Complete"
    log_success "Atlas CDK resources have been successfully cleaned up!"
    log_info ""
    log_info "All stacks and ECR repositories have been destroyed."
    log_info ""
}

# Execute main function
main "$@"
