class openvpn_server::config {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $openvpn_pki   = parsejson($secgen_params['openvpn_pki'][0])
  $start_on_boot = $secgen_params['start_on_boot'][0]

  $ca_cert        = $openvpn_pki['ca_cert']
  $server_cert    = $openvpn_pki['server_cert']
  $server_key     = $openvpn_pki['server_key']
  $dh_params      = $openvpn_pki['dh_params']
  $server_ip      = $openvpn_pki['server_ip']
  $port           = $openvpn_pki['port']
  $vpn_subnet     = $openvpn_pki['vpn_subnet']
  $vpn_subnet_mask = $openvpn_pki['vpn_subnet_mask']
  $push_routes    = $openvpn_pki['push_routes']

  file { '/etc/openvpn/server':
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0750',
  }

  file { '/etc/openvpn/server/ca.crt':
    ensure  => present,
    owner   => 'root',
    group   => 'root',
    mode    => '0640',
    content => $ca_cert,
    require => File['/etc/openvpn/server'],
  }

  file { '/etc/openvpn/server/server.crt':
    ensure  => present,
    owner   => 'root',
    group   => 'root',
    mode    => '0640',
    content => $server_cert,
    require => File['/etc/openvpn/server'],
  }

  file { '/etc/openvpn/server/server.key':
    ensure  => present,
    owner   => 'root',
    group   => 'root',
    mode    => '0600',
    content => $server_key,
    require => File['/etc/openvpn/server'],
  }

  file { '/etc/openvpn/server/dh.pem':
    ensure  => present,
    owner   => 'root',
    group   => 'root',
    mode    => '0640',
    content => $dh_params,
    require => File['/etc/openvpn/server'],
  }

  file { '/etc/openvpn/server/server.conf':
    ensure  => present,
    owner   => 'root',
    group   => 'root',
    mode    => '0640',
    content => template('openvpn_server/server.conf.erb'),
    require => File['/etc/openvpn/server'],
  }

  if $start_on_boot == 'true' {
    service { 'openvpn-server@server':
      ensure  => running,
      enable  => true,
      require => File['/etc/openvpn/server/server.conf'],
    }
  } else {
    service { 'openvpn-server@server':
      ensure  => stopped,
      enable  => false,
      require => File['/etc/openvpn/server/server.conf'],
    }
  }
}