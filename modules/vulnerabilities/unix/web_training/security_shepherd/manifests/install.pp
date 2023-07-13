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
  # If Security Shepherd ever updates, this will neep updating. 
  # Ensure you have Maven installed, then run:
  # mvn -Pdocker clean install -DskipTests
  # The war file is usually within the generated target directory
  # if it cannot be found reference the pom.xml file for all relevant files.
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
