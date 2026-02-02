class ldap_server::init {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)

  $domain = $secgen_parameters['domain'][0]
  $organization = $secgen_parameters['organization'][0]
  $admin_password = $secgen_parameters['admin_password'][0]

  class { 'ldap_server::install':
    domain         => $domain,
    organization   => $organization,
    admin_password => $admin_password,
  }
}
