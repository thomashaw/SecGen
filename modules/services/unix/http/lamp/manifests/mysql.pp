# Installs the distro MySQL-compatible database server (MariaDB on Debian 12).
class lamp::mysql {
  ensure_packages(['mariadb-server'])

  service { 'mariadb':
    ensure  => running,
    enable  => true,
    require => Package['mariadb-server'],
  }
}
