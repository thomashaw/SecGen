#!/usr/bin/env bash
# DEV ONLY: render VERIFY_WALKTHROUGH.md into the hackerbot web client so it is served at
#   http://hackerbot:8080/verify_walkthrough.html
# Run after editing the walkthrough, then rebuild the VMs. Remove (with the page and its
# puppet file resource in hackerbot_webclient/manifests/config.pp) before merging.
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/.." && pwd)"
md="$here/VERIFY_WALKTHROUGH.md"
tpl="$here/verify_page_template.html"
out="$repo/modules/utilities/unix/irc_clients/hackerbot_webclient/files/verify_walkthrough.html"

body="$(mktemp)"
trap 'rm -f "$body"' EXIT
pandoc -f gfm -t html --syntax-highlighting=none "$md" -o "$body" 2>/dev/null \
  || pandoc -f gfm -t html --no-highlight "$md" -o "$body"

version="$(date '+%Y-%m-%d %H:%M') · $(cd "$repo" && git rev-parse --short HEAD 2>/dev/null || echo nogit)"

awk -v body="$body" -v version="$version" '
  /<!--BODY-->/ { while ((getline line < body) > 0) print line; next }
  { gsub(/<!--VERSION-->/, version); print }
' "$tpl" > "$out"

echo "wrote $out"
