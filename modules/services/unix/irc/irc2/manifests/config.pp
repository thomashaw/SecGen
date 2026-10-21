class irc2::config {
  # Debian's ircd-irc2 reads /etc/ircd/ircd.conf (created by the package),
  # so write ours after the package is installed.
  file { '/etc/ircd/ircd.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    source  => 'puppet:///modules/irc2/ircd.conf',
    require => Package['ircd-irc2'],
    notify  => Service['ircd-irc2'],
  }

  file { '/etc/ircd/ircd.motd':
    ensure  => file,
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    source  => 'puppet:///modules/irc2/ircd.motd',
    require => Package['ircd-irc2'],
    notify  => Service['ircd-irc2'],
  }

  # The packaged unit is Type=simple, but ircd daemonises, so systemd sees the
  # main process exit and marks the service dead. Declare it as forking.
  file { '/etc/systemd/system/ircd-irc2.service.d':
    ensure  => directory,
    require => Package['ircd-irc2'],
  }

  file { '/etc/systemd/system/ircd-irc2.service.d/forking.conf':
    ensure  => file,
    content => "[Service]\nType=forking\nExecStart=\nExecStart=/usr/sbin/ircd\nPIDFile=/var/run/ircd/ircd.pid\n",
    require => File['/etc/systemd/system/ircd-irc2.service.d'],
    notify  => Exec['irc2-systemd-reload'],
  }
}
