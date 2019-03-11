#!/bin/sh
set -eo pipefail

host="$(hostname -i || echo '127.0.0.1')"
curl --fail "http://$host:8081/healthcheck" || exit 1
