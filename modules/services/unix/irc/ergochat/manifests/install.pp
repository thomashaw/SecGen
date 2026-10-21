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
  ->
  file { ['/etc/ergochat', '/usr/share/ergochat', '/var/lib/ergochat']:
    ensure => directory,
    owner  => 'root',
    group  => 'ergochat',
    mode   => '0750',
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
