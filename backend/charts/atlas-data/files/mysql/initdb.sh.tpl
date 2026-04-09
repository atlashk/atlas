#!/bin/bash
set -e

echo "Initializing databases and granting privileges to ${MYSQL_USER}"

cat <<EOF | mysql -uroot -p"${MYSQL_ROOT_PASSWORD}"
CREATE DATABASE IF NOT EXISTS db_user;
{{- if eq .Values.appStack.idp "keycloak" }}
CREATE DATABASE IF NOT EXISTS db_keycloak;
{{- end }}
CREATE DATABASE IF NOT EXISTS db_catalog;
CREATE DATABASE IF NOT EXISTS db_inventory;
CREATE DATABASE IF NOT EXISTS db_order;
CREATE DATABASE IF NOT EXISTS db_payment;

CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASSWORD}';

GRANT ALL PRIVILEGES ON db_user.* TO '${MYSQL_USER}'@'%';
{{- if eq .Values.appStack.idp "keycloak" }}
GRANT ALL PRIVILEGES ON db_keycloak.* TO '${MYSQL_USER}'@'%';
{{- end }}
GRANT ALL PRIVILEGES ON db_catalog.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_inventory.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_order.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_payment.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF
