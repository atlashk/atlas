#!/bin/bash

# Atlas Application Stack Configuration Setup

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Clear screen
clear

# Header
echo -e "${CYAN}${BOLD}Atlas Stack Configuration${NC}"
echo

# =============================================================================
# PLATFORM CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Platform Configuration${NC}"
echo -e "  ${GREEN}1)${NC} On-Premise (Docker Compose) ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} On-Premise (Kubernetes)"
echo -e "  ${GREEN}3)${NC} AWS (ECS)"
echo -e "  ${GREEN}4)${NC} AWS (Lambda)"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-4]: ")" platform_choice

# Handle empty input as default
if [ -z "$platform_choice" ]; then
    platform_choice=1
fi

case $platform_choice in
    2)
        platform="onprem-k8s"
        ;;
    3)
        platform="aws-ecs"
        ;;
    4)
        platform="aws-lambda"
        ;;
    *)
        platform="onprem-compose"
        ;;
esac

echo -e "${GREEN}✅ Selected Platform: ${platform}${NC}"
echo

# =============================================================================
# API SERVER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Server Configuration${NC}"
echo -e "  ${GREEN}1)${NC} REST ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} gRPC"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" api_server_choice

# Handle empty input as default
if [ -z "$api_server_choice" ]; then
    api_server_choice=1
fi

case $api_server_choice in
    2)
        api_server="grpc"
        ;;
    *)
        api_server="rest"
        ;;
esac

echo -e "${GREEN}✅ Selected API Server: ${api_server}${NC}"
echo

# =============================================================================
# API CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Client Configuration${NC}"

if [ "$api_server" = "grpc" ]; then
    echo -e "  ${GREEN}1)${NC} gRPC Netty ${YELLOW}(default)${NC}"
    read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1] (default: 1): ")" api_client_choice
    
    api_client="grpc-netdevh"
else
    echo -e "  ${GREEN}1)${NC} Apache HttpClient ${YELLOW}(default)${NC}"
    echo -e "  ${GREEN}2)${NC} Feign"
    echo -e "  ${GREEN}3)${NC} RestClient"
    echo -e "  ${GREEN}4)${NC} RestTemplate"
    read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-4]: ")" api_client_choice
    
    # Handle empty input as default
    if [ -z "$api_client_choice" ]; then
        api_client_choice=1
    fi
    
    case $api_client_choice in
        2)
            api_client="rest-feign"
            ;;
        3)
            api_client="rest-restclient"
            ;;
        4)
            api_client="rest-resttemplate"
            ;;
        *)
            api_client="rest-apachehttpclient"
            ;;
    esac
fi

echo -e "${GREEN}✅ Selected API Client: ${api_client}${NC}"
echo

# =============================================================================
# CACHE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Cache Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Redis ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} Simple (In-Memory)"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" cache_choice

if [ -z "$cache_choice" ]; then
    cache_choice=1
fi

case $cache_choice in
    2)
        cache="simple"
        ;;
    *)
        cache="redis"
        ;;
esac

echo -e "${GREEN}✅ Selected Cache: ${cache}${NC}"
echo

# =============================================================================
# DATASOURCE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Datasource Configuration${NC}"
echo -e "  ${GREEN}1)${NC} MySQL ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} PostgreSQL"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" datasource_choice

if [ -z "$datasource_choice" ]; then
    datasource_choice=1
fi

case $datasource_choice in
    2)
        datasource="postgresql"
        ;;
    *)
        datasource="mysql"
        ;;
esac

echo -e "${GREEN}✅ Selected Datasource: ${datasource}${NC}"
echo

# =============================================================================
# DISCOVERY CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Discovery Client Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Eureka ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} Kubernetes"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" discovery_choice

if [ -z "$discovery_choice" ]; then
    discovery_choice=1
fi

case $discovery_choice in
    2)
        discovery_client="kubernetes"
        ;;
    *)
        discovery_client="eureka"
        ;;
esac

echo -e "${GREEN}✅ Selected Discovery Client: ${discovery_client}${NC}"
echo

# =============================================================================
# FILE CSV CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File CSV Configuration${NC}"
echo -e "  ${GREEN}1)${NC} OpenCSV ${YELLOW}(default)${NC}"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1]: ")" csv_choice

file_csv="opencsv"
echo -e "${GREEN}✅ Selected CSV: ${file_csv}${NC}"
echo

# =============================================================================
# FILE EXCEL CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File Excel Configuration${NC}"
echo -e "  ${GREEN}1)${NC} POI ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} EasyExcel"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" excel_choice

if [ -z "$excel_choice" ]; then
    excel_choice=1
fi

case $excel_choice in
    2)
        file_excel="easyexcel"
        ;;
    *)
        file_excel="poi"
        ;;
esac

echo -e "${GREEN}✅ Selected Excel: ${file_excel}${NC}"
echo

# =============================================================================
# DISTRIBUTED LOCK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Distributed Lock Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Redisson ${YELLOW}(default)${NC}"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1]: ")" lock_choice

lock="redisson"
echo -e "${GREEN}✅ Selected Lock: ${lock}${NC}"
echo

# =============================================================================
# MESSAGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Messaging Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Kafka ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} SNS"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" messaging_choice

if [ -z "$messaging_choice" ]; then
    messaging_choice=1
fi

case $messaging_choice in
    2)
        messaging="sns"
        ;;
    *)
        messaging="kafka"
        ;;
esac

echo -e "${GREEN}✅ Selected Messaging: ${messaging}${NC}"
echo

# =============================================================================
# EMAIL NOTIFICATION CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Email Notification Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Spring ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} SendGrid"
echo -e "  ${GREEN}3)${NC} SES"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-3]: ")" email_choice

if [ -z "$email_choice" ]; then
    email_choice=1
fi

case $email_choice in
    2)
        notification_email="sendgrid"
        ;;
    3)
        notification_email="ses"
        ;;
    *)
        notification_email="spring"
        ;;
esac

echo -e "${GREEN}✅ Selected Email: ${notification_email}${NC}"
echo

# =============================================================================
# OBSERVABILITY LOGGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Logback ${YELLOW}(default)${NC}"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1]: ")" logging_choice

logging="logback"
echo -e "${GREEN}✅ Selected Logging: ${logging}${NC}"
echo

# =============================================================================
# OBSERVABILITY METRICS CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Metrics Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Prometheus ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} CloudWatch"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" metrics_choice

if [ -z "$metrics_choice" ]; then
    metrics_choice=1
fi

case $metrics_choice in
    2)
        metrics="cloudwatch"
        ;;
    *)
        metrics="prometheus"
        ;;
esac

echo -e "${GREEN}✅ Selected Metrics: ${metrics}${NC}"
echo

# =============================================================================
# OBSERVABILITY TRACING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Tracing Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Zipkin ${YELLOW}(default)${NC}"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1]: ")" tracing_choice

tracing="zipkin"
echo -e "${GREEN}✅ Selected Tracing: ${tracing}${NC}"
echo

# =============================================================================
# REDIS CONFIGURATION (conditional)
# =============================================================================
if [ "$cache" = "redis" ]; then
    echo -e "${BLUE}${BOLD}Redis Configuration${NC}"
    echo -e "  ${GREEN}1)${NC} Standalone ${YELLOW}(default)${NC}"
    echo -e "  ${GREEN}2)${NC} Cluster"
    read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" redis_choice
    
    if [ -z "$redis_choice" ]; then
        redis_choice=1
    fi
    
    case $redis_choice in
        2)
            redis="cluster"
            ;;
        *)
            redis="standalone"
            ;;
    esac
    
    echo -e "${GREEN}✅ Selected Redis: ${redis}${NC}"
    echo
else
    redis="standalone"
fi

# =============================================================================
# SCHEDULER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Scheduler Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Quartz ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} Spring"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" scheduler_choice

if [ -z "$scheduler_choice" ]; then
    scheduler_choice=1
fi

case $scheduler_choice in
    2)
        scheduler="spring"
        ;;
    *)
        scheduler="quartz"
        ;;
esac

echo -e "${GREEN}✅ Selected Scheduler: ${scheduler}${NC}"
echo

# =============================================================================
# STORAGE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Storage Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Filesystem ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} S3"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" storage_choice

if [ -z "$storage_choice" ]; then
    storage_choice=1
fi

case $storage_choice in
    2)
        storage="s3"
        ;;
    *)
        storage="filesystem"
        ;;
esac

echo -e "${GREEN}✅ Selected Storage: ${storage}${NC}"
echo

# =============================================================================
# TEMPLATE ENGINE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Template Engine Configuration${NC}"
echo -e "  ${GREEN}1)${NC} Freemarker ${YELLOW}(default)${NC}"
echo -e "  ${GREEN}2)${NC} Thymeleaf"
read -p "$(echo -e "${CYAN}>${NC} Enter your choice [1-2]: ")" template_choice

if [ -z "$template_choice" ]; then
    template_choice=1
fi

case $template_choice in
    2)
        template="thymeleaf"
        ;;
    *)
        template="freemarker"
        ;;
esac

echo -e "${GREEN}✅ Selected Template: ${template}${NC}"
echo

# Generate app-stack.cfg file
config_file="backend/app-stack.cfg"

echo -e "${PURPLE}${BOLD}Generating Configuration...${NC}"
echo -e "${YELLOW}Creating file: ${config_file}${NC}"
echo

cat > "$config_file" << EOF
api-server=$api_server
api-client=$api_client
cache=$cache
config=yaml
datasource=$datasource
discovery-client=$discovery_client
file.csv=$file_csv
file.excel=$file_excel
lock=$lock
messaging=$messaging
notification.email=$notification_email
observability.logging=$logging
observability.metrics=$metrics
observability.tracing=$tracing
persistence=jpa
platform=$platform
redis=$redis
scheduler=$scheduler
storage=$storage
template=$template
EOF

echo -e "${GREEN}${BOLD}Configuration file created successfully!${NC}"
echo
echo -e "${CYAN}${BOLD}Configuration Summary:${NC}"
echo -e "  ${BLUE}Platform:${NC} ${platform}"
echo -e "  ${BLUE}API Server:${NC} ${api_server}"
echo -e "  ${BLUE}API Client:${NC} ${api_client}"
echo -e "  ${BLUE}Datasource:${NC} ${datasource}"
echo -e "  ${BLUE}Cache:${NC} ${cache}"
echo -e "  ${BLUE}Redis:${NC} ${redis}"
echo -e "  ${BLUE}Messaging:${NC} ${messaging}"
echo -e "  ${BLUE}Email:${NC} ${notification_email}"
echo -e "  ${BLUE}Logging:${NC} ${logging}"
echo -e "  ${BLUE}Metrics:${NC} ${metrics}"
echo -e "  ${BLUE}Tracing:${NC} ${tracing}"
echo -e "  ${BLUE}CSV:${NC} ${file_csv}"
echo -e "  ${BLUE}Excel:${NC} ${file_excel}"
echo -e "  ${BLUE}Lock:${NC} ${lock}"
echo -e "  ${BLUE}Scheduler:${NC} ${scheduler}"
echo -e "  ${BLUE}Storage:${NC} ${storage}"
echo -e "  ${BLUE}Template:${NC} ${template}"
echo -e "  ${BLUE}Discovery:${NC} ${discovery_client}"
echo -e "  ${BLUE}Config File:${NC} ${config_file}"
echo
echo -e "${GREEN}${BOLD}Setup complete! Your Atlas stack is ready to go!${NC}"
