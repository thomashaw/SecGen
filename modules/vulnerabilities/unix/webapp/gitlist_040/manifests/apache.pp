class gitlist_040::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $gitlist_root = '/var/www/gitlist'

  class { '::apache':
    default_vhost => false,
    default_mods => ['rewrite'], # php5 via separate module
    overwrite_ports => false,
    mpm_module => 'prefork'
  }

  # Serve GitList from the /gitlist/ subdirectory, not the vhost root. The app's
  # own .htaccess mod_rewrite (RewriteBase /gitlist/) makes it emit /gitlist/-prefixed
  # repository links, which is what the discovery regex relies on. AllowOverride All
  # lets that .htaccess take effect; -MultiViews stops Apache content-negotiating
  # index.php away; +FollowSymLinks is required for RewriteRule in a .htaccess.
  ::apache::vhost { 'www-gitlist':
    port        => $port,
    docroot     => '/var/www/html',
    priority    => '05',
    aliases     => [
      { alias => '/gitlist', path => $gitlist_root },
    ],
    directories => [
      {
        path           => $gitlist_root,
        allow_override => ['All'],
        options        => ['-MultiViews', '+FollowSymLinks'],
        require        => 'all granted',
      },
    ],
    notify      => Tidy['gl remove default site'],
  }

  ensure_resource('tidy','gl remove default site', {'path'=>'/etc/apache2/sites-enabled/000-default.conf'})
  #
  #
  # exec { 'disable php7':
  #   command => '/usr/sbin/a2dismod php7.0',
  # }
}
