output "autoscaling_target_arn" {
  description = "ARN of the autoscaling target"
  value       = aws_appautoscaling_target.ecs_target.arn
}

output "cpu_scaling_policy_arn" {
  description = "ARN of the CPU-based scaling policy"
  value       = aws_appautoscaling_policy.ecs_policy_cpu.arn
}

output "memory_scaling_policy_arn" {
  description = "ARN of the memory-based scaling policy"
  value       = aws_appautoscaling_policy.ecs_policy_memory.arn
}

output "request_count_scaling_policy_arn" {
  description = "ARN of the request count-based scaling policy"
  value       = var.alb_target_group_arn != null ? aws_appautoscaling_policy.ecs_policy_request_count[0].arn : null
}

output "scale_up_scheduled_action_arn" {
  description = "ARN of the scale-up scheduled action"
  value       = var.enable_scheduled_scaling ? aws_appautoscaling_scheduled_action.scale_up[0].arn : null
}

output "scale_down_scheduled_action_arn" {
  description = "ARN of the scale-down scheduled action"
  value       = var.enable_scheduled_scaling ? aws_appautoscaling_scheduled_action.scale_down[0].arn : null
}