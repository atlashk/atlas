variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "services" {
  description = "List of service names"
  type        = list(string)
}

variable "log_retention_days" {
  description = "Number of days to retain logs"
  type        = number
  default     = 14
}

variable "alarm_actions" {
  description = "List of ARNs to notify when alarm triggers"
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}

variable "alb_arn_suffix" {
  description = "ALB ARN suffix for CloudWatch alarms"
  type        = string
  default     = ""
}

variable "enable_msk_monitoring" {
  description = "Enable MSK monitoring alarms"
  type        = bool
  default     = false
}