class openssl_heartbleed::install {

  package { ['openssl', 'libssl-dev']:
    ensure => purged,
  }

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'], }

  $openssl = 'openssl-1.0.1b'
  $archive = "$openssl.tar.gz"
  $archive_path = "/usr/local/src/$archive"
  $openssl_dir = "/usr/local/src/$openssl/"

  file { $archive_path:
    ensure => file,
    source => "puppet:///modules/openssl_heartbleed/$archive",
  }

  # remove current openssl and libssl packages as they are patched for heartbleed.
  exec { 'unpack-heartbleed-archive':
    cwd => '/usr/local/src',
    command => "/bin/tar -xzf $archive_path",
    creates => $openssl_dir,
  }

  exec { 'configure-make-openssl':
    require => Exec['unpack-heartbleed-archive'],
    cwd     => $openssl_dir,
    command => "bash config --prefix=/usr; make; make install",
  }
}