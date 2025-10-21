# IAM Role Outputs
output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = aws_iam_role.ecs_task_role.arn
}

output "ecs_task_role_name" {
  description = "Name of the ECS task role"
  value       = aws_iam_role.ecs_task_role.name
}

output "ecs_task_execution_role_arn" {
  description = "ARN of the ECS task execution role"
  value       = aws_iam_role.ecs_task_execution_role.arn
}

output "ecs_task_execution_role_name" {
  description = "Name of the ECS task execution role"
  value       = aws_iam_role.ecs_task_execution_role.name
}

# IAM Policy Outputs
output "msk_access_policy_arn" {
  description = "ARN of the MSK access policy"
  value       = aws_iam_policy.msk_access.arn
}

output "s3_access_policy_arn" {
  description = "ARN of the S3 access policy"
  value       = aws_iam_policy.s3_access.arn
}

output "ses_access_policy_arn" {
  description = "ARN of the SES access policy"
  value       = aws_iam_policy.ses_access.arn
}

output "cloudwatch_logs_access_policy_arn" {
  description = "ARN of the CloudWatch Logs access policy"
  value       = aws_iam_policy.cloudwatch_logs_access.arn
}

output "cloudwatch_metrics_access_policy_arn" {
  description = "ARN of the CloudWatch Metrics access policy"
  value       = aws_iam_policy.cloudwatch_metrics_access.arn
}

output "parameter_store_access_policy_arn" {
  description = "ARN of the Parameter Store access policy"
  value       = aws_iam_policy.parameter_store_access.arn
}

output "secrets_manager_access_policy_arn" {
  description = "ARN of the Secrets Manager access policy"
  value       = length(aws_iam_policy.secrets_manager_access) > 0 ? aws_iam_policy.secrets_manager_access[0].arn : null
}

output "xray_access_policy_arn" {
  description = "ARN of the X-Ray access policy"
  value       = aws_iam_policy.xray_access.arn
}