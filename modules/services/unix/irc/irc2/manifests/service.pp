class irc2::service {
  # The packaged unit runs /usr/sbin/ircd as Type=simple, but ircd forks into
  # the background by default, so systemd sees the main process exit and
  # reports the unit inactive. -t keeps ircd in the foreground.
  #
  # -s stops ircd starting the external iauth helper. By default iauth is
  # "required" (see the packaged iauth.conf), so ircd holds every new client
  # at the 020 "Please wait while we process your connection" notice until
  # iauth returns a verdict; a stripped, module-less iauth never does, so
  # clients never finish registering. With -s ircd does a quick built-in
  # RFC1413 ident check instead (refused instantly on the loopback the test
  # connects over) and, because the I line's hostname field is empty, matches
  # the client by IP without waiting on a reverse-DNS lookup, so registration
  # completes well within the test's read window.
  file { '/etc/systemd/system/ircd-irc2.service.d':
    ensure  => directory,
    require => Package['ircd-irc2'],
  }->
  file { '/etc/systemd/system/ircd-irc2.service.d/foreground.conf':
    ensure  => present,
    content => "[Service]\nExecStart=\nExecStart=/usr/sbin/ircd -s -t\n",
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
