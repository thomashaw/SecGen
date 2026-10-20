class pandora_fms_rce::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $db_password      = $secgen_parameters['db_password'][0]
  $strings_to_leak  = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  $db_name  = 'pandora'
  $db_user  = 'pandora'
  $db_host  = 'localhost'
  $homedir  = '/var/www/html/pandora_console'
  $homeurl  = '/pandora_console'
  $auth     = 'mysql'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # --- database -------------------------------------------------------------
  file { '/etc/mysql/mariadb.conf.d/99-pandora.cnf':
    ensure  => file,
    content => "[mysqld]\nsql_mode=NO_ENGINE_SUBSTITUTION\n",
    require => Package['mariadb-server'],
  }
  ~> exec { 'restart-mariadb-pandora':
    command     => 'systemctl restart mariadb',
    refreshonly => true,
  }
  -> exec { 'enable-mariadb':
    command => 'systemctl enable --now mariadb',
  }

  # SQL is fed on stdin from a root-only file, so the password is never on a command line
  file { '/root/pandora_db_setup.sql':
    ensure  => file,
    owner   => 'root',
    mode    => '0600',
    content => template('pandora_fms_rce/db_setup.sql.erb'),
  }
  -> exec { 'create-pandora-database':
    command  => 'mysql < /root/pandora_db_setup.sql',
    provider => shell,
    unless   => "mysql -e 'USE ${db_name}'",
    require  => Exec['enable-mariadb'],
  }
  -> exec { 'populate-pandora-database':
    command  => "mysql ${db_name} < ${homedir}/pandoradb.sql && mysql ${db_name} < ${homedir}/pandoradb_data.sql",
    provider => shell,
    unless   => "mysql -N -e 'SELECT 1 FROM tconfig LIMIT 1' ${db_name}",
    timeout  => 900,
    require  => Exec['unpack-pandora-console'],
  }

  # --- console config -------------------------------------------------------
  # The console refuses to run while the web installer is present
  file { "${homedir}/install.php":
    ensure  => absent,
    require => Exec['unpack-pandora-console'],
  }
  # NOTE: the console's actual entry point (index.php) requires
  # 'include/config.php' - not 'config.inc.php' (that name is a different CMS's
  # convention and does not match what this codebase looks for). Writing to the
  # wrong filename left the app permanently on its "no config found" code path,
  # which bypasses config_process.php entirely and crashes once it reaches a
  # function needing something only config_process.php defines.
  file { "${homedir}/include/config.php":
    ensure  => file,
    owner   => 'www-data',
    group   => 'www-data',
    mode    => '0600',
    content => template('pandora_fms_rce/config.inc.php.erb'),
    require => Exec['unpack-pandora-console'],
  }
  exec { 'chown-pandora-console':
    command => "chown -R www-data:www-data ${homedir}",
    require => [File["${homedir}/include/config.php"], File["${homedir}/install.php"]],
  }

  # --- PHP 8 compatibility shim ----------------------------------------------
  # Pandora 7.0NG.742 targets PHP 7. Debian 12 ships PHP 8.2, which removed a few
  # functions the console calls on every request. Rather than downgrade system
  # PHP, shim them for this app only (auto_prepend_file in the Apache vhost scope).
  file { '/usr/local/share/pandora_php8_compat.php':
    ensure => file,
    source => 'puppet:///modules/pandora_fms_rce/pandora_php8_compat.php',
  }
  file { '/etc/apache2/conf-available/pandora.conf':
    ensure  => file,
    content => template('pandora_fms_rce/pandora.conf.erb'),
    require => Package['apache2'],
  }
  -> exec { 'enable-pandora-conf':
    command => 'a2enconf pandora',
    creates => '/etc/apache2/conf-enabled/pandora.conf',
  }

  # Flag lives outside the web root: reading it needs the command execution the vuln grants
  ::secgen_functions::leak_files { 'pandora_fms_rce':
    storage_directory => '/var/www',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'www-data',
    group             => 'www-data',
    mode              => '0640',
    leaked_from       => 'pandora_fms_rce',
    require           => Package['apache2'],
  }
}
