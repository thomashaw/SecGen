# Mariadb setup
class security_shepherd::mariadb {
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
    ensure => file,
    source => 'puppet:///modules/security_shepherd/coreSchema.sql',
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
}
