variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
}

variable "cluster_name" {
  description = "Name of the ECS cluster"
  type        = string
}

variable "min_capacity" {
  description = "Minimum number of tasks"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum number of tasks"
  type        = number
  default     = 10
}

variable "target_cpu_utilization" {
  description = "Target CPU utilization percentage"
  type        = number
  default     = 70
}

variable "target_memory_utilization" {
  description = "Target memory utilization percentage"
  type        = number
  default     = 80
}

variable "target_request_count_per_target" {
  description = "Target request count per target for ALB-based scaling"
  type        = number
  default     = 1000
}

variable "scale_in_cooldown" {
  description = "Cooldown period for scale-in actions (seconds)"
  type        = number
  default     = 300
}

variable "scale_out_cooldown" {
  description = "Cooldown period for scale-out actions (seconds)"
  type        = number
  default     = 300
}

variable "alb_target_group_arn" {
  description = "ARN of the ALB target group (optional, for request-based scaling)"
  type        = string
  default     = ""
}

variable "alb_full_name" {
  description = "Full name of the ALB (required if alb_target_group_arn is provided)"
  type        = string
  default     = ""
}

variable "alb_target_group_name" {
  description = "Name of the ALB target group (required if alb_target_group_arn is provided)"
  type        = string
  default     = ""
}

variable "enable_scheduled_scaling" {
  description = "Enable scheduled scaling actions"
  type        = bool
  default     = false
}

variable "scale_up_schedule" {
  description = "Cron expression for scaling up (e.g., '0 8 * * MON-FRI' for 8 AM weekdays)"
  type        = string
  default     = "cron(0 8 * * MON-FRI)"
}

variable "scale_down_schedule" {
  description = "Cron expression for scaling down (e.g., '0 18 * * MON-FRI' for 6 PM weekdays)"
  type        = string
  default     = "cron(0 18 * * MON-FRI)"
}

variable "scheduled_min_capacity" {
  description = "Minimum capacity during scheduled scale-up periods"
  type        = number
  default     = 2
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}