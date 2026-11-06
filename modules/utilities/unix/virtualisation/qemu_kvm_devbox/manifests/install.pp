# Installs QEMU, libvirt and build tools, plus a Debian cloud image for disposable guests.
class qemu_kvm_devbox::install {
  Exec { path => ['/bin', '/usr/bin', '/sbin', '/usr/sbin'] }

  $packages = ['qemu-system-x86', 'qemu-utils', 'libvirt-daemon-system', 'virtinst', 'cloud-image-utils',
               'ruby', 'git', 'build-essential', 'wget']
  ensure_packages($packages)

  file { ['/var/lib/devbox', '/var/lib/devbox/images']:
    ensure => directory,
    mode   => '0755',
  }

  # The "latest" directory is rebuilt upstream, so the image is verified against its own checksum list.
  $image_url = 'https://cloud.debian.org/images/cloud/bookworm/latest'
  $image     = 'debian-12-genericcloud-amd64.qcow2'
  exec { 'download debian cloud image':
    command  => "wget -q ${image_url}/${image} ${image_url}/SHA512SUMS && sha512sum --ignore-missing -c SHA512SUMS && rm SHA512SUMS",
    cwd      => '/var/lib/devbox/images',
    provider => shell,
    creates  => "/var/lib/devbox/images/${image}",
    timeout  => 1800,
    require  => [File['/var/lib/devbox/images'], Package['wget']],
  }

  file { '/usr/local/bin/devbox-check':
    ensure => file,
    source => 'puppet:///modules/qemu_kvm_devbox/devbox-check',
    mode   => '0755',
  }

  file { '/usr/local/bin/devbox-new-target':
    ensure => file,
    source => 'puppet:///modules/qemu_kvm_devbox/devbox-new-target',
    mode   => '0755',
  }
}
