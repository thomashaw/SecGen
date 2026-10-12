# Refreshing the VirtualBox base boxes

Builds a new version of a SecGen base box (`secgen/bookworm-base`, `secgen/kali_light`)
from the current one. The boxes are hosted at https://hacktivity.co.uk/files/vagrant_boxes/
(on the Hacktivity server, `/home/deploy/Hacktivity/public/files/vagrant_boxes/`), each with
a catalog, `<box>.json`, that lists its versions, download URLs and checksums. Base modules
point `<url>` at the catalog, and SecGen's Vagrantfile passes it to Vagrant as `box_url`.
Vagrant Cloud closes at the end of 2026, so nothing is published there any more.

The build:

- puts Vagrant's insecure SSH keys (ed25519 and RSA) back in `vagrant`'s
  `authorized_keys`. A box packaged without them makes every `vagrant up` loop on
  "Warning: Authentication failure. Retrying...". `secgen/bookworm-base` 1.0.1 has
  that problem; SecGen works around it by also offering the `vagrant` password.
- fixes the apt sources (Debian: adds the security repo; Kali: installs Kali's
  current archive signing key) and runs a full upgrade
- reinstalls VirtualBox Guest Additions from the host's VirtualBox, for the new kernel
  (synced folders, which the Puppet provisioner uses, need `vboxsf`)
- mounts `/tmp` and `/vagrant` as tmpfs and cleans up (see ../../README-Creating-Bases.md),
  keeping fresh apt package lists (modules install packages without `apt-get update`)
- copies `overlay/<base>/` onto the box's `/` (root-owned): the desktop defaults from the
  Proxmox templates, that is `/etc/skel` and, for Bookworm, the Hacktivity wallpaper in
  `/usr/share/desktop-hacktivity/`. Keep Proxmox-only settings, such as proxies, out of it.
- removes proxy and registry-mirror settings that only work inside the Hacktivity Proxmox
  network (Docker's `daemon.json` and service drop-ins, `/etc/environment`, `pam_env.conf`,
  apt); `bookworm-base` 1.0.1 had Docker set to proxy through 172.33.0.x, so `docker pull`
  failed everywhere else
- adds `nox2apic rcupdate.rcu_normal=1` to the kernel command line: VirtualBox 7.2
  sometimes loses an inter-processor interrupt, and the guest then hangs at boot with
  "rcu_preempt detected expedited stalls" ([VirtualBox issue 861](https://github.com/VirtualBox/virtualbox/issues/861)).

Kali specifics (all in `update.sh`):

- Puppet 7 (`puppet-agent` from Puppet's bullseye repo, as on the Proxmox Kali template),
  apt-pinned over Kali's own OpenVox 9: that has no legacy facts (`$operatingsystem`,
  `$osfamily`, ...), which hundreds of SecGen manifests use.
- `rw` on the kernel command line: Kali's systemd 262 doesn't remount `/` read-write
  after booting with GRUB's default `ro`, which leaves networking down.
- a full upgrade can stop on a file moved between renamed packages; it's retried with
  `--force-overwrite`.

SecGen's VirtualBox Vagrantfile also turns on VT-x VPID for 64-bit bases: without it,
the current Kali kernel hangs at boot with RCU stalls.

`config.ssh.insert_key = false` stops Vagrant replacing the insecure key while it
works on the VM. Never package a VM that SecGen created: it has its own keys.

## Build

Needs Vagrant, VirtualBox and ~40 GB of free disk. In the SecGen container:

```bash
docker/secgen-docker.sh bash -c 'BASE=bookworm NEW_VERSION=1.1.0 scripts/vagrant-base-update/build.sh'
docker/secgen-docker.sh bash -c 'BASE=kali NEW_VERSION=2.1.0 scripts/vagrant-base-update/build.sh'
```

It starts from the version in `Vagrantfile` (override with `FROM_VERSION`), downloaded
through the hosted catalog. This writes:

- `output/<box>-<version>-virtualbox.box`
- `output/<box>-<version>-virtualbox.json`: metadata pointing at the local file, for testing
- `catalogs/<box>.json` (in git): this version added to the box's catalog, to publish

Set `BOX_BASE_URL` to build a catalog for somewhere else (default
`https://hacktivity.co.uk/files/vagrant_boxes`).

## Test before uploading

```bash
vagrant box add scripts/vagrant-base-update/output/bookworm-base-1.1.0-virtualbox.json
```

This adds the box as `secgen/bookworm-base` version 1.1.0. Set `<vagrant_box_version>` in
`modules/bases/debian_bookworm_desktop_kde/secgen_metadata.xml` to 1.1.0 and build a
scenario (such as `scenarios/ctf/nosferatu.xml`).

## Publish

Copy the `.box` and then `catalogs/<box>.json` into
`/home/deploy/Hacktivity/public/files/vagrant_boxes/` on the Hacktivity server (box first,
so the catalog never lists a file that isn't there yet; `chown deploy`). Keep older `.box`
files there while released SecGen versions still use them. Then commit the catalog, the new
`<vagrant_box_version>` and, for the next build, the version in `Vagrantfile`.

Copies of the boxes SecGen used from Vagrant Cloud (the older bases, and the versions
before 1.1.0 and 2.1.0) were downloaded to `archive/` in October 2026, with their SHA-256 sums
in `archive/sha256sums.txt`.
