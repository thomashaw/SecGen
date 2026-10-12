#!/bin/bash
# Runs as root in the base VM. DISTRO=debian|kali
set -euxo pipefail
export DEBIAN_FRONTEND=noninteractive

# Vagrant's well-known insecure keys (both, so old and new Vagrant releases can log in;
# Vagrant replaces them with a generated key pair on each VM it creates from the box)
install -d -m 700 -o vagrant -g vagrant /home/vagrant/.ssh
cat > /home/vagrant/.ssh/authorized_keys <<'EOF'
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIN1YdxBpNlzxDqfJyw/QKow1F+wvG9hXGoqiysfJOn5Y vagrant insecure public key
ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6NF8iallvQVp22WDkTkyrtvp9eWW6A8YVr+kz4TjGYe7gHzIw+niNltGEFHzD8+v1I2YJ6oXevct1YeS0o9HZyN1Q9qgCgzUFtdOKLv6IedplqoPkcmF0aYet2PkEDo3MlTBckFXPITAMzF8dJSIFo9D8HfdOV0IAdx4O7PtixWKn5y2hMNG0zQPyUecp4pzC6kivAIhyfHilFR61RGL+GPXQ2MWZWFYbAGjyiYJnAmCP3NOTd0jMZEnDkbUvxhMmBYSdETk1rRgm+R4LOzFUGaHqHDLKLX+FIPKcF96hrucXzcWyLbIbEgE98OHlnVYCzRdK8jlqm8tehUc9c9WhQ== vagrant insecure public key
EOF
chown vagrant:vagrant /home/vagrant/.ssh/authorized_keys
chmod 600 /home/vagrant/.ssh/authorized_keys
echo 'vagrant ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/vagrant
chmod 440 /etc/sudoers.d/vagrant

case "$DISTRO" in
  debian)
    . /etc/os-release
    cat > /etc/apt/sources.list <<EOF
deb http://deb.debian.org/debian ${VERSION_CODENAME} main contrib non-free-firmware
deb http://deb.debian.org/debian ${VERSION_CODENAME}-updates main contrib non-free-firmware
deb http://security.debian.org/debian-security ${VERSION_CODENAME}-security main contrib non-free-firmware
EOF
    headers=linux-headers-amd64
    ;;
  kali)
    # Kali rotated its archive signing key in 2025 (ED65462EC8D5E4C5); older images can't apt-get update
    wget -qO /usr/share/keyrings/kali-archive-keyring.gpg https://archive.kali.org/archive-keyring.gpg
    headers=linux-headers-amd64
    # Kali's systemd (262) doesn't remount / read-write when the kernel mounted it ro (GRUB's
    # default), leaving networking and the desktop down after the upgrade; boot rw (last wins)
    kernel_params="rw"
    ;;
  *) echo "unknown DISTRO $DISTRO"; exit 1 ;;
esac

apt_opts=(-y --allow-downgrades -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold)

# GRUB may be set to install to a disk name from the VM the box was made from (bookworm-base
# 1.0.1 says /dev/vda, from KVM); on VirtualBox it's /dev/sda, and a grub-pc upgrade fails
root_disk=/dev/$(lsblk -no pkname "$(findmnt -no SOURCE /)")
if dpkg -s grub-pc >/dev/null 2>&1; then
  echo "grub-pc grub-pc/install_devices multiselect $root_disk" | debconf-set-selections
fi

apt-get update
# rolling releases (Kali) sometimes move a file between renamed packages without declaring
# Replaces, so dpkg refuses to overwrite it; on failure, retry allowing that
if ! apt-get "${apt_opts[@]}" full-upgrade; then
  apt_opts+=(-o Dpkg::Options::=--force-overwrite)
  apt-get "${apt_opts[@]}" --fix-broken install
  apt-get "${apt_opts[@]}" full-upgrade
fi
# packages every SecGen base needs (README-Creating-Bases.md), plus what Guest Additions builds with
apt-get "${apt_opts[@]}" install curl wget rsync vim psmisc sudo \
  "$headers" build-essential dkms bzip2 tar
if [ "$DISTRO" = kali ]; then
  # Puppet 7 from Puppet's bullseye repo, as on the Proxmox Kali template. Kali now ships
  # OpenVox 9, which has no legacy facts ($operatingsystem etc.) and SecGen modules use them.
  wget -qO /tmp/puppet7-release.deb https://apt.puppet.com/puppet7-release-bullseye.deb
  dpkg -i /tmp/puppet7-release.deb
  rm -f /tmp/puppet7-release.deb
  cat > /etc/apt/preferences.d/secgen-puppet7 <<'EOF2'
Package: puppet-agent
Pin: origin apt.puppet.com
Pin-Priority: 1001

Package: openvox-agent ruby-openvox puppet-agent puppet facter
Pin: release n=kali-rolling
Pin-Priority: -1
EOF2
  apt-get update
  apt-get "${apt_opts[@]}" purge openvox-agent ruby-openvox || true
  apt-get "${apt_opts[@]}" install puppet-agent
  for b in puppet facter hiera; do ln -sf "/opt/puppetlabs/bin/$b" "/usr/local/bin/$b"; done
else
  # keep the distro's Puppet; Debian has renamed the package over time
  command -v puppet || apt-get "${apt_opts[@]}" install puppet || apt-get "${apt_opts[@]}" install puppet-agent
fi
puppet --version

# Remove proxy and mirror settings that only work inside the Hacktivity Proxmox network
# (bookworm-base 1.0.1 has Docker's set to 172.33.0.x; they break docker pull elsewhere)
rm -f /etc/systemd/system/docker.service.d/http-proxy.conf \
      /etc/systemd/system/docker.service.d/https-proxy.conf \
      /etc/systemd/system/docker.service.d/proxy.conf
if [ -f /etc/docker/daemon.json ] && grep -q registry-mirrors /etc/docker/daemon.json; then
  python3 - <<'EOF2'
import json
p = '/etc/docker/daemon.json'
d = json.load(open(p))
d.pop('registry-mirrors', None)
json.dump(d, open(p, 'w'), indent=2)
EOF2
  if [ "$(tr -d ' \n' < /etc/docker/daemon.json)" = '{}' ]; then rm -f /etc/docker/daemon.json; fi
fi
sed -i -E '/^(HTTPS?|FTP|SOCKS|NO)_PROXY|^(https?|ftp|socks|no)_proxy/Id' /etc/environment /etc/security/pam_env.conf
rm -f /etc/apt/apt.conf.d/*proxy*
if grep -qiE 'Acquire::.*proxy' /etc/apt/apt.conf 2>/dev/null; then sed -i -E '/Acquire::.*[Pp]roxy/d' /etc/apt/apt.conf; fi
if grep -rIl '172\.33\.' /etc 2>/dev/null; then echo "WARNING: Proxmox network settings remain in the files above"; fi

# Vagrant/Puppet working files go to tmpfs, so they are not left on generated VMs
sed -i -E '/^[^#]*[[:space:]]\/vagrant[[:space:]]/d; /^tmpfs[[:space:]]+\/tmp[[:space:]]/d' /etc/fstab
cat >> /etc/fstab <<'EOF'
tmpfs    /tmp    tmpfs    defaults,noatime   0  0
tmpfs    /vagrant    tmpfs    defaults,noatime   0  0
EOF
mkdir -p /vagrant

# VirtualBox 7.2 occasionally loses an IPI, so a guest hangs at boot with RCU stalls
# (https://github.com/VirtualBox/virtualbox/issues/861); this stops the kernel relying on it
kernel_params="${kernel_params:-} nox2apic rcupdate.rcu_normal=1"
for param in $kernel_params; do
  if ! grep -qE "^GRUB_CMDLINE_LINUX=\".*(^|[ \"])${param//./\\.}([ \"]|$)" /etc/default/grub; then
    sed -i -E "s/^GRUB_CMDLINE_LINUX=\"(.*)\"/GRUB_CMDLINE_LINUX=\"\\1 ${param}\"/" /etc/default/grub
  fi
done
grep '^GRUB_CMDLINE_LINUX=' /etc/default/grub
update-grub
