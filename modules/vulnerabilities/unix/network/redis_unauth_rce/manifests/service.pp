class redis_unauth_rce::service {
  service { 'redis-server':
    ensure  => running,
    enable  => true,
    require => Package['redis-server'],
  }
}
