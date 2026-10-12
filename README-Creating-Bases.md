# Creating new SecGen bases

We encourage you to use the existing bases when developing scenarios. Introducing new base boxes require careful thought and testing of modules for compatibility. This guide is mostly intended for those who wish to extend SecGen onto further VDI platforms (in addition to VirtualBox, and oVirt), which involves recreating our existing base images on these other platforms.

When creating base images for SecGen, follow [guidelines on creating Vagrant base boxes](https://www.vagrantup.com/docs/boxes/base.html), with these additional considerations.

## Make sure these packages are installed on every base

- puppet
- curl
- wget
- rsync
- vim
- psmisc
- sudo

Install VM guest tools software, to enable copy-paste between VMs, graphics, etc.

## SSH: keep Vagrant's insecure key

A box must let the `vagrant` user log in with Vagrant's insecure key (keep both the ed25519 and RSA public keys in `/home/vagrant/.ssh/authorized_keys`; password `vagrant`, passwordless sudo). Vagrant swaps in a generated key pair on every VM it creates. If you package a VM that Vagrant (or SecGen) has already started normally, the box contains that generated key instead, and every `vagrant up` from it loops on "Warning: Authentication failure. Retrying...". Use `config.ssh.insert_key = false` when working on a base.

## Refreshing the VirtualBox bases

[scripts/vagrant-base-update](scripts/vagrant-base-update/README.md) rebuilds `secgen/bookworm-base` and `secgen/kali_light` from their current versions with updates applied, current Guest Additions, the insecure keys, and the clean-up steps below, and writes the box catalog to publish with the new version on hacktivity.co.uk (these boxes are no longer on Vagrant Cloud, which closes at the end of 2026).

## Updating repository certificates

Occasionally we apt-get update; apt-get upgrade. This can be required to avoid package repo certificates from expiring; however, this does run the risk of breaking modules.

Alternatively, it may be possible to update the keys without updating other software: `sudo apt-key update`

## Avoid SecGen leaving extra files on the VMs

You should have these directories mounted as tmpfs, so that the files used by Vagrant to provision the VMs (including puppet files, SecGen module names, etc), don't get accidentally left on the VMs that are generated.

- /tmp/
- /vagrant/

This can be achieved via /etc/fstab:

```
tmpfs    /tmp    tmpfs    defaults,noatime   0  0
tmpfs    /vagrant    tmpfs    defaults,noatime   0  0
```

## Behind a proxy

If you need to modify your base boxes to connect to the Internet via proxy, you can create these files, replacing the IP address with your own proxy. They only work inside that network, so remove them again before packaging a box for others (scripts/vagrant-base-update does this):

```
/etc/systemd/system/docker.service.d/https-proxy.conf

Environment="HTTPS_PROXY=http://192.168.201.51:3128/"

/etc/systemd/system/docker.service.d/http-proxy.conf

Environment="HTTP_PROXY=http://192.168.201.51:3128/"

/etc/environment
http_proxy="http://192.168.201.51:3128"
https_proxy="http://192.168.201.51:3128"
ftp_proxy="ftp://192.168.201.51:3128"
socks_proxy="socks://192.168.201.51:3128"
HTTP_PROXY="http://192.168.201.51:3128"
HTTPS_PROXY="http://192.168.201.51:3128"

/etc/apt/apt.conf

Acquire::http::Proxy "http://192.168.201.51:3128";

/etc/security/pam_env.conf

HTTP_PROXY      DEFAULT="192.168.201.51:3128"
```

## After making changes

In the VM:

```bash
sudo apt-get clean
sudo dd if=/dev/zero of=/EMPTY bs=1M
sudo rm -f /EMPTY
history -c
history -w
```

And in oVirt we also:

```bash
sudo rm /etc/udev/rules.d/70-persistent-net.rules
```

Finally, on the host, package to upload: `vagrant package --base vmname --output packaged.box`
