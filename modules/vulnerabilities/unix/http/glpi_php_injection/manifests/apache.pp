# Class: glpi_php_injection::apache
# Apache configuration
#
class glpi_php_injection::apache {
  $port = '80'
  $docroot = '/var/www/html/glpi'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  class { '::apache':
    default_vhost   => false,
  }

  ::apache::vhost { 'glpi':
    port       => $port,
    docroot    => $docroot,
    options    => ['FollowSymLinks'],
    override   => ['All'],
    error_log  => true,
    access_log => true,
  }

  file { '/etc/apache2/sites-enabled/000-default.conf':
    ensure => 'absent',
  }
  -> exec { 'service-restart-apache2':
    command   => 'service apache2 restart',
    logoutput => true,
  }
}
