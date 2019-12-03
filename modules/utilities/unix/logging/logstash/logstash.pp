$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$logstash_port = 0 + $secgen_parameters['logstash_port'][0]
$elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
$elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]

class { 'logstash':
  settings => {
    'http.host' => $elasticsearch_ip,
  }
}
logstash::configfile { 'my_ls_config':
  content => template('logstash/configfile-template.erb'),
}