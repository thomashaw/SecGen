# Class lamp::apache
# Installs Apache with mod_php. Installing the mod_php package switches Apache to the prefork MPM.

class lamp::apache {
  if $::operatingsystemmajrelease == '7' {
    $packages = ['apache2', 'libapache2-mod-php5', 'php5', 'php5-cli', 'php5-mysql']
  } else {
    $packages = ['apache2', 'libapache2-mod-php', 'php', 'php-cli', 'php-mysql']
  }

  ensure_packages($packages)

  service { 'apache2':
    ensure  => running,
    enable  => true,
    require => Package[$packages],
  }
}
