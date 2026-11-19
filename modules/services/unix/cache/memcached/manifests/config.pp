# Writes /etc/memcached.conf. Vulnerability modules seed data after memcached
# starts; they never rewrite this file.
class memcached::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $listen    = $secgen_parameters['listen'][0]
  $port      = $secgen_parameters['port'][0]
  $memory_mb = $secgen_parameters['memory_mb'][0]

  file { '/etc/memcached.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('memcached/memcached.conf.erb'),
    require => Class['memcached::install'],
    notify  => Class['memcached::service'],
  }
}
