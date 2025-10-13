# Product Service Module Variables

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
  default     = "us-west-2"
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
variable "database_name" {
  description = "Database name for Product Service"
  type        = string
  default     = "atlas_product_service"
}

variable "container_port" {
  description = "Port on which the container listens"
  type        = number
  default     = 8082
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
variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = string
  default     = "5432"
}

variable "db_user" {
  description = "Database username"
  type        = string
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

# Redis Configuration
variable "redis_host" {
  description = "Redis host"
  type        = string
}

variable "redis_port" {
  description = "Redis port"
  type        = string
  default     = "6379"
}

# S3 Configuration
variable "s3_product_image_bucket_name" {
  description = "Name of the S3 bucket for product images"
  type        = string
}

variable "s3_product_image_policy_arn" {
  description = "ARN of the S3 policy for product image bucket access"
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
    Service     = "Product-Service"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}