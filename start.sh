#!/bin/bash

exec "$(dirname "$0")/deployment/onprem/compose/scripts/compose-start.sh" "$@"
