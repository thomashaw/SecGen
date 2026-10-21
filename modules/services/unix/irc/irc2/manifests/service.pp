class irc2::service {
  exec { 'irc2-systemd-reload':
    command     => 'systemctl daemon-reload',
    path        => [ '/usr/bin', '/bin', '/usr/sbin' ],
    refreshonly => true,
  }

  service { 'ircd-irc2':
    ensure   => 'running',
    enable   => true,
    provider => systemd,
    require  => Exec['irc2-systemd-reload'],
  }
}
