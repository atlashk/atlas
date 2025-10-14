-- Atlas Microservices Database Initialization Script
-- This script creates separate databases for each microservice
-- Run this script after RDS instance is created

-- Create databases for each microservice (matching on-premise naming)
CREATE DATABASE IF NOT EXISTS db_user;
CREATE DATABASE IF NOT EXISTS db_product;
CREATE DATABASE IF NOT EXISTS db_order;
CREATE DATABASE IF NOT EXISTS db_payment;

-- Create service-specific users (optional, for better security isolation)
-- You can use the same user for all databases or create separate users

-- Grant permissions to the main user for all databases
GRANT ALL PRIVILEGES ON db_user.* TO 'atlas_user'@'%';
GRANT ALL PRIVILEGES ON db_product.* TO 'atlas_user'@'%';
GRANT ALL PRIVILEGES ON db_order.* TO 'atlas_user'@'%';
GRANT ALL PRIVILEGES ON db_payment.* TO 'atlas_user'@'%';

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES;

-- Optional: Create service-specific users for better security
-- Uncomment the following lines if you want separate users for each service

/*
-- Create users for each service
CREATE USER IF NOT EXISTS 'user_service'@'%' IDENTIFIED BY 'user_service_password';
CREATE USER IF NOT EXISTS 'product_service'@'%' IDENTIFIED BY 'product_service_password';
CREATE USER IF NOT EXISTS 'order_service'@'%' IDENTIFIED BY 'order_service_password';
CREATE USER IF NOT EXISTS 'payment_service'@'%' IDENTIFIED BY 'payment_service_password';

-- Grant permissions to service-specific users
GRANT ALL PRIVILEGES ON db_user.* TO 'user_service'@'%';
GRANT ALL PRIVILEGES ON db_product.* TO 'product_service'@'%';
GRANT ALL PRIVILEGES ON db_order.* TO 'order_service'@'%';
GRANT ALL PRIVILEGES ON db_payment.* TO 'payment_service'@'%';

FLUSH PRIVILEGES;
*/