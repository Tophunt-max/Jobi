#!/usr/bin/env bash
# End-to-end smoke test against a local `wrangler dev` instance.
# Usage: ADMIN_KEY=... BASE=http://localhost:8787 ./test/smoke.sh
set -u
BASE="${BASE:-http://localhost:8787}"
KEY="${ADMIN_KEY:?set ADMIN_KEY}"
PASS=0; FAIL=0

check() { # name expected_status actual_status
  if [ "$2" = "$3" ]; then PASS=$((PASS+1)); echo "ok   $1 ($3)"; else FAIL=$((FAIL+1)); echo "FAIL $1 expected $2 got $3"; fi
}
code() { curl -s -o /tmp/smoke_body -w '%{http_code}' "$@"; }

check "health"                    200 "$(code "$BASE/api/health")"
check "jobs list"                 200 "$(code "$BASE/api/jobs?limit=2")"
check "jobs bad limit clamps"     200 "$(code "$BASE/api/jobs?limit=abc&offset=-5")"
check "jobs status=ALL ignored"   200 "$(code "$BASE/api/jobs?status=ALL")"
grep -q pending_review /tmp/smoke_body && { FAIL=$((FAIL+1)); echo "FAIL status=ALL leaked pending jobs"; } || { PASS=$((PASS+1)); echo "ok   no pending jobs in public list"; }
for p in results admit-cards syllabi admissions answer-keys notifications sync/all sources stats ads/config; do
  check "GET /api/$p" 200 "$(code "$BASE/api/$p")"
done
check "unknown route"             404 "$(code "$BASE/api/nope")"
check "public POST rejected"      405 "$(code -X POST "$BASE/api/jobs")"
check "old upload route gone"     405 "$(code -X POST "$BASE/api/upload")"

# Admin auth
check "admin no key"              401 "$(code "$BASE/api/admin/jobs/pending")"
check "admin wrong key"           401 "$(code -H 'X-Admin-Key: sarkari_admin_secret_key_change_me' "$BASE/api/admin/jobs/pending")"
check "admin key in query ignored" 401 "$(code "$BASE/api/admin/jobs/pending?admin_key=$KEY")"
check "admin sources no key"      401 "$(code "$BASE/api/admin/job-import/sources")"
check "admin runs no key"         401 "$(code "$BASE/api/admin/job-import/runs")"
check "admin pending ok"          200 "$(code -H "X-Admin-Key: $KEY" "$BASE/api/admin/jobs/pending")"
check "admin bearer ok"           200 "$(code -H "Authorization: Bearer $KEY" "$BASE/api/admin/jobs?status=ALL")"
check "admin malformed json"      400 "$(code -X POST -H "X-Admin-Key: $KEY" -H 'Content-Type: application/json' -d '{bad' "$BASE/api/admin/jobs")"

# Manual job create (validation + correct columns)
check "create job missing url"    400 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"title":"T","organization":"O"}' "$BASE/api/admin/jobs")"
URL_SUFFIX=$RANDOM$RANDOM
check "create job ok"             201 "$(code -X POST -H "X-Admin-Key: $KEY" -d "{\"title\":\"Smoke Test Recruitment\",\"organization\":\"Test Org\",\"official_notification_url\":\"https://example.gov.in/n$URL_SUFFIX.pdf\",\"last_date\":\"31/12/2099\",\"total_posts\":5}" "$BASE/api/admin/jobs")"
JOB_ID=$(sed -n 's/.*"jobId":"\([^"]*\)".*/\1/p' /tmp/smoke_body)
check "create job duplicate url"  409 "$(code -X POST -H "X-Admin-Key: $KEY" -d "{\"title\":\"Dup\",\"organization\":\"Test Org\",\"official_notification_url\":\"https://example.gov.in/n$URL_SUFFIX.pdf\"}" "$BASE/api/admin/jobs")"
check "new job public"            200 "$(code "$BASE/api/jobs/$JOB_ID")"
grep -q '"last_date":"2099-12-31"' /tmp/smoke_body && { PASS=$((PASS+1)); echo "ok   date normalised"; } || { FAIL=$((FAIL+1)); echo "FAIL date not normalised"; }
grep -q content_hash /tmp/smoke_body && { FAIL=$((FAIL+1)); echo "FAIL internal fields leaked"; } || { PASS=$((PASS+1)); echo "ok   internal fields hidden"; }

# Moderation: reject -> approve conflict
check "reject active job"         200 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"reason":"smoke"}' "$BASE/api/admin/jobs/$JOB_ID/reject")"
check "approve rejected -> 409"   409 "$(code -X POST -H "X-Admin-Key: $KEY" "$BASE/api/admin/jobs/$JOB_ID/approve")"
check "rejected hidden publicly"  404 "$(code "$BASE/api/jobs/$JOB_ID")"
check "approve missing -> 404"    404 "$(code -X POST -H "X-Admin-Key: $KEY" "$BASE/api/admin/jobs/does-not-exist/approve")"
check "batch approve empty"       400 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"ids":[]}' "$BASE/api/admin/jobs/batch-approve")"

# Ads validation
check "ads bad unit id"           400 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"bannerId":"evil"}' "$BASE/api/admin/ads")"
check "ads http sponsor url"      400 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"sponsorTargetUrl":"javascript:alert(1)"}' "$BASE/api/admin/ads")"
check "ads ok"                    200 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"interstitialClickInterval":4,"evilKey":1}' "$BASE/api/admin/ads")"
grep -q evilKey /tmp/smoke_body && { FAIL=$((FAIL+1)); echo "FAIL unknown ad key stored"; } || { PASS=$((PASS+1)); echo "ok   unknown ad keys dropped"; }

# Upload: only PDF, admin only (skipped when no R2 bucket is bound)
check "upload no key"             401 "$(code -X POST --data-binary 'x' "$BASE/api/admin/upload")"
if curl -s "$BASE/api/health" | grep -q '"storage":"connected"'; then
check "upload non-pdf"            415 "$(code -X POST -H "X-Admin-Key: $KEY" --data-binary '<html>' "$BASE/api/admin/upload")"
check "upload pdf"                201 "$(code -X POST -H "X-Admin-Key: $KEY" --data-binary $'%PDF-1.4\n%%EOF' "$BASE/api/admin/upload")"
R2KEY=$(sed -n 's/.*"r2Key":"\([^"]*\)".*/\1/p' /tmp/smoke_body)
check "download pdf"              200 "$(code "$BASE/api/files/$R2KEY")"
check "download traversal"        404 "$(code "$BASE/api/files/..%2Fsecret")"
else
  echo "skip upload/download checks (R2 not bound)"
  check "upload without R2 -> 503"  503 "$(code -X POST -H "X-Admin-Key: $KEY" --data-binary $'%PDF-1.4' "$BASE/api/admin/upload")"
fi

# Reports + AI proxy
ACTIVE_ID=$(curl -s "$BASE/api/jobs?limit=1" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)
check "report missing job"        404 "$(code -X POST -d '{"job_id":"nope","reason":"Wrong date"}' "$BASE/api/reports")"
check "report bad body"           400 "$(code -X POST -d '{"job_id":"x"}' "$BASE/api/reports")"
check "report ok"                 201 "$(code -X POST -d "{\"job_id\":\"$ACTIVE_ID\",\"reason\":\"Wrong date\",\"details\":\"smoke\"}" "$BASE/api/reports")"
check "admin reports no key"      401 "$(code "$BASE/api/admin/reports")"
check "admin reports"             200 "$(code -H "X-Admin-Key: $KEY" "$BASE/api/admin/reports")"
REP_ID=$(sed -n 's/.*"id":"\(rep-[^"]*\)".*/\1/p' /tmp/smoke_body | head -1)
check "resolve report"            200 "$(code -X POST -H "X-Admin-Key: $KEY" -d '{"status":"RESOLVED","note":"fixed"}' "$BASE/api/admin/reports/$REP_ID/resolve")"
check "resolve twice"             404 "$(code -X POST -H "X-Admin-Key: $KEY" "$BASE/api/admin/reports/$REP_ID/resolve")"
check "ai bad task"               400 "$(code -X POST -d '{"task":"hack","prompt":"x"}' "$BASE/api/ai/generate")"

# ---------- Email/password accounts ----------
JAR=/tmp/smoke_cookies; rm -f $JAR
EMAIL="owner$RANDOM@example.com"; PW='Correct-Horse-Battery-9'
ORIGIN_HDR="Origin: $BASE"
check "create user weak pw"       400 "$(code -X POST -H "X-Admin-Key: $KEY" -d "{\"email\":\"$EMAIL\",\"password\":\"short\",\"role\":\"owner\"}" "$BASE/api/admin/users")"
check "bootstrap owner (api key)" 201 "$(code -X POST -H "X-Admin-Key: $KEY" -d "{\"email\":\"$EMAIL\",\"password\":\"$PW\",\"role\":\"owner\"}" "$BASE/api/admin/users")"
check "duplicate user"            409 "$(code -X POST -H "X-Admin-Key: $KEY" -d "{\"email\":\"$EMAIL\",\"password\":\"$PW\"}" "$BASE/api/admin/users")"
check "login wrong password"      401 "$(code -X POST -d "{\"email\":\"$EMAIL\",\"password\":\"wrong-password-123\"}" "$BASE/api/admin/auth/login")"
check "login unknown email"       401 "$(code -X POST -d '{"email":"nobody@example.com","password":"whatever-123456"}' "$BASE/api/admin/auth/login")"
check "login ok"                  200 "$(code -c $JAR -X POST -d "{\"email\":\"$EMAIL\",\"password\":\"$PW\"}" "$BASE/api/admin/auth/login")"
grep -q "__Host-sjp_admin" $JAR && { PASS=$((PASS+1)); echo "ok   session cookie set"; } || { FAIL=$((FAIL+1)); echo "FAIL no session cookie"; }
check "me via cookie"             200 "$(code -b $JAR "$BASE/api/admin/auth/me")"
grep -q "\"email\":\"$EMAIL\"" /tmp/smoke_body && { PASS=$((PASS+1)); echo "ok   me returns email"; } || { FAIL=$((FAIL+1)); echo "FAIL me email"; }
check "cookie GET admin data"     200 "$(code -b $JAR "$BASE/api/admin/jobs/pending?limit=1")"
check "cookie POST no Origin"     403 "$(code -b $JAR -X POST -d '{}' "$BASE/api/admin/job-import/run")"
check "cookie POST evil Origin"   403 "$(code -b $JAR -H 'Origin: https://evil.example' -X POST -d '{"ids":["x"]}' "$BASE/api/admin/jobs/batch-approve")"
check "cookie POST same Origin"   200 "$(code -b $JAR -H "$ORIGIN_HDR" -X POST -d '{"ids":["x"]}' "$BASE/api/admin/jobs/batch-approve")"
check "list users (owner)"        200 "$(code -b $JAR "$BASE/api/admin/users")"
NEWPW='Another-Strong-Pass-42'
check "change pw wrong current"   400 "$(code -b $JAR -H "$ORIGIN_HDR" -X POST -d "{\"current_password\":\"nope-nope-nope\",\"new_password\":\"$NEWPW\"}" "$BASE/api/admin/auth/change-password")"
check "change pw ok"              200 "$(code -b $JAR -H "$ORIGIN_HDR" -X POST -d "{\"current_password\":\"$PW\",\"new_password\":\"$NEWPW\"}" "$BASE/api/admin/auth/change-password")"
check "logout"                    200 "$(code -b $JAR -c $JAR -H "$ORIGIN_HDR" -X POST "$BASE/api/admin/auth/logout")"
check "me after logout"           401 "$(code -b $JAR "$BASE/api/admin/auth/me")"
check "old password rejected"     401 "$(code -X POST -d "{\"email\":\"$EMAIL\",\"password\":\"$PW\"}" "$BASE/api/admin/auth/login")"
check "forged cookie"             401 "$(code -H 'Cookie: __Host-sjp_admin=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' "$BASE/api/admin/auth/me")"
# Lockout: the successful password change reset the counter; "old password rejected" was
# failure 1, four more make 5 -> account locked even for the correct password.
for _ in 1 2 3 4; do code -X POST -d "{\"email\":\"$EMAIL\",\"password\":\"bad-password-xyz\"}" "$BASE/api/admin/auth/login" >/dev/null; done
check "locked after 5 failures"   423 "$(code -X POST -d "{\"email\":\"$EMAIL\",\"password\":\"$NEWPW\"}" "$BASE/api/admin/auth/login")"

echo; echo "passed=$PASS failed=$FAIL"
[ "$FAIL" -eq 0 ]
