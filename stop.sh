#!/bin/bash

# Atlas Stop Script
exec "$(dirname "$0")/deployment/onprem/compose/scripts/compose-stop.sh" "$@"
