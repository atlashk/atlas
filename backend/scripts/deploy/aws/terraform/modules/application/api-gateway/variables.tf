# API Gateway Module Variables

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
  default     = 8080
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

# JWT Configuration
variable "user_service_dns" {
  description = "User service DNS name from Cloud Map for JWK Set URI"
  type        = string
}

# Service Discovery Configuration
variable "service_discovery_arn" {
  description = "ARN of the Cloud Map service discovery service for API Gateway"
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
    Project     = "atlas"
    Service     = "api-gateway"
    Environment = "dev"
    ManagedBy   = "terraform"
  }
}
