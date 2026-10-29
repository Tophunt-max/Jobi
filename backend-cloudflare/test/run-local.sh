#!/usr/bin/env bash
# Starts `wrangler dev` locally, runs the smoke test, then stops the server.
# Requires .dev.vars with ADMIN_API_KEY.
set -u
cd "$(dirname "$0")/.."
KEY=$(sed -n 's/^ADMIN_API_KEY="\{0,1\}\([^"]*\)"\{0,1\}$/\1/p' .dev.vars)
export CI=1 WRANGLER_SEND_METRICS=false
export XDG_CONFIG_HOME="${XDG_CONFIG_HOME:-/tmp/xdg-config}"
mkdir -p "$XDG_CONFIG_HOME"
npx wrangler dev --port 8787 --test-scheduled > /tmp/wrangler-dev.log 2>&1 &
PID=$!
trap 'kill $PID 2>/dev/null; pkill -f "wrangler dev" 2>/dev/null; pkill -f workerd 2>/dev/null' EXIT
for _ in $(seq 1 60); do
  curl -s http://localhost:8787/api/health >/dev/null && break
  sleep 1
done
ADMIN_KEY="$KEY" BASE=http://localhost:8787 ./test/smoke.sh
STATUS=$?
if [ "${RUN_IMPORT:-0}" = "1" ]; then
  echo "== Manual import run (real network) =="
  curl -s -X POST -H "X-Admin-Key: $KEY" -d "{\"source_id\":\"${IMPORT_SOURCE:-src-upsc}\"}" http://localhost:8787/api/admin/job-import/run | head -c 1500; echo
  echo "== Cron trigger =="
  curl -s "http://localhost:8787/__scheduled?cron=0+*/3+*+*+*"; echo
  sleep 20
  echo "== Runs =="
  curl -s -H "X-Admin-Key: $KEY" "http://localhost:8787/api/admin/job-import/runs?limit=3" | head -c 1200; echo
  FIRST=$(curl -s -H "X-Admin-Key: $KEY" "http://localhost:8787/api/admin/jobs/pending?limit=2" | grep -o '"id":"job-imp-[^"]*"' | head -2 | cut -d'"' -f4 | paste -sd, -)
  echo "== Batch approve: $FIRST =="
  IDS=$(echo "$FIRST" | sed 's/\([^,]*\)/"\1"/g')
  curl -s -X POST -H "X-Admin-Key: $KEY" -d "{\"ids\":[$IDS]}" http://localhost:8787/api/admin/jobs/batch-approve; echo
  curl -s -X POST -H "X-Admin-Key: $KEY" -d "{\"ids\":[$IDS]}" http://localhost:8787/api/admin/jobs/batch-approve; echo
  echo "== Notifications =="
  curl -s "http://localhost:8787/api/notifications?limit=5" | head -c 800; echo
fi
grep -E "✘|Error" /tmp/wrangler-dev.log | head -20
exit $STATUS
