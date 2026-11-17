# Writes the global-only /etc/rsyncd.conf and creates the include directory.
class rsyncd::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $address = $secgen_parameters['address'][0]

  # Vulnerability modules drop one NAME.conf module definition in here
  file { '/etc/rsyncd.d':
    ensure  => directory,
    owner   => 'root',
    group   => 'root',
    mode    => '0755',
    require => Class['rsyncd::install'],
  }

  file { '/etc/rsyncd.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('rsyncd/rsyncd.conf.erb'),
    require => File['/etc/rsyncd.d'],
    notify  => Service['rsync'],
  }
}
