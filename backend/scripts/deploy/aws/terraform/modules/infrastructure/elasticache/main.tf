# Random auth token for ElastiCache
resource "random_password" "elasticache_auth_token" {
  length  = 32
  special = true
}

# KMS Key for ElastiCache Secrets
resource "aws_kms_key" "elasticache_secrets" {
  description             = "KMS key for ElastiCache secrets encryption"
  deletion_window_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-secrets-key"
  })
}

resource "aws_kms_alias" "elasticache_secrets" {
  name          = "alias/${var.name_prefix}-elasticache-secrets"
  target_key_id = aws_kms_key.elasticache_secrets.key_id
}

# Store auth token in Secrets Manager
resource "aws_secretsmanager_secret" "elasticache_auth_token" {
  name                    = "${var.name_prefix}-elasticache-auth-token"
  description             = "ElastiCache auth token for ${var.name_prefix}"
  kms_key_id             = aws_kms_key.elasticache_secrets.key_id
  recovery_window_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-auth-token"
  })
}

resource "aws_secretsmanager_secret_version" "elasticache_auth_token" {
  secret_id     = aws_secretsmanager_secret.elasticache_auth_token.id
  secret_string = jsonencode({
    auth_token = random_password.elasticache_auth_token.result
  })
}

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
  # Use auto-generated auth token instead of manual variable
  auth_token                = random_password.elasticache_auth_token.result
  
  # Automatic failover
  automatic_failover_enabled = var.elasticache_num_cache_nodes > 1
  multi_az_enabled          = var.elasticache_num_cache_nodes > 1
  
  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache"
  })
}

# IAM Policy for ECS tasks to access ElastiCache secrets
resource "aws_iam_policy" "elasticache_secrets_access" {
  name        = "${var.name_prefix}-elasticache-secrets-access"
  description = "Policy for ECS tasks to access ElastiCache secrets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = aws_secretsmanager_secret.elasticache_auth_token.arn
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = aws_kms_key.elasticache_secrets.arn
      }
    ]
  })

  tags = var.tags
}