-- Atlas Microservices Database Initialization Script for PostgreSQL
-- This script creates separate databases for each microservice
-- Run this script after RDS PostgreSQL instance is created

-- Create databases for each microservice (matching on-premise naming)
CREATE DATABASE db_user;
CREATE DATABASE db_product;
CREATE DATABASE db_order;
CREATE DATABASE db_payment;

-- Connect to each database and grant permissions
-- Note: In PostgreSQL, you need to connect to each database to grant schema permissions

\c db_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO atlas_user;

\c db_product;
GRANT ALL PRIVILEGES ON SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO atlas_user;

\c db_order;
GRANT ALL PRIVILEGES ON SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO atlas_user;

\c db_payment;
GRANT ALL PRIVILEGES ON SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO atlas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO atlas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO atlas_user;

-- List all databases
\l

-- Optional: Create service-specific users for better security
-- Uncomment the following lines if you want separate users for each service

/*
-- Create users for each service
CREATE USER user_service WITH PASSWORD 'user_service_password';
CREATE USER product_service WITH PASSWORD 'product_service_password';
CREATE USER order_service WITH PASSWORD 'order_service_password';
CREATE USER payment_service WITH PASSWORD 'payment_service_password';

-- Grant database access to service-specific users
GRANT CONNECT ON DATABASE db_user TO user_service;
GRANT CONNECT ON DATABASE db_product TO product_service;
GRANT CONNECT ON DATABASE db_order TO order_service;
GRANT CONNECT ON DATABASE db_payment TO payment_service;

-- Grant schema permissions (run these after connecting to each database)
\c db_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO user_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO user_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO user_service;

\c db_product;
GRANT ALL PRIVILEGES ON SCHEMA public TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO product_service;

\c db_order;
GRANT ALL PRIVILEGES ON SCHEMA public TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO order_service;

\c db_payment;
GRANT ALL PRIVILEGES ON SCHEMA public TO payment_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO payment_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO payment_service;
*/