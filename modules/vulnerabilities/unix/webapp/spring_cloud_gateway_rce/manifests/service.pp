# Class: spring_cloud_gateway_rce::service
# Runs the gateway as a systemd service under the non-root gateway user.
class spring_cloud_gateway_rce::service {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user = $secgen_parameters['unix_username'][0]

  file { '/etc/systemd/system/scg.service':
    content => template('spring_cloud_gateway_rce/scg.service.erb'),
    owner   => 'root',
    mode    => '0644',
  }

  service { 'scg':
    ensure => 'running',
    enable => 'true',
  }
}
