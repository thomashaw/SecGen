class sudo_root_more::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']
  $pre_leak_filename = $secgen_parameters['pre_leak_filename'][0]

  # Purge sudoers.d directory, but leave sudoers file as it is
  class { 'sudo':
    config_file_replace => false,
  }
  sudo::conf { 'users_sudo_more':
    ensure  => present,
    content => "ALL  ALL=(root) /bin/more /root/$pre_leak_filename",
  }
  ::secgen_functions::leak_files { 'sudo-root-more-pre-leak':
    storage_directory => '/root',
    leaked_filenames  => [$pre_leak_filename],
    strings_to_leak   => $strings_to_pre_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-more-pre-leak',
  }
  ::secgen_functions::leak_files { 'sudo-root-more-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-more-flag-leak',
  }
}
