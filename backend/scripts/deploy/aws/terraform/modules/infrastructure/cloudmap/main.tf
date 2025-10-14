# Cloud Map Service Discovery

# Create a private DNS namespace for service discovery
resource "aws_service_discovery_private_dns_namespace" "atlas" {
  name        = "${var.name_prefix}-${var.environment}.local"
  description = "Private DNS namespace for Atlas microservices"
  vpc         = var.vpc_id

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${var.environment}-namespace"
  })
}

# Service discovery service for user-service
resource "aws_service_discovery_service" "user_service" {
  name = "user-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.atlas.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-user-service-discovery"
  })
}

# Service discovery service for product-service
resource "aws_service_discovery_service" "product_service" {
  name = "product-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.atlas.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-product-service-discovery"
  })
}

# Service discovery service for order-service
resource "aws_service_discovery_service" "order_service" {
  name = "order-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.atlas.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-order-service-discovery"
  })
}

# Service discovery service for payment-service
resource "aws_service_discovery_service" "payment_service" {
  name = "payment-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.atlas.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-payment-service-discovery"
  })
}

# Service discovery service for api-gateway
resource "aws_service_discovery_service" "api_gateway" {
  name = "api-gateway"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.atlas.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-api-gateway-discovery"
  })
}