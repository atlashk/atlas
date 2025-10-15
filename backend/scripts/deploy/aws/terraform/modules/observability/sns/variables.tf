variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "alarm_notification_email" {
  description = "Email address for alarm notifications"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}