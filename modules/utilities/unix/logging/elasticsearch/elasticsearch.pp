$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
$elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]

include ::java

include elasticsearch::update_apt

class { 'elasticsearch':
  api_host => $elasticsearch_ip,
  api_port => $elasticsearch_port,
  version => '7.6.0',
  require => Class['elasticsearch::update_apt']
}

elasticsearch::instance { 'es-01':
  config => {
    'network.host' => $elasticsearch_ip,
    'http.port' => $elasticsearch_port,
  },
}

