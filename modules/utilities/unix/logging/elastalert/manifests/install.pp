class elastalert::install ($elasticsearch_ip, $elasticsearch_port,$installdir = '/opt/elastalert/', $source='http://github.com/Yelp/elastalert') {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['python3-pip','build-essential','libssl-dev','libffi-dev','python-dev', 'supervisor' ])
  ensure_packages(['PyYAML>=5.1','elastalert','urllib3>=1.26.7'], { provider => 'pip3', require => [Package['python3-pip']] })

  # Create directory to install into   TODO: Change this to another variable name.  Should put configs in /etc/ probably if we're installing via...
  file { $installdir:
    ensure => directory,
  }

  # Clone elastalert from Github
  vcsrepo { 'elastalert':
    ensure   => present,
    provider => git,
    path     => $installdir,
    source   => $source,
    require  => File[$installdir],
  }

}
