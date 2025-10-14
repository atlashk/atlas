# VPC Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = module.vpc.private_subnet_ids
}

# Database Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.db_endpoint
  sensitive   = true
}

output "rds_port" {
  description = "RDS instance port"
  value       = module.rds.db_port
}

# ElastiCache Outputs
output "elasticache_endpoint" {
  description = "ElastiCache cluster endpoint"
  value       = module.elasticache.elasticache_endpoint
  sensitive   = true
}

output "elasticache_port" {
  description = "ElastiCache cluster port"
  value       = module.elasticache.elasticache_port
}

# S3 Outputs
output "s3_bucket_name" {
  description = "Name of the S3 bucket"
  value       = module.s3.bucket_name
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = module.s3.bucket_arn
}

# MSK Cluster Outputs
output "msk_cluster_arn" {
  description = "ARN of the MSK cluster"
  value       = module.msk.cluster_arn
}

output "msk_cluster_name" {
  description = "Name of the MSK cluster"
  value       = module.msk.cluster_name
}

output "msk_bootstrap_brokers" {
  description = "Bootstrap brokers for the MSK cluster"
  value       = module.msk.bootstrap_brokers
  sensitive   = true
}

output "msk_bootstrap_brokers_sasl_iam" {
  description = "Bootstrap brokers for SASL/IAM authentication"
  value       = module.msk.bootstrap_brokers_sasl_iam
  sensitive   = true
}

output "msk_bootstrap_brokers_tls" {
  description = "Bootstrap brokers for TLS authentication"
  value       = module.msk.bootstrap_brokers_tls
  sensitive   = true
}

output "msk_zookeeper_connect_string" {
  description = "Zookeeper connection string"
  value       = module.msk.zookeeper_connect_string
  sensitive   = true
}

output "msk_client_role_arn" {
  description = "ARN of the IAM role for MSK client access"
  value       = module.msk.msk_client_role_arn
}

output "kafka_topics" {
  description = "Kafka topic names for different event types"
  value       = module.msk.kafka_topics
}

# ECS Outputs
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_cluster_arn" {
  description = "ARN of the ECS cluster"
  value       = module.ecs.cluster_arn
}

output "load_balancer_dns" {
  description = "DNS name of the load balancer"
  value       = module.ecs.load_balancer_dns
}

output "load_balancer_zone_id" {
  description = "Zone ID of the load balancer"
  value       = module.ecs.load_balancer_zone_id
}

# Service URLs
output "api_gateway_url" {
  description = "URL for API Gateway - Single entry point for all services"
  value       = "http://${module.ecs.load_balancer_dns}/api"
}

# SES Outputs
output "ses_domain_identity_arn" {
  description = "ARN of the SES domain identity"
  value       = module.ses.domain_identity_arn
}

output "ses_domain_verification_token" {
  description = "SES domain verification token (add this as TXT record to your DNS)"
  value       = module.ses.domain_identity_verification_token
  sensitive   = true
}

output "ses_dkim_tokens" {
  description = "DKIM tokens for email authentication (add these as CNAME records to your DNS)"
  value       = module.ses.dkim_tokens
  sensitive   = true
}

output "ses_mail_from_domain" {
  description = "Mail from domain for SES"
  value       = module.ses.mail_from_domain
}

output "ses_configuration_set_name" {
  description = "Name of the SES configuration set"
  value       = module.ses.configuration_set_name
}

output "ses_sending_role_arn" {
  description = "ARN of the IAM role for SES email sending"
  value       = module.ses.sending_role_arn
}

output "ses_smtp_endpoint" {
  description = "SMTP endpoint for sending emails via SES"
  value       = module.ses.smtp_endpoint
}

output "ses_smtp_port" {
  description = "SMTP port for TLS connection"
  value       = module.ses.smtp_port
}
