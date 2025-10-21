# Development Environment Configuration

# Feature Flags
enable_waf                   = false
enable_deletion_protection   = false
enable_detailed_monitoring   = false
enable_cross_region_backup   = false

# Basic Configuration
environment    = "dev"
project_name   = "atlas"
aws_region     = "us-east-1"

# VPC Configuration
vpc_cidr = "10.0.0.0/16"

# Security Configuration
allowed_cidr_blocks = ["0.0.0.0/0"]  # More permissive for dev

# Database Configuration
db_instance_class          = "db.t3.micro"
db_allocated_storage       = 20
db_backup_retention_period = 1      # Reduced to 1 day for dev cost optimization
multi_az                   = false  # Single instance for dev environment
enable_enhanced_monitoring = false  # Disable enhanced monitoring for cost savings
enable_performance_insights = false # Disable Performance Insights for cost savings

# ECS Configuration
ecs_min_capacity              = 1
ecs_max_capacity              = 3
ecs_target_cpu_utilization    = 70
ecs_target_memory_utilization = 80

# Scheduled Scaling Configuration (Cost optimization for dev)
enable_scheduled_scaling = true
scale_down_schedule     = "cron(0 18 * * ? *)"  # Scale down at 6 PM UTC
scale_up_schedule       = "cron(0 8 * * ? *)"   # Scale up at 8 AM UTC
scheduled_min_capacity  = 1                      # Minimum during business hours

# ElastiCache Configuration (Cost-optimized for dev)
elasticache_node_type = "cache.t4g.nano"  # Smallest ARM-based instance for cost savings
elasticache_num_cache_nodes = 1            # Single node for dev environment

# MSK Configuration (Cost-optimized for demo - Single broker)
msk_number_of_broker_nodes = 1          # Single broker for demo (no replication)
msk_broker_instance_type   = "kafka.t3.small"  # Smallest production-ready instance
msk_broker_volume_size     = 20          # Reduced from 100GB to 20GB for demo

# Kafka Configuration (Optimized for single broker dev environment)
kafka_replication_factor    = 1          # Must be 1 for single broker
kafka_min_insync_replicas   = 1          # Must be 1 for single broker
kafka_num_partitions        = 1          # Minimal partitions for demo
kafka_log_retention_hours   = 24         # 1 day retention for demo
kafka_log_retention_bytes   = 268435456  # 256MB retention per topic
kafka_log_segment_bytes     = 268435456  # 256MB segment size

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

# Monitoring Configuration
alarm_notification_email = "dev-alerts@company.com"
