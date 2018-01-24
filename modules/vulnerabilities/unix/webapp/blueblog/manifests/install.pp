class blueblog::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }


  file { '/opt/tomcat/webapps/blueblog':
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/blueblog',
  }

}