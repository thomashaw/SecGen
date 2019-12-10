class elastalert::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $elastalert_dir = '/opt/elastalert/'

  ensure_packages('python3-pip')
  ensure_packages(['setuptools>=11.3', 'PyYAML>=5.1', 'elasticsearch==6.3.1'], { provider => 'pip3', require => [Package['python3-pip']], notify => File[$elastalert_dir] })

  file { $elastalert_dir:
    ensure => directory,
  }

  exec { 'clone elastalert repo':
    command => '/usr/bin/git clone https://github.com/Yelp/elastalert',
    cwd     => $elastalert_dir,
    require => [File[$elastalert_dir], Package['python3-pip']],
  }

  exec { 'run elastalert setup.py':
    command   => '/usr/bin/python3 setup.py install',
    cwd       => $elastalert_dir,
    logoutput => true,
    loglevel => info,
    timeout   => 0,
    require => [Exec['clone elastalert repo']],
  }

}
