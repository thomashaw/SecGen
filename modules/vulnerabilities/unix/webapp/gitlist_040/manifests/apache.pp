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

  # The php7 utility (a <requires> dependency) provisions as an earlier,
  # separate puppet apply, so its Package['libapache2-mod-php7.4'] lives in a
  # different catalogue - we cannot `require` it here or the catalogue fails to
  # compile. By the time this class runs the package is already installed on
  # the OS (and has dropped /etc/apache2/mods-available/php7.4.{load,conf}),
  # php7 only installs the packages and never touches Apache (apache2 doesn't
  # exist yet when it runs), so enabling the PHP 7.4 handler module is this
  # class's job once ::apache above has installed apache2. The `onlyif` guards
  # against the mod file being absent and makes the exec idempotent.
  exec { 'enable-apache-php7.4':
    command => '/usr/sbin/a2enmod php7.4',
    onlyif  => '/usr/bin/test -f /etc/apache2/mods-available/php7.4.load',
    unless  => '/usr/bin/test -L /etc/apache2/mods-enabled/php7.4.load',
    require => Class['::apache'],
    notify  => Class['apache::service'],
  }
}
