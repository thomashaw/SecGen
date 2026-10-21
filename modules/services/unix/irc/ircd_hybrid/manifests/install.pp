class ircd_hybrid::install {
  # The package postinst runs `openssl req -config /etc/ircd-hybrid/cert.cnf`, and
  # cert.cnf contains `RANDFILE = $ENV::HOME/.rnd`. HOME is unset under puppet apply,
  # so openssl aborts and dpkg leaves the package unconfigured. Install via exec with
  # HOME set; the package resource then just confirms the result.
  exec { 'install ircd-hybrid with HOME set':
    command     => '/usr/bin/apt-get -q -y -o DPkg::Options::=--force-confold install ircd-hybrid',
    unless      => '/usr/bin/dpkg -s ircd-hybrid 2>/dev/null | /bin/grep -q "^Status: install ok installed"',
    environment => ['HOME=/root', 'DEBIAN_FRONTEND=noninteractive'],
    timeout     => 600,
  }

  package { 'ircd-hybrid':
    ensure  => 'installed',
    require => Exec['install ircd-hybrid with HOME set'],
  }
}
