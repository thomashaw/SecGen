class apache_from_source::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'], }

  $httpd = 'httpd-2.4.23'
  $archive = "$httpd.tar.gz"
  $apr_archive = 'apr-1.5.2.tar.gz'
  $apr_util_archive = 'apr-util-1.5.4.tar.gz'

  file { "/usr/local/src/$archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$archive",
  }

  exec { 'unpack-apache':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $archive",
  }

  file { "/usr/local/src/$apr_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_archive",
  }

  exec { 'unpack-apr':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $apr_archive -C $httpd/srclib/; mv $httpd/srclib/apr-1.5.2 $httpd/srclib/apr",
  }

  file { "/usr/local/src/$apr_util_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_util_archive",
  }

  exec { 'unpack-apr-util':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $apr_util_archive -C $httpd/srclib/; mv $httpd/srclib/apr-util-1.5.4 $httpd/srclib/apr-util",
  }

  package { ['libpcre3','libpcre3-dev']:
    ensure => installed,
  }

  exec { 'configure-apache':
    cwd     => "/usr/local/src/$httpd",
    command => 'bash configure --with-included-apr --enable-ssl --with-ssl=/usr/bin --enable-ssl-staticlib-deps --enable-mods-static=ssl',
  }

  exec { 'make-apache':
    cwd     => "/usr/local/src/$httpd",
    command => 'make',
  }

  exec { 'make-install-apache':
    cwd     => "/usr/local/src/$httpd",
    command => 'make install',
  }

  file { '/etc/init.d/apache2':
    ensure => file,
    owner   => root,
    group   => root,
    mode    => '0755',
    source => 'puppet:///modules/apache_from_source/apache2.init.d',
  }

}
