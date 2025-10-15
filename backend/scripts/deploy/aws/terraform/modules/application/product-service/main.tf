# Product Service - Isolated Infrastructure

# ECS Cluster for Product Service
resource "aws_ecs_cluster" "product_service" {
  name = "${var.name_prefix}-product-service-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = var.tags
}

# Application Load Balancer for Product Service
resource "aws_lb" "product_service" {
  name               = "${var.name_prefix}-product-service-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.subnet_ids

  enable_deletion_protection = false

  tags = var.tags
}

# ALB Security Group
resource "aws_security_group" "alb" {
  name        = "${var.name_prefix}-product-service-alb-sg"
  description = "Security group for Product Service ALB"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-product-service-alb-sg"
  })
}

# ECS Security Group
resource "aws_security_group" "ecs" {
  name        = "${var.name_prefix}-product-service-ecs-sg"
  description = "Security group for Product Service ECS tasks"
  vpc_id      = var.vpc_id

  ingress {
    description     = "HTTP from ALB"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-product-service-ecs-sg"
  })
}

# IAM roles for Product Service using comprehensive IAM module
module "product_service_iam" {
  source = "../../infrastructure/iam"

  name_prefix    = var.name_prefix
  service_name   = "product"
  msk_cluster_arn = var.msk_cluster_arn
  s3_bucket_arn   = var.s3_bucket_arn

  # Product service needs access to MSK for event publishing/consuming
  enable_msk_access    = true
  # Product service needs S3 access for product image storage
  enable_s3_access     = true
  # Enable X-Ray tracing for observability
  enable_xray_tracing  = true
  
  # Enable access to AWS Secrets Manager
  enable_secrets_access = true
  secrets_arns = [
    var.db_secret_arn,
    var.redis_secret_arn
  ]
  secrets_kms_key_ids = [
    var.db_secret_kms_key_id,
    var.redis_secret_kms_key_id
  ]

  tags = var.tags
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "product_service" {
  name              = "/ecs/${var.name_prefix}-product-service"
  retention_in_days = 7

  tags = var.tags
}

# Target Group
resource "aws_lb_target_group" "product_service" {
  name        = "${var.name_prefix}-product-service-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = var.health_check_path
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 2
  }

  tags = var.tags
}

# ALB Listener
resource "aws_lb_listener" "product_service" {
  load_balancer_arn = aws_lb.product_service.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.product_service.arn
  }

  tags = var.tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "product_service" {
  family                   = "${var.name_prefix}-product-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = module.product_service_iam.ecs_task_execution_role_arn
  task_role_arn           = module.product_service_iam.ecs_task_role_arn

  container_definitions = jsonencode([
    {
      name  = "product-service"
      image = "${var.container_image}:${var.container_image_tag}"
      
      portMappings = [
        {
          containerPort = var.container_port
          protocol      = "tcp"
        }
      ]

      # Separate sensitive data into secrets
      environment = [
        for key, value in merge(
          {
            DB_URL                    = "jdbc:mysql://${var.db_host}:${var.db_port}/${var.db_name}?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
            DB_USERNAME               = var.db_username
            DB_QUARTZ_URL             = "jdbc:mysql://${var.db_host}:${var.db_port}/${var.db_name}?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
            DB_QUARTZ_USERNAME        = var.db_username
            REDIS_CLUSTER_NODES       = var.redis_cluster_nodes
            KAFKA_BOOTSTRAP_SERVERS   = var.msk_bootstrap_brokers
            AWS_REGION                = var.aws_region
            S3_PRODUCT_IMAGE_BUCKET   = var.s3_product_image_bucket_name
            # API Client Configuration
            API_CLIENT_REST_USER_SERVICE_BASE_URL    = var.api_client_type == "rest" ? var.user_service_endpoint : null
            API_CLIENT_REST_ORDER_SERVICE_BASE_URL   = var.api_client_type == "rest" ? var.order_service_endpoint : null
            API_CLIENT_REST_PAYMENT_SERVICE_BASE_URL = var.api_client_type == "rest" ? var.payment_service_endpoint : null
            GRPC_CLIENT_USER_ADDRESS                 = var.api_client_type == "grpc" ? var.user_service_endpoint : null
            GRPC_CLIENT_ORDER_ADDRESS                = var.api_client_type == "grpc" ? var.order_service_endpoint : null
            GRPC_CLIENT_PAYMENT_ADDRESS              = var.api_client_type == "grpc" ? var.payment_service_endpoint : null
          },
          var.environment_vars
        ) : {
          name  = key
          value = tostring(value)
        }
      ]

      # Use AWS Secrets Manager for sensitive data
      secrets = [
        {
          name      = "DB_PASSWORD"
          valueFrom = var.db_secret_arn
        },
        {
          name      = "DB_QUARTZ_PASSWORD"
          valueFrom = var.db_secret_arn
        },
        {
          name      = "REDIS_PASSWORD"
          valueFrom = var.redis_secret_arn
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.product_service.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }

      essential = true
    }
  ])

  tags = var.tags
}

# ECS Service
resource "aws_ecs_service" "product_service" {
  name            = "${var.name_prefix}-product-service-service"
  cluster         = aws_ecs_cluster.product_service.id
  task_definition = aws_ecs_task_definition.product_service.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.ecs.id]
    subnets          = var.subnet_ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.product_service.arn
    container_name   = "product-service"
    container_port   = var.container_port
  }

  service_registries {
    registry_arn = var.service_discovery_arn
  }

  depends_on = [aws_lb_listener.product_service]

  tags = var.tags
}