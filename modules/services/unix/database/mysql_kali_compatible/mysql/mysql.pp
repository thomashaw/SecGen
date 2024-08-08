ensure_packages('mariadb-server')

user { 'mysql':
  ensure => 'present',
  gid    => 'mysql',
} ->
group { 'mysql':
  ensure => 'present',
} ->
file { '/var/log/mysql':
  ensure => 'directory',
  owner  => 'mysql',
  group  => 'mysql',
  mode   => '0755',
} ->

class { '::mysql::server':
  package_name     => 'mariadb-server',
  service_name     => 'mariadb',
  package_manage   => false,
  service_manage   => true,
  override_options => {
    'mysqld' => {
      'ssl' => false,
      'skip-ssl' => true,
      'require_secure_transport' => false,
    },
    'client' => {
      'ssl' => false,
    }
  }
  
} ->

exec { 'start_and_enable_mariadb':
  command     => '/usr/bin/systemctl start mariadb && /usr/bin/systemctl enable mariadb',
  logoutput   => true,
}
