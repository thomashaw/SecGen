#!/bin/bash
# Builds an updated SecGen base box (RESUME=1: skip the update stage of a VM left by a failed run). Run where Vagrant and VirtualBox are available
# (natively, or in the SecGen container: docker/secgen-docker.sh bash -c '...').
#
#   BASE=bookworm NEW_VERSION=1.1.0 scripts/vagrant-base-update/build.sh
#
# Output: scripts/vagrant-base-update/output/<box>-<version>-virtualbox.box, a metadata .json
# that `vagrant box add` can use to test it locally before uploading, and adds the version
# to catalogs/<box>.json, the box catalog to publish beside it (URLs under BOX_BASE_URL).
set -euxo pipefail
cd "$(dirname "$0")"
: "${BASE:?BASE=bookworm|kali}" "${NEW_VERSION:?NEW_VERSION=x.y.z}"
: "${BOX_BASE_URL:=https://hacktivity.co.uk/files/vagrant_boxes}"
export BASE
# separate Vagrant state per base, so bases can be built in parallel
export VAGRANT_DOTFILE_PATH=".vagrant-$BASE"
box=$(grep "'$BASE'" Vagrantfile | grep -oE "secgen/[a-z_-]+")
vm="secgen-base-update-$BASE"
out="output/${box#secgen/}-$NEW_VERSION-virtualbox.box"
mkdir -p output

# RESUME=1 continues with an already updated VM (after a failure in a later step)
if [ -z "${RESUME:-}" ]; then
  vagrant destroy -f || true
  vagrant up --provision-with update,overlay_upload,overlay
  vagrant reload --no-provision   # into the updated kernel
fi

for iso in /usr/share/virtualbox/VBoxGuestAdditions.iso /usr/lib/virtualbox/additions/VBoxGuestAdditions.iso; do
  [ -f "$iso" ] && break
done
vagrant upload "$iso" /tmp/VBoxGuestAdditions.iso
vagrant provision --provision-with guest_additions
vagrant reload --no-provision
vagrant provision --provision-with check
vagrant provision --provision-with cleanup
vagrant halt

rm -f "$out"
vagrant package --base "$vm" --output "$out"
sha256=$(sha256sum "$out" | cut -d' ' -f1)
cat > "${out%.box}.json" <<JSON
{
  "name": "$box",
  "versions": [{
    "version": "$NEW_VERSION",
    "providers": [{ "name": "virtualbox", "architecture": "amd64", "default_architecture": true,
                    "url": "file://$(pwd)/$out", "checksum_type": "sha256", "checksum": "$sha256" }]
  }]
}
JSON
# The catalog SecGen's base metadata points at (<url>), kept in git: add this version
catalog="catalogs/${box#secgen/}.json"
(cat "$catalog" 2>/dev/null || echo '{}') | ruby -rjson -e '
  box, version, url, sha256 = ARGV
  catalog = JSON.parse($stdin.read)
  catalog["name"] = box
  versions = (catalog["versions"] || []).reject { |v| v["version"] == version }
  versions << { "version" => version, "providers" => [{
    "name" => "virtualbox", "architecture" => "amd64", "default_architecture" => true,
    "url" => url, "checksum_type" => "sha256", "checksum" => sha256 }] }
  catalog["versions"] = versions
  puts JSON.pretty_generate(catalog)
' "$box" "$NEW_VERSION" "$BOX_BASE_URL/$(basename "$out")" "$sha256" > "$catalog"
vagrant destroy -f
echo "Built $out (sha256 $sha256)"
echo "Test locally: vagrant box add $(pwd)/${out%.box}.json"
echo "Publish: copy $out and $catalog to $BOX_BASE_URL/"
