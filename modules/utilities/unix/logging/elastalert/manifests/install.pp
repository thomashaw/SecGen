class elastalert::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]

  ensure_packages('pip3')
  ensure_packages(['elastalert', 'setuptools>=11.3'], { provider => 'pip3', require => [Package['pip3']] })

}