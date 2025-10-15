# Atlas Backend - AWS Terraform Infrastructure

This directory contains the Terraform configuration for deploying the Atlas backend infrastructure on AWS.

## 🏗️ Architecture Overview

The infrastructure includes:
- **ECS Fargate** services for microservices (User, Product, Order, Payment, API Gateway)
- **RDS MySQL** for persistent data storage
- **ElastiCache Redis** for caching
- **MSK (Managed Streaming for Apache Kafka)** for event streaming
- **Application Load Balancers** for traffic distribution
- **VPC** with public/private subnets across multiple AZs
- **Auto Scaling** policies for all ECS services
- **CloudWatch** monitoring and alarms
- **SNS** notifications for alerts
- **WAF** for web application security (staging/prod)
- **SES** for email notifications

## 📁 Directory Structure

```
terraform/
├── modules/                          # Reusable Terraform modules
│   ├── infrastructure/
│   │   ├── autoscaling/             # ECS Auto Scaling policies
│   │   ├── msk/                     # Managed Streaming for Kafka
│   │   ├── networking/              # VPC, subnets, security groups
│   │   ├── rds/                     # RDS MySQL database
│   │   ├── redis/                   # ElastiCache Redis
│   │   └── security/                # Security groups
│   ├── observability/
│   │   ├── cloudwatch/              # CloudWatch logs and alarms
│   │   └── sns/                     # SNS topics for notifications
│   └── services/                    # ECS service modules
│       ├── api-gateway/
│       ├── order-service/
│       ├── payment-service/
│       ├── product-service/
│       └── user-service/
├── environments/                     # Environment-specific configurations
│   ├── dev.tfvars
│   ├── staging.tfvars
│   └── prod.tfvars
├── backend-configs/                  # Backend configurations for remote state
│   ├── dev.hcl
│   ├── staging.hcl
│   └── prod.hcl
├── bootstrap/                        # Bootstrap module for remote state setup
│   ├── main.tf
│   ├── variables.tf
│   └── outputs.tf
├── main.tf                          # Main Terraform configuration
├── variables.tf                     # Variable definitions
└── outputs.tf                       # Output definitions
```

## 🚀 Quick Start

### Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** >= 1.0 installed
3. **Bash shell** (Linux/macOS environment)

### Initial Setup

1. **Bootstrap Remote State Backend** (one-time setup per environment):
   ```bash
   cd bootstrap
   terraform init
   terraform apply -var="environment=dev" -var="project_name=atlas"
   ```

2. **Configure Backend** (update main.tf with bootstrap outputs):
   ```hcl
   terraform {
     backend "s3" {
       # Use values from bootstrap outputs
     }
   }
   ```

### Deployment

You can deploy using direct Terraform commands:

```bash
# Initialize Terraform with backend configuration
terraform init -backend-config="backend-configs/dev.hcl"

# Plan deployment
terraform plan -var-file="environments/dev.tfvars"

# Apply changes
terraform apply -var-file="environments/dev.tfvars"

# Destroy resources (use with caution)
terraform destroy -var-file="environments/prod.tfvars"
```

### Environment-Specific Deployment

For different environments, use the corresponding configuration files:

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

### Security
- **Network isolation**: Private subnets for services, public for ALBs
- **Security groups**: Restrictive rules with specific port access
- **WAF**: Web Application Firewall for staging/production
- **Encryption**: At-rest and in-transit encryption for all data stores

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

### Common Issues

1. **Backend Configuration**:
   - Ensure S3 bucket and DynamoDB table exist (run bootstrap first)
   - Verify AWS credentials and permissions

2. **Resource Limits**:
   - Check AWS service quotas
   - Verify instance types are available in the region

3. **Network Connectivity**:
   - Verify security group rules
   - Check route table configurations

### Useful Commands

```bash
# Check Terraform state
terraform state list

# Import existing resources
terraform import aws_instance.example i-1234567890abcdef0

# Refresh state
terraform refresh -var-file="environments/dev.tfvars"

# Validate configuration
terraform validate
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