class sudo_root_python3::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  ensure_packages(['python3'])

  # purge => false: the vendored class defaults to purging /etc/sudoers.d on
  # every apply, which would delete the rules written by other sudo_root_*
  # modules on the same VM. Leave the directory and sudoers file alone.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run python3 as root with any arguments, without a
  # password. On bookworm /usr/bin/python3 is a symlink to python3.11, so the
  # versioned binary is named too.
  sudo::conf { 'users_sudo_python3':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/python3 *, /usr/bin/python3.* *",
    require => Package['python3'],
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  # Root-only flag, readable only after escalating
  ::secgen_functions::leak_files { 'sudo-root-python3-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-python3-flag-leak',
  }
}
