# Shared setup for the scripts/ helpers. Source it; don't run it.
#
# Sets:
#   SECGEN_DIR   the checkout (or worktree) this script lives in
#   SECGEN_CONF  SecGen options file with the Proxmox creds (--read-options format)
# and exports BUNDLE_PATH when gems need to come from the main checkout.
#
# Per-user settings (SECGEN_RUN_OWNER, VLAN range, ...) can go in
# ~/.config/secgen/secgen-run.env, which is sourced here if it exists.

SECGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")/../.." && pwd)"

SECGEN_ENV_FILE="${SECGEN_ENV_FILE:-$HOME/.config/secgen/secgen-run.env}"
# shellcheck disable=SC1090
[[ -f "$SECGEN_ENV_FILE" ]] && source "$SECGEN_ENV_FILE"

SECGEN_CONF="${SECGEN_CONF:-$HOME/.config/secgen/secgen.conf}"

# A worktree has no vendor/bundle of its own. If gems were installed into the
# main checkout's vendor/bundle (e.g. a relative BUNDLE_PATH in ~/.bundle/config),
# point bundler there; otherwise leave bundler's own settings alone.
if [[ -z "${BUNDLE_PATH:-}" && ! -d "$SECGEN_DIR/vendor/bundle" ]]; then
  _common_git_dir="$(cd "$SECGEN_DIR" && git rev-parse --git-common-dir 2>/dev/null || true)"
  if [[ -n "$_common_git_dir" ]]; then
    _main_dir="$(cd "$SECGEN_DIR" && cd "$_common_git_dir/.." && pwd)"
    [[ -d "$_main_dir/vendor/bundle" ]] && export BUNDLE_PATH="$_main_dir/vendor/bundle"
  fi
  unset _common_git_dir _main_dir
fi
export BUNDLE_GEMFILE="${BUNDLE_GEMFILE:-$SECGEN_DIR/Gemfile}"

require_conf() {
  [[ -f "$SECGEN_CONF" ]] || { echo "Missing $SECGEN_CONF (SecGen --read-options file with Proxmox creds)" >&2; exit 1; }
}

# Value of one option from SECGEN_CONF (never printed by the helpers).
conf_get() { awk -v k="$1" '$1==k{print $2}' "$SECGEN_CONF"; }
