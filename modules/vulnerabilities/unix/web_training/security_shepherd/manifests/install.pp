# Installer process
class security_shepherd::install {
  include stdlib

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Temp fix. Seemed to not be able to generate this...
  file { '/etc/ssl/certs/java/':
    ensure => directory,
  }
  -> package { 'install-ca-certs':
    name => 'ca-certificates-java',
    ensure => installed,
  }

  ensure_packages(['tomcat9', 'mariadb-server', 'openjdk-11-jdk'], {ensure => installed})

  exec { 'remove-default-site':
    command => 'rm -rf /var/lib/tomcat9/webapps/*',
  }

  -> file { '/var/lib/tomcat9/webapps/ROOT.zip':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/ROOT.zip',
  }

  -> file { ['/var/lib/tomcat9/webapps/ROOT', 
            '/var/lib/tomcat9/webapps/ROOT/WEB-INF', 
            '/var/lib/tomcat9/webapps/ROOT/WEB-INF/classes']:
    ensure => directory,
  }

  -> exec { 'extract ROOT':
    command => 'unzip -o ROOT.zip -d ROOT',
    cwd     => '/var/lib/tomcat9/webapps/',
    creates => '/var/lib/tomcat9/webapps/ROOT/admin/',
    path    => ['/usr/bin', '/bin'],
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
    ensure     => running,
    name       => 'tomcat9',
    enable     => true,
    hasrestart => true,
  }
  

}
