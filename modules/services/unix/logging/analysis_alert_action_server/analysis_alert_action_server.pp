# Includes
include ::java
include elasticsearch
include logstash
include kibana
# include wazuh          # TODO: Might just leave this out for now.
include elastalert
include alert_actioner


# Pull out parameters from module
$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
$elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
$logstash_port = 0 + $secgen_parameters['logstash_port'][0]
$kibana_ip = $secgen_parameters['kibana_ip'][0]
$kibana_port = 0 + $secgen_parameters['kibana_port'][0]
$agent_name = $secgen_parameters['wazuh_agent_name'][0]


# Call puppet classes etc in order.

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


class { 'logstash':
  settings => {
    'http.host' => $elasticsearch_ip,
  }
}
logstash::configfile { 'my_ls_config':
  content => template('logstash/configfile-template.erb'),
}


class { 'kibana':
  ensure => '6.3.1',
  config => {
    'server.host'       => $kibana_ip,
    'elasticsearch.url' => "http://$elasticsearch_ip:$elasticsearch_port",
    'server.port'       => $kibana_port,
  }
}

#  TODO: Just leave wazuh out for now - not needed yet.
# class { '::wazuh::manager':
#   ossec_smtp_server   => 'localhost',
#   ossec_emailto => ['user@mycompany.com'],
#   agent_auth_password => '6663484170b2c69451e01ba11f319533', #todo: obviously fix this - must be 32char
# }
# class { '::wazuh::kibana':
#   kibana_elasticsearch_ip => $kibana_elasticsearch_ip,
# }
#
# exec{ 'enable ossec auth':
#   command => '/var/ossec/bin/ossec-control enable auth',
#   require => Class['::wazuh::manager'],
# }
#

require elastalert::install
require elastalert::config
require elastalert::service


require alert_actioner::init

# TODO: Test on remote.