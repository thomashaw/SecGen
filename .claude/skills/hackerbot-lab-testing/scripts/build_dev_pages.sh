#!/usr/bin/env bash
# Render markdown guides into self-contained HTML pages (copy buttons on every code block, clickable
# checkboxes saved per page, no external resources) for serving to lab VMs from hackerbot:8080.
#
# Usage: build_dev_pages.sh <out_dir> <guide.md>:<page.html>:"<Title>" [...] [--ph NAME,NAME,...]
#   e.g. build_dev_pages.sh modules/utilities/unix/irc_clients/hackerbot_webclient/files \
#          lab_dev/MANUAL_TEST.md:manual_test.html:"Backups Lab Manual Test"
# --ph adds input boxes whose values replace those tokens in code blocks. Easy to miss - prefer guides
# whose setup block works values out itself (U=$(whoami) ...; read -p ... IP).
# Needs pandoc (not installed on every box: apt install pandoc, with the owner's OK).
set -euo pipefail
here="$(dirname "$(readlink -f "$0")")"
tpl="$here/../assets/dev_page_template.html"
command -v pandoc >/dev/null || { echo "pandoc not found - install it (ask first) or build the pages elsewhere" >&2; exit 1; }
[[ $# -ge 2 ]] || { sed -n '2,11p' "$0" | sed 's/^# \{0,1\}//'; exit 1; }
out_dir="$1"; shift
ph=""
pages=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --ph) ph="$2"; shift 2 ;;
    *) pages+=("$1"); shift ;;
  esac
done
version="$(date '+%Y-%m-%d %H:%M') · $(git -C "$here" rev-parse --short HEAD 2>/dev/null || echo nogit)"
boxes=""
IFS=',' read -ra names <<< "$ph"
for n in "${names[@]}"; do
  [[ -n "$n" ]] && boxes+="    <label>$n <input data-ph=\"$n\"></label>"$'\n'
done
body="$(mktemp)"; boxes_f="$(mktemp)"
trap 'rm -f "$body" "$boxes_f"' EXIT
printf '%s' "$boxes" > "$boxes_f"
for spec in "${pages[@]}"; do
  IFS=':' read -r md html title <<< "$spec"
  pandoc -f gfm -t html --syntax-highlighting=none "$md" -o "$body" 2>/dev/null \
    || pandoc -f gfm -t html --no-highlight "$md" -o "$body"
  awk -v body="$body" -v boxes="$boxes_f" -v version="$version" -v title="${title:-Dev page}" '
    /<!--BODY-->/ { while ((getline l < body) > 0) print l; close(body); next }
    /<!--PLACEHOLDERS-->/ { while ((getline l < boxes) > 0) print l; close(boxes); next }
    { gsub(/<!--VERSION-->/, version); gsub(/<!--TITLE-->/, title); print }
  ' "$tpl" > "$out_dir/$html"
  echo "wrote $out_dir/$html"
done
