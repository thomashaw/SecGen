$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$component = $secgen_parameters['component'][0]
$kibana_elasticsearch_ip = $secgen_parameters['kibana_ip'][0]
if ($component == 'server') {
  class { '::wazuh::manager':
    ossec_smtp_server   => 'localhost',
    ossec_emailto => ['user@mycompany.com'],
    agent_auth_password => '6663484170b2c69451e01ba11f319533', #todo: obviously fix this - must be 32char
  }
  class { '::wazuh::kibana':
    kibana_elasticsearch_ip => $kibana_elasticsearch_ip,
  }

  exec{ 'enable ossec auth':
    command => '/var/ossec/bin/ossec-control enable auth',
    notify => Service[$wazuh::params_manager::server_service],
    require => Class['::wazuh::manager'],
  }

} elsif ($component == 'client') {
  class { "::wazuh::agent":
    wazuh_register_endpoint => $kibana_elasticsearch_ip,
    wazuh_reporting_endpoint => $kibana_elasticsearch_ip,
    agent_name => 'test_name',
    agent_auth_password => '6663484170b2c69451e01ba11f319533', #todo: obviously fix this - must be 32char
  }
}