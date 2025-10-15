# CloudWatch Log Groups for each service
resource "aws_cloudwatch_log_group" "services" {
  for_each = toset(var.services)

  name              = "/ecs/${var.name_prefix}/${each.value}"
  retention_in_days = var.log_retention_days

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-logs"
    Service = each.value
  })
}

# CloudWatch Log Group for ECS Cluster
resource "aws_cloudwatch_log_group" "ecs_cluster" {
  name              = "/ecs/${var.name_prefix}/cluster"
  retention_in_days = var.log_retention_days

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-cluster-logs"
  })
}

# CloudWatch Dashboard
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.name_prefix}-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6

        properties = {
          metrics = [
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-user-service"],
            [".", "MemoryUtilization", ".", "."],
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-product-service"],
            [".", "MemoryUtilization", ".", "."],
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-order-service"],
            [".", "MemoryUtilization", ".", "."],
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-payment-service"],
            [".", "MemoryUtilization", ".", "."],
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-api-gateway"],
            [".", "MemoryUtilization", ".", "."]
          ]
          view    = "timeSeries"
          stacked = false
          region  = data.aws_region.current.name
          title   = "ECS Service Metrics"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6

        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", "${var.name_prefix}-mysql"],
            [".", "DatabaseConnections", ".", "."],
            [".", "FreeableMemory", ".", "."]
          ]
          view    = "timeSeries"
          stacked = false
          region  = data.aws_region.current.name
          title   = "RDS Metrics"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 12
        height = 6

        properties = {
          metrics = [
            ["AWS/ElastiCache", "CPUUtilization", "CacheClusterId", "${var.name_prefix}-elasticache"],
            [".", "NetworkBytesIn", ".", "."],
            [".", "NetworkBytesOut", ".", "."]
          ]
          view    = "timeSeries"
          stacked = false
          region  = data.aws_region.current.name
          title   = "ElastiCache Metrics"
          period  = 300
        }
      }
    ]
  })
}

# Data source for current region
data "aws_region" "current" {}

# CloudWatch Alarms for ECS Services
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  for_each = toset(var.services)

  alarm_name          = "${var.name_prefix}-${each.value}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ECS CPU utilization for ${each.value}"
  alarm_actions       = var.alarm_actions

  dimensions = {
    ServiceName = "${var.name_prefix}-${each.value}"
  }

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-cpu-alarm"
    Service = each.value
  })
}

# CloudWatch Alarms for ECS Memory
resource "aws_cloudwatch_metric_alarm" "ecs_memory_high" {
  for_each = toset(var.services)

  alarm_name          = "${var.name_prefix}-${each.value}-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ECS Memory utilization for ${each.value}"
  alarm_actions       = var.alarm_actions

  dimensions = {
    ServiceName = "${var.name_prefix}-${each.value}"
  }

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-memory-alarm"
    Service = each.value
  })
}

# CloudWatch Alarms for ECS Task Count
resource "aws_cloudwatch_metric_alarm" "ecs_task_count_low" {
  for_each = toset(var.services)

  alarm_name          = "${var.name_prefix}-${each.value}-task-count-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "RunningTaskCount"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "1"
  alarm_description   = "This metric monitors ECS running task count for ${each.value}"
  alarm_actions       = var.alarm_actions

  dimensions = {
    ServiceName = "${var.name_prefix}-${each.value}"
  }

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-task-count-alarm"
    Service = each.value
  })
}

# CloudWatch Alarm for RDS CPU
resource "aws_cloudwatch_metric_alarm" "rds_cpu_high" {
  alarm_name          = "${var.name_prefix}-rds-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors RDS CPU utilization"
  alarm_actions       = var.alarm_actions

  dimensions = {
    DBInstanceIdentifier = "${var.name_prefix}-mysql"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-rds-cpu-alarm"
  })
}

# CloudWatch Alarm for RDS Database Connections
resource "aws_cloudwatch_metric_alarm" "rds_connections_high" {
  alarm_name          = "${var.name_prefix}-rds-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors RDS database connections"
  alarm_actions       = var.alarm_actions

  dimensions = {
    DBInstanceIdentifier = "${var.name_prefix}-mysql"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-rds-connections-alarm"
  })
}

# CloudWatch Alarm for RDS Free Storage Space
resource "aws_cloudwatch_metric_alarm" "rds_free_storage_low" {
  alarm_name          = "${var.name_prefix}-rds-free-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "2000000000" # 2GB in bytes
  alarm_description   = "This metric monitors RDS free storage space"
  alarm_actions       = var.alarm_actions

  dimensions = {
    DBInstanceIdentifier = "${var.name_prefix}-mysql"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-rds-storage-alarm"
  })
}

# CloudWatch Alarm for ElastiCache CPU
resource "aws_cloudwatch_metric_alarm" "elasticache_cpu_high" {
  alarm_name          = "${var.name_prefix}-elasticache-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ElastiCache CPU utilization"
  alarm_actions       = var.alarm_actions

  dimensions = {
    CacheClusterId = "${var.name_prefix}-elasticache"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-cpu-alarm"
  })
}

# CloudWatch Alarm for ElastiCache Memory
resource "aws_cloudwatch_metric_alarm" "elasticache_memory_high" {
  alarm_name          = "${var.name_prefix}-elasticache-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "DatabaseMemoryUsagePercentage"
  namespace           = "AWS/ElastiCache"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ElastiCache memory utilization"
  alarm_actions       = var.alarm_actions

  dimensions = {
    CacheClusterId = "${var.name_prefix}-elasticache"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-elasticache-memory-alarm"
  })
}

# CloudWatch Alarms for ALB Target Response Time
resource "aws_cloudwatch_metric_alarm" "alb_response_time_high" {
  for_each = toset(var.services)

  alarm_name          = "${var.name_prefix}-${each.value}-alb-response-time-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = "300"
  statistic           = "Average"
  threshold           = "1"
  alarm_description   = "This metric monitors ALB target response time for ${each.value}"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
  }

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-alb-response-time-alarm"
    Service = each.value
  })
}

# CloudWatch Alarms for ALB HTTP 5XX Errors
resource "aws_cloudwatch_metric_alarm" "alb_http_5xx_high" {
  for_each = toset(var.services)

  alarm_name          = "${var.name_prefix}-${each.value}-alb-5xx-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "HTTPCode_Target_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "10"
  alarm_description   = "This metric monitors ALB 5XX errors for ${each.value}"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
  }

  tags = merge(var.tags, {
    Name    = "${var.name_prefix}-${each.value}-alb-5xx-alarm"
    Service = each.value
  })
}

# CloudWatch Alarm for MSK Disk Usage
resource "aws_cloudwatch_metric_alarm" "msk_disk_usage_high" {
  count = var.enable_msk_monitoring ? 1 : 0

  alarm_name          = "${var.name_prefix}-msk-disk-usage-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "KafkaDataLogsDiskUsed"
  namespace           = "AWS/Kafka"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors MSK disk usage"
  alarm_actions       = var.alarm_actions

  dimensions = {
    "Cluster Name" = "${var.name_prefix}-msk"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-msk-disk-alarm"
  })
}