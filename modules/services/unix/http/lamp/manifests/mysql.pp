# Class lamp::mysql
# Installs the database server: MariaDB on Debian 9 and later (where mysql-server is not packaged), MySQL on older releases.

class lamp::mysql {
  if $::operatingsystemmajrelease == '7' {
    $package = 'mysql-server'
    $service_name = 'mysql'
  } else {
    $package = 'mariadb-server'
    $service_name = 'mariadb'
  }

  ensure_packages([$package])

  service { $service_name:
    ensure  => running,
    enable  => true,
    require => Package[$package],
  }
}
