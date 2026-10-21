class ircd_hybrid::install {
  # ircd-hybrid on bookworm needs two packaging work-arounds before it will install
  # and start cleanly under puppet apply.
  #
  # 1. The postinst starts the daemon (debconf restart_on_upgrade defaults to true)
  #    via the package's SysV init script. That script is run as /bin/sh, which is
  #    bash in POSIX mode on the desktop base. It has a packaging bug: $DEFAULT is
  #    never defined, so `[ -f $DEFAULT ]` tests the non-empty string "-f" (true)
  #    and then runs a bare `.`, which fails ("filename argument required"). Because
  #    `.` is a POSIX special built-in, the shell exits 2 and the service never
  #    starts; invoke-rc.d then fails and apt-get returns 100. We preseed
  #    restart_on_upgrade=false so the postinst does not try to start the daemon,
  #    letting the install complete, and start the service from config.pp instead.
  #
  # 2. We then fix the init script's $DEFAULT bug so the service can actually start.
  #    The old /etc/default/ircd-hybrid conffile was removed by the package, so once
  #    DEFAULT is defined the `[ -f $DEFAULT ]` guard is simply false (dead block).
  #
  # HOME=/root gives openssl a writable home for the postinst's cert generation; the
  # "Can't load /root/.rnd" message it still prints is a harmless warning under
  # OpenSSL 3 and does not abort the cert generation.
  exec { 'install ircd-hybrid without starting it':
    command     => '/bin/echo "ircd-hybrid ircd-hybrid/restart_on_upgrade boolean false" | /usr/bin/debconf-set-selections && /usr/bin/apt-get -q -y -o DPkg::Options::=--force-confold install ircd-hybrid',
    unless      => '/usr/bin/dpkg -s ircd-hybrid 2>/dev/null | /bin/grep -q "^Status: install ok installed"',
    environment => ['HOME=/root', 'DEBIAN_FRONTEND=noninteractive'],
    timeout     => 600,
  }

  # Define DEFAULT in the init script so the broken `[ -f $DEFAULT ]` / bare `.`
  # block becomes a harmless no-op and the daemon can start.
  exec { 'fix ircd-hybrid init script DEFAULT bug':
    command => '/bin/sed -i "/^DAEMON=/a DEFAULT=/etc/default/ircd-hybrid" /etc/init.d/ircd-hybrid',
    unless  => '/bin/grep -q "^DEFAULT=" /etc/init.d/ircd-hybrid',
    require => Exec['install ircd-hybrid without starting it'],
  }

  package { 'ircd-hybrid':
    ensure  => 'installed',
    require => Exec['install ircd-hybrid without starting it'],
  }
}
