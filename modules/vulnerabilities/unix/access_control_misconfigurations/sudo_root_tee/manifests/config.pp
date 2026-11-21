class sudo_root_tee::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # purge => false: the vendored class defaults to purging /etc/sudoers.d on every run,
  # which would delete the rules of other sudo_root_* modules on the same VM.
  # config_file_replace => false leaves /etc/sudoers as it is.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run tee as root with any arguments, without a password.
  # /bin/tee and /usr/bin/tee are the same file on bookworm (usr-merge); sudo matches the
  # path as typed, so name both. tee is a plain coreutils binary, not a versioned symlink.
  sudo::conf { 'users_sudo_tee':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/tee *, /usr/bin/tee *",
  }

  # Allow all users to run sudo -l without a password (same title and content as the
  # other sudo_root_* modules, so it is the same file when they share a VM)
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-tee-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-tee-flag-leak',
  }
}
