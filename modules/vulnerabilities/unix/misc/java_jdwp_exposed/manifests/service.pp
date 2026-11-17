# Runs the compiled Java service under systemd as the service account, with the
# JDWP debug agent bound to all interfaces so it is reachable over the network.
# After=network-online.target so the agent only binds once the isolated lab
# interface is up, making it answer dependably on first contact.
class java_jdwp_exposed::service {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user              = $secgen_parameters['unix_username'][0]
  $port              = $secgen_parameters['port'][0]
  $app_dir           = '/opt/jdwp_service'

  file { '/etc/systemd/system/jdwp_service.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('java_jdwp_exposed/jdwp.service.erb'),
    require => Class['java_jdwp_exposed::install'],
  }
  -> exec { 'jdwp_service systemd daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    path        => ['/bin', '/usr/bin', '/sbin', '/usr/sbin'],
    refreshonly => true,
    subscribe   => File['/etc/systemd/system/jdwp_service.service'],
  }
  -> service { 'jdwp_service':
    ensure => running,
    enable => true,
  }
}
