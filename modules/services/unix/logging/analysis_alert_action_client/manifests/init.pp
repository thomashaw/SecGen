# Includes
include filebeat
include auditbeat
# include wazuh          # TODO: Might just leave this out for now.

# Pull out parameters from module
$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$aaa_config = $secgen_parameters['aaa_config'][0]
$elasticsearch_ip = $aaa_config['server_ip']
$elasticsearch_port = 0 + $aaa_config['elasticsearch_port']
$logstash_ip = 0 + $aaa_config['server_ip']
$logstash_port = 0 + $aaa_config['logstash_port']
$kibana_ip = $aaa_config['server_ip']
$kibana_port = 0 + $aaa_config['kibana_port']
# $agent_name = $secgen_parameters['wazuh_agent_name'][0]


# Call puppet classes etc in order.

class { 'filebeat':
  major_version => '6',
  outputs => {
    'logstash' => {
      'hosts' => [
        "$logstash_ip:$logstash_port",
      ],
      'index' => 'filebeat',
    },
  },
}

filebeat::prospector { 'syslogs':
  paths    => [
    '/var/log/auth.log',
    '/var/log/syslog',
  ],
  doc_type => 'syslog-beat',
}


class { 'auditbeat':
  modules => [
    {
      'module'  => 'auditd',
      'enabled' => true,
      'audit_rule_files' => '${path.config}/audit.rules.d/*.conf',
    },
  ],
  outputs => {
    'logstash' => {
      'hosts' => ["$logstash_ip:$logstash_port"],
    },
  },
}


ensure_package('libnotify-bin')

# TODO: Test on remote.