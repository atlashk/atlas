#!/bin/bash

# Script to read configuration from app-stack.aws.cfg and generate terraform.tfvars
# This script reads the datasource configuration and sets appropriate database engine

CONFIG_FILE="../../../app-stack.aws.cfg"
TFVARS_FILE="terraform.tfvars"

# Function to read configuration value
read_config() {
    local key=$1
    local config_file=$2
    grep "^${key}=" "$config_file" | cut -d'=' -f2
}

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "ERROR: Configuration file $CONFIG_FILE not found!"
    exit 1
fi

# Read datasource configuration
DATASOURCE=$(read_config "datasource" "$CONFIG_FILE")

echo "Reading configuration from $CONFIG_FILE"
echo "Detected datasource: $DATASOURCE"

# Set database engine and related configurations based on datasource
case "$DATASOURCE" in
    "mysql")
        DB_ENGINE="mysql"
        DB_ENGINE_VERSION="8.0"
        DB_PORT="3306"
        DB_PARAMETER_GROUP_FAMILY="mysql8.0"
        echo "Configuring for MySQL database"
        ;;
    "postgres")
        DB_ENGINE="postgres"
        DB_ENGINE_VERSION="15.4"
        DB_PORT="5432"
        DB_PARAMETER_GROUP_FAMILY="postgres15"
        echo "Configuring for PostgreSQL database"
        ;;
    *)
        echo "ERROR: Unsupported datasource '$DATASOURCE'. Supported: mysql, postgres"
        exit 1
        ;;
esac

# Create or update terraform.tfvars with database configuration
if [ -f "$TFVARS_FILE" ]; then
    echo "Updating existing $TFVARS_FILE with database configuration"
    # Remove existing database configuration lines
    sed -i '/^db_engine/d' "$TFVARS_FILE"
    sed -i '/^db_engine_version/d' "$TFVARS_FILE"
    sed -i '/^db_port/d' "$TFVARS_FILE"
    sed -i '/^db_parameter_group_family/d' "$TFVARS_FILE"
else
    echo "Creating new $TFVARS_FILE"
fi

# Append database configuration
cat >> "$TFVARS_FILE" << EOF

# Database Configuration (auto-generated from app-stack.aws.cfg)
db_engine                  = "$DB_ENGINE"
db_engine_version         = "$DB_ENGINE_VERSION"
db_port                   = $DB_PORT
db_parameter_group_family = "$DB_PARAMETER_GROUP_FAMILY"
EOF

echo "Database configuration added to $TFVARS_FILE:"
echo "  Engine: $DB_ENGINE"
echo "  Version: $DB_ENGINE_VERSION"
echo "  Port: $DB_PORT"
echo "  Parameter Group Family: $DB_PARAMETER_GROUP_FAMILY"