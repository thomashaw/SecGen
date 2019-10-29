$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
$component = $secgen_parameters['component'][0]
if ($component == 'server') {
  class { '::wazuh::manager':
    ossec_smtp_server   => 'localhost',
    ossec_emailto => ['user@mycompany.com'],
    agent_auth_password => '6663484170b2c69451e01ba11f319533', #todo: obviously fix this - must be 32char
  }
} elsif ($component == 'client') {
  class { "::wazuh::agent":
    wazuh_register_endpoint => "192.168.209.166",
    wazuh_reporting_endpoint => "192.168.209.166",
    agent_name => 'test_name',
  }
}