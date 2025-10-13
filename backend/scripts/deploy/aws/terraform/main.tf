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
  source = "./modules/vpc"
  
  name_prefix         = local.name_prefix
  vpc_cidr           = var.vpc_cidr
  availability_zones = slice(data.aws_availability_zones.available.names, 0, 2)
  
  tags = local.common_tags
}

# Security Groups
module "security_groups" {
  source = "./modules/security"
  
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id
  
  tags = local.common_tags
}

# RDS Database (MySQL or PostgreSQL)
module "rds" {
  source = "./modules/rds"
  
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
  source = "./modules/elasticache"
  
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
  source = "./modules/s3"
  
  name_prefix = local.name_prefix
  
  tags = local.common_tags
}

# MSK (Managed Streaming for Apache Kafka)
module "msk" {
  source = "./modules/msk"
  
  name_prefix           = local.name_prefix
  private_subnet_ids    = module.vpc.private_subnet_ids
  security_group_ids    = [module.security_groups.msk_security_group_id]
  
  # MSK Configuration
  number_of_broker_nodes = var.msk_number_of_broker_nodes
  broker_instance_type   = var.msk_broker_instance_type
  broker_volume_size     = var.msk_broker_volume_size
  
  tags = local.common_tags
}

# ECS Cluster
module "ecs" {
  source = "./modules/ecs"
  
  name_prefix           = local.name_prefix
  vpc_id               = module.vpc.vpc_id
  public_subnet_ids    = module.vpc.public_subnet_ids
  private_subnet_ids   = module.vpc.private_subnet_ids
  
  # Security groups
  alb_security_group_id = module.security_groups.alb_security_group_id
  ecs_security_group_id = module.security_groups.ecs_security_group_id
  
  # Database and cache endpoints
  db_endpoint        = module.rds.db_endpoint
  elasticache_endpoint = module.elasticache.elasticache_endpoint
  s3_bucket_name  = module.s3.bucket_name
  
  # MSK Configuration
  msk_cluster_arn                = module.msk.cluster_arn
  msk_bootstrap_brokers          = module.msk.bootstrap_brokers_sasl_iam
  msk_client_role_arn           = module.msk.msk_client_role_arn
  kafka_topics                  = module.msk.kafka_topics
  
  # Services configuration
  services = local.services
  
  # Environment variables
  db_name     = var.db_name
  db_username = var.db_username
  db_password = var.db_password
  db_port     = var.db_port
  
  tags = local.common_tags
}

# CloudWatch Log Groups
module "cloudwatch" {
  source = "./modules/cloudwatch"
  
  name_prefix = local.name_prefix
  services    = local.services
  
  tags = local.common_tags
}