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

output "elasticache_auth_token" {
  description = "ElastiCache auth token"
  value       = aws_elasticache_replication_group.main.auth_token
  sensitive   = true
}