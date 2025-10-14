# General Configuration
variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "atlas"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}

# VPC Configuration
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# Database Configuration
variable "db_engine" {
  description = "Database engine (mysql or postgres)"
  type        = string
  default     = "mysql"
  validation {
    condition     = contains(["mysql", "postgres"], var.db_engine)
    error_message = "Database engine must be either 'mysql' or 'postgres'."
  }
}

variable "db_engine_version" {
  description = "Database engine version"
  type        = string
  default     = "8.0"
}

variable "db_port" {
  description = "Database port"
  type        = number
  default     = 3306
}

variable "db_parameter_group_family" {
  description = "Database parameter group family"
  type        = string
  default     = "mysql8.0"
}

variable "db_name" {
  description = "Name of the initial database (additional service databases will be created via init script)"
  type        = string
  default     = "atlas_main"
}

variable "db_username" {
  description = "Username for the database"
  type        = string
  default     = "atlas_user"
}

variable "db_password" {
  description = "Password for the database"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage for RDS instance (GB)"
  type        = number
  default     = 20
}

# ElastiCache Configuration
variable "elasticache_node_type" {
  description = "ElastiCache node type"
  type        = string
  default     = "cache.t3.micro"
}

variable "elasticache_num_cache_nodes" {
  description = "Number of cache nodes in ElastiCache cluster"
  type        = number
  default     = 2
}

# ECS Configuration
variable "ecs_task_cpu" {
  description = "CPU units for ECS tasks"
  type        = number
  default     = 256
}

variable "ecs_task_memory" {
  description = "Memory for ECS tasks (MB)"
  type        = number
  default     = 512
}

variable "ecs_desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
}

# Application Configuration
variable "app_port" {
  description = "Port on which the application runs"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Health check path for load balancer"
  type        = string
  default     = "/actuator/health"
}

# Docker Configuration
variable "docker_image_tag" {
  description = "Docker image tag for services"
  type        = string
  default     = "latest"
}

variable "ecr_repository_prefix" {
  description = "Prefix for ECR repository names"
  type        = string
  default     = "atlas"
}

# Redis Configuration
variable "redis_password" {
  description = "Redis cluster password"
  type        = string
  sensitive   = true
}

# MSK Configuration
variable "msk_number_of_broker_nodes" {
  description = "Number of broker nodes in the MSK cluster"
  type        = number
  default     = 2
}

variable "msk_broker_instance_type" {
  description = "Instance type for MSK brokers"
  type        = string
  default     = "kafka.t3.small"
}

variable "msk_broker_volume_size" {
  description = "Size of EBS volume for each broker (in GB)"
  type        = number
  default     = 100
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
  default     = "http://user-service.atlas.local:8081"
}

variable "product_service_endpoint" {
  description = "Product service endpoint"
  type        = string
  default     = "http://product-service.atlas.local:8082"
}

variable "order_service_endpoint" {
  description = "Order service endpoint"
  type        = string
  default     = "http://order-service.atlas.local:8083"
}

variable "payment_service_endpoint" {
  description = "Payment service endpoint"
  type        = string
  default     = "http://payment-service.atlas.local:8084"
}

# SES Configuration
variable "ses_domain_name" {
  description = "Domain name for SES email sending"
  type        = string
  default     = "example.com"
}

variable "ses_route53_zone_id" {
  description = "Route53 hosted zone ID for SES domain verification (optional)"
  type        = string
  default     = null
}

variable "ses_from_addresses" {
  description = "List of allowed from email addresses for SES"
  type        = list(string)
  default     = [
    "noreply@example.com",
    "support@example.com",
    "notifications@example.com"
  ]
}