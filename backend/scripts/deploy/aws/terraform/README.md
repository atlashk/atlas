# Atlas Backend AWS ECS Deployment

This Terraform configuration deploys the Atlas backend microservices to AWS ECS with supporting infrastructure.

## Architecture

The infrastructure includes:

- **VPC**: Custom VPC with public and private subnets across 2 AZs
- **ECS Cluster**: Fargate-based cluster running 6 microservices
- **RDS**: Database for persistent storage
- **ElastiCache**: Caching and session storage
- **MSK**: Messaging infrastructure for event-driven architecture
- **Application Load Balancer**: Routes traffic to services
- **S3**: File storage
- **CloudWatch**: Logging and monitoring

## Services Deployed

1. **API Gateway** - Routes requests to appropriate services
2. **Auth Server** - Authentication and authorization
3. **User Service** - User management
4. **Product Service** - Product catalog
5. **Order Service** - Order processing
6. **Payment Service** - Payment processing

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** >= 1.0 installed
3. **Docker images** pushed to ECR repositories

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

## Deployment

1. **Clone and navigate to the terraform directory**:
   ```bash
   cd backend/scripts/deploy/aws/terraform
   ```

2. **Copy and configure variables**:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your specific values
   ```

3. **Initialize Terraform**:
   ```bash
   terraform init
   ```

4. **Plan the deployment**:
   ```bash
   terraform plan
   ```

5. **Apply the configuration**:
   ```bash
   terraform apply
   ```

## Configuration

### Required Variables

Edit `terraform.tfvars` with your specific values:

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
# Update desired count in terraform.tfvars
ecs_desired_count = 3

# Apply changes
terraform apply
```

## Cleanup

To destroy all resources:

```bash
terraform destroy
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