#!/bin/bash

# Atlas Clean Script
exec "$(dirname "$0")/deployment/onprem/compose/scripts/compose-clean.sh" "$@"
