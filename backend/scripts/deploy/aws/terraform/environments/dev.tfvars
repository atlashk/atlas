# Development Environment Configuration

# Basic Configuration
environment    = "dev"
project_name   = "atlas"
aws_region     = "us-west-2"

# VPC Configuration
vpc_cidr = "10.0.0.0/16"

# Database Configuration
db_instance_class     = "db.t3.micro"
db_allocated_storage  = 20
db_backup_retention_period = 1

# ECS Configuration
ecs_min_capacity              = 1
ecs_max_capacity              = 3
ecs_target_cpu_utilization    = 70
ecs_target_memory_utilization = 80

# Security Configuration
allowed_cidr_blocks = ["0.0.0.0/0"]  # More permissive for dev

# Monitoring Configuration
alarm_notification_email = "dev-alerts@company.com"

# Feature Flags
enable_waf                   = false
enable_deletion_protection   = false
enable_detailed_monitoring   = false
enable_cross_region_backup   = false

# SES Configuration
ses_domain_name     = "dev.atlas.com"
ses_route53_zone_id = "Z1234567890DEV"
ses_from_addresses  = ["noreply@dev.atlas.com", "support@dev.atlas.com"]

# API Client Configuration
api_client_type          = "rest"
user_service_endpoint    = "http://atlas-dev-user-service-alb-123456789.us-east-1.elb.amazonaws.com"
product_service_endpoint = "http://atlas-dev-product-service-alb-123456789.us-east-1.elb.amazonaws.com"
order_service_endpoint   = "http://atlas-dev-order-service-alb-123456789.us-east-1.elb.amazonaws.com"
payment_service_endpoint = "http://atlas-dev-payment-service-alb-123456789.us-east-1.elb.amazonaws.com"

# Stripe Configuration (Development - Test Keys)
# Note: Replace with actual Stripe test keys for development
stripe_secret_key              = ""  # Add your Stripe test secret key here (sk_test_...)
stripe_publishable_key         = ""  # Add your Stripe test publishable key here (pk_test_...)
stripe_webhook_endpoint_secret = ""  # Add your Stripe webhook endpoint secret here (whsec_...)