class irc2::config {
  # The Debian package reads /etc/ircd/ircd.conf and /etc/ircd/ircd.motd
  # (compiled in), and creates /etc/ircd (root:irc 0750) on install, so these
  # must be written after the package. /etc/ircd.conf is never read.
  # The daemon runs as the irc user, hence group irc and mode 0640.
  file { '/etc/ircd/ircd.conf':
    ensure  => present,
    source  => 'puppet:///modules/irc2/ircd.conf',
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    require => Package['ircd-irc2'],
    notify  => Service['ircd-irc2'],
  }

  file { '/etc/ircd/ircd.motd':
    ensure  => present,
    source  => 'puppet:///modules/irc2/ircd.motd',
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    require => Package['ircd-irc2'],
    notify  => Service['ircd-irc2'],
  }

  # Without this, the packaged iauth.conf (required + rfc931 ident lookups)
  # delays client registration after the 020 "Please wait" notice.
  file { '/etc/ircd/iauth.conf':
    ensure  => present,
    source  => 'puppet:///modules/irc2/iauth.conf',
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    require => Package['ircd-irc2'],
    notify  => Service['ircd-irc2'],
  }
}
