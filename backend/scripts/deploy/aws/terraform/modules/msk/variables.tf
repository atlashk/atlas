variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for MSK cluster"
  type        = list(string)
}

variable "security_group_ids" {
  description = "List of security group IDs for MSK cluster"
  type        = list(string)
}

variable "number_of_broker_nodes" {
  description = "Number of broker nodes in the MSK cluster"
  type        = number
  default     = 2
}

variable "broker_instance_type" {
  description = "Instance type for MSK brokers"
  type        = string
  default     = "kafka.t3.small"
}

variable "broker_volume_size" {
  description = "Size of EBS volume for each broker (in GB)"
  type        = number
  default     = 100
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}