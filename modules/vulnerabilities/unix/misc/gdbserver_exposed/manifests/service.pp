class gdbserver_exposed::service {
  require gdbserver_exposed::install
  require gdbserver_exposed::config

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port     = $secgen_parameters['port'][0]
  $username = $secgen_parameters['username'][0]

  # gdbserver in --multi mode keeps listening across client sessions and binds
  # all interfaces (0.0.0.0), so the debug stub is reachable from the network.
  file { '/etc/systemd/system/gdbserver-exposed.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => @("UNIT"),
      [Unit]
      Description=Exposed gdbserver (multi mode)
      After=network-online.target
      Wants=network-online.target

      [Service]
      Type=simple
      User=${username}
      Group=${username}
      ExecStart=/usr/bin/gdbserver --multi 0.0.0.0:${port}
      Restart=always
      RestartSec=3

      [Install]
      WantedBy=multi-user.target
      | UNIT
    notify  => Exec['gdbserver-exposed-daemon-reload'],
    require => User[$username],
  }

  exec { 'gdbserver-exposed-daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    path        => ['/usr/bin', '/bin', '/usr/sbin', '/sbin'],
    refreshonly => true,
  }

  service { 'gdbserver-exposed':
    ensure    => running,
    enable    => true,
    provider  => 'systemd',
    subscribe => File['/etc/systemd/system/gdbserver-exposed.service'],
    require   => [
      Exec['gdbserver-exposed-daemon-reload'],
      Package['gdbserver'],
    ],
  }
}
