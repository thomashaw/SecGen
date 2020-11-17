class elastalert::install ($elasticsearch_ip, $elasticsearch_port,$installdir = '/opt/elastalert/', $source='http://github.com/Yelp/elastalert') {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['python-pip','build-essential','libssl-dev','libffi-dev','python-dev'])
  ensure_packages(['thehive4py','configparser>=3.5.0','setuptools>=11.3', 'stomp.py<=4.1.22','cryptography>=2.8','mock>=2.0.0,<4.0.0', 'elasticsearch==6.3.1', 'PyJWT==1.7.1'], { provider => 'pip', require => [Package['python-pip']] })

  # Create directory to install into
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
    revision => '98c7867',   # reset to 0.1.39
  }

  exec { 'setup.py install':
    command => '/usr/bin/python2.7 setup.py install',
    cwd => '/opt/elastalert',
    require => Vcsrepo['elastalert'],
  }

}
