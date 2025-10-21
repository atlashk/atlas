# Production Environment Configuration

# Basic Configuration
environment    = "prod"
project_name   = "atlas"
aws_region     = "us-east-1"

# VPC Configuration
vpc_cidr = "10.2.0.0/16"

# Database Configuration
db_instance_class          = "db.r5.large"
db_allocated_storage       = 200
db_backup_retention_period = 30

# ECS Configuration
ecs_min_capacity              = 3
ecs_max_capacity              = 20
ecs_target_cpu_utilization    = 60
ecs_target_memory_utilization = 70

# Security Configuration
allowed_cidr_blocks = ["203.0.113.0/24", "198.51.100.0/24"]  # Specific trusted networks

# Monitoring Configuration
alarm_notification_email = "prod-alerts@company.com"

# Feature Flags
enable_waf                   = true
enable_deletion_protection   = true
enable_detailed_monitoring   = true
enable_cross_region_backup   = true

# SES Configuration
ses_domain_name     = "atlas.com"
ses_route53_zone_id = "Z1234567890PROD"
ses_from_addresses  = ["noreply@atlas.com", "support@atlas.com"]

# Service Endpoints
order_service_endpoint   = "http://prod-order-service:8080"
payment_service_endpoint = "http://prod-payment-service:8080"

# API Client Configuration
api_client_type          = "rest"
user_service_endpoint    = "https://api.atlas.com/user"
product_service_endpoint = "https://api.atlas.com/product"
order_service_endpoint   = "https://api.atlas.com/order"
payment_service_endpoint = "https://api.atlas.com/payment"

# Stripe Configuration (Production - Live Keys)
# IMPORTANT: Replace with actual Stripe live keys for production
# These should be stored securely and never committed to version control
stripe_secret_key              = ""  # Add your Stripe live secret key here (sk_live_...)
stripe_publishable_key         = ""  # Add your Stripe live publishable key here (pk_live_...)
stripe_webhook_endpoint_secret = ""  # Add your Stripe webhook endpoint secret here (whsec_...)