# Installer process
class security_shepherd::install {
  ensure_packages(['mariadb-server',
  'tomcat',
  'openjdk-11-jdk'])

  file { '/var/lib/tomcat9/ROOT.war':
    ensure => file,
    source => 'puppet://modules/modules/security_shepherd/ROOT.war',
  }
  -> file { '/var/lib/tomcat9/conf/shepherdKeystore.p12':
    ensure => file,
    source => 'puppet://modules/modules/security_shepherd/shepherdKeystore.p12',
  }
  -> file { '/var/lib/tomcat9/conf/server.xml':
    ensure  => file,
    source  => 'puppet://modules/modules/security_shepherd/server.xml',
    replace => true,
  }
  -> file { '/var/lib/tomcat9/conf/web.xml':
    ensure  => file,
    source  => 'puppet://modules/modules/security_shepherd/web.xml',
    replace => true,
  }
  -> file { '/var/lib/tomcat9/conf/database.properties':
    ensure    => file,
    source    => 'puppet://modules/modules/security_shepherd/database.properties',
    subscribe => Service['tomcat9'],
  }
}
