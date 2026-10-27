class phish_me_website::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]

  class { '::apache':
    default_vhost => false,
    mpm_module => 'prefork',
  }

  include apache::mod::php

  apache::vhost { 'accountingnow.com':
    port    => $port,
    docroot => '/var/www/accountingnow/',
    notify => Tidy['pws remove default site'],
  }

  ensure_resource('tidy','pws remove default site', {'path'=>'/etc/apache2/sites-enabled/000-default.conf'})
}
