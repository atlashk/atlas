# ElastiCache Subnet Group
resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.name_prefix}-elasticache-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-subnet-group"
  })
}

# ElastiCache Parameter Group
resource "aws_elasticache_parameter_group" "main" {
  family = "redis7"
  name   = "${var.name_prefix}-elasticache-params"

  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-params"
  })
}

# ElastiCache Replication Group (Redis Cluster)
resource "aws_elasticache_replication_group" "main" {
  replication_group_id         = "${var.name_prefix}-elasticache"
  description                  = "ElastiCache cluster for ${var.name_prefix}"
  
  # Engine
  engine               = "redis"
  engine_version       = "7.0"
  node_type           = var.elasticache_node_type
  port                = 6379
  
  # Cluster configuration
  num_cache_clusters = var.elasticache_num_cache_nodes
  
  # Network
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = var.security_group_ids
  
  # Parameter group
  parameter_group_name = aws_elasticache_parameter_group.main.name
  
  # Backup
  snapshot_retention_limit = 5
  snapshot_window         = "03:00-05:00"
  maintenance_window      = "sun:05:00-sun:07:00"
  
  # Security
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  auth_token                = var.elasticache_auth_token
  
  # Automatic failover
  automatic_failover_enabled = var.elasticache_num_cache_nodes > 1
  multi_az_enabled          = var.elasticache_num_cache_nodes > 1
  
  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache"
  })
}