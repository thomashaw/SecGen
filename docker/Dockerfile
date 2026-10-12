# SecGen in a container, building VMs with the host's VirtualBox.
#
# VirtualBox runs VMs through a kernel driver, which a container cannot bring
# with it: VirtualBox must also be installed on the host, and the version
# installed here must match the host's exactly (VBOX_VERSION).
# docker/secgen-docker.sh reads the host version and passes it in for you.
#
#   docker build -f docker/Dockerfile --build-arg VBOX_VERSION=7.2.20 -t secgen .
#
# The SecGen checkout itself is mounted into the container at run time, so
# scenarios you edit and projects you build stay on the host.

ARG UBUNTU_VERSION=24.04
FROM ubuntu:${UBUNTU_VERSION}

# VirtualBox version on the host (`VBoxManage --version`, e.g. 7.2.20r175154 -> 7.2.20)
ARG VBOX_VERSION=7.2.20
# Vagrant version from releases.hashicorp.com ("latest" for the newest)
ARG VAGRANT_VERSION=latest

ENV DEBIAN_FRONTEND=noninteractive LANG=C.UTF-8

# Same packages as the native install in README.md
RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates curl wget gpg lsb-release sudo git openssh-client rsync kmod procps iproute2 \
      ruby-full ruby-dev build-essential patch pkg-config zlib1g-dev liblzma-dev \
      imagemagick exiftool libpq-dev libcurl4-openssl-dev libxml2-dev libxslt1-dev \
      graphviz libgraphviz-dev libpcap-dev libsqlite3-dev libgpgme-dev libyaml-dev libffi-dev libssl-dev \
 && rm -rf /var/lib/apt/lists/*

# Vagrant (HashiCorp's apt repo) and VirtualBox.
# VirtualBox comes from Oracle's download archive, not their apt repo: the apt repo only
# carries the newest build of each series, so it cannot supply the host's exact version
# once the host (e.g. Arch) is behind. The archive keeps every release, and the .deb is
# checked against the SHA256SUMS published beside it.
# VirtualBox's post-install step tries to build kernel modules and fails in a container;
# that is expected, as the host's modules are used.
RUN wget -qO- https://apt.releases.hashicorp.com/gpg | gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" > /etc/apt/sources.list.d/hashicorp.list \
 && apt-get update \
 && if [ "$VAGRANT_VERSION" = latest ]; then apt-get install -y vagrant; else apt-get install -y "vagrant=${VAGRANT_VERSION}*"; fi \
 && VBOX_MINOR=$(echo "$VBOX_VERSION" | cut -d. -f1,2) \
 && base="https://download.virtualbox.org/virtualbox/${VBOX_VERSION}" \
 && mkdir /tmp/vbox && cd /tmp/vbox \
 && curl -fsSLO "$base/SHA256SUMS" \
 && deb=$(grep -oE "virtualbox-${VBOX_MINOR}_${VBOX_VERSION}-[0-9]+~Ubuntu~$(lsb_release -cs)_amd64\.deb" SHA256SUMS | head -1) \
 && { [ -n "$deb" ] || { echo "No VirtualBox ${VBOX_VERSION} build for Ubuntu $(lsb_release -cs) at $base/" >&2; exit 1; }; } \
 && curl -fsSLO "$base/$deb" \
 && grep -F " *$deb" SHA256SUMS | sha256sum -c - \
 && (apt-get install -y --no-install-recommends "/tmp/vbox/$deb" || true) \
 && { dpkg -s "virtualbox-${VBOX_MINOR}" | grep -q '^Status: install ok installed' \
      || { echo "virtualbox-${VBOX_MINOR} did not install; dpkg status:" >&2; dpkg -s "virtualbox-${VBOX_MINOR}" >&2; exit 1; }; } \
 && cd / && rm -rf /tmp/vbox /var/lib/apt/lists/*

# Let host-only networks use any range (VirtualBox 6.1.28+ only allows 192.168.56.0/21
# by default, and SecGen scenarios use other ranges)
RUN mkdir -p /etc/vbox && echo '* 0.0.0.0/0 ::/0' > /etc/vbox/networks.conf

# Gems are installed system-wide from SecGen's Gemfile.lock, as in the native install
COPY Gemfile Gemfile.lock /tmp/secgen-gems/
RUN cd /tmp/secgen-gems && gem install --no-document bundler -v '~> 2.4' \
 && bundle config set --global silence_root_warning true \
 && bundle install --jobs "$(nproc)" \
 && rm -rf /tmp/secgen-gems

COPY docker/entrypoint.sh /usr/local/bin/secgen-entrypoint
WORKDIR /secgen
ENTRYPOINT ["/usr/local/bin/secgen-entrypoint"]
CMD ["ruby", "secgen.rb", "--help"]
