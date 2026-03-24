class bind9::config {
  $secgen_params  = secgen_functions::get_parameters($::base64_inputs_file)
  $domain         = $secgen_params['domain'][0]
  $ns_hostname    = $secgen_params['ns_hostname'][0]
  $serial         = $secgen_params['serial'][0]
  $flag           = $secgen_params['strings_to_leak'][0]
  $flag_hostname  = $secgen_params['flag_hostname'][0]
  $a_records      = $secgen_params['a_records']
  $allow_transfer = $secgen_params['allow_transfer'][0]

  # Split a_records into hostname/ip pairs
  $a_record_pairs = $a_records.map |$r| {
    $parts = split($r, '=')
    { 'hostname' => $parts[0], 'ip' => $parts[1] }
  }

  file { '/etc/bind/named.conf.options':
    ensure  => present,
    owner   => 'root',
    group   => 'bind',
    mode    => '0644',
    content => template('bind9/named.conf.options.erb'),
    require => Package['bind9'],
    notify  => Service['bind9'],
  }

  file { '/etc/bind/named.conf.local':
    ensure  => present,
    owner   => 'root',
    group   => 'bind',
    mode    => '0644',
    content => template('bind9/named.conf.local.erb'),
    require => Package['bind9'],
    notify  => Service['bind9'],
  }

  file { "/etc/bind/db.${domain}":
    ensure  => present,
    owner   => 'root',
    group   => 'bind',
    mode    => '0644',
    content => template('bind9/zone.db.erb'),
    require => Package['bind9'],
    notify  => Service['bind9'],
  }

  service { 'bind9':
    ensure  => running,
    enable  => true,
    require => Package['bind9'],
  }
}