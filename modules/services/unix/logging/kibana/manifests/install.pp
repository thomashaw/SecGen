class kibana::install () {
  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  # Add the Elastic apt repository here so the module also works without the
  # elasticsearch module on the same VM (same repo file, so no clash if both run).
  exec { 'kibana add gpg key':
    command => 'wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -',
    unless  => 'apt-key list 2>/dev/null | grep -qi elasticsearch',
  }->
  exec { 'kibana add apt repository':
    command => "echo \"deb ${secgen_functions::mirror_url('https://artifacts.elastic.co/packages/7.x/apt')} stable main\" > /etc/apt/sources.list.d/elastic-7.x.list",
    unless  => 'test -s /etc/apt/sources.list.d/elastic-7.x.list',
  }->
  exec { 'kibana update apt':
    command => 'apt-get update',
    unless  => 'dpkg -s kibana >/dev/null 2>&1',
  }->
  package { 'kibana':
    ensure => present,
  }
}
