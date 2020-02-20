class elastalert::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $diff_path = '/opt/elastalert/elastalert.diff'
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages('python3-pip')
  ensure_packages(['elastalert', 'setuptools>=11.3', 'PyYAML>=5.1', 'elasticsearch==7.5.1'], { provider => 'pip3', require => [Package['python3-pip']] })

}
