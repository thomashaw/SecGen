# Enables memcached at boot and keeps it running (restarts when the config changes).
class memcached::service {
  service { 'memcached':
    ensure  => running,
    enable  => true,
    require => Class['memcached::install'],
  }
}
