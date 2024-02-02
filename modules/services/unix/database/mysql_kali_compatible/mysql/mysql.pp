ensure_packages('mariadb-server')

user { 'mysql':
  ensure => 'present',
  gid    => 'mysql',
} ->
group { 'mysql':
  ensure => 'present',
} ->

class {'::mysql::server':
  package_name     => 'mariadb-server',
  service_name     => 'mariadb',
  package_manage   => false, # avoid managing the client package
  service_manage   => true, # this doesn't work (workaround below)
} ->


service { "enable_mariadb":
    name => "mariadb"
    ensure  => running,
    enable  => true,
}
