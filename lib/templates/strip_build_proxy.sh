#!/bin/sh
# Removes the HTTP proxy settings that Proxmox templates carry for provisioning.
# The proxy is on the provisioning network, which the post-build removes, so a
# finished VM that keeps them sends curl, wget and apt to an unreachable proxy.
# Runs as root in the guest just before net0 is removed. Optional $1 is a root
# directory to work under, for testing against a copy of /etc.
R="${1:-}"
PROXY_LINE='^[[:space:]]*(export[[:space:]]+)?(https?|ftp|socks|no|all)_proxy[[:space:]]*='

for f in "$R/etc/environment" "$R/etc/security/pam_env.conf" "$R/etc/profile" \
         "$R/etc/bash.bashrc" "$R/etc/wgetrc" "$R"/etc/profile.d/*.sh; do
  [ -f "$f" ] && grep -qiE "$PROXY_LINE" "$f" && sed -i -E "/$PROXY_LINE/Id" "$f" && echo "removed proxy settings from ${f#$R}"
done
# pam_env.conf also takes "HTTP_PROXY DEFAULT=..." lines
f="$R/etc/security/pam_env.conf"
[ -f "$f" ] && grep -qiE '^[[:space:]]*(https?|ftp|socks|no|all)_proxy[[:space:]]' "$f" \
  && sed -i -E '/^[[:space:]]*(https?|ftp|socks|no|all)_proxy[[:space:]]/Id' "$f" && echo "removed proxy settings from ${f#$R}"

for f in "$R"/etc/apt/apt.conf.d/*proxy* "$R"/etc/systemd/system/docker.service.d/*proxy*.conf; do
  [ -f "$f" ] && rm -f "$f" && echo "removed ${f#$R}"
done
for f in "$R"/etc/apt/apt.conf "$R"/etc/apt/apt.conf.d/*; do
  [ -f "$f" ] && grep -qiE 'Acquire::.*proxy' "$f" && sed -i -E '/Acquire::.*proxy/Id' "$f" && echo "removed proxy settings from ${f#$R}"
done

left=$(grep -rIl '172\.33\.' "$R/etc" 2>/dev/null)
[ -n "$left" ] && echo "provisioning network addresses remain in: $left"
sync
exit 0
