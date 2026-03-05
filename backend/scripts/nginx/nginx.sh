#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# nginx domain proxy helper
#
# Create an nginx config that proxies:
#     http://<domain>  ->  http://127.0.0.1:<port>
#
# This script:
#   1. Generates nginx config from nginx.conf template
#   2. Installs it into nginx config directory
#   3. Optionally updates /etc/hosts
#   4. Reloads nginx
#
# -----------------------------------------------------------------------------
# Examples
#
# Local development (recommended)
# Map a fake domain to localhost and proxy to your backend.
#
#   sudo bash nginx.sh api.local
#
# Result:
#   http://api.local  ->  http://127.0.0.1:8080
#
#
# Custom port
#
#   sudo bash nginx.sh api.local --port 3000
#
# Result:
#   http://api.local  ->  http://127.0.0.1:3000
#
#
# Production server (domain already resolved via DNS)
#
#   sudo bash nginx.sh example.com --port 8080 --no-hosts
#
# Result:
#   http://example.com  ->  http://127.0.0.1:8080
#
# -----------------------------------------------------------------------------

usage() {
  echo "Usage: $0 <domain> [--port <port>] [--hosts|--no-hosts]"
  echo
  echo "Options:"
  echo "  --port <port>     Backend port (default: 8080)"
  echo "  --hosts           Add domain -> 127.0.0.1 to /etc/hosts (default)"
  echo "  --no-hosts        Do not modify /etc/hosts"
  echo "  -h, --help        Show this help"
}

domain=""
port=8080
update_hosts=true

install_nginx() {
  if command -v apt-get >/dev/null 2>&1; then
    apt-get update -y
    DEBIAN_FRONTEND=noninteractive apt-get install -y nginx
    return 0
  fi

  if command -v dnf >/dev/null 2>&1; then
    dnf install -y nginx
    return 0
  fi

  if command -v yum >/dev/null 2>&1; then
    yum install -y nginx
    return 0
  fi

  if command -v apk >/dev/null 2>&1; then
    apk add --no-cache nginx
    return 0
  fi

  if command -v pacman >/dev/null 2>&1; then
    pacman -Sy --noconfirm nginx
    return 0
  fi

  if command -v zypper >/dev/null 2>&1; then
    zypper --non-interactive in nginx
    return 0
  fi

  return 1
}

# -----------------------------------------------------------------------------
# Parse arguments
# -----------------------------------------------------------------------------

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    --port)
      port="${2:-}"
      shift 2
      ;;
    --hosts)
      update_hosts=true
      shift
      ;;
    --no-hosts)
      update_hosts=false
      shift
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
    *)
      if [[ -z "$domain" ]]; then
        domain="$1"
      else
        echo "Unexpected argument: $1" >&2
        usage >&2
        exit 2
      fi
      shift
      ;;
  esac
done

# -----------------------------------------------------------------------------
# Validation
# -----------------------------------------------------------------------------

[[ -z "$domain" ]] && { usage >&2; exit 2; }

if ! [[ "$port" =~ ^[0-9]+$ ]] || ((port < 1 || port > 65535)); then
  echo "Invalid port: $port" >&2
  exit 2
fi

if [[ "$domain" =~ [[:space:]] ]]; then
  echo "Invalid domain: $domain" >&2
  exit 2
fi

if [[ "${EUID:-$(id -u)}" -ne 0 ]]; then
  echo "Please run as root (e.g. sudo $0 ...)" >&2
  exit 1
fi

# -----------------------------------------------------------------------------
# Locate template
# -----------------------------------------------------------------------------

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
template="$script_dir/nginx.conf"

[[ -f "$template" ]] || { echo "Template not found: $template" >&2; exit 1; }

# -----------------------------------------------------------------------------
# Locate nginx
# -----------------------------------------------------------------------------

nginx_bin="${NGINX_BIN:-nginx}"

if ! command -v "$nginx_bin" >/dev/null 2>&1; then
  echo "nginx not found. Attempting to install nginx..." >&2
  if ! install_nginx; then
    echo "Automatic install failed. Install nginx manually or set NGINX_BIN." >&2
    exit 1
  fi
fi

command -v "$nginx_bin" >/dev/null 2>&1 || {
  echo "nginx is still not available after installation attempt. Set NGINX_BIN." >&2
  exit 1
}

# -----------------------------------------------------------------------------
# Determine nginx config layout
# -----------------------------------------------------------------------------

target=""
enabled=""

if [[ -d /etc/nginx/sites-available && -d /etc/nginx/sites-enabled ]]; then
  target="/etc/nginx/sites-available/${domain}.conf"
  enabled="/etc/nginx/sites-enabled/${domain}.conf"
elif [[ -d /etc/nginx/conf.d ]]; then
  target="/etc/nginx/conf.d/${domain}.conf"
else
  echo "Unsupported nginx layout." >&2
  exit 1
fi

# -----------------------------------------------------------------------------
# Generate config
# -----------------------------------------------------------------------------

tmp="$(mktemp)"

sed \
  -e "s/__DOMAIN__/${domain}/g" \
  -e "s/__PORT__/${port}/g" \
  "$template" > "$tmp"

install -m 0644 "$tmp" "$target"
rm -f "$tmp"

[[ -n "$enabled" ]] && ln -sf "$target" "$enabled"

# -----------------------------------------------------------------------------
# Update /etc/hosts
# -----------------------------------------------------------------------------

if [[ "$update_hosts" == true ]]; then
  tmp="$(mktemp)"

  awk -v d="$domain" '
    /^[[:space:]]*#/ { print; next }
    NF < 2 { print; next }
    {
      keep = 1
      for (i = 2; i <= NF; i++)
        if ($i == d) keep = 0
      if (keep) print
    }
  ' /etc/hosts > "$tmp"

  printf "127.0.0.1\t%s\n" "$domain" >> "$tmp"

  install -m 0644 "$tmp" /etc/hosts
  rm -f "$tmp"
fi

# -----------------------------------------------------------------------------
# Reload nginx
# -----------------------------------------------------------------------------

"$nginx_bin" -t

if command -v systemctl >/dev/null 2>&1; then
  systemctl reload nginx || systemctl restart nginx
elif command -v service >/dev/null 2>&1; then
  service nginx reload || service nginx restart
else
  "$nginx_bin" -s reload
fi

echo "OK: http://${domain} -> http://127.0.0.1:${port}"
