# Includes
include filebeat
include auditbeat
# include wazuh          # TODO: Might just leave this out for now.

# Pull out parameters from module
$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
$elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
$logstash_port = 0 + $secgen_parameters['logstash_port'][0]
$kibana_ip = $secgen_parameters['kibana_ip'][0]
$kibana_port = 0 + $secgen_parameters['kibana_port'][0]
$agent_name = $secgen_parameters['wazuh_agent_name'][0]


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