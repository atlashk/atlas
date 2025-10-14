# Payment Service Module Variables

# Basic Configuration
variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
  default     = "atlas"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

# Network Configuration
variable "vpc_id" {
  description = "VPC ID where resources will be created"
  type        = string
}

variable "subnet_ids" {
  description = "List of subnet IDs for the ALB and ECS service"
  type        = list(string)
}

# Service Configuration
variable "container_port" {
  description = "Port on which the container listens"
  type        = number
  default     = 8084
}

variable "health_check_path" {
  description = "Health check path for the service"
  type        = string
  default     = "/health"
}

variable "cpu" {
  description = "CPU units for the ECS task"
  type        = number
  default     = 256
}

variable "memory" {
  description = "Memory (MB) for the ECS task"
  type        = number
  default     = 512
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
}

# Container Configuration
variable "container_image" {
  description = "Container image URL"
  type        = string
  default     = "nginx"
}

variable "container_image_tag" {
  description = "Container image tag"
  type        = string
  default     = "latest"
}

# Database Configuration
variable "db_name" {
  description = "Database name for Payment Service"
  type        = string
  default     = "atlas_payment_service"
}

variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = string
  default     = "5432"
}

variable "db_username" {
  description = "Database username"
  type        = string
}

# Use secret ARN instead of plain password
variable "db_secret_arn" {
  description = "ARN of the secret containing database password"
  type        = string
  sensitive   = true
}

variable "db_secret_kms_key_id" {
  description = "KMS key ID used to encrypt the database secret"
  type        = string
}

# Redis Cluster Configuration
variable "redis_cluster_nodes" {
  description = "Redis cluster nodes connection string"
  type        = string
}

# Use secret ARN instead of plain password
variable "redis_secret_arn" {
  description = "ARN of the secret containing Redis auth token"
  type        = string
  sensitive   = true
}

variable "redis_secret_kms_key_id" {
  description = "KMS key ID used to encrypt the Redis secret"
  type        = string
}

# MSK Configuration
variable "msk_bootstrap_brokers" {
  description = "MSK cluster bootstrap brokers"
  type        = string
}

variable "msk_cluster_arn" {
  description = "ARN of the MSK cluster for IAM permissions"
  type        = string
}

# S3 Configuration
variable "s3_bucket_arn" {
  description = "ARN of the S3 bucket for file storage"
  type        = string
  default     = ""
}

variable "enable_s3_access" {
  description = "Enable S3 access for this service"
  type        = bool
  default     = false
}

variable "enable_ses_access" {
  description = "Enable SES (email) access for this service"
  type        = bool
  default     = true
}

# API Client Configuration
variable "api_client_type" {
  description = "Type of API client (rest or grpc)"
  type        = string
  default     = "rest"
  validation {
    condition     = contains(["rest", "grpc"], var.api_client_type)
    error_message = "API client type must be either 'rest' or 'grpc'."
  }
}

variable "user_service_endpoint" {
  description = "User service endpoint"
  type        = string
}

variable "product_service_endpoint" {
  description = "Product service endpoint"
  type        = string
}

variable "order_service_endpoint" {
  description = "Order service endpoint"
  type        = string
}

# Service Discovery Configuration
variable "service_discovery_arn" {
  description = "ARN of the Cloud Map service discovery service"
  type        = string
}

# Environment Variables
variable "environment_vars" {
  description = "Additional environment variables for the container"
  type        = map(string)
  default     = {}
}

# Resource Tags
variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Project     = "Atlas"
    Service     = "Payment-Service"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}