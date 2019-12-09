class elastalert::service {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $ea_service_file = '/etc/systemd/system/elastalert.service'

  file { $ea_service_file:
    ensure => file,
    source => 'puppet:///modules/elastalert/elastalert.service',
  }

  service { 'elastalert':
    ensure => undef,
    enable => true,
    provider => 'systemd',
    path => '/etc/systemd/system/',
    require => File[$ea_service_file],
  }

}