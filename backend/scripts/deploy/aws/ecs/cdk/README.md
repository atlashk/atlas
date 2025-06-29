# Atlas CDK Deployment

This directory contains the AWS CDK (Cloud Development Kit) infrastructure code for deploying the Atlas microservices platform to AWS ECS.

## Overview

The CDK infrastructure is organized into three main stacks:

1. **Atlas Infrastructure Stack** (`atlas-infrastructure-{env}`)
   - VPC with public and private subnets
   - RDS MySQL database
   - ElastiCache Redis cluster
   - ECS cluster
   - Application Load Balancer
   - Security groups
   - IAM roles
   - CloudWatch log groups
   - AWS Secrets Manager secrets
   - AWS Cloud Map service discovery

2. **Atlas API Gateway Stack** (`atlas-api-gateway-{env}`)
   - ECS Fargate service for API Gateway
   - Target group and load balancer rules
   - Container definitions with environment variables

3. **Atlas Auth Server Stack** (`atlas-auth-server-{env}`)
   - ECS Fargate service for Auth Server
   - Target group and load balancer rules
   - Container definitions with environment variables

---

## Prerequisites

1. **AWS CLI** - Configured with appropriate credentials
2. **Node.js** - Version 18 or later
3. **Docker** - For building container images
4. **Java 17+** - For building backend services
5. **AWS CDK CLI** - Will be installed via npx if not available globally

---

## AWS Account Setup

### 1. AWS Account Requirements

You need an AWS account with sufficient permissions to create and manage the following services:
- **Amazon ECS** (Elastic Container Service)
- **Amazon ECR** (Elastic Container Registry)
- **Amazon RDS** (Relational Database Service)
- **Amazon ElastiCache** (Redis)
- **Amazon VPC** (Virtual Private Cloud)
- **Application Load Balancer** (ALB)
- **AWS IAM** (Identity and Access Management)
- **AWS Secrets Manager**
- **AWS CloudWatch**
- **AWS Cloud Map** (Service Discovery)

### 2. AWS CLI Configuration

Configure AWS CLI with your credentials:

```bash
# Configure default profile
aws configure
# AWS Access Key ID: [Your Access Key]
# AWS Secret Access Key: [Your Secret Key]
# Default region name: us-east-1
# Default output format: json

# Or configure named profiles for different environments
aws configure --profile dev
aws configure --profile stg
aws configure --profile prod
```

### 3. Required IAM Permissions

Your AWS user/role needs the following permissions (you can use these managed policies):
- `PowerUserAccess` (recommended for development)
- Or create a custom policy with these specific permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecs:*",
                "ecr:*",
                "rds:*",
                "elasticache:*",
                "ec2:*",
                "elasticloadbalancing:*",
                "iam:*",
                "secretsmanager:*",
                "logs:*",
                "servicediscovery:*",
                "cloudformation:*",
                "s3:*",
                "ssm:*"
            ],
            "Resource": "*"
        }
    ]
}
```

### 4. Verify AWS Configuration

Test your AWS configuration:

```bash
# Check current identity
aws sts get-caller-identity

# List available profiles
aws configure list-profiles

# Test with specific profile
aws sts get-caller-identity --profile dev
```

### 5. Regional Considerations

- **Default Region**: `us-east-1` (N. Virginia)
- **Alternative Regions**: `us-west-2` (Oregon), `eu-west-1` (Ireland)
- **Cost Optimization**: Choose regions closer to your users
- **Service Availability**: Ensure all required services are available in your chosen region

### 6. Cost Estimates

Approximate monthly costs for development environment:
- **RDS MySQL (db.t3.micro)**: ~$13-15
- **ElastiCache Redis (cache.t3.micro)**: ~$11-13
- **ECS Fargate**: ~$15-25 (depends on usage)
- **Application Load Balancer**: ~$16-20
- **NAT Gateway**: ~$32-45
- **ECR Storage**: ~$1-5
- **CloudWatch Logs**: ~$1-3
- **Total Estimated**: ~$89-126/month

> **Note**: Costs may vary based on usage patterns, data transfer, and region selection.

---

## Quick Start

### 1. Bootstrap CDK (First Time Only)

```bash
# Bootstrap CDK for your AWS account/region
./deploy.sh --bootstrap
```

### 2. Deploy to Development Environment

```bash
# Deploy everything (build + deploy)
./deploy.sh

# Deploy without building (if images already exist)
./deploy.sh --skip-build
```

### 3. Deploy to Other Environments

```bash
# Deploy to staging
./deploy.sh --env stg --region us-west-2

# Deploy to production
./deploy.sh --env prod --region us-east-1 --profile prod
```

---

## Configuration

### Environment Variables

The CDK stacks support the following context variables:

- `environment` - Target environment (dev, stg, prod)
- `region` - AWS region
- `account` - AWS account ID
- `imageTag` - Docker image tag (default: latest)
- `ecrRepository` - ECR repository URI
- `desiredCount` - Number of desired ECS tasks (default: 1)
- `taskCpu` - CPU units for ECS tasks (default: 512)
- `taskMemory` - Memory for ECS tasks in MB (default: 1024)

### Customizing Deployment

You can customize the deployment by passing context variables:

```bash
# Deploy with custom configuration
npx cdk deploy atlas-infrastructure-dev \
  --context environment=dev \
  --context taskCpu=1024 \
  --context taskMemory=2048 \
  --context desiredCount=2
```

---

## Scripts

### `deploy.sh`

Main deployment script that handles the complete deployment process:

```bash
Usage: ./deploy.sh [OPTIONS]

Options:
  --env ENVIRONMENT   Target environment (default: dev)
  --region REGION     AWS region (default: us-east-1)
  --profile PROFILE   AWS profile (default: default)
  --skip-build        Skip all build steps (JAR files, Docker images, ECR push)
  --bootstrap         Bootstrap CDK for the account/region
  -h, --help          Show this help message

Examples:
  ./deploy.sh                                    # Deploy to dev environment
  ./deploy.sh --env stg --region us-west-2      # Deploy to staging
  ./deploy.sh --skip-build                      # Deploy without building
  ./deploy.sh --bootstrap                       # Bootstrap CDK first
```

### `cleanup.sh`

Cleanup script that destroys all resources:

```bash
Usage: ./cleanup.sh [OPTIONS]

Options:
  --env ENVIRONMENT   Target environment (default: dev)
  --region REGION     AWS region (default: us-east-1)
  --profile PROFILE   AWS profile (default: default)
  --force             Skip confirmation prompts
  -h, --help          Show this help message

Examples:
  ./cleanup.sh                                    # Cleanup dev environment
  ./cleanup.sh --env stg --region us-west-2      # Cleanup staging
  ./cleanup.sh --force                           # Skip confirmations
```

---

## Manual CDK Commands

### Synthesize CloudFormation Templates

```bash
# Synthesize all stacks
npx cdk synth

# Synthesize specific stack
npx cdk synth atlas-infrastructure-dev
```

### Deploy Individual Stacks

```bash
# Deploy infrastructure stack
npx cdk deploy atlas-infrastructure-dev --context environment=dev

# Deploy API Gateway stack
npx cdk deploy atlas-api-gateway-dev --context environment=dev

# Deploy Auth Server stack
npx cdk deploy atlas-auth-server-dev --context environment=dev
```

### Destroy Stacks

```bash
# Destroy all stacks
npx cdk destroy --all

# Destroy specific stack
npx cdk destroy atlas-infrastructure-dev
```

### Diff Changes

```bash
# Show differences between deployed stack and current code
npx cdk diff atlas-infrastructure-dev
```

---

## Database Initialization

After the infrastructure is deployed, you need to initialize the MySQL database with the required schemas:

1. Connect to the RDS instance using the credentials from AWS Secrets Manager
2. Execute the SQL scripts located in `./mysql/`
3. The scripts should be executed in the following order:
   - `01-db_user.sql`
   - `02-db_auth.sql`
   - `03-db_product.sql`
   - `04-db_order.sql`
   - `05-db_notification.sql`
   - `06-db_quartz.sql`
   - `07-db_zipkin.sql`

## Monitoring and Troubleshooting

### CloudWatch Logs

All application logs are sent to CloudWatch under the log group `/ecs/{environment}-atlas`:

```bash
# View logs for a specific service
aws logs filter-log-events \
  --log-group-name "/ecs/dev-atlas" \
  --log-stream-name-prefix "api-gateway"
```

### ECS Service Status

Check the status of ECS services:

```bash
# List ECS services
aws ecs list-services --cluster dev-atlas-cluster

# Describe a specific service
aws ecs describe-services \
  --cluster dev-atlas-cluster \
  --services dev-api-gateway
```

### Load Balancer Health

Check the health of targets in the load balancer:

```bash
# List target groups
aws elbv2 describe-target-groups

# Check target health
aws elbv2 describe-target-health \
  --target-group-arn arn:aws:elasticloadbalancing:region:account:targetgroup/dev-api-gateway-tg/id
```

---

## Security Considerations

1. **Secrets Management**: Database and Redis passwords are stored in AWS Secrets Manager
2. **Network Security**: Private subnets for databases, security groups with minimal required access
3. **IAM Roles**: Least privilege principle applied to ECS task roles
4. **Encryption**: RDS encryption at rest enabled, ELB uses HTTPS (configure SSL certificate)

---

## Cost Optimization

1. **RDS Instance**: Uses `db.t3.micro` for development (upgrade for production)
2. **ElastiCache**: Uses `cache.t3.micro` for development
3. **ECS Fargate**: Uses minimal CPU/memory allocation
4. **NAT Gateway**: Single NAT gateway for cost optimization (consider multiple for HA)

---

## Troubleshooting Common Issues

### CDK Bootstrap Issues

If you encounter bootstrap issues:

```bash
# Check if CDK is bootstrapped
npx cdk doctor

# Re-bootstrap if needed
npx cdk bootstrap aws://ACCOUNT-ID/REGION
```

### ECS Task Startup Issues

If ECS tasks fail to start:

1. Check CloudWatch logs for application errors
2. Verify ECR repository exists and contains images
3. Check ECS task definition for correct resource allocation
4. Verify security groups allow required traffic

### Database Connection Issues

If services can't connect to the database:

1. Verify RDS instance is running
2. Check security group rules
3. Verify database credentials in Secrets Manager
4. Ensure database is initialized with required schemas

---

## Support

For issues and questions:

1. Check the CloudWatch logs for application errors
2. Review the CDK documentation: https://docs.aws.amazon.com/cdk/
3. Check AWS service status: https://status.aws.amazon.com/
4. Consult the Atlas project documentation
