class gitlist_040::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $docroot = '/var/www/gitlist'

  class { '::apache':
    default_vhost => false,
    default_mods => ['rewrite'], # php7.4 enabled separately below, once installed
    overwrite_ports => false,
    mpm_module => 'prefork'
  }

  ::apache::vhost { 'www-gitlist':
    port     => $port,
    docroot  => $docroot,
    priority => '05',
    notify   => Tidy['gl remove default site']
  }

  ensure_resource('tidy','gl remove default site', {'path'=>'/etc/apache2/sites-enabled/000-default.conf'})

  # The php7 utility (a <requires> dependency, applied before this class)
  # only installs the PHP 7.4 packages - it never touches Apache, since
  # apache2 doesn't exist yet when it runs. So enabling the PHP 7.4 handler
  # module is this class's job, once ::apache above has installed apache2.
  exec { 'enable-apache-php7.4':
    command => '/usr/sbin/a2enmod php7.4',
    require => [Class['::apache'], Package['libapache2-mod-php7.4']],
    notify  => Class['apache::service'],
  }
}
