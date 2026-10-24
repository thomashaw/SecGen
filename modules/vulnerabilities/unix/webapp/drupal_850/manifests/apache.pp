class drupal_850::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $docroot = '/var/www/drupal-8.5.0'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_resource('tidy','gl remove default site', {'path'=>'/etc/apache2/sites-enabled/000-default.conf'})

  class { '::apache':
    default_vhost => false,
  }
  # Hand .php to php7_patched's php-fpm over FastCGI, and let Drupal's .htaccess
  # rewrites work (clean URLs) so the app routes as normal.
  include apache::mod::proxy
  include apache::mod::proxy_fcgi
  include apache::mod::rewrite

  ::apache::vhost { 'www-drupal':
    port            => $port,
    docroot         => $docroot,
    priority        => '05',
    directoryindex  => 'index.php',
    override        => ['All'],
    custom_fragment => "<FilesMatch \\.php$>\n    SetHandler \"proxy:fcgi://127.0.0.1:9000\"\n  </FilesMatch>",
    require         => [Class['apache::mod::proxy_fcgi'], Class['apache::mod::rewrite']],
  }
  -> exec { 'restart-apache-drupal':
    command   => 'service apache2 restart; sleep 3;',
    logoutput => true,
  }
}
