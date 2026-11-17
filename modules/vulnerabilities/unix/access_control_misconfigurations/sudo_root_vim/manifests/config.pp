class sudo_root_vim::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # The base may not ship vim
  ensure_packages(['vim'])

  # purge => false: the vendored class defaults to purging /etc/sudoers.d, which
  # would delete the rules of any other sudo_root_* module on the same VM.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run vim as root with any arguments, without a password.
  # /usr/bin/vim is a symlink (via /etc/alternatives) to /usr/bin/vim.basic on
  # Debian; /bin is a symlink to /usr/bin on bookworm. Name the real binary too.
  sudo::conf { 'users_sudo_vim':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/vim *, /usr/bin/vim.basic *, /bin/vim *",
    require => Package['vim'],
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list_vim':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-vim-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-vim-flag-leak',
  }
}
