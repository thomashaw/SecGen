class irc2::service {
  # The ircd-irc2 package ships a working /etc/ircd/ircd.conf and systemd unit,
  # and its postinst starts the daemon on port 6667. Just make sure the service
  # is running and enabled after the package is in place. Do not force a restart
  # or swap the unit type: ircd races on rebinding 6667, so a restart during
  # provisioning times out even though the initial start is fine.
  service { 'ircd-irc2':
    ensure  => 'running',
    enable  => true,
    require => Package['ircd-irc2'],
  }
}
