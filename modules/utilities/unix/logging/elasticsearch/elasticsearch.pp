$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
$elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]

include ::java

class { 'elasticsearch':
  api_host => $elasticsearch_ip,
  api_port => $elasticsearch_port,
  version => '6.3.1',
}

elasticsearch::instance { 'es-01':
  config => {
    'network.host' => $elasticsearch_ip,
    'http.port' => $elasticsearch_port,
  },
}

