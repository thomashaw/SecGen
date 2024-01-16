# Mariadb setup
class security_shepherd::mariadb {
  $secgen_parameters=secgen_functions::get_parameters($::base64_inputs_file)
  $unix_username = $secgen_parameters['unix_username'][0]
  $flag_store = $secgen_parameters['flag_store']
  $modules = $secgen_parameters['modules']
  $user = 'root'
  $db_pass = 'CowSaysMoo'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Execute this before we lock down root permissions.
  file { '/tmp/grant.sql':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/grant.sql',
  }
  -> exec { 'grant-root':
    cwd     => '/tmp',
    command => "mysql -u ${user} -p${db_pass} < grant.sql",
  }

  file { '/tmp/coreSchema.sql':
    ensure  => file,
    content => template('security_shepherd/coreSchema.sql.erb'),
  }
  -> file { '/tmp/moduleSchemas.sql':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/moduleSchemas.sql',
  }

  exec { 'create-core':
    cwd     => '/tmp',
    command => "mysql -u ${user} -p${db_pass} < coreSchema.sql",
  }
  -> exec { 'create-modules':
    cwd     => '/tmp',
    command => "mysql -u ${user} -p${db_pass} < moduleSchemas.sql",
  }

  file { '/var/lib/tomcat9/webapps/ROOT/WEB-INF/classes/flags':
    ensure  => file,
    content => template('security_shepherd/flags.erb'),
  }
  -> file { '/var/lib/tomcat9/webapps/ROOT/WEB-INF/classes/active-modules':
    ensure  => file,
    content => template('security_shepherd/active-modules.erb'),
    notify  => Service['tomcat9']
  }
  # This needs updating? Weird chicanery happens if not used this way
  exec { 'restart-tom':
    command => 'systemctl restart tomcat9',
  }
}
