# MSK Cluster Configuration
resource "aws_msk_configuration" "main" {
  kafka_versions = ["3.5.1"]
  name           = "${var.name_prefix}-msk-config"

  server_properties = <<PROPERTIES
auto.create.topics.enable=true
default.replication.factor=${var.kafka_replication_factor}
min.insync.replicas=${var.kafka_min_insync_replicas}
num.partitions=${var.kafka_num_partitions}
log.retention.hours=${var.kafka_log_retention_hours}
log.retention.bytes=${var.kafka_log_retention_bytes}
log.segment.bytes=${var.kafka_log_segment_bytes}
log.cleanup.policy=delete
PROPERTIES
}

# MSK Cluster
resource "aws_msk_cluster" "main" {
  cluster_name           = "${var.name_prefix}-msk-cluster"
  kafka_version          = "3.5.1"
  number_of_broker_nodes = var.number_of_broker_nodes

  broker_node_group_info {
    instance_type   = var.broker_instance_type
    client_subnets  = var.private_subnet_ids
    security_groups = var.security_group_ids
    
    storage_info {
      ebs_storage_info {
        volume_size = var.broker_volume_size
      }
    }
  }

  configuration_info {
    arn      = aws_msk_configuration.main.arn
    revision = aws_msk_configuration.main.latest_revision
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
      in_cluster    = true
    }
  }

  client_authentication {
    sasl {
      iam = true
    }
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.msk.name
      }
    }
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-msk-cluster"
  })
}

# CloudWatch Log Group for MSK
resource "aws_cloudwatch_log_group" "msk" {
  name              = "/aws/msk/${var.name_prefix}"
  retention_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-msk-logs"
  })
}

# IAM Role for MSK Client Access
resource "aws_iam_role" "msk_client_role" {
  name = "${var.name_prefix}-msk-client-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for MSK Client Access
resource "aws_iam_policy" "msk_client_policy" {
  name        = "${var.name_prefix}-msk-client-policy"
  description = "Policy for MSK client access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:Connect",
          "kafka-cluster:AlterCluster",
          "kafka-cluster:DescribeCluster"
        ]
        Resource = aws_msk_cluster.main.arn
      },
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:*Topic*",
          "kafka-cluster:WriteData",
          "kafka-cluster:ReadData"
        ]
        Resource = "${aws_msk_cluster.main.arn}/*"
      },
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:AlterGroup",
          "kafka-cluster:DescribeGroup"
        ]
        Resource = "${aws_msk_cluster.main.arn}/*"
      }
    ]
  })

  tags = var.tags
}

# Attach policy to role
resource "aws_iam_role_policy_attachment" "msk_client_policy_attachment" {
  role       = aws_iam_role.msk_client_role.name
  policy_arn = aws_iam_policy.msk_client_policy.arn
}