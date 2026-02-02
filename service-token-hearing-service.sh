#!/usr/bin/env bash
set -eu
microservice=${1:-xui_webapp}
curl --insecure --fail --show-error --silent -X POST \
 http://rpe-service-auth-provider-ithc.service.core-compute-ithc.internal/testing-support/lease \
 -H "Content-Type: application/json" \
 -d '{
    "microservice": "'${microservice}'"
 }' \
 -w "\n"
