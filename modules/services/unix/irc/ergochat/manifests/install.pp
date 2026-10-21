class ergochat::install {
  # The 'ergochat' package is not in Debian bookworm (apt: "Unable to locate
  # package"), so install the upstream release tarball instead. Paths match
  # those used by files/ircd.yaml (/etc/ergochat, /var/lib/ergochat,
  # /usr/share/ergochat/languages).
  $version = '2.19.1'
  $archive = "ergo-${version}-linux-x86_64"
  $url     = "https://github.com/ergochat/ergo/releases/download/v${version}/${archive}.tar.gz"

  ensure_packages(['curl', 'ca-certificates', 'tar'])

  group { 'ergochat':
    ensure => present,
    system => true,
  }
  ->
  user { 'ergochat':
    ensure     => present,
    system     => true,
    gid        => 'ergochat',
    home       => '/var/lib/ergochat',
    shell      => '/usr/sbin/nologin',
    managehome => false,
  }
  # Config and shared data are only read by ergo, so they stay root-owned,
  # readable by the ergochat group.
  file { ['/etc/ergochat', '/usr/share/ergochat']:
    ensure  => directory,
    owner   => 'root',
    group   => 'ergochat',
    mode    => '0750',
    require => User['ergochat'],
  }

  # The datastore/working directory must be owned by the ergochat user: ergo
  # runs as that user and writes ircd.db, ircd.lock and profiling output here
  # (see datastore.path, lock-file and output-path in files/ircd.yaml). A
  # root-owned 0750 directory only gives the group r-x, so 'ergo initdb'
  # failed with "permission denied" creating /var/lib/ergochat/ircd.db.
  file { '/var/lib/ergochat':
    ensure  => directory,
    owner   => 'ergochat',
    group   => 'ergochat',
    mode    => '0750',
    require => User['ergochat'],
  }

  exec { 'ergochat-download':
    command => "curl -fsSL -o /tmp/${archive}.tar.gz ${url} && tar -xzf /tmp/${archive}.tar.gz -C /tmp",
    path    => ['/usr/local/bin', '/usr/bin', '/bin'],
    creates => "/tmp/${archive}/ergo",
    timeout => 600,
    require => Package['curl', 'ca-certificates', 'tar'],
  }
  ->
  exec { 'ergochat-install-files':
    command => "install -m 0755 /tmp/${archive}/ergo /usr/local/bin/ergo && install -m 0644 /tmp/${archive}/ergo.motd /etc/ergochat/ergo.motd && cp -r /tmp/${archive}/languages /usr/share/ergochat/ && chmod -R a+rX /usr/share/ergochat",
    path    => ['/usr/local/bin', '/usr/bin', '/bin'],
    creates => '/usr/local/bin/ergo',
    require => File['/etc/ergochat', '/usr/share/ergochat'],
  }
}
