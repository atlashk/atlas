# Atlas Backend - AWS Terraform Infrastructure

This directory contains the Terraform configuration for deploying the Atlas backend infrastructure on AWS using modern cloud-native patterns and best practices.

## 🏗️ Architecture Overview

The Atlas backend is deployed as a microservices architecture on AWS with the following components:

### Core Services
- **API Gateway** (Port 8080) - Central entry point and request routing
- **User Service** (Port 8081) - User management and authentication
- **Product Service** (Port 8082) - Product catalog and inventory
- **Order Service** (Port 8083) - Order processing and management
- **Payment Service** (Port 8084) - Payment processing with Stripe integration

### Infrastructure Components
- **ECS Fargate** - Serverless container orchestration for all microservices
- **RDS** - Managed relational database (MySQL/PostgreSQL support)
- **ElastiCache Redis** - In-memory caching with auth token management
- **MSK (Managed Streaming for Apache Kafka)** - Event streaming platform
- **Application Load Balancers** - High-availability traffic distribution
- **VPC** - Isolated network with public/private subnets across multiple AZs
- **Auto Scaling** - Dynamic scaling based on CPU/memory utilization
- **CloudWatch** - Comprehensive monitoring, logging, and alerting
- **SNS** - Notification system for operational alerts
- **WAF** - Web Application Firewall (staging/production environments)
- **SES** - Email service for transactional notifications
- **S3** - Object storage for application assets
- **Secrets Manager** - Secure credential management for RDS and ElastiCache
- **CloudMap** - Service discovery for internal communication

## 📁 Directory Structure

```
terraform/
├── modules/                          # Reusable Terraform modules
│   ├── application/                  # Application service modules
│   │   ├── api-gateway/             # API Gateway service with ALB and ECS
│   │   ├── order-service/           # Order service with isolated infrastructure
│   │   ├── payment-service/         # Payment service with Stripe integration
│   │   ├── product-service/         # Product catalog service
│   │   └── user-service/            # User management service
│   ├── infrastructure/              # Core infrastructure modules
│   │   ├── autoscaling/             # ECS Auto Scaling policies and targets
│   │   ├── cloudmap/                # AWS Cloud Map for service discovery
│   │   ├── elasticache/             # ElastiCache Redis with auth tokens
│   │   ├── iam/                     # IAM roles and policies
│   │   ├── msk/                     # Managed Streaming for Apache Kafka
│   │   ├── rds/                     # RDS database with Secrets Manager
│   │   ├── s3/                      # S3 buckets for application storage
│   │   ├── security/                # Security groups and network ACLs
│   │   ├── ses/                     # Simple Email Service configuration
│   │   ├── stripe/                  # Stripe payment integration
│   │   └── vpc/                     # VPC, subnets, and networking
│   └── observability/               # Monitoring and alerting modules
│       ├── cloudwatch/              # CloudWatch logs, metrics, and alarms
│       └── sns/                     # SNS topics for notifications
├── environments/                     # Environment-specific variable files
│   ├── dev.tfvars                   # Development environment settings
│   ├── staging.tfvars               # Staging environment settings
│   └── prod.tfvars                  # Production environment settings
├── backend-configs/                  # Remote state backend configurations
│   ├── dev.hcl                      # Development backend config
│   ├── staging.hcl                  # Staging backend config
│   └── prod.hcl                     # Production backend config
├── bootstrap/                        # Bootstrap infrastructure for state management
│   ├── main.tf                      # S3 bucket and state management setup
│   ├── variables.tf
│   └── outputs.tf
├── main.tf                          # Main Terraform configuration
├── variables.tf                     # Variable definitions
└── outputs.tf                       # Output definitions
```

## 🚀 Quick Start

### Prerequisites

1. **AWS CLI** v2.x configured with appropriate credentials and permissions
2. **Terraform** >= 1.11 installed (required for S3 native state locking)
3. **Docker** installed and running (for building container images)
4. **Java** 17+ installed (for building Spring Boot applications)
5. **Bash shell** (Linux/macOS) or **PowerShell** (Windows)

### Initial Setup (One-time per Environment)

1. **Bootstrap Remote State Backend**:
   ```bash
   cd bootstrap
   terraform init
   terraform apply -var="environment=dev" -var="project_name=atlas" -var="aws_region=us-east-1"
   ```

2. **Configure Backend** (update root `main.tf` with bootstrap outputs):
   ```hcl
   terraform {
      backend "s3" {
         bucket         = "atlas-terraform-state-dev-xxxxxxxx"  # Replace with actual bucket name from bootstrap
         key            = "infrastructure/terraform.tfstate"
         region         = "us-east-1"
         encrypt        = true
         kms_key_id     = "arn:aws:kms:us-east-1:xxxxxxxxxxxx:key/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"  # Replace with actual KMS key ARN
      }
   }
   ```

### Deployment Options

#### Option 1: Automated Deployment (Recommended)

Use the provided deployment script for a complete end-to-end deployment:

```bash
# Full deployment with build
./deploy.sh

# Deploy without rebuilding (if images already exist)
./deploy.sh --skip-build

# Show help
./deploy.sh --help
```

The deployment script automatically:
- Reads configuration from `app-stack.aws.cfg`
- Creates ECR repositories
- Builds and pushes Docker images
- Configures Terraform variables
- Deploys infrastructure

#### Option 2: Manual Terraform Commands

For more control, use direct Terraform commands:

```bash
# Initialize Terraform with backend configuration
terraform init -backend-config="backend-configs/dev.hcl"

# Plan deployment
terraform plan -var-file="environments/dev.tfvars"

# Apply changes
terraform apply -var-file="environments/dev.tfvars"

# Destroy resources (use with caution)
terraform destroy -var-file="environments/dev.tfvars"
```

### Environment-Specific Deployment

Deploy to different environments using the corresponding configuration files:

```bash
# Development environment
terraform init -backend-config="backend-configs/dev.hcl"
terraform apply -var-file="environments/dev.tfvars"

# Staging environment
terraform init -backend-config="backend-configs/staging.hcl"
terraform apply -var-file="environments/staging.tfvars"

# Production environment
terraform init -backend-config="backend-configs/prod.hcl"
terraform apply -var-file="environments/prod.tfvars"
```

### Configuration Management

The deployment automatically reads from `app-stack.aws.cfg` to configure:
- **Database Engine**: MySQL or PostgreSQL based on `datasource` setting
- **API Client Type**: REST or gRPC based on `api-client` setting
- **Service Endpoints**: Automatically configured for service discovery

## 🌍 Environment Configurations

### Development (dev.tfvars)
- **Purpose**: Development and testing
- **Resources**: Minimal (t3.micro instances, small storage)
- **Security**: Permissive (for development ease)
- **Monitoring**: Basic
- **Auto Scaling**: 1-3 tasks per service

### Staging (staging.tfvars)
- **Purpose**: Pre-production testing
- **Resources**: Moderate (t3.small instances, medium storage)
- **Security**: Enhanced (WAF enabled, restricted access)
- **Monitoring**: Detailed
- **Auto Scaling**: 2-6 tasks per service

### Production (prod.tfvars)
- **Purpose**: Live production environment
- **Resources**: High-performance (r5.large instances, large storage)
- **Security**: Maximum (WAF, deletion protection, restricted networks)
- **Monitoring**: Comprehensive with alerts
- **Auto Scaling**: 3-20 tasks per service

## 🔧 Key Features

### Auto Scaling
- **CPU-based scaling**: Targets 60-70% utilization
- **Memory-based scaling**: Targets 70-80% utilization
- **Scheduled scaling**: Production environment scales up during business hours
- **Request-based scaling**: ALB request count per target

### Security Features

#### Network Security
- **VPC Isolation**: Dedicated VPC with public/private subnet architecture
- **Security Groups**: Restrictive ingress/egress rules with least privilege access
- **Private Subnets**: All application services run in private subnets
- **Public Subnets**: Only load balancers exposed to internet traffic
- **WAF Integration**: Web Application Firewall for staging and production environments

#### Data Protection
- **Encryption at Rest**: RDS and ElastiCache encrypted with AWS managed keys
- **Encryption in Transit**: TLS/SSL for all service communications
- **Secrets Management**: AWS Secrets Manager for database and cache credentials
- **Auto-Generated Passwords**: RDS and ElastiCache passwords managed by AWS

#### Access Control
- **IAM Roles**: Service-specific IAM roles with minimal required permissions
- **Task Execution Roles**: Separate roles for ECS task execution and application access
- **Cross-Service Authentication**: Secure service-to-service communication
- **Environment Isolation**: Complete separation between dev/staging/production

### Monitoring & Alerting
- **CloudWatch Alarms**: CPU, memory, disk, response time, error rates
- **SNS Notifications**: Email alerts for critical issues
- **Dashboard**: Centralized monitoring dashboard
- **Log Aggregation**: Centralized logging for all services

### High Availability
- **Multi-AZ deployment**: Resources distributed across availability zones
- **Auto Scaling**: Automatic scaling based on demand
- **Health checks**: ALB health checks with automatic failover
- **Backup**: Automated RDS backups with configurable retention

## 📊 Monitoring

### CloudWatch Alarms
- **ECS Services**: CPU, memory, task count
- **RDS**: CPU, connections, storage space
- **ElastiCache**: CPU, memory usage
- **ALB**: Response time, 5XX errors
- **MSK**: Disk usage (production)

### Notifications
Configure `alarm_notification_email` in environment files to receive alerts.

## 🔐 Security Best Practices

1. **Network Security**:
   - Private subnets for application services
   - Security groups with minimal required access
   - Environment-specific CIDR restrictions

2. **Data Protection**:
   - Encryption at rest for RDS and ElastiCache
   - SSL/TLS for all communications
   - Secrets management via AWS Systems Manager

3. **Access Control**:
   - IAM roles with least privilege
   - Service-to-service authentication
   - Environment isolation

## 🛠️ Customization

### Adding New Services
1. Create service module in `modules/services/`
2. Add service to `local.services` in `main.tf`
3. Add auto scaling module for the service
4. Update environment configurations

### Modifying Resources
- Update environment-specific `.tfvars` files
- Modify module configurations in `main.tf`
- Add new variables in `variables.tf` with validation

### Environment-Specific Changes
- Modify the appropriate `.tfvars` file
- Use conditional logic in modules based on `var.environment`

## 📝 Variables Reference

### Core Variables
- `environment`: Environment name (dev/staging/prod)
- `project_name`: Project identifier
- `aws_region`: AWS region for deployment

### Networking
- `vpc_cidr`: VPC CIDR block
- `allowed_cidr_blocks`: Allowed IP ranges for access

### Database
- `db_instance_class`: RDS instance type
- `db_allocated_storage`: Storage size in GB
- `db_backup_retention_period`: Backup retention days

### ECS Configuration
- `ecs_min_capacity`: Minimum task count
- `ecs_max_capacity`: Maximum task count
- `ecs_target_cpu_utilization`: CPU target for scaling
- `ecs_target_memory_utilization`: Memory target for scaling

### Security Features
- `enable_waf`: Enable Web Application Firewall
- `enable_deletion_protection`: Protect resources from deletion
- `enable_detailed_monitoring`: Enhanced monitoring
- `enable_cross_region_backup`: Cross-region backup replication

## 🚨 Troubleshooting

### Common Issues and Solutions

#### 1. State Lock Issues
```bash
# Force unlock if Terraform state is locked
terraform force-unlock <LOCK_ID>

# Check current state locks
aws dynamodb scan --table-name atlas-terraform-locks-dev
```

#### 2. Backend Configuration Problems
```bash
# Verify S3 bucket exists and is accessible
aws s3 ls s3://atlas-terraform-state-dev-<suffix>

# Check AWS credentials and permissions
aws sts get-caller-identity
aws iam get-user
```

#### 3. ECR Repository Issues
```bash
# Create ECR repositories manually if needed
aws ecr create-repository --repository-name atlas/api-gateway
aws ecr create-repository --repository-name atlas/user-service
aws ecr create-repository --repository-name atlas/product-service
aws ecr create-repository --repository-name atlas/order-service
aws ecr create-repository --repository-name atlas/payment-service
```

#### 4. Docker Build Failures
```bash
# Check Docker daemon is running
docker info

# Verify Java version for Spring Boot builds
java --version

# Manual build troubleshooting
cd ../../..  # Navigate to project root
./build.sh
```

#### 5. Resource Conflicts
- **VPC CIDR Overlaps**: Ensure VPC CIDR doesn't conflict with existing VPCs
- **Resource Name Conflicts**: Check for existing resources with same names
- **Security Group Rules**: Verify port conflicts and CIDR block access

#### 6. Service Health Check Failures
```bash
# Check ECS service status
aws ecs describe-services --cluster atlas-dev --services atlas-api-gateway-dev

# View ECS task logs
aws logs get-log-events --log-group-name /ecs/atlas-api-gateway-dev
```

#### 7. Database Connection Issues
```bash
# Test RDS connectivity from ECS tasks
aws rds describe-db-instances --db-instance-identifier atlas-dev

# Check security group rules for database access
aws ec2 describe-security-groups --group-ids <rds-security-group-id>
```

#### 8. Load Balancer Health Checks
- Verify health check path is correct (`/actuator/health`)
- Check security group allows ALB to reach ECS tasks
- Ensure application starts within health check timeout

### Useful Commands

```bash
# View Terraform state
terraform show

# List all resources
terraform state list

# Get specific resource details
terraform state show aws_ecs_service.api_gateway

# Refresh state from AWS
terraform refresh

# Import existing resources
terraform import aws_s3_bucket.example bucket-name

# Validate configuration
terraform validate

# Format configuration files
terraform fmt -recursive
```

## 📞 Support

For issues and questions:
1. Check the troubleshooting section
2. Review AWS CloudWatch logs
3. Consult Terraform documentation
4. Contact the infrastructure team

---

**Note**: Always test changes in development environment before applying to staging or production.

## Services Deployed

1. **API Gateway** - Routes requests to appropriate services
2. **Auth Server** - Authentication and authorization
3. **User Service** - User management
4. **Product Service** - Product catalog
5. **Order Service** - Order processing
6. **Payment Service** - Payment processing

## Docker Images and ECR Setup

### ECR Repositories

Before deploying, ensure you have created ECR repositories and pushed your Docker images:

```bash
# Create ECR repositories
aws ecr create-repository --repository-name atlas/api-gateway
aws ecr create-repository --repository-name atlas/user-service
aws ecr create-repository --repository-name atlas/product-service
aws ecr create-repository --repository-name atlas/order-service
aws ecr create-repository --repository-name atlas/payment-service

# Build and push images (example for user-service)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker build -t atlas/user-service .
docker tag atlas/user-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/atlas/user-service:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/atlas/user-service:latest
```

## Step-by-Step Deployment Guide

1. **Clone and navigate to the terraform directory**:
   ```bash
   cd backend/scripts/deploy/aws/terraform
   ```

2. **Configure environment variables**:
   ```bash
   # Edit the appropriate environment file with your specific values
   vim environments/dev.tfvars
   # or
   vim environments/staging.tfvars
   # or  
   vim environments/prod.tfvars
   ```

3. **Initialize Terraform with backend configuration**:
   ```bash
   # For development environment
   terraform init -backend-config="backend-configs/dev.hcl"
   ```

4. **Plan the deployment**:
   ```bash
   terraform plan -var-file="environments/dev.tfvars"
   ```

5. **Apply the configuration**:
   ```bash
   terraform apply -var-file="environments/dev.tfvars"
   ```

## Configuration

### Required Variables

Edit the appropriate environment file (`environments/dev.tfvars`, `environments/staging.tfvars`, or `environments/prod.tfvars`) with your specific values:

- `aws_region`: AWS region for deployment
- `project_name`: Name prefix for all resources
- `environment`: Environment name (dev, staging, prod)
- `db_password`: Secure password for MySQL database
- `vpc_cidr`: CIDR block for VPC

### Optional Variables

- `db_instance_class`: RDS instance size
- `elasticache_node_type`: ElastiCache node size
- `ecs_task_cpu`: CPU allocation for ECS tasks
- `ecs_task_memory`: Memory allocation for ECS tasks

## Service URLs

After deployment, all services are accessible through the API Gateway:

- **API Gateway**: `http://<load-balancer-dns>/api`
  - All microservices are accessible through this single entry point
  - Routes are handled internally by the API Gateway
  - Individual services are not directly exposed for security

## Monitoring

- **CloudWatch Dashboard**: Available in AWS Console
- **Log Groups**: `/ecs/atlas-<env>/<service-name>`
- **Metrics**: CPU, Memory, Request counts
- **Alarms**: Configured for high CPU usage

## Security

- All services run in private subnets
- Database and ElastiCache are not publicly accessible
- Security groups restrict access between components
- S3 bucket has public access blocked
- ElastiCache and RDS encryption enabled

## Scaling

To scale services:

```bash
# Update desired count in the appropriate environment file
# For example, in environments/dev.tfvars:
ecs_desired_count = 3

# Apply changes with the environment file
terraform apply -var-file="environments/dev.tfvars"
```

## Cleanup

To destroy all resources for a specific environment:

```bash
# Destroy development environment
terraform destroy -var-file="environments/dev.tfvars"

# Destroy staging environment  
terraform destroy -var-file="environments/staging.tfvars"

# Destroy production environment (use with extreme caution)
terraform destroy -var-file="environments/prod.tfvars"
```

## Troubleshooting

### Common Issues

1. **ECR Repository Not Found**: Ensure ECR repositories exist and images are pushed
2. **Database Connection Issues**: Check security groups and database credentials
3. **Service Health Checks Failing**: Verify health check endpoint is accessible

### Logs

Check CloudWatch logs for each service:
```bash
aws logs tail /ecs/atlas-dev/user-service --follow
```

## Cost Optimization

For development environments:
- Use `db.t3.micro` for RDS
- Use `cache.t3.micro` for ElastiCache
- Set `ecs_desired_count = 1`
- Consider using Spot instances for non-critical workloads

## Support

For issues or questions, refer to the project documentation or create an issue in the repository.