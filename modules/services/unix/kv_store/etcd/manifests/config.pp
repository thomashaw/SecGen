# Writes /etc/default/etcd with the client URL and a matching advertise URL.
class etcd::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port           = $secgen_parameters['port'][0]
  $listen_address = $secgen_parameters['listen_address'][0]

  # etcd rejects a wildcard advertise URL, so fall back to loopback
  if $listen_address == '0.0.0.0' {
    $advertise_address = '127.0.0.1'
  } else {
    $advertise_address = $listen_address
  }

  file { '/etc/default/etcd':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('etcd/etcd.erb'),
    require => Class['etcd::install'],
    notify  => Service['etcd'],
  }
}
