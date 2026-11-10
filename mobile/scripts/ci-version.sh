#!/usr/bin/env bash
# Works out VERSION_NAME and VERSION_CODE for CI builds and appends them to $GITHUB_ENV.
#
#   tag v2.3.4  ->  VERSION_NAME=2.3.4  VERSION_CODE=OFFSET + 2*10000 + 3*100 + 4
#   otherwise   ->  VERSION_NAME from app.config.ts default, VERSION_CODE=1 (not for store upload)
#
# VERSION_CODE_OFFSET (repo variable, default 0) lets you jump above an old Play Store
# version code if needed. Minor and patch must stay below 100.
set -euo pipefail

ref_name="${GITHUB_REF_NAME:-}"
ref_type="${GITHUB_REF_TYPE:-branch}"
offset="${VERSION_CODE_OFFSET:-0}"

if [[ "$ref_type" == "tag" ]]; then
  if [[ ! "$ref_name" =~ ^v([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "::error::Release tags must look like v2.0.0 (got '$ref_name')"
    exit 1
  fi
  major="${BASH_REMATCH[1]}"; minor="${BASH_REMATCH[2]}"; patch="${BASH_REMATCH[3]}"
  if (( minor > 99 || patch > 99 )); then
    echo "::error::Minor and patch must be below 100"
    exit 1
  fi
  name="$major.$minor.$patch"
  code=$(( offset + major * 10000 + minor * 100 + patch ))
else
  name=""
  code=1
fi

echo "VERSION_NAME=$name" >> "${GITHUB_ENV:-/dev/null}"
echo "VERSION_CODE=$code" >> "${GITHUB_ENV:-/dev/null}"
echo "Version: ${name:-<default>} ($code)"
