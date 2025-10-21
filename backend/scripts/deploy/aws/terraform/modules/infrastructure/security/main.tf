# Application Load Balancer Security Group
resource "aws_security_group" "alb" {
  name_prefix = "${var.name_prefix}-alb-"
  vpc_id      = var.vpc_id
  description = "Security group for Application Load Balancer"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-alb-sg"
    Type = "ALB Security Group"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# Security Group Rules (defined separately to avoid circular dependencies)

# ALB Security Group Rules
resource "aws_security_group_rule" "alb_http_ingress" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = var.allowed_cidr_blocks
  security_group_id = aws_security_group.alb.id
  description       = "HTTP from allowed CIDR blocks"
}

resource "aws_security_group_rule" "alb_https_ingress" {
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = var.allowed_cidr_blocks
  security_group_id = aws_security_group.alb.id
  description       = "HTTPS from allowed CIDR blocks"
}

resource "aws_security_group_rule" "alb_ecs_egress" {
  type                     = "egress"
  from_port                = 8080
  to_port                  = 8090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.alb.id
  description              = "HTTP to ECS tasks"
}

resource "aws_security_group_rule" "alb_https_egress" {
  type              = "egress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.alb.id
  description       = "HTTPS outbound"
}

resource "aws_security_group_rule" "alb_dns_egress" {
  type              = "egress"
  from_port         = 53
  to_port           = 53
  protocol          = "udp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.alb.id
  description       = "DNS"
}

# ECS Tasks Security Group Rules
resource "aws_security_group_rule" "ecs_alb_ingress" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.alb.id
  security_group_id        = aws_security_group.ecs_tasks.id
  description              = "HTTP from ALB"
}

resource "aws_security_group_rule" "ecs_self_ingress" {
  type              = "ingress"
  from_port         = 8080
  to_port           = 8090
  protocol          = "tcp"
  self              = true
  security_group_id = aws_security_group.ecs_tasks.id
  description       = "Inter-service communication"
}

resource "aws_security_group_rule" "ecs_https_egress" {
  type              = "egress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.ecs_tasks.id
  description       = "HTTPS outbound"
}

resource "aws_security_group_rule" "ecs_http_egress" {
  type              = "egress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.ecs_tasks.id
  description       = "HTTP outbound"
}

resource "aws_security_group_rule" "ecs_dns_egress" {
  type              = "egress"
  from_port         = 53
  to_port           = 53
  protocol          = "udp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.ecs_tasks.id
  description       = "DNS"
}

resource "aws_security_group_rule" "ecs_rds_egress" {
  type                     = "egress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.rds.id
  security_group_id        = aws_security_group.ecs_tasks.id
  description              = "Database access"
}

resource "aws_security_group_rule" "ecs_elasticache_egress" {
  type                     = "egress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.elasticache.id
  security_group_id        = aws_security_group.ecs_tasks.id
  description              = "ElastiCache access"
}

resource "aws_security_group_rule" "ecs_msk_egress" {
  type                     = "egress"
  from_port                = 9092
  to_port                  = 9096
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.msk.id
  security_group_id        = aws_security_group.ecs_tasks.id
  description              = "MSK access"
}

# RDS Security Group Rules
resource "aws_security_group_rule" "rds_ecs_ingress" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.rds.id
  description              = "MySQL from ECS"
}

resource "aws_security_group_rule" "rds_all_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.rds.id
  description       = "All outbound traffic"
}

# ElastiCache Security Group Rules
resource "aws_security_group_rule" "elasticache_ecs_ingress" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.elasticache.id
  description              = "ElastiCache from ECS"
}

resource "aws_security_group_rule" "elasticache_all_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.elasticache.id
  description       = "All outbound traffic"
}

# MSK Security Group Rules
resource "aws_security_group_rule" "msk_kafka_ingress" {
  type                     = "ingress"
  from_port                = 9092
  to_port                  = 9092
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.msk.id
  description              = "Kafka from ECS"
}

resource "aws_security_group_rule" "msk_kafka_tls_ingress" {
  type                     = "ingress"
  from_port                = 9094
  to_port                  = 9094
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.msk.id
  description              = "Kafka TLS from ECS"
}

resource "aws_security_group_rule" "msk_kafka_sasl_ingress" {
  type                     = "ingress"
  from_port                = 9096
  to_port                  = 9096
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.msk.id
  description              = "Kafka SASL from ECS"
}

resource "aws_security_group_rule" "msk_zookeeper_ingress" {
  type                     = "ingress"
  from_port                = 2181
  to_port                  = 2181
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_tasks.id
  security_group_id        = aws_security_group.msk.id
  description              = "Zookeeper from ECS"
}

resource "aws_security_group_rule" "msk_all_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.msk.id
  description       = "All outbound traffic"
}

# MSK Security Group
resource "aws_security_group" "msk" {
  name_prefix = "${var.name_prefix}-msk-"
  vpc_id      = var.vpc_id
  description = "Security group for MSK cluster"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-msk-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# ECS Tasks Security Group
resource "aws_security_group" "ecs_tasks" {
  name_prefix = "${var.name_prefix}-ecs-tasks-"
  vpc_id      = var.vpc_id
  description = "Security group for ECS tasks"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-ecs-tasks-sg"
    Type = "ECS Tasks Security Group"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# RDS Security Group
resource "aws_security_group" "rds" {
  name_prefix = "${var.name_prefix}-rds-"
  vpc_id      = var.vpc_id
  description = "Security group for RDS database"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-rds-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# ElastiCache Security Group
resource "aws_security_group" "elasticache" {
  name_prefix = "${var.name_prefix}-elasticache-"
  vpc_id      = var.vpc_id
  description = "Security group for ElastiCache cluster"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}