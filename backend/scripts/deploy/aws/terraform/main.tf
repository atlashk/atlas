terraform {
  required_version = ">= 1.11"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # Remote state backend configuration with S3 native locking (Terraform 1.11+)
  # Note: Run bootstrap module first to create these resources
  # Then uncomment and configure with actual values from bootstrap output
  backend "s3" {
    bucket     = "atlas-terraform-state-dev-9d7b2af2"
    key        = "infrastructure/terraform.tfstate"
    region     = "us-east-1"
    encrypt    = true
    kms_key_id = "arn:aws:kms:us-east-1:604803839284:key/c0cbf423-0b03-43d4-847a-7ed529a78edf"
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# Local values
locals {
  name_prefix = "${var.project_name}-${var.environment}"
  
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  services = [
    "user-service",
    "product-service", 
    "order-service",
    "payment-service",
    "api-gateway"
  ]
}

# VPC and Networking
module "vpc" {
  source = "./modules/infrastructure/vpc"
  
  name_prefix         = local.name_prefix
  vpc_cidr           = var.vpc_cidr
  availability_zones = slice(data.aws_availability_zones.available.names, 0, 2)
  
  tags = local.common_tags
}

# Security Groups
module "security_groups" {
  source = "./modules/infrastructure/security"
  
  name_prefix         = local.name_prefix
  vpc_id              = module.vpc.vpc_id
  allowed_cidr_blocks = var.allowed_cidr_blocks
  
  tags = local.common_tags
}

# RDS Database with AWS managed passwords
module "rds" {
  source = "./modules/infrastructure/rds"
  
  name_prefix           = local.name_prefix
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  security_group_ids   = [module.security_groups.rds_security_group_id]
  
  # Database engine configuration
  db_engine                  = var.db_engine
  db_engine_version         = var.db_engine_version
  db_port                   = var.db_port
  db_parameter_group_family = var.db_parameter_group_family
  
  # Database instance configuration
  db_name                     = var.db_name
  manage_master_user_password = true  # AWS manages password in Secrets Manager
  db_instance_class           = var.db_instance_class
  db_allocated_storage        = var.db_allocated_storage
  db_backup_retention_period  = var.db_backup_retention_period
  multi_az                    = var.multi_az
  
  # Monitoring configuration
  enable_enhanced_monitoring  = var.enable_enhanced_monitoring
  enable_performance_insights = var.enable_performance_insights
  
  tags = local.common_tags
}

# ElastiCache Cluster with auto-generated auth token
module "elasticache" {
  source = "./modules/infrastructure/elasticache"
  
  name_prefix                    = local.name_prefix
  vpc_id                        = module.vpc.vpc_id
  private_subnet_ids            = module.vpc.private_subnet_ids
  security_group_ids            = [module.security_groups.elasticache_security_group_id]
  elasticache_node_type         = var.elasticache_node_type
  elasticache_num_cache_nodes   = var.elasticache_num_cache_nodes
  # REMOVED: redis_password - now auto-generated and stored in Secrets Manager
  
  tags = local.common_tags
}

# S3 Bucket
module "s3" {
  source = "./modules/infrastructure/s3"
  
  name_prefix = local.name_prefix
  
  tags = local.common_tags
}

# SES (Simple Email Service)
module "ses" {
  source = "./modules/infrastructure/ses"
  
  name_prefix     = local.name_prefix
  domain_name     = var.ses_domain_name
  route53_zone_id = var.ses_route53_zone_id
  
  from_addresses = var.ses_from_addresses
  allowed_senders = [
    data.aws_caller_identity.current.account_id
  ]
  
  trusted_services = [
    "ec2.amazonaws.com",
    "ecs-tasks.amazonaws.com",
    "lambda.amazonaws.com"
  ]
  
  tags = local.common_tags
}

# Stripe
module "stripe" {
  source = "./modules/infrastructure/stripe"
  
  name_prefix                    = local.name_prefix
  stripe_secret_key              = var.stripe_secret_key
  stripe_publishable_key         = var.stripe_publishable_key
  stripe_webhook_endpoint_secret = var.stripe_webhook_endpoint_secret
  
  tags = local.common_tags
}

# MSK (Managed Streaming for Apache Kafka)
module "msk" {
  source = "./modules/infrastructure/msk"
  
  name_prefix           = local.name_prefix
  private_subnet_ids    = module.vpc.private_subnet_ids
  security_group_ids    = [module.security_groups.msk_security_group_id]
  
  # MSK Configuration
  number_of_broker_nodes = var.msk_number_of_broker_nodes
  broker_instance_type   = var.msk_broker_instance_type
  broker_volume_size     = var.msk_broker_volume_size

  # Kafka Configuration
  kafka_replication_factor    = var.kafka_replication_factor
  kafka_min_insync_replicas   = var.kafka_min_insync_replicas
  kafka_num_partitions        = var.kafka_num_partitions
  kafka_log_retention_hours   = var.kafka_log_retention_hours
  kafka_log_retention_bytes   = var.kafka_log_retention_bytes
  kafka_log_segment_bytes     = var.kafka_log_segment_bytes
  
  tags = local.common_tags
}

# Cloud Map Service Discovery
module "cloudmap" {
  source = "./modules/infrastructure/cloudmap"
  
  name_prefix = local.name_prefix
  environment = var.environment
  vpc_id      = module.vpc.vpc_id
  
  tags = local.common_tags
}

# User Service with Secrets Manager integration
module "user_service" {
  source = "./modules/application/user-service"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids

  # Database configuration
  db_host     = module.rds.db_endpoint
  db_port     = var.db_port
  db_name     = var.db_name
  db_username = module.rds.db_username  # Use AWS-managed username
  # Use secret ARN instead of plain password
  db_secret_arn        = module.rds.db_secret_arn
  db_secret_kms_key_id = module.rds.db_secret_kms_key_id
  
  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  # Use secret ARN instead of plain password
  redis_secret_arn        = module.elasticache.elasticache_secret_arn
  redis_secret_kms_key_id = module.elasticache.elasticache_secret_kms_key_id
  
  # MSK Configuration
  msk_bootstrap_brokers = module.msk.bootstrap_brokers_sasl_iam

  # Service Discovery
  service_discovery_arn = module.cloudmap.user_service_discovery_arn
  
  # API Client Configuration
  api_client_type           = var.api_client_type
  product_service_endpoint  = var.product_service_endpoint
  order_service_endpoint    = var.order_service_endpoint
  payment_service_endpoint  = var.payment_service_endpoint
  
  # IAM Configuration
  msk_cluster_arn   = module.msk.cluster_arn
  
  tags = local.common_tags
}

# Product Service
module "product_service" {
  source = "./modules/application/product-service"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids

  # Database configuration
  db_host     = module.rds.db_endpoint
  db_port     = var.db_port
  db_name     = var.db_name
  db_username = module.rds.db_username  # ✅ Use AWS-managed username
  # Use secret ARN instead of plain password
  db_secret_arn        = module.rds.db_secret_arn
  db_secret_kms_key_id = module.rds.db_secret_kms_key_id
  
  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  # Use secret ARN instead of plain password
  redis_secret_arn        = module.elasticache.elasticache_secret_arn
  redis_secret_kms_key_id = module.elasticache.elasticache_secret_kms_key_id
  
  # MSK Configuration
  msk_bootstrap_brokers = module.msk.bootstrap_brokers_sasl_iam
  
  # S3 configuration
  s3_product_image_bucket_name = module.s3.bucket_name
  s3_product_image_policy_arn  = module.s3.policy_arn
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.product_service_discovery_arn
  
  # API Client Configuration
  api_client_type          = var.api_client_type
  user_service_endpoint    = var.user_service_endpoint
  order_service_endpoint   = var.order_service_endpoint
  payment_service_endpoint = var.payment_service_endpoint
  
  # IAM Configuration
  msk_cluster_arn   = module.msk.cluster_arn
  s3_bucket_arn     = module.s3.bucket_arn
  
  tags = local.common_tags
}

# Order Service
module "order_service" {
  source = "./modules/application/order-service"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids
  
  # Database configuration
  db_host     = module.rds.db_endpoint
  db_port     = var.db_port
  db_name     = var.db_name
  db_username = module.rds.db_username  # Use AWS-managed username
  # Use secret ARN instead of plain password
  db_secret_arn        = module.rds.db_secret_arn
  db_secret_kms_key_id = module.rds.db_secret_kms_key_id

  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  # Use secret ARN instead of plain password
  redis_secret_arn        = module.elasticache.elasticache_secret_arn
  redis_secret_kms_key_id = module.elasticache.elasticache_secret_kms_key_id
  
  # MSK Configuration
  msk_bootstrap_brokers = module.msk.bootstrap_brokers_sasl_iam
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.order_service_discovery_arn
  
  # API Client Configuration
  api_client_type           = var.api_client_type
  user_service_endpoint     = var.user_service_endpoint
  product_service_endpoint  = var.product_service_endpoint
  payment_service_endpoint  = var.payment_service_endpoint
  
  # IAM Configuration
  msk_cluster_arn   = module.msk.cluster_arn
  enable_ses_access = true   # Order service needs SES for order notifications
  
  tags = local.common_tags
}

# Payment Service
module "payment_service" {
  source = "./modules/application/payment-service"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids

  # Database configuration
  db_host     = module.rds.db_endpoint
  db_port     = var.db_port
  db_name     = var.db_name
  db_username = module.rds.db_username  # ✅ Use AWS-managed username
  # Use secret ARN instead of plain password
  db_secret_arn        = module.rds.db_secret_arn
  db_secret_kms_key_id = module.rds.db_secret_kms_key_id

  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  # Use secret ARN instead of plain password
  redis_secret_arn        = module.elasticache.elasticache_secret_arn
  redis_secret_kms_key_id = module.elasticache.elasticache_secret_kms_key_id
  
  # MSK Configuration
  msk_bootstrap_brokers = module.msk.bootstrap_brokers_sasl_iam
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.payment_service_discovery_arn
  
  # Stripe Configuration
  stripe_secret_key_arn              = module.stripe.stripe_secret_key_arn
  stripe_publishable_key_arn         = module.stripe.stripe_publishable_key_arn
  stripe_webhook_endpoint_secret_arn = module.stripe.stripe_webhook_endpoint_secret_arn
  stripe_secrets_kms_key_id          = module.stripe.stripe_secrets_kms_key_id
  
  # API Client Configuration
  api_client_type          = var.api_client_type
  user_service_endpoint    = var.user_service_endpoint
  product_service_endpoint = var.product_service_endpoint
  order_service_endpoint   = var.order_service_endpoint
  
  # IAM Configuration
  msk_cluster_arn   = module.msk.cluster_arn
  
  tags = local.common_tags
}

# API Gateway
module "api_gateway" {
  source = "./modules/application/api-gateway"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids
  

  
  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  # Use secret ARN instead of plain password
  redis_secret_arn        = module.elasticache.elasticache_secret_arn
  redis_secret_kms_key_id = module.elasticache.elasticache_secret_kms_key_id
  
  # User service DNS for JWK Set URI
  user_service_dns = module.cloudmap.user_service_dns
  
  # AWS Region
  aws_region = var.aws_region
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.api_gateway_discovery_arn
  
  tags = local.common_tags
}

# SNS Topic for CloudWatch Alarms
module "sns_alarms" {
  source = "./modules/observability/sns"
  
  name_prefix               = local.name_prefix
  alarm_notification_email  = var.alarm_notification_email
  
  tags = local.common_tags
}

# CloudWatch Log Groups and Alarms
module "cloudwatch_log_groups" {
  source = "./modules/observability/cloudwatch"
  
  name_prefix           = local.name_prefix
  services              = local.services
  alarm_actions         = [module.sns_alarms.sns_topic_arn]
  alb_arn_suffix        = try(split("/", module.api_gateway.alb_arn)[1], "")
  enable_msk_monitoring = true
  
  tags = local.common_tags
}

# Auto Scaling for User Service
module "user_service_autoscaling" {
  source = "./modules/infrastructure/autoscaling"
  
  name_prefix                    = local.name_prefix
  service_name                   = "user-service"
  cluster_name                   = module.user_service.ecs_cluster_name
  min_capacity                   = var.ecs_min_capacity
  max_capacity                   = var.ecs_max_capacity
  target_cpu_utilization         = var.ecs_target_cpu_utilization
  target_memory_utilization      = var.ecs_target_memory_utilization
  # ALB-based scaling disabled to avoid computed value issues during initial deployment
  # alb_target_group_arn          = module.user_service.target_group_arn
  # alb_full_name                 = module.user_service.alb_dns_name
  # alb_target_group_name         = module.user_service.target_group_name
  enable_scheduled_scaling       = var.enable_scheduled_scaling
  scale_up_schedule             = var.scale_up_schedule
  scale_down_schedule           = var.scale_down_schedule
  scheduled_min_capacity        = var.scheduled_min_capacity
  
  tags = local.common_tags
}

# Auto Scaling for Product Service
module "product_service_autoscaling" {
  source = "./modules/infrastructure/autoscaling"
  
  name_prefix                    = local.name_prefix
  service_name                   = "product-service"
  cluster_name                   = module.product_service.cluster_name
  min_capacity                   = var.ecs_min_capacity
  max_capacity                   = var.ecs_max_capacity
  target_cpu_utilization         = var.ecs_target_cpu_utilization
  target_memory_utilization      = var.ecs_target_memory_utilization
  # ALB-based scaling disabled to avoid computed value issues during initial deployment
  # alb_target_group_arn          = module.product_service.target_group_arn
  # alb_full_name                 = module.product_service.alb_dns_name
  # alb_target_group_name         = module.product_service.target_group_name
  enable_scheduled_scaling       = var.enable_scheduled_scaling
  scale_up_schedule             = var.scale_up_schedule
  scale_down_schedule           = var.scale_down_schedule
  scheduled_min_capacity        = var.scheduled_min_capacity
  
  tags = local.common_tags
}

# Auto Scaling for Order Service
module "order_service_autoscaling" {
  source = "./modules/infrastructure/autoscaling"
  
  name_prefix                    = local.name_prefix
  service_name                   = "order-service"
  cluster_name                   = module.order_service.cluster_name
  min_capacity                   = var.ecs_min_capacity
  max_capacity                   = var.ecs_max_capacity
  target_cpu_utilization         = var.ecs_target_cpu_utilization
  target_memory_utilization      = var.ecs_target_memory_utilization
  # ALB-based scaling disabled to avoid computed value issues during initial deployment
  # alb_target_group_arn          = module.order_service.target_group_arn
  # alb_full_name                 = module.order_service.alb_dns_name
  # alb_target_group_name         = module.order_service.target_group_name
  enable_scheduled_scaling       = var.enable_scheduled_scaling
  scale_up_schedule             = var.scale_up_schedule
  scale_down_schedule           = var.scale_down_schedule
  scheduled_min_capacity        = var.scheduled_min_capacity
  
  tags = local.common_tags
}

# Auto Scaling for Payment Service
module "payment_service_autoscaling" {
  source = "./modules/infrastructure/autoscaling"
  
  name_prefix                    = local.name_prefix
  service_name                   = "payment-service"
  cluster_name                   = module.payment_service.cluster_name
  min_capacity                   = var.ecs_min_capacity
  max_capacity                   = var.ecs_max_capacity
  target_cpu_utilization         = var.ecs_target_cpu_utilization
  target_memory_utilization      = var.ecs_target_memory_utilization
  # ALB-based scaling disabled to avoid computed value issues during initial deployment
  # alb_target_group_arn          = module.payment_service.target_group_arn
  # alb_full_name                 = module.payment_service.alb_dns_name
  # alb_target_group_name         = module.payment_service.target_group_name
  enable_scheduled_scaling       = var.enable_scheduled_scaling
  scale_up_schedule             = var.scale_up_schedule
  scale_down_schedule           = var.scale_down_schedule
  scheduled_min_capacity        = var.scheduled_min_capacity
  
  tags = local.common_tags
}

# Auto Scaling for API Gateway
module "api_gateway_autoscaling" {
  source = "./modules/infrastructure/autoscaling"
  
  name_prefix                    = local.name_prefix
  service_name                   = "api-gateway"
  cluster_name                   = module.api_gateway.ecs_cluster_name
  min_capacity                   = var.ecs_min_capacity
  max_capacity                   = var.ecs_max_capacity
  target_cpu_utilization         = var.ecs_target_cpu_utilization
  target_memory_utilization      = var.ecs_target_memory_utilization
  # ALB-based scaling disabled to avoid computed value issues during initial deployment
  # alb_target_group_arn          = module.api_gateway.target_group_arn
  # alb_full_name                 = module.api_gateway.alb_dns_name
  # alb_target_group_name         = module.api_gateway.target_group_name
  enable_scheduled_scaling       = var.enable_scheduled_scaling
  scale_up_schedule             = var.scale_up_schedule
  scale_down_schedule           = var.scale_down_schedule
  scheduled_min_capacity        = var.scheduled_min_capacity
  
  tags = local.common_tags
}
