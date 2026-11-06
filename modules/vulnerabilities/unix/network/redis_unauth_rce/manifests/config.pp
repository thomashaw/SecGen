class redis_unauth_rce::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]

  $conf = '/etc/redis/redis.conf'

  # Every edit below needs the config file in place first, and a daemon restart
  # once it changes.
  File_line {
    path    => $conf,
    require => Package['redis-server'],
    notify  => Service['redis-server'],
  }

  # Listen on every interface rather than loopback only, so the daemon is
  # reachable across the network.
  file_line { 'redis-bind-all':
    line  => 'bind 0.0.0.0',
    match => '^bind ',
  }

  # Protected mode refuses commands from non-loopback clients when no password
  # is set; turning it off exposes the unauthenticated instance.
  file_line { 'redis-protected-mode-off':
    line  => 'protected-mode no',
    match => '^protected-mode ',
  }

  # Serve on the configured port.
  file_line { 'redis-port':
    line  => "port ${port}",
    match => '^port ',
  }

  # Since Redis 7.0 the MODULE command is disabled by default; the replication
  # attack loads an attacker-supplied module, so it must be allowed.
  file_line { 'redis-enable-module-command':
    line  => 'enable-module-command yes',
    match => '^enable-module-command ',
  }

  # No requirepass is configured (the default config ships it commented out),
  # so the instance accepts commands without authentication.
}
