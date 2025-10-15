# SNS Topic for CloudWatch Alarms
resource "aws_sns_topic" "cloudwatch_alarms" {
  name = "${var.name_prefix}-cloudwatch-alarms"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-cloudwatch-alarms"
  })
}

# SNS Topic Subscription for Email Notifications
resource "aws_sns_topic_subscription" "email_notification" {
  count = var.alarm_notification_email != "" ? 1 : 0

  topic_arn = aws_sns_topic.cloudwatch_alarms.arn
  protocol  = "email"
  endpoint  = var.alarm_notification_email
}

# SNS Topic Policy
resource "aws_sns_topic_policy" "cloudwatch_alarms_policy" {
  arn = aws_sns_topic.cloudwatch_alarms.arn

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "cloudwatch.amazonaws.com"
        }
        Action = "SNS:Publish"
        Resource = aws_sns_topic.cloudwatch_alarms.arn
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = data.aws_caller_identity.current.account_id
          }
        }
      }
    ]
  })
}

# Data source for current AWS account
data "aws_caller_identity" "current" {}