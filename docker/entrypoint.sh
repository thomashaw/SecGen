#!/bin/bash
# Runs the command as a user matching the host user (HOST_UID/HOST_GID), so files
# written into the mounted SecGen checkout (projects/ etc.) belong to the host user.
set -e

if [ ! -e /dev/vboxdrv ]; then
  echo "WARNING: /dev/vboxdrv not found. Install VirtualBox on the host (same version as this" >&2
  echo "image: $(VBoxManage --version 2>/dev/null)) and run the container with --privileged." >&2
fi

uid=${HOST_UID:-0}
gid=${HOST_GID:-0}
if [ "$(id -u)" != 0 ] || [ "$uid" = 0 ]; then
  exec "$@"
fi

group=$(getent group "$gid" | cut -d: -f1)
if [ -z "$group" ]; then
  group=secgen
  groupadd -g "$gid" "$group"
fi
user=$(getent passwd "$uid" | cut -d: -f1)
if [ -z "$user" ]; then
  user=secgen
  useradd -u "$uid" -g "$gid" -M -d /home/secgen -s /bin/bash "$user"
fi
usermod -a -G vboxusers "$user"
# SecGen falls back to `sudo gem install` if a gem executable is missing
echo "$user ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/secgen

export HOME=/home/secgen
mkdir -p "$HOME"
chown "$uid:$gid" "$HOME"
export USER="$user" LOGNAME="$user"
exec setpriv --reuid="$uid" --regid="$gid" --init-groups "$@"
