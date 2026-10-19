# Shared setup for the scripts/ helpers. Source it; don't run it.
#
# Sets:
#   SECGEN_DIR   the checkout (or worktree) this script lives in
#   SECGEN_CONF  SecGen options file with the Proxmox creds (--read-options format):
#                SecGen/secgen.conf in the main checkout (the same file from every worktree),
#                else ~/.config/secgen/secgen.conf, unless SECGEN_CONF is already set
# and exports BUNDLE_PATH when gems need to come from the main checkout.
#
# Per-user settings (SECGEN_RUN_OWNER, VLAN range, ...) can go in
# ~/.config/secgen/secgen-dev-run.env, which is sourced here if it exists
# (secgen-run.env, its name before secgen-run was renamed, is still read).

SECGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")/../.." && pwd)"

if [[ -z "${SECGEN_ENV_FILE:-}" ]]; then
  SECGEN_ENV_FILE="$HOME/.config/secgen/secgen-dev-run.env"
  [[ -f "$SECGEN_ENV_FILE" ]] || SECGEN_ENV_FILE="$HOME/.config/secgen/secgen-run.env"
fi
# shellcheck disable=SC1090
[[ -f "$SECGEN_ENV_FILE" ]] && source "$SECGEN_ENV_FILE"

# The main checkout, which holds the gitignored secgen.conf (a worktree has no copy of it)
_common_git_dir="$(cd "$SECGEN_DIR" && git rev-parse --git-common-dir 2>/dev/null || true)"
if [[ -n "$_common_git_dir" ]]; then
  SECGEN_MAIN_DIR="$(cd "$SECGEN_DIR" && cd "$_common_git_dir/.." && pwd)"
else
  SECGEN_MAIN_DIR="$SECGEN_DIR"
fi
unset _common_git_dir
if [[ -z "${SECGEN_CONF:-}" ]]; then
  SECGEN_CONF="$SECGEN_MAIN_DIR/secgen.conf"
  [[ -f "$SECGEN_CONF" || ! -f "$HOME/.config/secgen/secgen.conf" ]] || SECGEN_CONF="$HOME/.config/secgen/secgen.conf"
fi

# A worktree has no vendor/bundle of its own. If gems were installed into the
# main checkout's vendor/bundle (e.g. a relative BUNDLE_PATH in ~/.bundle/config),
# point bundler there; otherwise leave bundler's own settings alone.
if [[ -z "${BUNDLE_PATH:-}" && ! -d "$SECGEN_DIR/vendor/bundle" ]]; then
  [[ -d "$SECGEN_MAIN_DIR/vendor/bundle" ]] && export BUNDLE_PATH="$SECGEN_MAIN_DIR/vendor/bundle"
fi
export BUNDLE_GEMFILE="${BUNDLE_GEMFILE:-$SECGEN_DIR/Gemfile}"

require_conf() {
  [[ -f "$SECGEN_CONF" ]] || { echo "Missing $SECGEN_CONF (SecGen --read-options file with Proxmox creds)" >&2; exit 1; }
}

# Value of one option from SECGEN_CONF (never printed by the helpers).
conf_get() { awk -v k="$1" '$1==k{print $2}' "$SECGEN_CONF"; }
