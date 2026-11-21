class sudo_root_tar::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # purge => false keeps other sudo modules' rules in /etc/sudoers.d (the
  # vendored class purges that directory by default); sudoers itself is left as is
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run tar as root with any arguments, without a password.
  # /bin is a symlink to /usr/bin on bookworm, but sudo matches the path the user types, so name both.
  sudo::conf { 'users_sudo_tar':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/tar *, /usr/bin/tar *",
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-tar-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-tar-flag-leak',
  }
}
