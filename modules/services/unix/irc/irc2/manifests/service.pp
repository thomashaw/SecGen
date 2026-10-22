class irc2::service {
  # The packaged unit runs /usr/sbin/ircd as Type=simple, but ircd forks into
  # the background by default, so systemd sees the main process exit and
  # reports the unit inactive. -t keeps ircd in the foreground.
  file { '/etc/systemd/system/ircd-irc2.service.d':
    ensure  => directory,
    require => Package['ircd-irc2'],
  }->
  file { '/etc/systemd/system/ircd-irc2.service.d/foreground.conf':
    ensure  => present,
    content => "[Service]\nExecStart=\nExecStart=/usr/sbin/ircd -t\n",
    notify  => Exec['irc2-systemd-reload'],
  }

  exec { 'irc2-systemd-reload':
    command     => 'systemctl daemon-reload',
    path        => [ '/usr/bin', '/bin', '/usr/sbin' ],
    refreshonly => true,
  }->
  service { 'ircd-irc2':
    enable   => true,
    ensure   => 'running',
    provider => systemd,
  }
}
