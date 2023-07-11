# Mariadb setup
class security_shepherd::mariadb {
  $user = 'root'
  $db_pass = 'CowSaysMoo'

  file { '/tmp/mysql_secure.sh':
    ensure => file,
    source => 'puppet:///modules/security_shepherd/mysql_secure.sh',
  }
  -> exec { 'chmod-mysql-secure':
    cwd     => '/tmp',
    command => 'chmod +x mysql_secure.sh',
  }
  -> exec { 'mysql-secure-install':
    provider => 'shell',
    cwd      => '/tmp',
    command  => "./mysql_secure.sh ${db_pass}",
  }

  mysql::db {  'core':
    user     => $user,
    password => $db_pass,
    dbname   => 'core',
    host     => 'localhost',
    grant    => ['ALL'],
  }

  file { '/tmp/coreSchema.sql':
    ensure => file,
    source => 'puppet://modules/modules/security_shepherd/coreSchema.sql',
  }
  -> file { '/tmp/moduleSchemas.sql':
    ensure => file,
    source => 'puppet://modules/modules/security_shepherd/moduleSchemas.sql',
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
