class elastalert::install ($elasticsearch_ip, $elasticsearch_port,$installdir = '/opt/elastalert/', $source='http://github.com/Yelp/elastalert') {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['python3-pip','build-essential','libssl-dev','libffi-dev','python-dev', 'supervisor' ])

  exec { 'run pip3 update':
    command => '/usr/bin/python3 -m pip install --upgrade pip',
    require => Package['python3-pip']
  }

  ensure_packages(['urllib3>=1.26.7'], { provider => 'pip3', require=> [Package['python3-pip'], Exec['run pip3 update']] })
 
  exec { 'install PyYAML ignore existing':
	command => '/usr/bin/python3 -m pip install --ignore-installed PyYAML>=5.1',
	require => Package['urllib3>=1.26.7'],
  }

  ensure_packages(['elastalert'], { provider => 'pip3', require => [Package['python3-pip'], Exec['run pip3 update'], Package['urllib3>=1.26.7'], Exec['install PyYAML ignore existing']] })

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
