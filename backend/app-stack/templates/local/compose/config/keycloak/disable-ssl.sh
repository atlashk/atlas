#!/bin/bash

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to start..."
until curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; do
  sleep 5
done

echo "Keycloak is ready. Configuring SSL settings..."

# Authenticate with Keycloak
/opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user "${KC_BOOTSTRAP_ADMIN_USERNAME:-atlas}" \
  --password "${KC_BOOTSTRAP_ADMIN_PASSWORD:-secret123456}"

# Disable SSL for master realm
/opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE

echo "SSL requirement disabled for master realm"
