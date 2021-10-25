$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$aaa_config = parsejson($secgen_parameters['aaa_config'][0])
$elasticsearch_ip = $aaa_config['server_ip']
$elasticsearch_port = 0 + $aaa_config['elasticsearch_port']
$logstash_ip = $aaa_config['server_ip']
$logstash_port = 0 + $aaa_config['logstash_port']
$kibana_ip = $aaa_config['server_ip']
$kibana_port = 0 + $aaa_config['kibana_port']

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

class { 'filebeat':
  major_version => '7',
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

class { 'analysis_alert_action_client': }