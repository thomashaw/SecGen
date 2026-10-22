class drupal_850::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $docroot = '/var/www/drupal-8.5.0'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['apache2'])

  # hand-written vhost for any hostname on $port, instead of the
  # puppetlabs-apache "compatible" wrapper (old stdlib/Puppet 7 dependency
  # conflicts on bookworm); AllowOverride All matches what Drupal's own
  # .htaccess expects for clean URLs.
  file { '/etc/apache2/sites-enabled/000-default.conf':
    ensure => absent,
  } ->
  file { '/etc/apache2/sites-available/drupal.conf':
    ensure  => file,
    content => "Listen ${port}\n<VirtualHost *:${port}>\n    DocumentRoot ${docroot}\n    <Directory ${docroot}>\n        AllowOverride All\n        Require all granted\n    </Directory>\n</VirtualHost>\n",
  } ->
  file { '/etc/apache2/sites-enabled/drupal.conf':
    ensure => link,
    target => '/etc/apache2/sites-available/drupal.conf',
  } ->
  exec { 'enable-apache-mod-rewrite-drupal':
    command => 'a2enmod rewrite',
    unless  => 'test -L /etc/apache2/mods-enabled/rewrite.load',
  } ->
  exec { 'enable-apache-php7.4-drupal':
    command => 'a2enmod php7.4',
    unless  => 'test -L /etc/apache2/mods-enabled/php7.4.load',
  } ->
  exec { 'restart-apache-drupal':
    command => 'systemctl restart apache2; sleep 3',
    logoutput => true
  }
}
