#!/bin/bash
set -e

echo "Initializing PostgreSQL databases and setting owners to ${POSTGRES_USER}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --set=db_owner="$POSTGRES_USER" <<-EOSQL
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_user', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_user')\gexec
{{- if eq .Values.appStack.idp "keycloak" }}
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_keycloak', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_keycloak')\gexec
{{- end }}
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_catalog', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_catalog')\gexec
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_inventory', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_inventory')\gexec
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_order', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_order')\gexec
SELECT format('CREATE DATABASE %I OWNER %I;', 'db_payment', :'db_owner')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_payment')\gexec
EOSQL
