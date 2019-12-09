class elastalert::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]


  file { ['/opt/elastalert/', '/opt/elastalert/rules/']:
    ensure => directory,
  }

  file { '/opt/elastalert/config.yml':
    ensure => file,
    content => template('elastalert/config.yml.erb'),
    require => File['/opt/elastalert/'],
  }

}