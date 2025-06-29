#!/bin/bash

# =============================================================================
# Atlas CDK Deployment Script
# =============================================================================
# This script builds and deploys the Atlas microservices to AWS ECS using CDK
# =============================================================================

set -euo pipefail

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../.." && pwd)"

# Source logger
source "$PROJECT_ROOT/backend/scripts/log/logger.sh"

# Default options
ENVIRONMENT="dev"
REGION="us-east-1"
PROFILE="default"
SKIP_BUILD=false
BOOTSTRAP=false
FORCE_REDEPLOY=false

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

show_help() {
    log_section "Atlas CDK Deployment Script"
    log_info ""
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas CDK Deployment Script - Builds and deploys Atlas microservices to AWS ECS using CDK"
    log_info ""
    log_info "This script automatically:"
    log_info "  - Builds backend services and Docker images using centralized build script"
    log_info "  - Pushes Docker images to ECR"
    log_info "  - Deploys infrastructure stack (VPC, RDS, ElastiCache, ECS, etc.)"
    log_info "  - Provides database initialization guidance"
    log_info "  - Deploys auth-server and api-gateway stacks"
    log_info ""
    log_info "⚠️  First-time setup: Run with --bootstrap flag to initialize CDK in your AWS account"
    log_info ""
    log_info "Options:"
    log_info "  --env ENVIRONMENT   Target environment (default: dev)"
    log_info "  --region REGION     AWS region (default: us-east-1)"
    log_info "  --profile PROFILE   AWS profile (default: default)"
    log_info "  --skip-build        Skip all build steps (JAR files, Docker images, ECR push)"
    log_info "  --bootstrap         Bootstrap CDK for the account/region"
    log_info "  --force-redeploy    Force redeployment of all stacks even if they already exist"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Environments:"
    log_info "  dev (default)       Development environment"
    log_info "  stg                Staging environment"
    log_info "  prod               Production environment"
    log_info ""
    log_info "Examples:"
    log_info "  $0                                    # Deploy to dev environment"
    log_info "  $0 --env stg --region us-west-2      # Deploy to staging"
    log_info "  $0 --skip-build                      # Deploy without building"
    log_info "  $0 --bootstrap                       # Bootstrap CDK first"
    log_info "  $0 --force-redeploy                  # Force redeploy all stacks"
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
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            --bootstrap)
                BOOTSTRAP=true
                shift
                ;;
            --force-redeploy)
                FORCE_REDEPLOY=true
                shift
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
    
    local errors=()
    
    # Check build prerequisites only if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        # Check Java
        if command -v java &> /dev/null; then
            local java_version
            java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
            local major_version
            major_version=$(echo $java_version | cut -d'.' -f1)
            
            # Handle both old (1.8) and new (17) version formats
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

        # Check Docker
        if docker info > /dev/null 2>&1; then
            log_success "Docker found and running"
        else
            errors+=("Docker is not running. Please start Docker and try again.")
        fi
    fi

    # Check AWS CLI
    if command -v aws &> /dev/null; then
        log_success "AWS CLI found: $(aws --version | head -1)"
    else
        errors+=("AWS CLI not found. Please install AWS CLI.")
    fi

    # Check Node.js and npm
    if command -v node &> /dev/null; then
        log_success "Node.js found: $(node --version)"
    else
        errors+=("Node.js not found. Please install Node.js.")
    fi

    if command -v npm &> /dev/null; then
        log_success "npm found: $(npm --version)"
    else
        errors+=("npm not found. Please install npm.")
    fi

    # Check CDK
    if command -v cdk &> /dev/null; then
        log_success "CDK found: $(cdk --version)"
    else
        errors+=("CDK not found. Please install CDK globally: npm install -g aws-cdk")
    fi

    # Check if profile exists
    if ! aws configure list-profiles | grep -q "^${PROFILE}$"; then
        errors+=("AWS profile '${PROFILE}' not found. Available profiles: $(aws configure list-profiles | tr '\n' ' ')")
    else
        log_success "AWS profile '${PROFILE}' found"
    fi
    
    # Check if region is valid
    if ! aws ec2 describe-regions --profile ${PROFILE} --output text 2>/dev/null | grep -q ${REGION}; then
        errors+=("Invalid AWS region: ${REGION}")
    else
        log_success "AWS region '${REGION}' is valid"
    fi

    # Report errors if any
    if [[ ${#errors[@]} -gt 0 ]]; then
        log_error "Prerequisites check failed:"
        for error in "${errors[@]}"; do
            log_error "  $error"
        done
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

build_services() {
    log_section "Building Services"

    local build_script="$PROJECT_ROOT/backend/scripts/build/build.sh"
    if [ ! -f "$build_script" ]; then
        log_error "Build script not found: $build_script"
        exit 1
    fi

    log_info "Granting execute permission to build script..."
    chmod +x "$build_script"

    log_info "Invoking build script..."
    if "$build_script" --infra-stack=aws-ecs; then
        log_success "Build completed successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

push_docker_images_to_ecr() {
    log_section "Pushing Docker Images to ECR"

    # Get AWS account ID
    local account_id
    account_id=$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)

    # Login to ECR
    log_info "Logging in to ECR..."
    aws ecr get-login-password --profile ${PROFILE} --region ${REGION} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.${REGION}.amazonaws.com

    # Services that need to be pushed to ECR (these should already be built by build.sh)
    local services=("api-gateway" "auth-server")

    # Create ECR repositories and push images
    for service_name in "${services[@]}"; do
        local repo_name="atlas-${service_name}"

        # Create ECR repository if it doesn't exist
        if ! aws ecr describe-repositories --profile ${PROFILE} --region ${REGION} --repository-names ${repo_name} &>/dev/null; then
            log_info "Creating ECR repository: ${repo_name}"
            aws ecr create-repository --profile ${PROFILE} --region ${REGION} --repository-name ${repo_name}
        fi
        
        # Check if local image exists (should be built by build.sh)
        if ! docker image inspect ${service_name}:latest &>/dev/null; then
            log_error "Docker image ${service_name}:latest not found. Make sure build.sh completed successfully."
            exit 1
        fi

        # Tag and push image
        log_info "Tagging and pushing ${service_name} image to ECR..."
        docker tag ${service_name}:latest ${account_id}.dkr.ecr.${REGION}.amazonaws.com/${repo_name}:latest
        if docker push ${account_id}.dkr.ecr.${REGION}.amazonaws.com/${repo_name}:latest; then
            log_success "${service_name} image pushed to ECR successfully"
        else
            log_error "Failed to push ${service_name} image to ECR"
            exit 1
        fi
    done
    
    log_success "All Docker images pushed to ECR successfully"
}

# =============================================================================
# CDK FUNCTIONS
# =============================================================================

check_stack_status() {
    local stack_name="$1"
    local stack_status
    
    stack_status=$(aws cloudformation describe-stacks \
        --profile ${PROFILE} \
        --region ${REGION} \
        --stack-name "${stack_name}" \
        --query "Stacks[0].StackStatus" \
        --output text 2>/dev/null || echo "NOT_EXISTS")
    
    echo "$stack_status"
}

should_skip_stack_deployment() {
    local stack_name="$1"
    local stack_status
    
    # If force redeploy is enabled, never skip
    if [[ "$FORCE_REDEPLOY" == true ]]; then
        return 1  # Deploy stack
    fi
    
    stack_status=$(check_stack_status "$stack_name")
    
    case "$stack_status" in
        "CREATE_COMPLETE"|"UPDATE_COMPLETE")
            return 0  # Skip deployment
            ;;
        "NOT_EXISTS"|"ROLLBACK_COMPLETE"|"UPDATE_ROLLBACK_COMPLETE")
            return 1  # Deploy stack
            ;;
        *)
            log_warn "Stack $stack_name is in state: $stack_status"
            log_warn "Proceeding with deployment anyway..."
            return 1  # Deploy stack
            ;;
    esac
}

bootstrap_cdk() {
    log_section "Bootstrapping CDK"
    
    cd "$SCRIPT_DIR"
    
    log_info "Bootstrapping CDK for account/region..."
    cdk bootstrap --profile ${PROFILE} aws://$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)/${REGION}
    
    log_success "CDK bootstrapped successfully"
}

deploy_infrastructure() {
    log_section "Deploying Infrastructure Stack"

    cd "$SCRIPT_DIR"
    
    local stack_name="atlas-infrastructure-${ENVIRONMENT}"
    
    # Check if we should skip deployment
    if should_skip_stack_deployment "$stack_name"; then
        log_info "Skipping infrastructure deployment..."
        return 0
    fi

    # Get AWS account ID
    local account_id
    account_id=$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)

    log_info "Deploying infrastructure stack '$stack_name'..."
    cdk deploy "$stack_name" \
        --profile ${PROFILE} \
        --context environment=${ENVIRONMENT} \
        --context region=${REGION} \
        --context account=${account_id} \
        --require-approval never
    
    log_success "Infrastructure stack deployed successfully"
}

deploy_services() {
    log_section "Deploying Service Stacks"

    cd "$SCRIPT_DIR"

    # Get AWS account ID
    local account_id
    account_id=$(aws sts get-caller-identity --profile ${PROFILE} --query Account --output text)

    # Deploy Auth Server
    local auth_server_stack_name="atlas-auth-server-${ENVIRONMENT}"
    if should_skip_stack_deployment "$auth_server_stack_name"; then
        log_info "Skipping auth-server deployment..."
    else
        log_info "Deploying auth-server stack..."
        cdk deploy "$auth_server_stack_name" \
            --profile ${PROFILE} \
            --context environment=${ENVIRONMENT} \
            --context region=${REGION} \
            --context account=${account_id} \
            --context ecrRepository=${account_id}.dkr.ecr.${REGION}.amazonaws.com/atlas-auth-server \
            --require-approval never
    fi
    
    # Deploy API Gateway
    local api_gateway_stack_name="atlas-api-gateway-${ENVIRONMENT}"
    if should_skip_stack_deployment "$api_gateway_stack_name"; then
        log_info "Skipping api-gateway deployment..."
    else
        log_info "Deploying api-gateway stack..."
        cdk deploy "$api_gateway_stack_name" \
            --profile ${PROFILE} \
            --context environment=${ENVIRONMENT} \
            --context region=${REGION} \
            --context account=${account_id} \
            --context ecrRepository=${account_id}.dkr.ecr.${REGION}.amazonaws.com/atlas-api-gateway \
            --require-approval never
    fi
    
    log_success "Service stacks deployment completed"
}

# =============================================================================
# DATABASE INITIALIZATION
# =============================================================================

initialize_database() {
    log_section "Initializing Database"
    
    # Get MySQL endpoint from CDK output
    local mysql_endpoint
    mysql_endpoint=$(aws cloudformation describe-stacks \
        --profile ${PROFILE} \
        --region ${REGION} \
        --stack-name atlas-infrastructure-${ENVIRONMENT} \
        --query "Stacks[0].Outputs[?OutputKey=='MySQLEndpoint'].OutputValue" \
        --output text)
    
    if [[ -z "$mysql_endpoint" ]]; then
        log_error "Could not retrieve MySQL endpoint from stack outputs"
        return 1
    fi
    
    log_info "MySQL endpoint: $mysql_endpoint"
    log_info "Database initialization should be done manually or via a separate process"
    log_info "SQL scripts are available in: $SCRIPT_DIR/mysql/"
    
    log_success "Database initialization information provided"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    parse_arguments "$@"
    check_prerequisites

    log_section "Atlas CDK Deployment"
    log_info "Environment: $ENVIRONMENT"
    log_info "Region: $REGION"
    log_info "Profile: $PROFILE"
    log_info "Skip Build: $SKIP_BUILD"
    log_info "Bootstrap: $BOOTSTRAP"
    log_info "Force Redeploy: $FORCE_REDEPLOY"
    log_info ""

    # Only build and push if not skipping build
    if [[ "$SKIP_BUILD" == false ]]; then
        build_services
        push_docker_images_to_ecr
    else
        log_info "Skipping build and ECR push steps as requested"
    fi

    # Only bootstrap if requested
    if [[ "$BOOTSTRAP" == true ]]; then
        bootstrap_cdk
    fi

    # Note: CDK synthesis is automatically performed by 'cdk deploy'

    deploy_infrastructure
    initialize_database
    deploy_services

    log_section "Deployment Complete"
    log_success "Atlas has been successfully deployed to AWS ECS using CDK!"
}

# Execute main function
main "$@"
