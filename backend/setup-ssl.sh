#!/usr/bin/env bash
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    print_warning "This script requires root privileges for some operations."
    print_info "You may be prompted for sudo password."
fi

# Get domain name
if [ -z "$1" ]; then
    read -p "Enter your domain name (e.g., api.yourdomain.com): " DOMAIN
else
    DOMAIN=$1
fi

if [ -z "$DOMAIN" ]; then
    print_error "Domain name is required!"
    exit 1
fi

print_info "Domain: $DOMAIN"

# Step 1: Install Certbot
print_info "Installing Certbot..."

if command -v apt &> /dev/null; then
    # Ubuntu/Debian
    sudo apt update
    sudo apt install -y certbot
elif command -v yum &> /dev/null; then
    # Amazon Linux/CentOS
    sudo yum install -y certbot
elif command -v dnf &> /dev/null; then
    # Fedora
    sudo dnf install -y certbot
else
    print_error "Unsupported package manager. Please install certbot manually."
    exit 1
fi

print_info "Certbot installed successfully!"

# Step 2: Get SSL Certificate
print_info "Obtaining SSL certificate from Let's Encrypt..."

sudo certbot certonly --standalone \
    -d "$DOMAIN" \
    --register-unsafely-without-email \
    --agree-tos \
    --non-interactive
