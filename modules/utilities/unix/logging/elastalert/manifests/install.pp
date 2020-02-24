class elastalert::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $installdir = '/opt/elastalert/'
  $source = 'http://github.com/Yelp/elastalert'
  # $diff_path = '/opt/elastalert/elastalert.diff'
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['python-pip','build-essential','libssl-dev','libffi-dev','python-dev'])
  ensure_packages(['thehive4py','configparser>=3.5.0','setuptools>=11.3'], { provider => 'pip', require => [Package['python-pip']] })
  # ensure_packages(['elastalert', 'setuptools>=11.3', 'PyYAML>=5.1', 'elasticsearch==6.3.1'], { provider => 'pip3', require => [Package['python3-pip']] })

  # apt upgrade cryptography
  # exec { 'apt remove cryptography':
  #   command => '/usr/bin/apt-get remove cryptography',
  # }

  ensure_packages(['cryptography>=2.8','mock>=2.0.0,<4.0.0', 'elasticsearch==6.3.1'], {provider => 'pip'})

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

  # file { $diff_path:
  #   ensure => file,
  #   source => 'puppet:///modules/elastalert/elastalert.diff',
  # }
  #
  # exec { 'apply diff patch':
  #   command => "patch -p1 /usr/local/lib/python3.5/dist-packages/elastalert/alerts.py < $diff_path",
  #   require => File[$diff_path],
  # }

}
