#!/usr/bin/env bash
# Creates or updates a Render web service via API (requires RENDER_API_KEY).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SERVICE_NAME="${RENDER_SERVICE_NAME:-url-shortener-demo}"
REPO_URL="${RENDER_REPO_URL:-}"
BRANCH="${RENDER_BRANCH:-main}"
ROOT_DIR="${RENDER_ROOT_DIR:-url-shortener}"

if [[ -z "${RENDER_API_KEY:-}" ]]; then
  echo "Set RENDER_API_KEY (Render → Account → API Keys)" >&2
  exit 1
fi

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "Set DATABASE_URL (Neon connection string)" >&2
  exit 1
fi

if [[ -z "$REPO_URL" ]]; then
  echo "Set RENDER_REPO_URL to your GitHub repo URL (https://github.com/owner/repo)" >&2
  exit 1
fi

API="https://api.render.com/v1"
auth=(-H "Authorization: Bearer $RENDER_API_KEY" -H "Content-Type: application/json")

echo "Looking for existing service '$SERVICE_NAME'..."
existing="$(curl -s "${auth[@]}" "$API/services?limit=50" | python3 -c "
import json,sys,os
name=os.environ.get('SERVICE_NAME','')
for s in json.load(sys.stdin):
    if s.get('service',{}).get('name')==name:
        print(s['service']['id'])
        break
" SERVICE_NAME="$SERVICE_NAME")"

APP_BASE="${APP_BASE_URL:-https://${SERVICE_NAME}.onrender.com}"

env_json="$(python3 <<PY
import json, os
pairs = [
    ('SPRING_PROFILES_ACTIVE', 'postgres'),
    ('DATABASE_URL', os.environ['DATABASE_URL']),
    ('APP_BASE_URL', os.environ.get('APP_BASE_URL', '')),
]
print(json.dumps([{'key': k, 'value': v} for k, v in pairs if v]))
PY
)"

if [[ -z "$existing" ]]; then
  echo "Creating Render web service..."
  payload="$(python3 <<PY
import json, os
print(json.dumps({
  'type': 'web_service',
  'name': os.environ['SERVICE_NAME'],
  'repo': os.environ['REPO_URL'],
  'branch': os.environ['BRANCH'],
  'rootDir': os.environ['ROOT_DIR'],
  'runtime': 'docker',
  'plan': 'free',
  'healthCheckPath': '/actuator/health',
  'envVars': json.loads(os.environ['ENV_JSON']),
}))
PY
  )"
  curl -s "${auth[@]}" -X POST "$API/services" -d "$payload" | python3 -m json.tool
else
  echo "Service exists: $existing (update env via Render dashboard; API PATCH varies by account)"
  echo "Dashboard: https://dashboard.render.com"
fi

echo "Done. Set APP_BASE_URL on Render to your real HTTPS URL after first deploy."
echo "Health: ${APP_BASE}/actuator/health"
