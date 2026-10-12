#!/bin/bash
# Runs as root just before packaging (README-Creating-Bases.md "After making changes")
set -ux
export DEBIAN_FRONTEND=noninteractive

apt-get -y autoremove --purge
apt-get clean
# keep current package lists: SecGen modules install packages without running apt-get update
apt-get update
rm -rf /tmp/* /var/tmp/*
rm -f /var/lib/dhcp/*.leases /etc/udev/rules.d/70-persistent-net.rules
journalctl --rotate && journalctl --vacuum-time=1s
find /var/log -type f -name '*.gz' -delete
find /var/log -type f -exec truncate -s 0 {} \;
rm -f /root/.bash_history /home/*/.bash_history

# zero free space so the packaged box compresses well
dd if=/dev/zero of=/EMPTY bs=1M status=none || true
rm -f /EMPTY
sync
