$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$aaa_config = parsejson($secgen_parameters['aaa_config'][0])
$elasticsearch_ip = $aaa_config['server_ip']
$elasticsearch_port = 0 + $aaa_config['elasticsearch_port']
$logstash_port = 0 + $aaa_config['logstash_port']
$kibana_ip = $aaa_config['server_ip']
$kibana_port = 0 + $aaa_config['kibana_port']

class { 'elasticsearch':
  api_host => $elasticsearch_ip,
  api_port => $elasticsearch_port,
  version => '6.3.1',
}~>
elasticsearch::instance { 'es-01':
  config => {
    'network.host' => $elasticsearch_ip,
    'http.port' => $elasticsearch_port,
  },
}~>
class { 'logstash':
  settings => {
    'http.host' => $elasticsearch_ip,
  }
}
logstash::configfile { 'my_ls_config':
  content => template('logstash/configfile-template.erb'),
}~>
class { 'kibana':
  ensure => '6.3.1',
  config => {
    'server.host'       => $kibana_ip,
    'elasticsearch.url' => "http://$elasticsearch_ip:$elasticsearch_port",
    'server.port'       => $kibana_port,
  }
}~>
class { 'elastalert':
  elasticsearch_ip => $elasticsearch_ip,
  elasticsearch_port => $elasticsearch_port,
}~>
class { 'analysis_alert_action_server': }