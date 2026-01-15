#!/usr/bin/env bash
set -euo pipefail

BASE_REF="${1:-origin/${GITHUB_BASE_REF:-main}}"
HEAD_REF="${2:-HEAD}"

git fetch --no-tags origin "+refs/heads/*:refs/remotes/origin/*" >/dev/null 2>&1 || true
CHANGED=$(git diff --name-only "${BASE_REF}...${HEAD_REF}" || true)

# If build logic changes, safest is full test run
if echo "$CHANGED" | grep -E -q '(^build\.gradle|^settings\.gradle|^gradle\.properties|^gradle/|\.gradle\.kts$|^app/build\.gradle|^app/build\.gradle\.kts)'; then
  echo "FULL"
  exit 0
fi

# If no app source changes, skip tests
if ! echo "$CHANGED" | grep -E -q '^app/src/(main|test)/'; then
  echo "NONE"
  exit 0
fi

# Map changed app/src/main Kotlin/Java files -> expected test class candidates
TEST_PATTERNS=()
while IFS= read -r f; do
  if [[ "$f" =~ ^app/src/main/(java|kotlin)/.*\.(kt|java)$ ]]; then
    rel="${f#app/src/main/}"
    rel_noext="${rel%.*}"
    t1="app/src/test/${rel_noext}Test.kt"
    t2="app/src/test/${rel_noext}Test.java"
    if [[ -f "$t1" ]]; then
      cls="${t1#app/src/test/}"
      cls="${cls#java/}"
      cls="${cls#kotlin/}"
      cls="${cls%.*}"
      cls="${cls//\//.}"
      TEST_PATTERNS+=("$cls")
    elif [[ -f "$t2" ]]; then
      cls="${t2#app/src/test/}"
      cls="${cls#java/}"
      cls="${cls#kotlin/}"
      cls="${cls%.*}"
      cls="${cls//\//.}"
      TEST_PATTERNS+=("$cls")
    fi
  fi
done <<< "$CHANGED"

if [[ ${#TEST_PATTERNS[@]} -eq 0 ]]; then
  echo "FULL"
  exit 0
fi

echo "${TEST_PATTERNS[*]}"
