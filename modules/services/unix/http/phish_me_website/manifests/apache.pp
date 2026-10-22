class phish_me_website::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]

  # login.php and apply.php in Action/ are PHP, so Apache needs the PHP handler
  package { ['php', 'libapache2-mod-php']:
    ensure => installed,
  }

  class { '::apache':
    default_vhost => false,
    # overwrite_ports => false,
    mpm_module => 'prefork',
  }

  apache::vhost { 'accountingnow.com':
    port    => $port,
    docroot => '/var/www/accountingnow/',
    notify => Tidy['pws remove default site'],
  }

  ensure_resource('tidy','pws remove default site', {'path'=>'/etc/apache2/sites-enabled/000-default.conf'})
}
