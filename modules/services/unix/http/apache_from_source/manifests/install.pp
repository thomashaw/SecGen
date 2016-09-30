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
    cwd => '/usr/local/src/',
    command => "tar -xzf $archive",
  }

  file { "/usr/local/src/$apr_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_archive",
  }

  exec { 'unpack-apr':
    cwd => '/usr/local/src/',
    command => "tar -xzf $apr_archive -C $httpd/srclib/; mv apr-1.5.2 apr",
  }


  file { "/usr/local/src/$apr_util_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_util_archive",
  }

  exec { 'unpack-apr-util':
    cwd => '/usr/local/src/',
    command => "tar -xzf $apr_util_archive -C $httpd/srclib/; mv apr-util-1.5.4 apr-util",
  }

  ##
  # ./configure --use-ssl=/usr/bin
  ##


}
