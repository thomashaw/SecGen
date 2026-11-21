class sudo_root_find::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # purge => false so this module stacks with other sudo_root_* modules on one VM
  # (the vendored class purges /etc/sudoers.d by default). Leave sudoers as it is.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run find as root with any arguments, without a password.
  # On bookworm /bin is a symlink to /usr/bin and find is a real binary, not a symlink.
  sudo::conf { 'users_sudo_find':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/find *, /usr/bin/find *",
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list_find':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-find-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-find-flag-leak',
  }
}
