unless defined('analysis_alert_action_server') or defined('analysis_alert_action_client') {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $component = $secgen_parameters['wazuh_component'][0]
  $kibana_elasticsearch_ip = $secgen_parameters['server_address'][0]
  $agent_name = $secgen_parameters['wazuh_agent_name'][0]

  if ($component == 'server') {
    class { '::wazuh::manager':
      ossec_smtp_server   => 'localhost',
      ossec_emailto       => ['user@mycompany.com'],
      agent_auth_password => '6663484170b2c69451e01ba11f319533', #todo: obviously fix this - must be 32char
    }
    class { '::wazuh::kibana':
      kibana_elasticsearch_ip => $kibana_elasticsearch_ip,
    }

    exec { 'enable ossec auth':
      command => '/var/ossec/bin/ossec-control enable auth',
      require => Class['::wazuh::manager'],
    }

  } elsif ($component == 'client') {
    class { "::wazuh::agent":
      wazuh_register_endpoint  => $kibana_elasticsearch_ip,
      wazuh_reporting_endpoint => $kibana_elasticsearch_ip,
      agent_name               => $agent_name,
    }
  }
}