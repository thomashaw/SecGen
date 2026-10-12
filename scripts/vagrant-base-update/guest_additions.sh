#!/bin/bash
# Runs as root after rebooting into the updated kernel, once build.sh has uploaded
# the host's VBoxGuestAdditions.iso to /tmp.
set -euxo pipefail

mkdir -p /mnt/ga
mount -o loop,ro /tmp/VBoxGuestAdditions.iso /mnt/ga
# returns 2 when it can't load the new modules into the running kernel; check below instead
/mnt/ga/VBoxLinuxAdditions.run || true
umount /mnt/ga
rm -f /tmp/VBoxGuestAdditions.iso

# synced folders (used by the Puppet provisioner) need vboxsf for this kernel
modinfo -k "$(uname -r)" vboxsf
/opt/VBoxGuestAdditions-*/bin/VBoxControl --version
