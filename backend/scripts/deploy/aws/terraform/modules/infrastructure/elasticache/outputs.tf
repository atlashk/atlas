output "elasticache_replication_group_id" {
  description = "ID of the ElastiCache replication group"
  value       = aws_elasticache_replication_group.main.id
}

output "elasticache_endpoint" {
  description = "ElastiCache cluster endpoint"
  value       = aws_elasticache_replication_group.main.configuration_endpoint_address != "" ? aws_elasticache_replication_group.main.configuration_endpoint_address : aws_elasticache_replication_group.main.primary_endpoint_address
}

output "elasticache_port" {
  description = "ElastiCache cluster port"
  value       = aws_elasticache_replication_group.main.port
}

# Output secret ARN instead of plain auth token
output "elasticache_secret_arn" {
  description = "ARN of the secret containing ElastiCache auth token"
  value       = aws_secretsmanager_secret.elasticache_auth_token.arn
  sensitive   = true
}

output "elasticache_secret_kms_key_id" {
  description = "KMS key ID used to encrypt the ElastiCache secret"
  value       = aws_kms_key.elasticache_secrets.key_id
}

# IAM policy ARN for ECS tasks to access secrets
output "elasticache_secrets_policy_arn" {
  description = "IAM policy ARN for accessing ElastiCache secrets"
  value       = aws_iam_policy.elasticache_secrets_access.arn
}

# Traditional outputs for backward compatibility
output "elasticache_cluster_address" {
  description = "ElastiCache cluster address"
  value       = aws_elasticache_replication_group.main.primary_endpoint_address
}

output "elasticache_configuration_endpoint" {
  description = "ElastiCache configuration endpoint"
  value       = aws_elasticache_replication_group.main.configuration_endpoint_address
}