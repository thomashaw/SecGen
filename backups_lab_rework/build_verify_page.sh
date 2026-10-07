#!/usr/bin/env bash
# DEV ONLY: render the dev/testing pages into the hackerbot web client so they are served at
#   http://hackerbot:8080/verify_walkthrough.html   (VERIFY_WALKTHROUGH.md)
#   http://hackerbot:8080/manual_test.html          (MANUAL_TEST.md)
# and copy the automated tester next to them (http://hackerbot:8080/backups_lab_test.py).
# Run after editing any of them, then rebuild the VMs. Remove (with the files and their puppet
# file resources in hackerbot_webclient/manifests/config.pp) before merging.
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/.." && pwd)"
tpl="$here/verify_page_template.html"
dest="$repo/modules/utilities/unix/irc_clients/hackerbot_webclient/files"
version="$(date '+%Y-%m-%d %H:%M') · $(cd "$repo" && git rev-parse --short HEAD 2>/dev/null || echo nogit)"

body="$(mktemp)"
trap 'rm -f "$body"' EXIT

render() {  # render <markdown> <output html> <title>
  pandoc -f gfm -t html --syntax-highlighting=none "$1" -o "$body" 2>/dev/null \
    || pandoc -f gfm -t html --no-highlight "$1" -o "$body"
  awk -v body="$body" -v version="$version" -v title="$3" '
    /<!--BODY-->/ { while ((getline line < body) > 0) print line; close(body); next }
    { gsub(/<!--VERSION-->/, version); gsub(/<!--TITLE-->/, title); print }
  ' "$tpl" > "$2"
  echo "wrote $2"
}

render "$here/VERIFY_WALKTHROUGH.md" "$dest/verify_walkthrough.html" "Backups Lab Verification"
render "$here/MANUAL_TEST.md" "$dest/manual_test.html" "Backups Lab Manual Test"

cp "$here/backups_lab_test.py" "$dest/backups_lab_test.py"
echo "wrote $dest/backups_lab_test.py"
