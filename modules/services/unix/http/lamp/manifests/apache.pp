# Installs Apache with mod_php from distro packages. The php package pulls in
# the prefork MPM that mod_php needs.
class lamp::apache {
  ensure_packages(['apache2', 'libapache2-mod-php', 'php-mysql'])

  service { 'apache2':
    ensure  => running,
    enable  => true,
    require => Package['apache2', 'libapache2-mod-php'],
  }
}
