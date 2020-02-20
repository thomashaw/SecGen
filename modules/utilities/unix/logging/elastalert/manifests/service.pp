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

  # Service to automatically create elastalert index, runs after reboot
  file { '/etc/systemd/system/elastalert-index.service':
    ensure => present,
    source => 'puppet:///modules/elastalert/elastalert-index.service'
  }

  file { '/usr/local/bin/elastalert-index.rb':
    ensure => file,
    source => 'puppet:///modules/elastalert/elastalert-index.rb',
  }

  service { 'elastalert-index':
    ensure => undef,
    enable => true,
    require => [File['/usr/local/bin/elastalert-index.rb'], File['/etc/systemd/system/elastalert-index.service']],
    provider => 'systemd',
    path => '/etc/systemd/system/'
  }
}