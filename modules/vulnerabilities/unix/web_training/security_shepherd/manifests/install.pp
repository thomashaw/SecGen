# Installer process
class security_shepherd::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  package { 'install-tomcat':
    ensure => installed,
    name   => 'tomcat9',
  }
  -> package { 'install-mariadb':
    ensure => installed,
    name   => 'mariadb-server',
  } -> package { 'install-jdk11':
    ensure => installed,
    name   => 'openjdk-11-jdk',
  }

  exec { 'remove-default-site':
    command => 'rm -rf /var/lib/tomcat9/webapps/*',
  }
  -> file { '/var/lib/tomcat9/webapps/ROOT.war':
    ensure  => file,
    source  => 'puppet:///modules/security_shepherd/ROOT.war',
    replace => true,
  }
  -> file { '/var/lib/tomcat9/conf/shepherdKeystore.p12':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/shepherdKeystore.p12',
  }
  -> file { '/var/lib/tomcat9/conf/server.xml':
    ensure  => file,
    source  => 'puppet:///modules/security_shepherd/server.xml',
    replace => true,
  }
  -> file { '/var/lib/tomcat9/conf/web.xml':
    ensure  => file,
    source  => 'puppet:///modules/security_shepherd/web.xml',
    replace => true,
  }
  -> file { '/var/lib/tomcat9/conf/database.properties':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/database.properties',
  }
  -> file { '/etc/mysql/my.cnf':
    ensure  => file,
    source  => 'puppet:///modules/security_shepherd/my.cnf',
    replace => true,
  }
  -> service { 'tomcat9':
    restart => '',
  }
}
