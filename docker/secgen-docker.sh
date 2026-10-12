#!/bin/bash
# Run SecGen in a Docker container that builds VMs with the host's VirtualBox.
#
#   docker/secgen-docker.sh start                 # build the image if needed, start the container
#   docker/secgen-docker.sh run -s scenarios/ctf/agent001.xml   # = ruby secgen.rb ... (in the container)
#   docker/secgen-docker.sh vagrant status        # any other command runs in the container's /secgen
#   docker/secgen-docker.sh shell                 # interactive shell in the container
#   docker/secgen-docker.sh stop                  # stops the container -- and the VMs running in it
#
# VMs run inside the container, so they keep running only while it does (start/stop).
# Vagrant boxes and VirtualBox VM files are kept in $SECGEN_DOCKER_HOME (default ~/.secgen-docker).
set -e

repo=$(cd "$(dirname "$0")/.." && pwd)
name=${SECGEN_CONTAINER:-secgen}
data=${SECGEN_DOCKER_HOME:-$HOME/.secgen-docker}
ubuntu=${SECGEN_UBUNTU:-24.04}

die() { echo "secgen-docker: $*" >&2; exit 1; }

host_vbox_version() {
  command -v VBoxManage >/dev/null || die "VirtualBox is not installed on the host (see docker/README.md)"
  [ -e /dev/vboxdrv ] || die "/dev/vboxdrv is missing: the host VirtualBox kernel driver is not loaded (try: sudo /sbin/vboxconfig)"
  VBoxManage --version | sed -E 's/^([0-9]+\.[0-9]+\.[0-9]+).*/\1/'
}

image_tag() { echo "secgen:vbox$(host_vbox_version)-ubuntu$ubuntu"; }

build() {
  local tag; tag=$(image_tag)
  docker build -f "$repo/docker/Dockerfile" --build-arg UBUNTU_VERSION="$ubuntu" \
    --build-arg VBOX_VERSION="$(host_vbox_version)" -t "$tag" "$repo"
}

running() { [ "$(docker inspect -f '{{.State.Running}}' "$name" 2>/dev/null)" = true ]; }

start() {
  running && return 0
  local tag; tag=$(image_tag)
  docker image inspect "$tag" >/dev/null 2>&1 || build
  docker rm -f "$name" >/dev/null 2>&1 || true
  mkdir -p "$data"
  # --privileged: access to /dev/vboxdrv etc. --network host: VirtualBox host-only
  # networks (vboxnetN) are created on the host, and VMs are reachable from it.
  docker run -d --name "$name" --privileged --network host \
    -e HOST_UID="$(id -u)" -e HOST_GID="$(id -g)" \
    -v "$repo":/secgen -v "$data":/home/secgen \
    "$tag" sleep infinity >/dev/null
  echo "secgen-docker: container '$name' started ($tag)"
}

exec_in() {
  local tty=-i
  [ -t 0 ] && [ -t 1 ] && tty=-it
  docker exec $tty -e HOST_UID="$(id -u)" -e HOST_GID="$(id -g)" -w /secgen "$name" \
    /usr/local/bin/secgen-entrypoint "$@"
}

case "${1:-}" in
  build) build ;;
  start) start ;;
  stop) docker rm -f "$name" >/dev/null && echo "secgen-docker: container '$name' stopped" ;;
  shell) start; exec_in bash ;;
  ""|-h|--help) sed -n '2,13p' "$0" | sed 's/^# \{0,1\}//' ;;
  run|r|build-project|p|build-vms|v|list-scenarios|list-projects|delete-all-projects|-*)
    start; exec_in ruby secgen.rb "$@" ;;
  *) start; exec_in "$@" ;;
esac
