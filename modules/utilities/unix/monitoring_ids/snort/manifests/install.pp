class snort::install {

  # Snort is not in the Debian 12 (bookworm) repositories: it was dropped after
  # buster. Ubuntu 22.04 (jammy) ships Snort 2.9.15.1 built against libssl3 and
  # glibc 2.34+, and every library it depends on is in bookworm, so install the
  # four jammy .debs with apt (which resolves the dependencies from bookworm).
  # The download script verifies SHA256s from the jammy universe Packages index.
  $deb_dir = '/opt/snort-debs'
  $path    = ['/bin/', '/sbin/', '/usr/bin/', '/usr/sbin/']

  file { $deb_dir:
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
  }
  ->
  file { '/usr/local/sbin/download_snort_debs.sh':
    ensure => file,
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
    source => 'puppet:///modules/snort/download_snort_debs.sh',
  }
  ->
  exec { 'download snort debs':
    path      => $path,
    timeout   => 600,
    creates   => "${deb_dir}/.verified",
    logoutput => true,
    command   => "/usr/local/sbin/download_snort_debs.sh ${deb_dir}",
  }
  ->
  # keep the postinst from starting snort (the configured interface may not exist)
  exec { 'preseed snort debconf':
    path     => $path,
    provider => shell,
    command  => 'echo "snort snort/startup select manual" | debconf-set-selections',
    unless   => 'dpkg -s snort 2>/dev/null | grep -q "^Status: install ok installed"',
  }
  ->
  exec { 'install snort debs':
    path        => $path,
    provider    => shell,
    timeout     => 900,
    logoutput   => true,
    environment => ['DEBIAN_FRONTEND=noninteractive'],
    command     => "apt-get install -y -o DPkg::Options::=--force-confold ${deb_dir}/snort-common_2.9.15.1-6build1_all.deb ${deb_dir}/snort-rules-default_2.9.15.1-6build1_all.deb ${deb_dir}/snort-common-libraries_2.9.15.1-6build1_amd64.deb ${deb_dir}/snort_2.9.15.1-6build1_amd64.deb",
    unless      => 'dpkg -s snort 2>/dev/null | grep -q "^Status: install ok installed"',
  }
  ->
  # force it to not be enabled because the interface in the config may be wrong
  exec { 'disable snort at boot':
    path     => $path,
    command  => 'systemctl disable snort; systemctl stop snort; true',
    provider => shell,
    unless   => 'systemctl is-enabled snort 2>&1 | grep -q "^disabled"',
  }
}
