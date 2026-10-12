# Running SecGen in Docker

The container holds everything SecGen needs on the host side (Ruby and its gems,
Vagrant, Puppet module tooling, ImageMagick, ...), so a fresh Linux machine only
needs Docker and VirtualBox. It has been tested on Ubuntu 24.04 with a Linux 7.0
kernel and VirtualBox 7.2.20.

## Why VirtualBox is also needed on the host

VirtualBox runs VMs through a kernel driver (`/dev/vboxdrv`), and a container
cannot load kernel modules. The host provides the driver, and the container runs
VirtualBox's user-space tools against it. The two **must be the same version**,
which `docker/secgen-docker.sh` handles by building the image for the host's
`VBoxManage --version`.

## Host setup (once)

Install Docker and VirtualBox. Use Oracle's VirtualBox packages rather than the
distribution's: Ubuntu 24.04's `virtualbox` (7.0.16) cannot build its kernel
driver for the newer HWE kernels, and VirtualBox needs 7.2.8 or later for Linux 7.0.

```bash
sudo apt-get install -y docker.io docker-buildx
sudo usermod -aG docker "$USER"

wget -qO- https://www.virtualbox.org/download/oracle_vbox_2016.asc \
  | sudo gpg --yes --dearmor -o /usr/share/keyrings/oracle-virtualbox-2016.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/oracle-virtualbox-2016.gpg] https://download.virtualbox.org/virtualbox/debian $(lsb_release -cs) contrib" \
  | sudo tee /etc/apt/sources.list.d/virtualbox.list
sudo apt-get update
sudo apt-get remove -y virtualbox virtualbox-qt virtualbox-dkms   # if installed
sudo apt-get install -y virtualbox-7.2
```

If KVM is loaded (it usually is), VirtualBox cannot start VMs while KVM holds
the CPU's virtualisation extensions (`VERR_VMX_IN_VMX_ROOT_MODE`). On kernels 6.12+
you can let them coexist, so that KVM only takes VT-x while a KVM VM is running:

```bash
echo "options kvm enable_virt_at_load=0" | sudo tee /etc/modprobe.d/kvm-virtualbox.conf
sudo modprobe -r kvm_intel kvm_amd kvm; sudo modprobe kvm_intel || sudo modprobe kvm_amd
```

Log out and in again (for the `docker` group), then check the driver is loaded:
`ls -l /dev/vboxdrv` (if not: `sudo /sbin/vboxconfig`).

## Usage

From the SecGen checkout:

```bash
docker/secgen-docker.sh start                                    # builds the image the first time (~10 min)
docker/secgen-docker.sh run -s scenarios/ctf/nosferatu.xml       # = ruby secgen.rb -s ... run
docker/secgen-docker.sh list-projects
docker/secgen-docker.sh shell                                    # a shell inside the container
docker/secgen-docker.sh stop                                     # stops the container AND its VMs
```

Any other command runs inside the container, in the checkout, for example
`docker/secgen-docker.sh bash -c 'cd projects/SecGen2026... && vagrant ssh server'`.

- The checkout is mounted at `/secgen`, so projects are written to `projects/` on the host.
- Vagrant boxes and VirtualBox VM files are kept in `~/.secgen-docker` (override with
  `SECGEN_DOCKER_HOME`), so boxes are downloaded once.
- VMs run inside the container, so they only run while it does. They are reachable from
  the host on their private network IPs (the container uses the host's network).
- The VMs are headless; use `vagrant ssh` or SSH to their IPs.
- After upgrading VirtualBox on the host, the next `start` builds a matching image.
  Stop the container first if one is running.

## Other Ubuntu releases / Ruby versions

`SECGEN_UBUNTU=26.04 docker/secgen-docker.sh start` builds the image on another Ubuntu
release (stop any running container first): 22.04 (Ruby 3.0), 24.04 (Ruby 3.2, the
default) or 26.04 (Ruby 3.3). 20.04 isn't supported for the image: its apt can't verify
HashiCorp's current repository key. Gemfile.lock is resolved under Ruby 2.7, so a native
install on 20.04 still works (see the comment at the top of the Gemfile).
