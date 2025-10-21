# General Configuration
variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
  validation {
    condition = can(regex("^[a-z]{2}-[a-z]+-[0-9]$", var.aws_region))
    error_message = "AWS region must be in the format like 'us-east-1'."
  }
}

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "atlas"
  validation {
    condition = can(regex("^[a-z][a-z0-9-]*[a-z0-9]$", var.project_name)) && length(var.project_name) >= 3 && length(var.project_name) <= 20
    error_message = "Project name must be 3-20 characters, start with a letter, contain only lowercase letters, numbers, and hyphens, and end with a letter or number."
  }
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
  validation {
    condition = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

# VPC Configuration
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
  validation {
    condition = can(cidrhost(var.vpc_cidr, 0))
    error_message = "VPC CIDR must be a valid IPv4 CIDR block."
  }
  validation {
    condition = can(regex("^10\\.|^172\\.(1[6-9]|2[0-9]|3[0-1])\\.|^192\\.168\\.", var.vpc_cidr))
    error_message = "VPC CIDR must use private IP address ranges (10.0.0.0/8, 172.16.0.0/12, or 192.168.0.0/16)."
  }
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

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
  validation {
    condition = can(regex("^db\\.[a-z0-9]+\\.[a-z0-9]+$", var.db_instance_class))
    error_message = "Database instance class must be a valid RDS instance type (e.g., db.t3.micro, db.r6g.large)."
  }
}

variable "db_allocated_storage" {
  description = "Allocated storage for RDS instance (GB)"
  type        = number
  default     = 20
  validation {
    condition = var.db_allocated_storage >= 20 && var.db_allocated_storage <= 65536
    error_message = "Database allocated storage must be between 20 and 65536 GB."
  }
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
  validation {
    condition = alltrue([for email in var.ses_from_addresses : can(regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email))])
    error_message = "All SES from addresses must be valid email addresses."
  }
}

# Security Configuration
variable "allowed_cidr_blocks" {
  description = "List of CIDR blocks allowed to access the application"
  type        = list(string)
  default     = ["0.0.0.0/0"]
  validation {
    condition = alltrue([for cidr in var.allowed_cidr_blocks : can(cidrhost(cidr, 0))])
    error_message = "All CIDR blocks must be valid IPv4 CIDR notation."
  }
}

variable "enable_waf" {
  description = "Enable AWS WAF for the Application Load Balancer"
  type        = bool
  default     = false
}

variable "enable_deletion_protection" {
  description = "Enable deletion protection for critical resources"
  type        = bool
  default     = true
}

# Auto Scaling Configuration
variable "ecs_min_capacity" {
  description = "Minimum number of ECS tasks"
  type        = number
  default     = 1
  validation {
    condition = var.ecs_min_capacity >= 1 && var.ecs_min_capacity <= 100
    error_message = "ECS minimum capacity must be between 1 and 100."
  }
}

variable "ecs_max_capacity" {
  description = "Maximum number of ECS tasks"
  type        = number
  default     = 10
  validation {
    condition = var.ecs_max_capacity >= 1 && var.ecs_max_capacity <= 1000
    error_message = "ECS maximum capacity must be between 1 and 1000."
  }
}

variable "ecs_target_cpu_utilization" {
  description = "Target CPU utilization percentage for ECS auto scaling"
  type        = number
  default     = 70
  validation {
    condition = var.ecs_target_cpu_utilization >= 10 && var.ecs_target_cpu_utilization <= 90
    error_message = "ECS target CPU utilization must be between 10 and 90 percent."
  }
}

variable "ecs_target_memory_utilization" {
  description = "Target memory utilization percentage for ECS auto scaling"
  type        = number
  default     = 80
  validation {
    condition = var.ecs_target_memory_utilization >= 10 && var.ecs_target_memory_utilization <= 90
    error_message = "ECS target memory utilization must be between 10 and 90 percent."
  }
}

# Monitoring Configuration
variable "enable_detailed_monitoring" {
  description = "Enable detailed CloudWatch monitoring"
  type        = bool
  default     = true
}

variable "alarm_notification_email" {
  description = "Email address for CloudWatch alarm notifications"
  type        = string
  default     = ""
  validation {
    condition = var.alarm_notification_email == "" || can(regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", var.alarm_notification_email))
    error_message = "Alarm notification email must be a valid email address or empty string."
  }
}

# Backup Configuration
variable "db_backup_retention_period" {
  description = "Number of days to retain automated backups"
  type        = number
  default     = 7
  validation {
    condition = var.db_backup_retention_period >= 1 && var.db_backup_retention_period <= 35
    error_message = "Backup retention period must be between 1 and 35 days."
  }
}

variable "enable_cross_region_backup" {
  description = "Enable cross-region backup for disaster recovery"
  type        = bool
  default     = false
}

# Stripe Configuration
variable "stripe_secret_key" {
  description = "Stripe secret key for payment processing"
  type        = string
  default     = ""
  sensitive   = true
  validation {
    condition = var.stripe_secret_key == "" || can(regex("^sk_(test_|live_)[a-zA-Z0-9]+$", var.stripe_secret_key))
    error_message = "Stripe secret key must be empty or start with 'sk_test_' or 'sk_live_' followed by alphanumeric characters."
  }
}

variable "stripe_publishable_key" {
  description = "Stripe publishable key for client-side payment processing"
  type        = string
  default     = ""
  sensitive   = true
  validation {
    condition = var.stripe_publishable_key == "" || can(regex("^pk_(test_|live_)[a-zA-Z0-9]+$", var.stripe_publishable_key))
    error_message = "Stripe publishable key must be empty or start with 'pk_test_' or 'pk_live_' followed by alphanumeric characters."
  }
}

variable "stripe_webhook_endpoint_secret" {
  description = "Stripe webhook endpoint secret for webhook signature verification"
  type        = string
  default     = ""
  sensitive   = true
  validation {
    condition = var.stripe_webhook_endpoint_secret == "" || can(regex("^whsec_[a-zA-Z0-9]+$", var.stripe_webhook_endpoint_secret))
    error_message = "Stripe webhook endpoint secret must be empty or start with 'whsec_' followed by alphanumeric characters."
  }
}