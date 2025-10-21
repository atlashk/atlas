# ECS Auto Scaling Target
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${var.cluster_name}/${var.service_name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"

  tags = var.tags
}

# Auto Scaling Policy - CPU Utilization
resource "aws_appautoscaling_policy" "ecs_policy_cpu" {
  name               = "${var.name_prefix}-${var.service_name}-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = var.target_cpu_utilization
    scale_in_cooldown  = var.scale_in_cooldown
    scale_out_cooldown = var.scale_out_cooldown
  }

  depends_on = [aws_appautoscaling_target.ecs_target]
}

# Auto Scaling Policy - Memory Utilization
resource "aws_appautoscaling_policy" "ecs_policy_memory" {
  name               = "${var.name_prefix}-${var.service_name}-memory-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value       = var.target_memory_utilization
    scale_in_cooldown  = var.scale_in_cooldown
    scale_out_cooldown = var.scale_out_cooldown
  }

  depends_on = [aws_appautoscaling_target.ecs_target]
}

# Auto Scaling Policy - ALB Request Count (if ALB target group ARN is provided)
resource "aws_appautoscaling_policy" "ecs_policy_request_count" {
  count = var.alb_target_group_arn != "" ? 1 : 0
  
  name               = "${var.name_prefix}-${var.service_name}-request-count-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ALBRequestCountPerTarget"
      resource_label         = "${var.alb_full_name}/${var.alb_target_group_name}"
    }
    target_value       = var.target_request_count_per_target
    scale_in_cooldown  = var.scale_in_cooldown
    scale_out_cooldown = var.scale_out_cooldown
  }

  depends_on = [aws_appautoscaling_target.ecs_target]
}

# Scheduled Scaling - Scale up during business hours (optional)
resource "aws_appautoscaling_scheduled_action" "scale_up" {
  count = var.enable_scheduled_scaling ? 1 : 0
  
  name               = "${var.name_prefix}-${var.service_name}-scale-up"
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  schedule           = var.scale_up_schedule

  scalable_target_action {
    min_capacity = var.scheduled_min_capacity
    max_capacity = var.max_capacity
  }
}

# Scheduled Scaling - Scale down during off-hours (optional)
resource "aws_appautoscaling_scheduled_action" "scale_down" {
  count = var.enable_scheduled_scaling ? 1 : 0
  
  name               = "${var.name_prefix}-${var.service_name}-scale-down"
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  schedule           = var.scale_down_schedule

  scalable_target_action {
    min_capacity = var.min_capacity
    max_capacity = var.max_capacity
  }
}