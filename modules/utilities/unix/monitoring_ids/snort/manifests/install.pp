class snort::install {

  # install the binary, rules and config via the Debian repo (the apt provider
  # sets DEBIAN_FRONTEND=noninteractive, so the snort debconf prompts are skipped)
  package { ['snort-common', 'snort-rules-default', 'snort']:
    ensure => installed,
  }
  ->
  # force it to not be enabled because the interface in the config may be wrong
  exec { 'disable snort at boot':
    path     => ['/bin/', '/sbin/', '/usr/bin/', '/usr/sbin/'],
    command  => 'systemctl disable snort',
    provider => shell,
    unless   => 'systemctl is-enabled snort 2>&1 | grep -q "^disabled"',
  }
}
