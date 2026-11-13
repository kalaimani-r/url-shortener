#!/usr/bin/env bash
set -euo pipefail
BASE="${1:-http://localhost:8080}"

echo "Creating short link..."
RESP=$(curl -s -X POST "$BASE/api/v1/links" \
  -H 'Content-Type: application/json' \
  -d '{"longUrl":"https://example.com/smoke-test"}')
CODE=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['shortCode'])")
echo "shortCode=$CODE"

echo "Redirect:"
curl -s -o /dev/null -w "%{http_code} -> %{redirect_url}\n" "$BASE/$CODE"

echo "Stats:"
curl -s "$BASE/api/v1/links/$CODE/stats"
echo
