class sudo_root_less::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']
  $pre_leak_filename = $secgen_parameters['pre_leak_filename'][0]

  # Purge sudoers.d directory, but leave sudoers file as it is
  class { 'sudo':
    config_file_replace => false,
  }

  # Allow all users to run less on the pre-leak file as root without a password
  sudo::conf { 'users_sudo_less':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/less /root/$pre_leak_filename",
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }
  
  ::secgen_functions::leak_files { 'sudo-root-less-pre-leak':
    storage_directory => '/root',
    leaked_filenames  => [$pre_leak_filename],
    strings_to_leak   => $strings_to_pre_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-less-pre-leak',
  }
  ::secgen_functions::leak_files { 'sudo-root-less-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-less-flag-leak',
  }
}
