class sudo_env_keep_ld_preload::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # purge => false keeps other modules' rules in /etc/sudoers.d; the
  # vendored class would otherwise empty the directory on every run.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # A harmless root-owned command for sudo to run
  file { '/usr/local/bin/system_status':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0755',
    content => "#!/bin/sh\nuptime\ndf -h /\n",
  }

  # The misconfiguration: sudo passes the caller's LD_PRELOAD to the command
  sudo::conf { 'ld_preload_env_keep':
    ensure  => present,
    content => 'Defaults env_keep += "LD_PRELOAD"',
  }

  # Allow all users to run the status script as root without a password
  sudo::conf { 'ld_preload_status_script':
    ensure  => present,
    content => 'ALL  ALL=(root) NOPASSWD: /usr/local/bin/system_status',
    require => File['/usr/local/bin/system_status'],
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'ld_preload_sudo_list':
    ensure  => present,
    content => 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l',
  }

  ::secgen_functions::leak_files { 'sudo-env-keep-ld-preload-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-env-keep-ld-preload-flag-leak',
  }
}
