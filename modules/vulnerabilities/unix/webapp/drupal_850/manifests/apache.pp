class drupal_850::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $docroot = '/var/www/drupal-8.5.0'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['apache2'])

  # Apache listens only on $port. Debian ships /etc/apache2/ports.conf with its
  # own 'Listen 80'; putting another 'Listen' in the vhost (or leaving the
  # stock one when $port is 80) gives a duplicate bind and apache2 fails to
  # start with AH00072. So own ports.conf outright and keep the vhost free of
  # any Listen directive.
  file { '/etc/apache2/ports.conf':
    ensure  => file,
    content => "Listen ${port}\n",
  } ->
  # hand-written vhost for any hostname on $port, instead of the
  # puppetlabs-apache "compatible" wrapper (old stdlib/Puppet 7 dependency
  # conflicts on bookworm); AllowOverride All matches what Drupal's own
  # .htaccess expects for clean URLs.
  file { '/etc/apache2/sites-enabled/000-default.conf':
    ensure => absent,
  } ->
  file { '/etc/apache2/sites-available/drupal.conf':
    ensure  => file,
    content => "<VirtualHost *:${port}>\n    DocumentRoot ${docroot}\n    <Directory ${docroot}>\n        AllowOverride All\n        Require all granted\n    </Directory>\n</VirtualHost>\n",
  } ->
  file { '/etc/apache2/sites-enabled/drupal.conf':
    ensure => link,
    target => '/etc/apache2/sites-available/drupal.conf',
  } ->
  # mod_php7.4 (from the php7 utility) only loads under the prefork MPM;
  # bookworm's apache2 defaults to the event MPM, which refuses to start once
  # php7.4 is enabled. Switch to prefork before enabling PHP. a2dismod/a2enmod
  # are idempotent, so this is safe to re-run.
  exec { 'apache-mpm-prefork-drupal':
    command => 'a2dismod mpm_event mpm_worker 2>/dev/null; a2enmod mpm_prefork',
    unless  => 'test -L /etc/apache2/mods-enabled/mpm_prefork.load',
  } ->
  exec { 'enable-apache-mod-rewrite-drupal':
    command => 'a2enmod rewrite',
    unless  => 'test -L /etc/apache2/mods-enabled/rewrite.load',
  } ->
  exec { 'enable-apache-php7.4-drupal':
    command => 'a2enmod php7.4',
    unless  => 'test -L /etc/apache2/mods-enabled/php7.4.load',
  } ->
  # Verify apache is actually listening afterwards: the old 'restart; sleep 3'
  # masked a failed start (non-zero restart swallowed by the ';'), which is how
  # a closed port 80 slipped past provisioning before. is-active makes a failed
  # start a real Puppet error instead.
  exec { 'restart-apache-drupal':
    command   => 'systemctl restart apache2 && sleep 3 && systemctl is-active --quiet apache2',
    logoutput => true,
  }
}
