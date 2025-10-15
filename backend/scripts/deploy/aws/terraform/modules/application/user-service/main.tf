# User Service - Isolated Infrastructure

# ECS Cluster for User Service
resource "aws_ecs_cluster" "user_service" {
  name = "${var.name_prefix}-user-service-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = var.tags
}

# Application Load Balancer for User Service
resource "aws_lb" "user_service" {
  name               = "${var.name_prefix}-user-service-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.subnet_ids

  enable_deletion_protection = false

  tags = var.tags
}

# ALB Security Group
resource "aws_security_group" "alb" {
  name        = "${var.name_prefix}-user-service-alb-sg"
  description = "Security group for User Service ALB"
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
    Name = "${var.name_prefix}-user-service-alb-sg"
  })
}

# ECS Security Group
resource "aws_security_group" "ecs" {
  name        = "${var.name_prefix}-user-service-ecs-sg"
  description = "Security group for User Service ECS tasks"
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
    Name = "${var.name_prefix}-user-service-ecs-sg"
  })
}

# ECS Task Execution Role
# IAM roles for User Service using comprehensive IAM module
module "user_service_iam" {
  source = "../../infrastructure/iam"

  name_prefix    = var.name_prefix
  service_name   = "user"
  msk_cluster_arn = var.msk_cluster_arn
  
  # User service needs access to MSK for event publishing/consuming
  enable_msk_access    = true
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
resource "aws_cloudwatch_log_group" "user_service" {
  name              = "/ecs/${var.name_prefix}-user-service"
  retention_in_days = 7

  tags = var.tags
}

# Target Group
resource "aws_lb_target_group" "user_service" {
  name        = "${var.name_prefix}-user-service-tg"
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
resource "aws_lb_listener" "user_service" {
  load_balancer_arn = aws_lb.user_service.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.user_service.arn
  }

  tags = var.tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "user_service" {
  family                   = "${var.name_prefix}-user-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = module.user_service_iam.ecs_task_execution_role_arn
  task_role_arn           = module.user_service_iam.ecs_task_role_arn

  container_definitions = jsonencode([
    {
      name  = "user-service"
      image = "${var.container_image}:${var.container_image_tag}"
      
      portMappings = [
        {
          containerPort = var.container_port
          protocol      = "tcp"
        }
      ]

      # Use secrets for sensitive data
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

      environment = [
        for key, value in merge(
          {
            DB_URL                  = "jdbc:mysql://${var.db_host}:${var.db_port}/${var.db_name}?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
            DB_USERNAME             = var.db_username
            # REMOVED: DB_PASSWORD - now in secrets
            DB_QUARTZ_URL           = "jdbc:mysql://${var.db_host}:${var.db_port}/${var.db_name}?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false"
            DB_QUARTZ_USERNAME      = var.db_username
            # REMOVED: DB_QUARTZ_PASSWORD - now in secrets
            REDIS_CLUSTER_NODES     = var.redis_cluster_nodes
            # REMOVED: REDIS_PASSWORD - now in secrets
            KAFKA_BOOTSTRAP_SERVERS = var.msk_bootstrap_brokers
            AWS_REGION              = var.aws_region
            # API Client Configuration
            API_CLIENT_REST_PRODUCT_SERVICE_BASE_URL = var.api_client_type == "rest" ? var.product_service_endpoint : null
            API_CLIENT_REST_ORDER_SERVICE_BASE_URL   = var.api_client_type == "rest" ? var.order_service_endpoint : null
            API_CLIENT_REST_PAYMENT_SERVICE_BASE_URL = var.api_client_type == "rest" ? var.payment_service_endpoint : null
            GRPC_CLIENT_PRODUCT_ADDRESS              = var.api_client_type == "grpc" ? var.product_service_endpoint : null
            GRPC_CLIENT_ORDER_ADDRESS                = var.api_client_type == "grpc" ? var.order_service_endpoint : null
            GRPC_CLIENT_PAYMENT_ADDRESS              = var.api_client_type == "grpc" ? var.payment_service_endpoint : null
          },
          var.environment_vars
        ) : {
          name  = key
          value = tostring(value)
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.user_service.name
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
resource "aws_ecs_service" "user_service" {
  name            = "${var.name_prefix}-user-service-service"
  cluster         = aws_ecs_cluster.user_service.id
  task_definition = aws_ecs_task_definition.user_service.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.ecs.id]
    subnets          = var.subnet_ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.user_service.arn
    container_name   = "user-service"
    container_port   = var.container_port
  }

  service_registries {
    registry_arn = var.service_discovery_arn
  }

  depends_on = [aws_lb_listener.user_service]

  tags = var.tags
}