$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$aaa_config = parsejson($secgen_parameters['aaa_config'][0])
$elasticsearch_ip = $aaa_config['server_ip']
$elasticsearch_port = 0 + $aaa_config['elasticsearch_port']
$logstash_port = 0 + $aaa_config['logstash_port']
$kibana_ip = $aaa_config['server_ip']
$kibana_port = 0 + $aaa_config['kibana_port']

class { 'elasticsearch_7':
  api_host => $elasticsearch_ip,
  api_port => $elasticsearch_port,
}~>
class { 'logstash_7':
  elasticsearch_ip => $elasticsearch_ip,
  elasticsearch_port => $elasticsearch_port,
  logstash_port => $logstash_port
}
class { 'kibana_7':
  elasticsearch_ip => $elasticsearch_ip,
  elasticsearch_port => $elasticsearch_port,
  kibana_port => $kibana_port
}~>
class { 'elastalert':
  elasticsearch_ip => $elasticsearch_ip,
  elasticsearch_port => $elasticsearch_port,
}~>
class { 'analysis_alert_action_server': }