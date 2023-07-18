# Installer process
class security_shepherd::install {
  include stdlib

  $secgen_parameters=secgen_functions::get_parameters($::base64_inputs_file)
  $flag_store = $secgen_parameters['flag_store']
  $modules = $secgen_parameters['modules']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['tomcat9', 'mariadb-server', 'openjdk-11-jdk'], {ensure => installed})

  service { 'tomcat9':
    ensure     => running,
    name       => 'tomcat9',
    enable     => true,
    hasrestart => true,
    subscribe  => [
      File['/var/lib/tomcat9/webapps/ROOT.war'],
    ],
  }

  exec { 'remove-default-site':
    command => 'rm -rf /var/lib/tomcat9/webapps/*',
  }
  -> file { '/var/lib/tomcat9/webapps/ROOT.war':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/ROOT.war',
  }
  file { '/var/lib/tomcat9/conf/shepherdKeystore.p12':
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
}
