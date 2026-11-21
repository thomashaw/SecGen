class redis_unauth_rce::install {
  # redis-server ships the daemon, /etc/redis/redis.conf and the systemd unit
  # (the unit runs redis as the non-root 'redis' user).
  package { 'redis-server':
    ensure => installed,
  }
}
