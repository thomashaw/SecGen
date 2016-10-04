class openssl_heartbleed {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'], }

  # remove current openssl and libssl packages as they are patched.
  package { ['openssl', 'libssl-dev']:
    ensure => purged,
  }

  # $openssl = 'openssl-1.0.1b'
  # $archive = "$openssl.tar.gz"
  # $archive_path = "/usr/local/src/$archive"
  # $openssl_dir = "/usr/local/src/$openssl/"
  #
  # file { $archive_path:
  #   ensure => file,
  #   source => "puppet:///modules/openssl_heartbleed/$archive",
  # }
  #
  # exec { 'unpack-heartbleed-archive':
  #   cwd => '/usr/local/src',
  #   command => "/bin/tar -xzf $archive_path",
  #   creates => $openssl_dir,
  # }
  #
  # exec { 'configure-make-openssl':
  #   require => Exec['unpack-heartbleed-archive'],
  #   cwd     => $openssl_dir,
  #   command => "bash config --prefix=/usr; make; make install",
  # }

  file { '/tmp/openssl_1.0.1b-1_i386.deb':
    ensure => file,
    source => "puppet:///modules/openssl_heartbleed/openssl_1.0.1b-1_i386.deb",
  }

  package { 'openssl_1.0.1b-1_i386':
    provider => dpkg,
    source => '/tmp/openssl_1.0.1b-1_i386.deb',
    ensure => installed,
  }
}