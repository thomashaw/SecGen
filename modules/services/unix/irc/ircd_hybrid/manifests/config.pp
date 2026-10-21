class ircd_hybrid::config {
  # Start the daemon only after the init script has been patched (see install.pp);
  # the package postinst deliberately leaves it stopped.
  service { 'ircd-hybrid':
    ensure  => 'running',
    enable  => true,
    require => Exec['fix ircd-hybrid init script DEFAULT bug'],
  }
}
