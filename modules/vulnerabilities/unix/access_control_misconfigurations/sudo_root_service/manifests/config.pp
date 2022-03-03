class sudo_root_service::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # Purge sudoers.d directory, but leave sudoers file as it is
  class { 'sudo':
    config_file_replace => false,
  }
  sudo::conf { 'users_sudo_service':
    ensure  => present,
    content => "ALL  ALL=(root) /bin/sbin/service",
  }
  ::secgen_functions::leak_files { 'sudo-root-service-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-service-flag-leak',
  }
}
