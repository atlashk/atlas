# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-db-subnet-group"
  })
}

# DB Parameter Group
resource "aws_db_parameter_group" "main" {
  family = var.db_parameter_group_family
  name   = "${var.name_prefix}-db-params"

  # MySQL specific parameters
  dynamic "parameter" {
    for_each = var.db_engine == "mysql" ? [1] : []
    content {
      name  = "innodb_buffer_pool_size"
      value = "{DBInstanceClassMemory*3/4}"
    }
  }

  # Common parameters for both MySQL and PostgreSQL
  parameter {
    name  = "max_connections"
    value = "1000"
  }

  # PostgreSQL specific parameters
  dynamic "parameter" {
    for_each = var.db_engine == "postgres" ? [1] : []
    content {
      name  = "shared_preload_libraries"
      value = "pg_stat_statements"
    }
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-db-params"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# RDS Instance
resource "aws_db_instance" "main" {
  identifier = "${var.name_prefix}-${var.db_engine}"

  # Engine
  engine         = var.db_engine
  engine_version = var.db_engine_version
  instance_class = var.db_instance_class

  # Storage
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_allocated_storage * 2
  storage_type          = "gp2"
  storage_encrypted     = true

  # Database
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = var.db_port

  # Network
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = var.security_group_ids
  publicly_accessible    = false

  # Backup
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"

  # Monitoring
  monitoring_interval = 60
  monitoring_role_arn = aws_iam_role.rds_monitoring.arn

  # Performance Insights
  performance_insights_enabled = true

  # Parameter Group
  parameter_group_name = aws_db_parameter_group.main.name

  # Deletion protection
  deletion_protection = false
  skip_final_snapshot = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-mysql"
  })
}

# IAM Role for RDS Enhanced Monitoring
resource "aws_iam_role" "rds_monitoring" {
  name = "${var.name_prefix}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

# Attach policy to the monitoring role
resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  role       = aws_iam_role.rds_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# Database initialization - Creates service-specific databases
# This resource runs after RDS instance is created to initialize separate databases
# for each microservice following the "Database per Service" pattern
resource "null_resource" "db_initialization" {
  depends_on = [aws_db_instance.main]

  provisioner "local-exec" {
    # Run appropriate SQL script based on database engine
    # MySQL: Uses mysql client to execute init-databases.sql
    # PostgreSQL: Uses psql client to execute init-databases-postgres.sql
    command = var.db_engine == "mysql" ? "mysql -h ${aws_db_instance.main.endpoint} -P ${var.db_port} -u ${var.db_username} -p${var.db_password} < ${path.module}/mysql/scripts/init_db.sql" : "PGPASSWORD=${var.db_password} psql -h ${aws_db_instance.main.endpoint} -p ${var.db_port} -U ${var.db_username} -d ${var.db_name} -f ${path.module}/postgres/scripts/init_db.sql"
  }

  # Triggers ensure the script runs when:
  # 1. RDS instance changes
  # 2. SQL script content changes
  triggers = {
    db_instance_id = aws_db_instance.main.id
    script_hash    = var.db_engine == "mysql" ? filemd5("${path.module}/mysql/scripts/init_db.sql") : filemd5("${path.module}/postgres/scripts/init_db.sql")
  }
}