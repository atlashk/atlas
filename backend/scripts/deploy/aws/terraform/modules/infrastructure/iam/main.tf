# Comprehensive IAM policies for ECS services to access AWS infrastructure

# Data source for current AWS account
data "aws_caller_identity" "current" {}

# Data source for current AWS region
data "aws_region" "current" {}

# IAM Policy for MSK (Kafka) Access
resource "aws_iam_policy" "msk_access" {
  name        = "${var.name_prefix}-ecs-msk-access"
  description = "Policy for ECS services to access MSK cluster"

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
        Resource = var.msk_cluster_arn
      },
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:*Topic*",
          "kafka-cluster:WriteData",
          "kafka-cluster:ReadData"
        ]
        Resource = "${var.msk_cluster_arn}/*"
      },
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:AlterGroup",
          "kafka-cluster:DescribeGroup"
        ]
        Resource = "${var.msk_cluster_arn}/*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for S3 Access
resource "aws_iam_policy" "s3_access" {
  name        = "${var.name_prefix}-ecs-s3-access"
  description = "Policy for ECS services to access S3 buckets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetObjectVersion",
          "s3:PutObjectAcl",
          "s3:GetObjectAcl"
        ]
        Resource = [
          var.s3_bucket_arn,
          "${var.s3_bucket_arn}/*"
        ]
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for SES Access
resource "aws_iam_policy" "ses_access" {
  name        = "${var.name_prefix}-ecs-ses-access"
  description = "Policy for ECS services to send emails via SES"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail",
          "ses:GetSendQuota",
          "ses:GetSendStatistics"
        ]
        Resource = "*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for CloudWatch Logs
resource "aws_iam_policy" "cloudwatch_logs_access" {
  name        = "${var.name_prefix}-ecs-cloudwatch-logs-access"
  description = "Policy for ECS services to write to CloudWatch Logs"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:DescribeLogGroups"
        ]
        Resource = "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for CloudWatch Metrics
resource "aws_iam_policy" "cloudwatch_metrics_access" {
  name        = "${var.name_prefix}-ecs-cloudwatch-metrics-access"
  description = "Policy for ECS services to publish CloudWatch metrics"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "cloudwatch:PutMetricData",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:ListMetrics"
        ]
        Resource = "*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for Parameter Store Access
resource "aws_iam_policy" "parameter_store_access" {
  name        = "${var.name_prefix}-ecs-parameter-store-access"
  description = "Policy for ECS services to access Parameter Store"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
          "ssm:GetParametersByPath"
        ]
        Resource = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.name_prefix}/*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for Secrets Manager Access
resource "aws_iam_policy" "secrets_manager_access" {
  count       = var.enable_secrets_access && length(var.secrets_arns) > 0 ? 1 : 0
  name        = "${var.name_prefix}-${var.service_name}-secrets-manager-access"
  description = "Policy for ECS services to access specific Secrets Manager secrets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = concat([
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = var.secrets_arns
      }
    ], length(var.secrets_kms_key_ids) > 0 ? [
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = [
          for key_id in var.secrets_kms_key_ids : "arn:aws:kms:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:key/${key_id}"
        ]
      }
    ] : [])
  })

  tags = var.tags
}

# Generic IAM Policy for Secrets Manager Access (fallback)
resource "aws_iam_policy" "secrets_manager_access_generic" {
  count       = var.enable_secrets_access && length(var.secrets_arns) == 0 ? 1 : 0
  name        = "${var.name_prefix}-${var.service_name}-secrets-manager-access-generic"
  description = "Generic policy for ECS services to access Secrets Manager"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = "arn:aws:secretsmanager:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:secret:${var.name_prefix}/*"
      }
    ]
  })

  tags = var.tags
}

# IAM Policy for X-Ray Tracing
resource "aws_iam_policy" "xray_access" {
  name        = "${var.name_prefix}-ecs-xray-access"
  description = "Policy for ECS services to send traces to X-Ray"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "xray:PutTraceSegments",
          "xray:PutTelemetryRecords",
          "xray:GetSamplingRules",
          "xray:GetSamplingTargets"
        ]
        Resource = "*"
      }
    ]
  })

  tags = var.tags
}

# Comprehensive ECS Task Role
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.name_prefix}-${var.service_name}-ecs-task-role"

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

# Enhanced ECS Task Execution Role
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.name_prefix}-${var.service_name}-ecs-task-execution-role"

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

# Attach AWS managed policy for ECS task execution
resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Attach custom policies to task execution role
resource "aws_iam_role_policy_attachment" "ecs_task_execution_cloudwatch_logs" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.cloudwatch_logs_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_secrets_manager" {
  count      = var.enable_secrets_access && length(var.secrets_arns) > 0 ? 1 : 0
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.secrets_manager_access[0].arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_secrets_manager_generic" {
  count      = var.enable_secrets_access && length(var.secrets_arns) == 0 ? 1 : 0
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.secrets_manager_access_generic[0].arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_parameter_store" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.parameter_store_access.arn
}

# Attach policies to task role based on service requirements
resource "aws_iam_role_policy_attachment" "ecs_task_msk_access" {
  count      = var.enable_msk_access ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.msk_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_s3_access" {
  count      = var.enable_s3_access ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.s3_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_ses_access" {
  count      = var.enable_ses_access ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ses_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_cloudwatch_logs" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.cloudwatch_logs_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_cloudwatch_metrics" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.cloudwatch_metrics_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_parameter_store" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.parameter_store_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_secrets_manager" {
  count      = var.enable_secrets_access && length(var.secrets_arns) > 0 ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.secrets_manager_access[0].arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_secrets_manager_generic" {
  count      = var.enable_secrets_access && length(var.secrets_arns) == 0 ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.secrets_manager_access_generic[0].arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_xray_access" {
  count      = var.enable_xray_tracing ? 1 : 0
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.xray_access.arn
}