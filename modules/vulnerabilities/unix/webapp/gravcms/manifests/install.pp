class gravcms::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  ensure_packages(['apache2', 'php', 'libapache2-mod-php', 'php-mbstring', 'php-curl', 'php-dom', 'php-xml', 'php-zip', 'php-gd', 'cron'])

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  exec {'enable-mod-rewrite':
    command => 'a2enmod rewrite',
  } ->
  file {'/var/www/html':
    ensure => directory,
    owner => 'www-data',
  } ->
  file {'/var/www/html/grav_admin.zip':
    source => 'puppet:///modules/gravcms/grav-admin.zip',
    owner => 'www-data',
    group => 'www-data',
  } ->
  exec {'extract-grav-cmd':
    command => 'unzip -x grav_admin.zip; rm grav_admin.zip',
    cwd => '/var/www/html/',
    user => 'www-data',
    creates => '/var/www/html/grav-admin',
  } ->
  exec {'enable-htaccess':
    command => 'sed -i "/<Directory \/var\/www\/>/,/<\/Directory>/ s/AllowOverride None/AllowOverride all/" /etc/apache2/apache2.conf',
  } ->
  # Grav only runs scheduled jobs when `bin/grav scheduler` is invoked by cron.
  # The admin-plugin RCE (CVE-2021-21425) works by implanting a scheduler job,
  # so without this entry the exploit lands but never fires and the target is
  # not actually exploitable as provisioned.
  #
  # Puppet's `cron` resource moved out of core into puppetlabs-cron_core (Puppet
  # 6+) and isn't available here, so drop a cron.d file directly instead.
  file {'/etc/cron.d/grav-scheduler':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "* * * * * www-data cd /var/www/html/grav-admin && /usr/bin/php bin/grav scheduler 1>> /dev/null 2>&1\n",
  } ->
  exec {'enable-web-server':
    command => 'systemctl enable apache2',
  } ->
  exec {'restart-web-server':
    command => 'systemctl restart apache2',
  }

  # The leaked flag is the post-exploitation objective. It is stored outside the
  # web root (/var/www, not /var/www/html) so it cannot be fetched directly over
  # HTTP; reading it requires the www-data command execution the exploit grants.
  ::secgen_functions::leak_files { 'gravcms':
    storage_directory => '/var/www',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'www-data',
    group             => 'www-data',
    mode              => '0640',
    leaked_from       => 'gravcms',
  }
}
