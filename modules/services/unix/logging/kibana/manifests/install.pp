class kibana::install () {
  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  # Kibana is not in Debian's repositories: add Elastic's 7.x apt repo (same
  # release line as the elasticsearch module). The list file name is shared
  # with that module, so on a combined box whichever runs first wins.
  exec { 'kibana add gpg key':
    command => 'mkdir -p /etc/apt/keyrings && wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | gpg --dearmor > /etc/apt/keyrings/elastic.gpg',
    creates => '/etc/apt/keyrings/elastic.gpg',
  }->
  exec { 'kibana add apt repository':
    command => "echo \"deb [signed-by=/etc/apt/keyrings/elastic.gpg] ${secgen_functions::mirror_url('https://artifacts.elastic.co/packages/7.x/apt')} stable main\" > /etc/apt/sources.list.d/elastic-7.x.list",
    creates => '/etc/apt/sources.list.d/elastic-7.x.list',
  }->
  exec { 'kibana update apt':
    command => 'apt-get update',
    unless  => 'dpkg -s kibana 2>/dev/null | grep -q "^Status: install ok installed"',
  }->
  package { 'kibana':
    ensure => present,
  }
}
