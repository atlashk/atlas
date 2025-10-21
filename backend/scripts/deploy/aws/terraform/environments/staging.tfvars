# Staging Environment Configuration

# Basic Configuration
environment    = "staging"
project_name   = "atlas"
aws_region     = "us-east-1"

# VPC Configuration
vpc_cidr = "10.1.0.0/16"

# Database Configuration
db_instance_class     = "db.t3.small"
db_allocated_storage  = 50
db_backup_retention_period = 7

# ECS Configuration
ecs_min_capacity              = 2
ecs_max_capacity              = 6
ecs_target_cpu_utilization    = 70
ecs_target_memory_utilization = 80

# Security Configuration
allowed_cidr_blocks = ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"]  # Private networks only

# Monitoring Configuration
alarm_notification_email = "staging-alerts@company.com"

# Feature Flags
enable_waf                   = true
enable_deletion_protection   = false
enable_detailed_monitoring   = true
enable_cross_region_backup   = false

# SES Configuration
ses_domain_name     = "staging.atlas.com"
ses_route53_zone_id = "Z1234567890STG"
ses_from_addresses  = ["noreply@staging.atlas.com", "support@staging.atlas.com"]

# API Client Configuration
api_client_type          = "rest"
user_service_endpoint    = "http://atlas-staging-user-service-alb-123456789.us-east-1.elb.amazonaws.com"
product_service_endpoint = "http://atlas-staging-product-service-alb-123456789.us-east-1.elb.amazonaws.com"
order_service_endpoint   = "http://atlas-staging-order-service-alb-123456789.us-east-1.elb.amazonaws.com"
payment_service_endpoint = "http://atlas-staging-payment-service-alb-123456789.us-east-1.elb.amazonaws.com"

# Stripe Configuration (Staging - Test Keys)
# Note: Replace with actual Stripe test keys for staging
stripe_secret_key              = ""  # Add your Stripe test secret key here (sk_test_...)
stripe_publishable_key         = ""  # Add your Stripe test publishable key here (pk_test_...)
stripe_webhook_endpoint_secret = ""  # Add your Stripe webhook endpoint secret here (whsec_...)