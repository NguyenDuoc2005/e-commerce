#!/usr/bin/env bash
set -euo pipefail

elasticsearch_url="${ELASTICSEARCH_URL:-http://elasticsearch:9200}"

until curl --fail --silent --show-error "${elasticsearch_url}/_cluster/health?wait_for_status=yellow&timeout=5s" >/dev/null; do
  sleep 2
done

curl --fail --silent --show-error \
  --request PUT "${elasticsearch_url}/_ilm/policy/ecommerce-logs-policy" \
  --header 'Content-Type: application/json' \
  --data-binary @/setup/ecommerce-logs-policy.json >/dev/null

echo "Elasticsearch logging lifecycle policy is ready."
