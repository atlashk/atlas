terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
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
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  
  tags = local.common_tags
}

# RDS Database (MySQL or PostgreSQL)
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
  db_name              = var.db_name
  db_username          = var.db_username
  db_password          = var.db_password
  db_instance_class    = var.db_instance_class
  db_allocated_storage = var.db_allocated_storage
  
  tags = local.common_tags
}

# ElastiCache Cluster
module "elasticache" {
  source = "./modules/infrastructure/elasticache"
  
  name_prefix                    = local.name_prefix
  vpc_id                        = module.vpc.vpc_id
  private_subnet_ids            = module.vpc.private_subnet_ids
  security_group_ids            = [module.security_groups.elasticache_security_group_id]
  elasticache_node_type         = var.elasticache_node_type
  elasticache_num_cache_nodes   = var.elasticache_num_cache_nodes
  
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

# User Service
module "user_service" {
  source = "./modules/application/user-service"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids

  # Database configuration
  db_host     = module.rds.db_endpoint
  db_port     = var.db_port
  db_name     = var.db_name
  db_username = var.db_username
  db_password = var.db_password
  
  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  redis_password      = var.redis_password
  
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
  s3_bucket_arn     = module.s3.bucket_arn
  enable_s3_access  = false  # User service doesn't need S3 access by default
  
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
  db_username = var.db_username
  db_password = var.db_password
  
  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  redis_password      = var.redis_password
  
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
  enable_ses_access = false  # Product service doesn't need SES access by default
  
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
  db_username = var.db_username
  db_password = var.db_password

  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  redis_password      = var.redis_password
  
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
  s3_bucket_arn     = module.s3.bucket_arn
  enable_s3_access  = false  # Order service doesn't need S3 access by default
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
  db_username = var.db_username
  db_password = var.db_password

  # Redis configuration
  redis_cluster_nodes = module.elasticache.elasticache_endpoint
  redis_password      = var.redis_password
  
  # MSK Configuration
  msk_bootstrap_brokers = module.msk.bootstrap_brokers_sasl_iam
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.payment_service_discovery_arn
  
  # API Client Configuration
  api_client_type          = var.api_client_type
  user_service_endpoint    = var.user_service_endpoint
  product_service_endpoint = var.product_service_endpoint
  order_service_endpoint   = var.order_service_endpoint
  
  # IAM Configuration
  msk_cluster_arn   = module.msk.cluster_arn
  s3_bucket_arn     = module.s3.bucket_arn
  enable_s3_access  = false  # Payment service doesn't need S3 access by default
  enable_ses_access = true   # Payment service needs SES for payment notifications
  
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
  redis_password      = var.redis_password
  
  # User service DNS for JWK Set URI
  user_service_dns = module.cloudmap.user_service_dns
  
  # AWS Region
  aws_region = var.aws_region
  
  # Service Discovery
  service_discovery_arn = module.cloudmap.api_gateway_discovery_arn
  
  tags = local.common_tags
}

# CloudWatch Log Groups
module "cloudwatch_log_groups" {
  source = "./modules/observability/cloudwatch"
  
  name_prefix = local.name_prefix
  services    = local.services
  
  tags = local.common_tags
}